package br.com.foursales.order_service.infrastructure.client;

import br.com.foursales.order_service.domain.model.ProductStockResponse;
import br.com.foursales.order_service.infrastructure.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

//@FeignClient(name = "product-service", url = "${product.service.url}", configuration = FeignConfig.class)
@FeignClient(
        name = "product-service",
        url = "${product.service.url}",
        path = "/products",
        configuration = FeignConfig.class
)
public interface ProductClient {
    @GetMapping("/stock-check")
    ResponseEntity<Map<Long, ProductStockResponse>> checkStock(@RequestParam List<Long> productIds);

    @PutMapping("/update-stock")
    void updateStock(@RequestBody Map<Long, Integer> stockUpdates);
}
