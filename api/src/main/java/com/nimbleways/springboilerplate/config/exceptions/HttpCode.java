package com.nimbleways.springboilerplate.config.exceptions;

public enum HttpCode {
    OK(200 ),
    CREATED(201),
    ACCEPTED(202),

    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    NOT_FOUND(404),

    INTERNAL_SERVER_ERROR(500),
    BAD_GATEWAY(502),
    SERVICE_UNAVAILABLE(503);

    private final int code;

    HttpCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

}
