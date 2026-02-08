package com.api.project.Ecomerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Long orderId;
    private String orderNumber;
    private BigDecimal totalAmount;
    private String status;
    private String paymentStatus;
    private String paymentMethod;
    private LocalDateTime createdAt;

    // Delivery tracking fields
    private String trackingId;
    private DeliveryPartnerResponse deliveryPartner;
    private LocalDateTime assignedAt;
    private LocalDateTime shippedAt;
    private LocalDateTime outForDeliveryAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime estimatedDelivery;

    // Customer and address info
    private String userName;
    private String userEmail;
    private String shippingAddress;

    private List<OrderItemResponse> items;
    private List<OrderStatusHistoryResponse> statusHistory;
}