package com.api.project.Ecomerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderStatusRequest {

    private String status; // SHIPPED, DELIVERED, CANCELLED
    private String note; // Optional note for status history
}