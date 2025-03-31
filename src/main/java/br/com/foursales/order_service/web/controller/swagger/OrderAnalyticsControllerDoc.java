package br.com.foursales.order_service.web.controller.swagger;

import br.com.foursales.order_service.domain.model.UserAverageTicketDto;
import br.com.foursales.order_service.domain.model.UserOrderCountDto;
import br.com.foursales.order_service.web.annotations.DefaultSwaggerMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Relatórios", description = "Endpoints para relatórios!")
public interface OrderAnalyticsControllerDoc {


    @GetMapping("/top-users")
    @PreAuthorize("hasAuthority('ADMIN')")
    @DefaultSwaggerMessage
    @Operation(summary = "Top 5 usuários que mais compraram")
    ResponseEntity<List<UserOrderCountDto>> getTop5UsersByOrderCount(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate);

    @GetMapping("/average-ticket")
    @PreAuthorize("hasAuthority('ADMIN')")
    @DefaultSwaggerMessage
    @Operation(summary = "Ticket médio")
    ResponseEntity<List<UserAverageTicketDto>> getAverageTicketPerUser(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate);

    @GetMapping("/total-revenue")
    @PreAuthorize("hasAuthority('ADMIN')")
    @DefaultSwaggerMessage
    @Operation(summary = "Valor total faturado no mês atual")
    ResponseEntity<BigDecimal> getTotalRevenueForCurrentMonth();

}
