package com.api.project.Ecomerce.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.api.project.Ecomerce.response.ErrorResponse;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex) {
        log.error("ApiException: {} - Status: {}", ex.getMessage(), ex.getStatus());
        return ResponseEntity.status(ex.getStatus())
                .body(ErrorResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .errorCode(ex.getStatus().name())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unhandled exception occurred: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder()
                        .success(false)
                        .message("Internal Server Error: " + ex.getMessage())
                        .errorCode("INTERNAL_SERVER_ERROR")
                        .timestamp(LocalDateTime.now())
                        .build());
    }
}
