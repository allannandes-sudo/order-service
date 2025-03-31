package br.com.foursales.order_service.web.controller;

import br.com.foursales.order_service.application.service.OrderAnalyticsService;
import br.com.foursales.order_service.domain.model.UserAverageTicketDto;
import br.com.foursales.order_service.domain.model.UserOrderCountDto;
import br.com.foursales.order_service.web.controller.swagger.OrderAnalyticsControllerDoc;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/orders/analytics")
@RequiredArgsConstructor
public class OrderAnalyticsController implements OrderAnalyticsControllerDoc {

    private final OrderAnalyticsService orderAnalyticsService;

    @GetMapping("/top-users")
    public ResponseEntity<List<UserOrderCountDto>> getTop5UsersByOrderCount(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        List<UserOrderCountDto> topUsers = orderAnalyticsService.getTop5UsersByOrderCount(startDate, endDate);
        return ResponseEntity.ok(topUsers);
    }

    @GetMapping("/average-ticket")
    public ResponseEntity<List<UserAverageTicketDto>> getAverageTicketPerUser(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        List<UserAverageTicketDto> averageTickets = orderAnalyticsService.getAverageTicketPerUser(startDate, endDate);
        return ResponseEntity.ok(averageTickets);
    }

    @GetMapping("/total-revenue")
    public ResponseEntity<BigDecimal> getTotalRevenueForCurrentMonth() {
        BigDecimal totalRevenue = orderAnalyticsService.getTotalRevenueForCurrentMonth();
        return ResponseEntity.ok(totalRevenue);
    }

}