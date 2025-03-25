package br.com.foursales.order_service.application.event.consumer;

import br.com.foursales.order_service.application.event.dto.OrderCanceledEvent;
import br.com.foursales.order_service.domain.model.enums.OrderStatus;
import br.com.foursales.order_service.infrastructure.persistence.entity.OrderEntity;
import br.com.foursales.order_service.infrastructure.persistence.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCanceledConsumer {

    private final OrderRepository orderRepository;

    @KafkaListener(topics = "order.canceled", groupId = "order-service-group")
    @Transactional
    public void consumeOrderCanceled(OrderCanceledEvent event) {
        UUID orderId = event.getOrderId();
        log.info("Recebido evento de cancelamento de pedido: {}", orderId);

        // Buscar pedido no banco
        OrderEntity orderEntity = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado: " + orderId));

        // Atualizar status para CANCELADO
        orderEntity.setStatus(OrderStatus.CANCELED);
        orderRepository.save(orderEntity);

        log.info("Pedido {} atualizado para CANCELADO com sucesso!", orderId);
    }
}
