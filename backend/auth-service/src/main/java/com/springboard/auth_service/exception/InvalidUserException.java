package com.springboard.auth_service.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;


public class InvalidUserException extends RuntimeException {

    public InvalidUserException(String message){
        super(message);
    }
}
