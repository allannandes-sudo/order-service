package br.com.foursales.order_service.web.controller.swagger;


import br.com.foursales.order_service.application.dto.CreateOrderRequest;
import br.com.foursales.order_service.domain.model.Order;
import br.com.foursales.order_service.web.annotations.DefaultSwaggerMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Pedidos", description = "Endpoints para gerenciamento de pedidos!")
public interface OrderControllerDoc {

    @DefaultSwaggerMessage
    @PostMapping
    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "Criar order de serviço")
    ResponseEntity<Order> createOrder(@RequestBody CreateOrderRequest request);

    @DefaultSwaggerMessage
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    @Operation(summary = "Lista pedidos por Id!!")
    @ApiResponse(
            responseCode = "200",
            description = "OK. A solicitação foi bem-sucedida.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = Order.class)
            )
    )
    ResponseEntity<Order> getOrderById(@PathVariable UUID id);

    @DefaultSwaggerMessage
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    ResponseEntity<List<Order>> getAllOrders();

    @DefaultSwaggerMessage
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    @Operation(summary = "Cancelar ordem de pedido")
    ResponseEntity<Order> cancelOrder(@PathVariable UUID id);

    @DefaultSwaggerMessage
    @PostMapping("/{id}/pay")
    @PreAuthorize("hasAuthority('USER')")
    @ApiResponse(
            responseCode = "201",
            description = "Created. A solicitação foi bem-sucedida e um novo recurso foi criado.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = Order.class)
            )
    )
    @Operation(summary = "Pagar order de serviço")
    ResponseEntity<Order> payOrder(@PathVariable UUID id);
}
