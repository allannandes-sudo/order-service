package br.com.foursales.order_service.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
//@ToString(exclude = "order")
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem {
    private Long productId;
    private int quantity;
    private BigDecimal unitPrice;
//    private Order order;
}
