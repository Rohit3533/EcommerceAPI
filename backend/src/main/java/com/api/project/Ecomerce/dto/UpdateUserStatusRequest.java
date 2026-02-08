package com.api.project.Ecomerce.dto;

import lombok.Data;

@Data
public class UpdateUserStatusRequest {

    private String status; // ACTIVE or BLOCKED
}

