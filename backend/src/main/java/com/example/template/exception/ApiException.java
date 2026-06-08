package com.example.template.exception;

import org.springframework.http.HttpStatus;

/**
 * Application-level exception that carries an HTTP status.
 * Throw this from any service layer; GlobalExceptionHandler converts it to the standard error shape.
 */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
