package com.api.project.Ecomerce.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentIntentResponse {
    private String clientSecret;
    private String paymentIntentId;
    private Long orderId;
    private Long amount;
    private String currency;
}
