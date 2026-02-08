package com.api.project.Ecomerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderRequest {
    private String paymentMethod; // COD, UPI, CARD
    private Long addressId; // Optional: specific address to use
}
