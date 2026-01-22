package com.api.project.Ecomerce.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ErrorResponse {

    private boolean success;
    private String message;
    private String errorCode;
    private LocalDateTime timestamp;
}
