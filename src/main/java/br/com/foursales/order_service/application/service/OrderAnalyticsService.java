package br.com.foursales.order_service.application.service;

import br.com.foursales.order_service.domain.model.UserAverageTicketDto;
import br.com.foursales.order_service.domain.model.UserOrderCountDto;

import java.math.BigDecimal;
import java.util.List;

public interface OrderAnalyticsService {
    List<UserOrderCountDto> getTop5UsersByOrderCount(String startDate, String endDate);
    List<UserAverageTicketDto> getAverageTicketPerUser(String startDate, String endDate);
    BigDecimal getTotalRevenueForCurrentMonth();
}