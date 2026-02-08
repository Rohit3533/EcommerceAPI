package com.api.project.Ecomerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryPartnerResponse {
    private Long id;
    private Long userId;
    private String name;
    private String phone;
    private String email;
    private String vehicleNumber;
    private String vehicleType;
    private String status;

    // Verification
    private String verificationStatus;
    private String rejectionReason;
    private LocalDateTime verifiedAt;

    // Documents
    private String identityDocType;
    private String identityDocNumber;
    private Boolean hasIdentityDoc;
    private Boolean hasDrivingLicense;

    // Location
    private Double currentLatitude;
    private Double currentLongitude;
    private Boolean isOnline;
    private LocalDateTime lastLocationUpdate;

    // Stats
    private Integer currentOrders;
    private Integer totalDeliveries;
    private Double averageRating;

    private LocalDateTime createdAt;
}
