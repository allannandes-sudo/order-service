package br.com.foursales.order_service.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAverageTicketDto {
    private String userId;
    private BigDecimal ticketMedio;
}