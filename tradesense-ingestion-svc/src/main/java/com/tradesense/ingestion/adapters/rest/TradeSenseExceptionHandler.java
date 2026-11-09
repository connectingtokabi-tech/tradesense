package com.tradesense.ingestion.adapters.rest;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class TradeSenseExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(TradeSenseExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpectedException(
            Exception exception, HttpServletRequest request) {
        HttpStatusCode status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "Unexpected server error";
        if (exception instanceof ErrorResponse errorResponse) {
            status = errorResponse.getStatusCode();
            message = errorResponse.getBody().getDetail();
        }

        if (status.is5xxServerError()) {
            logger.error("Request {} {} failed: {}", request.getMethod(), request.getRequestURI(), message, exception);
        } else {
            logger.warn("Request {} {} rejected with {}: {}",
                    request.getMethod(), request.getRequestURI(), status.value(), message);
        }

        return errorResponse(status, message, request.getRequestURI());
    }

    private ResponseEntity<Map<String, Object>> errorResponse(
            HttpStatusCode status, String message, String path) {
        HttpStatus knownStatus = HttpStatus.resolve(status.value());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", knownStatus == null ? "Error" : knownStatus.getReasonPhrase());
        body.put("message", message);
        body.put("path", path);
        return ResponseEntity.status(status).body(body);
    }
}
