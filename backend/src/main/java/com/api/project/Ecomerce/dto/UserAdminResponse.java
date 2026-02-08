package com.api.project.Ecomerce.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserAdminResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String role;
    private String status;
    private Boolean emailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
