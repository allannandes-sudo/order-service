package br.com.foursales.order_service.application.event.producer;

import br.com.foursales.order_service.application.event.dto.OrderCreatedEvent;
import br.com.foursales.order_service.application.event.dto.OrderPaidEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String ORDER_CREATED_TOPIC = "order.created";
    private static final String ORDER_PAID_TOPIC = "order.paid";

    public void publishOrderCreatedEvent(OrderCreatedEvent event) {
        kafkaTemplate.send(ORDER_CREATED_TOPIC, event);
    }

    public void publishOrderPaidEvent(OrderPaidEvent event) {
        kafkaTemplate.send(ORDER_PAID_TOPIC, event);
    }
}
