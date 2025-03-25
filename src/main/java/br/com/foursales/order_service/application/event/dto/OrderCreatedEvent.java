package br.com.foursales.order_service.application.event.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreatedEvent {
    @JsonProperty("id")
    private UUID orderId;
    @JsonProperty("items")
    private List<OrderItemEvent> items;
}
