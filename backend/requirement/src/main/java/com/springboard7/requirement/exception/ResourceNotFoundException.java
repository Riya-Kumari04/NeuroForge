package com.springboard7.requirement.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String specificationNotFound) {
        super(specificationNotFound);
    }
}
