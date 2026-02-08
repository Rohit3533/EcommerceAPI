package com.api.project.Ecomerce.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreatePaymentIntentRequest {
    private Long orderId;
}
