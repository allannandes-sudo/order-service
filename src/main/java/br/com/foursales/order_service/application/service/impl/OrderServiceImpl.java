package br.com.foursales.order_service.application.service.impl;

import br.com.foursales.order_service.application.dto.CreateOrderRequest;
import br.com.foursales.order_service.application.event.dto.OrderCreatedEvent;
import br.com.foursales.order_service.application.event.dto.OrderItemEvent;
import br.com.foursales.order_service.application.event.dto.OrderPaidEvent;
import br.com.foursales.order_service.application.event.producer.OrderKafkaProducer;
import br.com.foursales.order_service.application.service.OrderService;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final ProductClient productClient;
    private final OrderRepository orderRepository;
    private final OrderKafkaProducer kafkaProducer;
    private final OrderMapper orderMapper;


    @Override
    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        UUID orderId = UUID.randomUUID();
        String userId = getAuthenticatedUserId(); // Obtém o ID do usuário autenticado

        // Converte itens da requisição para o domínio
        List<OrderItem> items = request.getItems().stream()
                .map(item -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setProductId(item.getProductId());
                    orderItem.setQuantity(item.getQuantity());
                    return orderItem;
                })
                .toList();

        // Verifica estoque via Feign Client
        List<Long> productIds = items.stream()
                .map(OrderItem::getProductId)
                .toList();

        Map<Long, ProductStockResponse> stockStatus = productClient.checkStock(productIds).getBody();
        if (stockStatus == null || stockStatus.values().stream().anyMatch(response -> response.getStock() <= 0)) {
            throw new RuntimeException("Produto sem estoque disponível!");
        }

// Atualiza o preço unitário de cada item
        items.forEach(item -> {
            ProductStockResponse productStockResponse = stockStatus.get(item.getProductId());
            if (productStockResponse != null) {
                BigDecimal price = BigDecimal.valueOf(productStockResponse.getPrice());
                item.setUnitPrice(price);
            } else {
                throw new RuntimeException("Informações do produto não encontradas para o ID: " + item.getProductId());
            }
        });

        // Cria o pedido com status PENDING
        Order order = new Order();
        order.setId(orderId);
        order.setUserId(userId);
        order.setItems(items);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(BigDecimal.ZERO); // O total será calculado após o pagamento
        order.setCreatedAt(LocalDateTime.now());

        // Converte o pedido para entidade e associa os itens ao pedido
        OrderEntity orderEntity = orderMapper.toEntity(order);
        orderEntity.getItems().forEach(item -> item.setOrder(orderEntity)); // Associa cada item ao pedido

        // Salva no banco
        OrderEntity savedOrder = orderRepository.save(orderEntity);

        // Publica evento Kafka
        List<OrderItemEvent> orderItemEvents = savedOrder.getItems().stream()
                .map(item -> new OrderItemEvent(item.getProductId(), item.getQuantity()))
                .toList();

        OrderCreatedEvent event = new OrderCreatedEvent(savedOrder.getId(), orderItemEvents);
        kafkaProducer.publishOrderCreatedEvent(event);

        return orderMapper.toDto(savedOrder);
    }

    /**
     * Obtém o ID do usuário autenticado do contexto de segurança.
     */
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
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado!"));
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Order updateOrder(Order order) {
        if (!orderRepository.existsById(order.getId())) {
            throw new RuntimeException("Pedido não encontrado para atualização!");
        }

        OrderEntity updatedOrder = orderRepository.save(orderMapper.toEntity(order));
        return orderMapper.toDto(updatedOrder);
    }

    @Override
    @Transactional
    public void deleteOrder(UUID orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new RuntimeException("Pedido não encontrado para exclusão!");
        }

        orderRepository.deleteById(orderId);
    }

    @Override
    @Transactional
    public Order payOrder(UUID orderId) {
        OrderEntity orderEntity = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado!"));

        if (!orderEntity.getStatus().equals(OrderStatus.PENDING)) {
            throw new RuntimeException("Pedido já foi pago ou cancelado!");
        }

        // Atualizar status para PAGO
        orderEntity.setStatus(OrderStatus.PAID);
        orderRepository.save(orderEntity);

        // Criar eventos para cada item do pedido
        List<OrderItemEvent> orderItemEvents = orderEntity.getItems().stream()
                .map(item -> new OrderItemEvent(item.getProductId(), item.getQuantity()))
                .collect(Collectors.toList());

        OrderPaidEvent event = new OrderPaidEvent(orderEntity.getId(), orderItemEvents);
        kafkaProducer.publishOrderPaidEvent(event);

        return orderMapper.toDto(orderEntity);
    }
}
