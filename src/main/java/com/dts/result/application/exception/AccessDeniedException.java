package com.dts.result.application.exception;

import org.springframework.http.HttpStatus;

public class AccessDeniedException extends BusinessException {

    public AccessDeniedException(String message) {
        super("RES-403-001", message, HttpStatus.FORBIDDEN);
    }

    public AccessDeniedException() {
        super("RES-403-001", "You do not have permission to access this resource.", HttpStatus.FORBIDDEN);
    }
}

