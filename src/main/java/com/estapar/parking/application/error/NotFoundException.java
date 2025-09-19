package com.estapar.parking.application.error;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class NotFoundException extends AppException {
    public NotFoundException(String code, String message) {
        super(HttpStatus.NOT_FOUND, code, message);
    }
    public NotFoundException(String code, String message, Map<String,Object> extra) {
        super(HttpStatus.NOT_FOUND, code, message, extra);
    }
}