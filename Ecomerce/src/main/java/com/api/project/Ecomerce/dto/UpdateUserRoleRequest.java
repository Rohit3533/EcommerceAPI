package com.api.project.Ecomerce.dto;

import lombok.Data;

@Data
public class UpdateUserRoleRequest {

    private String role; // ADMIN or CUSTOMER
}

