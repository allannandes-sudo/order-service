package br.com.foursales.order_service.domain.model.enums;

import lombok.Getter;

@Getter
public enum OrderStatus {
    PENDING, PAID, CANCELED;
}
