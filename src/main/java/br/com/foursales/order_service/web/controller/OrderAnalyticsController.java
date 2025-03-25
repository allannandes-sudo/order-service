package br.com.foursales.order_service.web.controller;

import br.com.foursales.order_service.domain.model.UserAverageTicketDto;
import br.com.foursales.order_service.domain.model.UserOrderCountDto;
import br.com.foursales.order_service.infrastructure.persistence.repository.OrderRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders/analytics")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class OrderAnalyticsController {

    private final OrderRepository orderRepository;

    @GetMapping("/top-users")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<UserOrderCountDto>> getTop5UsersByOrderCount() {
        List<Object[]> results = orderRepository.findTop5UsersByOrderCount();
        List<UserOrderCountDto> topUsers = results.stream()
                .map(result -> new UserOrderCountDto((String) result[0], (Long) result[1]))
                .collect(Collectors.toList());
        return ResponseEntity.ok(topUsers);
    }

    @GetMapping("/average-ticket")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<UserAverageTicketDto>> getAverageTicketPerUser() {
        List<Object[]> results = orderRepository.findAverageTicketPerUser();
        List<UserAverageTicketDto> averageTickets = results.stream()
                .map(result -> new UserAverageTicketDto((String) result[0], (BigDecimal) result[1]))
                .collect(Collectors.toList());
        return ResponseEntity.ok(averageTickets);
    }

    @GetMapping("/total-revenue")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<BigDecimal> getTotalRevenueForCurrentMonth() {
        BigDecimal totalRevenue = orderRepository.findTotalRevenueForCurrentMonth();
        return ResponseEntity.ok(totalRevenue);
    }
}