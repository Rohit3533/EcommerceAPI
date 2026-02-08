package com.api.project.Ecomerce.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class UserProfileResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String role;
    private String status;
    private Boolean emailVerified;
    private List<AddressResponse> addresses;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
