package com.api.project.Ecomerce.dto;

import lombok.Data;

@Data
public class InitiateRegistrationRequest {
    private String name;
    private String email;
    private String password;
    private String phone;
}
