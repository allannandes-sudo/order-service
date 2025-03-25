package br.com.foursales.order_service.infrastructure.security.dto;


import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serial;
import java.util.Collection;
import java.util.Map;

public class CustomAuthenticationToken extends AbstractAuthenticationToken {

    @Serial
    private static final long serialVersionUID = 3249589696114675471L;
    private final String principal;
    private final Map<String, Object> claims;

    public CustomAuthenticationToken(String principal, Map<String, Object> claims, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.claims = claims;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    public Map<String, Object> getClaims() {
        return claims;
    }
}