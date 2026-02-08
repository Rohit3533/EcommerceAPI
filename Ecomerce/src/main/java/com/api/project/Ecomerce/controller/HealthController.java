package com.api.project.Ecomerce.controller;

import com.api.project.Ecomerce.response.ApiResponse;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "http://localhost:5173")
public class HealthController {

    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.<String>builder()
                .success(true)
                .message("Application is running")
                .data("UP")
                .timestamp(LocalDateTime.now())
                .build();
    }
}
