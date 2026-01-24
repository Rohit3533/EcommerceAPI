package com.api.project.Ecomerce.dto;

import lombok.Data;

@Data
public class UpdateOrderStatusRequest {

    private String status; // SHIPPED, DELIVERED, CANCELLED
}