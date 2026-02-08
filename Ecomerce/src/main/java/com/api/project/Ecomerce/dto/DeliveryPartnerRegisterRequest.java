package com.api.project.Ecomerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryPartnerRegisterRequest {
    private String name;
    private String email;
    private String password;
    private String phone;
    private String vehicleType; // BIKE, VAN, TRUCK
    private String vehicleNumber;
}
