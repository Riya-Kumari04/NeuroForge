package com.neuroforge.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidSprintStateException extends RuntimeException {
    public InvalidSprintStateException(String message) {
        super(message);
    }
}
