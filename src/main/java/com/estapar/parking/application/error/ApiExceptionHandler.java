package com.estapar.parking.application.error;

import org.springframework.validation.BindException;
import java.time.Instant;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.*;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ProblemDetail> handleApp(AppException ex, HttpServletRequest req) {
        log.warn("Domain error: code={} status={} msg={}", ex.getCode(), ex.getStatus(), ex.getMessage());
        var pd = ProblemDetail.forStatus(ex.getStatus());
        pd.setTitle(ex.getCode());
        pd.setDetail(ex.getMessage());
        pd.setProperty("path", req.getRequestURI());
        pd.setProperty("timestamp", Instant.now().toString());
        ex.getExtra().forEach(pd::setProperty);
        return ResponseEntity.status(ex.getStatus()).body(pd);
    }

    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            BindException.class,
            ConstraintViolationException.class
    })
    public ResponseEntity<ProblemDetail> handleClientErrors(Exception ex, HttpServletRequest req) {
        String detail = switch (ex) {
            case MissingServletRequestParameterException m -> "Missing parameter: " + m.getParameterName();
            case MethodArgumentTypeMismatchException m -> "Invalid type for '" + m.getName() + "'";
            case BindException b -> b.getAllErrors().stream()
                    .map(e -> e.getDefaultMessage() != null ? e.getDefaultMessage() : e.toString())
                    .collect(Collectors.joining("; "));
            case ConstraintViolationException c -> c.getConstraintViolations().stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining("; "));
            default -> "Bad request";
        };

        var pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("BAD_REQUEST");
        pd.setDetail(detail);
        pd.setProperty("path", req.getRequestURI());
        pd.setProperty("timestamp", Instant.now().toString());
        return ResponseEntity.badRequest().body(pd);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ProblemDetail> handleResponseStatus(ResponseStatusException ex, HttpServletRequest req) {
        var status = ex.getStatusCode();
        var pd = ProblemDetail.forStatus(status);
        pd.setTitle(status.toString());
        pd.setDetail(ex.getReason() != null ? ex.getReason() : ex.getMessage());
        pd.setProperty("path", req.getRequestURI());
        pd.setProperty("timestamp", Instant.now().toString());
        return ResponseEntity.status(status).body(pd);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGeneric(Exception ex, HttpServletRequest req) {
        log.error("Unexpected error", ex);
        var pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        pd.setTitle("INTERNAL_ERROR");
        pd.setDetail("Unexpected error");
        pd.setProperty("path", req.getRequestURI());
        pd.setProperty("timestamp", Instant.now().toString());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(pd);
    }
}
