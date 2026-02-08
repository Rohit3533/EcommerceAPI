package com.api.project.Ecomerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryPartnerDocumentRequest {
    private String identityDocType; // AADHAAR or PAN
    private String identityDocNumber;
    private String identityDocUrl;
    private String drivingLicenseNumber;
    private String drivingLicenseUrl;
}
