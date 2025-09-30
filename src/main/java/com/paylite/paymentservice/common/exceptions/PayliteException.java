package com.paylite.paymentservice.common.exceptions;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class PayliteException extends RuntimeException {
    private final HttpStatus status;
    private final String message;

    public PayliteException(HttpStatus status, String message) {
        super(message);
        this.status = status;
        this.message = message;
    }

    public PayliteException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.message = message;
    }

    public static PayliteException notFound(String message) {
        return new PayliteException(HttpStatus.NOT_FOUND, message);
    }

    public static PayliteException conflict(String message) {
        return new PayliteException(HttpStatus.CONFLICT, message);
    }

    public static PayliteException badRequest(String message) {
        return new PayliteException(HttpStatus.BAD_REQUEST, message);
    }

    public static PayliteException unauthorized(String message) {
        return new PayliteException(HttpStatus.UNAUTHORIZED, message);
    }

    public static PayliteException internalError(String message) {
        return new PayliteException(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }
}