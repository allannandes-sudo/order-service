package br.com.foursales.order_service.domain.exception;

import br.com.foursales.order_service.application.dto.error.ErrorDetail;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorDetail> handleProductNotFoundException(BusinessException ex) {
        ErrorDetail errorResponse = new ErrorDetail(
                String.valueOf(HttpStatus.NOT_FOUND.value()),
                HttpStatus.NOT_FOUND.name(),
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }


    @ExceptionHandler({SecurityException.class, UserNotAuthenticatedException.class})
    public ResponseEntity<ErrorDetail> handleSecurityException(SecurityException ex) {
        ErrorDetail errorResponse = new ErrorDetail(
                String.valueOf(HttpStatus.UNAUTHORIZED.value()),
                HttpStatus.UNAUTHORIZED.name(),
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
}
