package br.com.foursales.order_service.domain.exception;


import java.io.Serial;

public class UserNotAuthenticatedException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -5526158149569980316L;

    public UserNotAuthenticatedException(String message) {
        super(message);
    }
}