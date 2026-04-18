package com.nimbleways.springboilerplate.config.exceptions;

import org.springframework.http.HttpStatus;

public class AppException extends Exception {

    private static final long serialVersionUID = 1L;
    private final String message;
    private final HttpStatus httpCode;


    AppException(final String message, HttpStatus httpCode, final Throwable cause) {
        super(message,cause);
        this.message = message;
        this.httpCode = httpCode;
    }

    public AppException(final String message, HttpStatus httpCode) {
        super(message);
        this.message = message;
        this.httpCode = httpCode;
    }

    public HttpStatus getHttpCode() {
        return httpCode;
    }
}


