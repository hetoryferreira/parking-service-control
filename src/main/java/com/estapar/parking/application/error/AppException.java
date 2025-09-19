package com.estapar.parking.application.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
public class AppException extends RuntimeException {
    private final HttpStatus status;
    private final String code;
    private final Map<String, Object> extra;

    public AppException(HttpStatus status, String code, String message) {
        this(status, code, message, Map.of());
    }
    public AppException(HttpStatus status, String code, String message, Map<String, Object> extra) {
        super(message);
        this.status = status;
        this.code = code;
        this.extra = (extra == null ? Map.of() : extra);
    }
}
