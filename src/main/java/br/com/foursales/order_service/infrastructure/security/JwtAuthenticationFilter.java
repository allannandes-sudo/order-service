package br.com.foursales.order_service.infrastructure.security;

import br.com.foursales.order_service.infrastructure.security.dto.CustomAuthenticationToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String token = extractToken(request);
        if (token == null) {
            chain.doFilter(request, response);
            return;
        }

        try {
            log.info("Validando token...");
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            log.info("Token válido! Claims: " + claims);


            String username = claims.getSubject();
            List<SimpleGrantedAuthority> authorities = extractAuthorities(claims);
            CustomAuthenticationToken authentication = new CustomAuthenticationToken(username, claims, authorities);
//            Object rolesObject = claims.get("roles");
//            List<String> roles = rolesObject instanceof List<?>
//                    ? ((List<?>) rolesObject).stream().map(Object::toString).toList()
//                    : List.of();
//
//            List<SimpleGrantedAuthority> authorities = roles.stream()
//                    .map(SimpleGrantedAuthority::new)
//                    .collect(Collectors.toList());

//            UsernamePasswordAuthenticationToken authentication =
//                    new UsernamePasswordAuthenticationToken(new User(username, "", authorities), null, authorities);

//            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            log.info("Autenticado: " + username + " | Roles: " + authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);


        } catch (ExpiredJwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token expirado");
            response.getWriter().flush();
            return;
        } catch (JwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token inválido");
            response.getWriter().flush();
            return;
        }

        chain.doFilter(request, response);
    }

    private List<SimpleGrantedAuthority> extractAuthorities(Claims claims) {
        Object rolesObject = claims.get("roles");
        return rolesObject instanceof List<?>
                ? ((List<?>) rolesObject).stream().map(Object::toString).map(SimpleGrantedAuthority::new).toList()
                : List.of();
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        String token = header.substring(7);
        log.info("Token recebido: " + token);
        return token;
    }

}
