package br.com.foursales.order_service.infrastructure.client;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


@Slf4j
@Component
public class FeignClientInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        String token = getTokenFromRequest();

        if (token != null) {
            requestTemplate.header("Authorization",  token);
            log.info("Token JWT adicionado ao header da requisição Feign.");
        } else {
            log.warn("Nenhum token JWT encontrado no contexto da requisição.");
        }
    }

    private String getTokenFromRequest() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (requestAttributes != null) {
            HttpServletRequest request = requestAttributes.getRequest();
            return request.getHeader("Authorization");
        }

        return null;
    }
}
