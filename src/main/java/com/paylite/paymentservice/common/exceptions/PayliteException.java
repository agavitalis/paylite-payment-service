package com.paylite.paymentservice.common.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@AllArgsConstructor
public class PayliteException extends RuntimeException {
    private HttpStatus status;
    private String message;
}

