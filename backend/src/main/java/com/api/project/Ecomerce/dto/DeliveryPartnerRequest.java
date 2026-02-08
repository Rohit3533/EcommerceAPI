package com.api.project.Ecomerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryPartnerRequest {
    private String name;
    private String phone;
    private String email;
    private String vehicleNumber;
    private String vehicleType;
    private String status;
}
