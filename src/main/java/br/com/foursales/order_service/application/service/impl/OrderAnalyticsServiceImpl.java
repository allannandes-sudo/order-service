package br.com.foursales.order_service.application.service.impl;

import br.com.foursales.order_service.application.service.OrderAnalyticsService;
import br.com.foursales.order_service.domain.model.UserAverageTicketDto;
import br.com.foursales.order_service.domain.model.UserOrderCountDto;
import br.com.foursales.order_service.infrastructure.persistence.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderAnalyticsServiceImpl implements OrderAnalyticsService {

    private final OrderRepository orderRepository;

    @Override
    public List<UserOrderCountDto> getTop5UsersByOrderCount(String startDate, String endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime startDateTime = startDate != null ? LocalDate.parse(startDate, formatter).atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? LocalDate.parse(endDate, formatter).atTime(LocalTime.MAX) : null;

        List<Object[]> results = orderRepository.findTop5UsersByOrderCount(startDateTime, endDateTime);
        return results.stream()
                .map(result -> new UserOrderCountDto((String) result[0], (Long) result[1]))
                .limit(5)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserAverageTicketDto> getAverageTicketPerUser(String startDate, String endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime startDateTime = startDate != null ? LocalDate.parse(startDate, formatter).atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? LocalDate.parse(endDate, formatter).atTime(LocalTime.MAX) : null;

        List<Object[]> results = orderRepository.findAverageTicketPerUser(startDateTime, endDateTime);
        return results.stream()
                .map(result -> {
                    String userId = (String) result[0];
                    Double averageTicket = (Double) result[1];
                    BigDecimal averageTicketBigDecimal = averageTicket != null ? BigDecimal.valueOf(averageTicket) : BigDecimal.ZERO;
                    return new UserAverageTicketDto(userId, averageTicketBigDecimal);
                })
                .collect(Collectors.toList());
    }

    @Override
    public BigDecimal getTotalRevenueForCurrentMonth() {
        return orderRepository.findTotalRevenueForCurrentMonth();
    }
}