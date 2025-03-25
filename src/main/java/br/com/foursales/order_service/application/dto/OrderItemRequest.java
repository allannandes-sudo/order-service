package br.com.foursales.order_service.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemRequest {
    @NotNull(message = "O ID do produto é obrigatório")
    private Long productId;

    @Min(value = 1, message = "A quantidade deve ser pelo menos 1")
    private int quantity;
}
