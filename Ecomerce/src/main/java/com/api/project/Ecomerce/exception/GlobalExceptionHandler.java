package com.api.project.Ecomerce.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.api.project.Ecomerce.response.ErrorResponse;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex) {
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
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder()
                        .success(false)
                        .message("Something went wrong")
                        .errorCode("INTERNAL_SERVER_ERROR")
                        .timestamp(LocalDateTime.now())
                        .build());
    }
}
