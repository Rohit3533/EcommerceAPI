package com.api.project.Ecomerce.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private String token;
    private String role;
    private String expiresAt;
}