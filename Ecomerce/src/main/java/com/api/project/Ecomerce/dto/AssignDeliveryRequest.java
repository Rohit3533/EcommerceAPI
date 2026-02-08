package com.api.project.Ecomerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignDeliveryRequest {
    private Long deliveryPartnerId;
    private String estimatedDeliveryDays; // e.g., "3" for 3 days from now
}
