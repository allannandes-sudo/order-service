package br.com.foursales.order_service.application.service.impl;

import br.com.foursales.order_service.application.dto.CreateOrderRequest;
import br.com.foursales.order_service.application.event.dto.OrderCreatedEvent;
import br.com.foursales.order_service.application.event.dto.OrderItemEvent;
import br.com.foursales.order_service.application.event.dto.OrderPaidEvent;
import br.com.foursales.order_service.application.event.mapper.OrderEventMapper;
import br.com.foursales.order_service.application.event.producer.OrderKafkaProducer;
import br.com.foursales.order_service.application.service.OrderService;
import br.com.foursales.order_service.domain.exception.BusinessException;
import br.com.foursales.order_service.domain.exception.UserNotAuthenticatedException;
import br.com.foursales.order_service.domain.model.Order;
import br.com.foursales.order_service.domain.model.OrderItem;
import br.com.foursales.order_service.domain.model.ProductStockResponse;
import br.com.foursales.order_service.domain.model.enums.OrderStatus;
import br.com.foursales.order_service.infrastructure.client.ProductClient;
import br.com.foursales.order_service.infrastructure.persistence.entity.OrderEntity;
import br.com.foursales.order_service.infrastructure.persistence.mapper.OrderMapper;
import br.com.foursales.order_service.infrastructure.persistence.repository.OrderRepository;
import br.com.foursales.order_service.infrastructure.security.dto.CustomAuthenticationToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final ProductClient productClient;
    private final OrderRepository orderRepository;
    private final OrderKafkaProducer kafkaProducer;
    private final OrderMapper orderMapper;
    private final OrderEventMapper orderEventMapper;

    @Override
    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        log.info("Starting createOrder method");
        UUID orderId = UUID.randomUUID();
        String userId = getAuthenticatedUserId();

        List<OrderItem> items = orderMapper.toOrderItems(request.getItems());
        validateStockAvailability(items);

        calculateTotalAmountForItems(items);

        BigDecimal totalOrderAmount = calculateTotalOrderAmount(items);

        Order order = buildOrder(orderId, userId, items, totalOrderAmount);

        OrderEntity savedOrder = saveOrder(order);

        publishOrderCreatedEvent(savedOrder);

        log.info("Finished createOrder method");
        return orderMapper.toDto(savedOrder);
    }

    private void calculateTotalAmountForItems(List<OrderItem> items) {
        items.forEach(item -> {
            if (item.getUnitPrice() != null) {
                item.setTotalAmount(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            } else {
                item.setTotalAmount(BigDecimal.ZERO);
            }
        });
    }

    private void validateStockAvailability(List<OrderItem> items) {
        List<UUID> productIds = items.stream()
                .map(OrderItem::getProductId)
                .toList();

        Map<UUID, ProductStockResponse> stockStatus = productClient.checkStock(productIds).getBody();
        if (stockStatus == null || stockStatus.values().stream().anyMatch(response -> response.getStock() <= 0)) {
            throw new RuntimeException("Produto sem estoque disponível!");
        }

        items.forEach(item -> {
            ProductStockResponse productStockResponse = stockStatus.get(item.getProductId());
            if (productStockResponse != null) {
                BigDecimal price = BigDecimal.valueOf(productStockResponse.getPrice());
                item.setUnitPrice(price);
            } else {
                throw new RuntimeException("Informações do produto não encontradas para o ID: " + item.getProductId());
            }
        });
    }

    private BigDecimal calculateTotalOrderAmount(List<OrderItem> items) {
        return items.stream()
                .map(OrderItem::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Order buildOrder(UUID orderId, String userId, List<OrderItem> items, BigDecimal totalOrderAmount) {
        Order order = new Order();
        order.setId(orderId);
        order.setUserId(userId);
        order.setItems(items);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(totalOrderAmount);
        order.setCreatedAt(LocalDateTime.now());
        return order;
    }

    private OrderEntity saveOrder(Order order) {
        OrderEntity orderEntity = orderMapper.toEntity(order);
        orderEntity.getItems().forEach(item -> item.setOrder(orderEntity));
        return orderRepository.save(orderEntity);
    }

    private void publishOrderCreatedEvent(OrderEntity savedOrder) {
        List<OrderItemEvent> orderItemEvents = orderEventMapper.toOrderItemEvents(savedOrder.getItems());

        OrderCreatedEvent event = new OrderCreatedEvent(savedOrder.getId(), orderItemEvents);
        kafkaProducer.publishOrderCreatedEvent(event);
    }

    private String getAuthenticatedUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof CustomAuthenticationToken customAuthToken) {
            Map<String, Object> claims = customAuthToken.getClaims();
            return claims.get("userId").toString();
        }
        throw new UserNotAuthenticatedException("Usuário não autenticado");
    }

    @Override
    public Order getOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .map(orderMapper::toDto)
                .orElseThrow(() -> new BusinessException("Pedido não encontrado!"));
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(orderMapper::toDto)
                .collect(toList());
    }

    @Override
    @Transactional
    public Order cancelOrder(UUID orderId) {
        return orderRepository.findById(orderId)
                .map(orderEntity -> {
                    if (orderEntity.getStatus() == OrderStatus.PAID) {
                        throw new BusinessException("Pedido já foi pago e não pode ser cancelado!");
                    }
                    orderEntity.setStatus(OrderStatus.CANCELED);
                    return orderRepository.save(orderEntity);
                })
                .map(orderMapper::toDto)
                .orElseThrow(() -> new BusinessException("Pedido não encontrado!"));
    }


    @Override
    @Transactional
    public Order payOrder(UUID orderId) {
        OrderEntity orderEntity = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("Pedido não encontrado!"));
        validateOrderStatus(orderEntity);
        if (!isStockAvailable(orderEntity)) {
            cancelOrder(orderEntity);
            throw new BusinessException("Produto sem estoque disponível! Pedido foi cancelado.");
        }
        updateOrderStatusToPaid(orderEntity);
        publishOrderPaidEvent(orderEntity);
        return orderMapper.toDto(orderEntity);
    }

    private void validateOrderStatus(OrderEntity orderEntity) {
        if (!orderEntity.getStatus().equals(OrderStatus.PENDING)) {
            throw new BusinessException("Pedido já foi pago ou cancelado!");
        }
    }

    private boolean isStockAvailable(OrderEntity orderEntity) {
        List<OrderItem> orderItems = orderEntity.getItems().stream()
                .map(orderMapper::toEntityOrderItem)
                .collect(Collectors.toList());

        try {
            validateStockAvailability(orderItems);
            return true;
        } catch (BusinessException e) {
            return false;
        }
    }

    private void cancelOrder(OrderEntity orderEntity) {
        orderEntity.setStatus(OrderStatus.CANCELED);
        orderRepository.save(orderEntity);
    }

    private void updateOrderStatusToPaid(OrderEntity orderEntity) {
        orderEntity.setStatus(OrderStatus.PAID);
        orderRepository.save(orderEntity);
    }

    private void publishOrderPaidEvent(OrderEntity orderEntity) {
        List<OrderItemEvent> orderItemEvents = orderEventMapper.toOrderItemEvents(orderEntity.getItems());
        OrderPaidEvent event = new OrderPaidEvent(orderEntity.getId(), orderItemEvents);
        kafkaProducer.publishOrderPaidEvent(event);
    }

}