package br.com.foursales.order_service.application.service;

import br.com.foursales.order_service.application.dto.CreateOrderRequest;
import br.com.foursales.order_service.domain.model.Order;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    Order createOrder(CreateOrderRequest request);

    Order getOrderById(UUID orderId);

    List<Order> getAllOrders();

    Order cancelOrder(UUID orderId);

    Order payOrder(UUID orderId);

}
