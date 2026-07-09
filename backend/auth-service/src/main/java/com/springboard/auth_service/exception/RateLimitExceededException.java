package com.springboard.auth_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class RateLimitExceededException extends RuntimeException {

    private final HttpStatus status;

    public RateLimitExceededException(String message) {
        super(message);
        this.status = HttpStatus.TOO_MANY_REQUESTS;

    }
}