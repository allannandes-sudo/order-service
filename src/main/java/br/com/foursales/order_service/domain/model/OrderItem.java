package br.com.foursales.order_service.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem {
    private UUID productId;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
}