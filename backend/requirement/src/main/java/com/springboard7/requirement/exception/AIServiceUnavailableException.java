package com.springboard7.requirement.exception;


public class AIServiceUnavailableException extends RuntimeException {

    public AIServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}