
package com.estapar.parking.application.error;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class BadRequestException extends AppException {
    public BadRequestException(String code, String message) {
        super(HttpStatus.BAD_REQUEST, code, message);
    }
    public BadRequestException(String code, String message, Map<String,Object> extra) {
        super(HttpStatus.BAD_REQUEST, code, message, extra);
    }
}
