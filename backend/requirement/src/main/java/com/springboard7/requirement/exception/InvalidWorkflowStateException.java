package com.springboard7.requirement.exception;

public class InvalidWorkflowStateException extends RuntimeException {

    public InvalidWorkflowStateException(String message) {
        super(message);
    }

}