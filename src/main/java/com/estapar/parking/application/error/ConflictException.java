package com.estapar.parking.application.error;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class ConflictException extends AppException {
    public ConflictException(String code, String message) {
        super(HttpStatus.CONFLICT, code, message);
    }
    public ConflictException(String code, String message, Map<String,Object> extra) {
        super(HttpStatus.CONFLICT, code, message, extra);
    }
}