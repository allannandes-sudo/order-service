package br.com.foursales.order_service.infrastructure.config;

import br.com.foursales.order_service.infrastructure.client.FeignClientInterceptor;
import feign.RequestInterceptor;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableFeignClients("br.com.foursales")
public class FeignConfig {

    @Bean
    public RequestInterceptor customFeignClientInterceptor() {
        return new FeignClientInterceptor();
    }
}
