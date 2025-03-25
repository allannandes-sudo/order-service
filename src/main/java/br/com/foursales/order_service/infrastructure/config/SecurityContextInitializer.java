package br.com.foursales.order_service.infrastructure.config;


import jakarta.annotation.PostConstruct;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityContextInitializer {

    @PostConstruct
    public void init() {
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
        System.out.println("🔄 SecurityContextHolder configurado para INHERITABLETHREADLOCAL");
    }
}
