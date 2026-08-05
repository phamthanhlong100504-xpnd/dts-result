package com.dts.result.application.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String message) {
        super("RES-404-001", message, HttpStatus.NOT_FOUND);
    }
}
