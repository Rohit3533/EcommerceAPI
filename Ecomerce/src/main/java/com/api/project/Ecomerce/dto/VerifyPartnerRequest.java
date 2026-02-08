package com.api.project.Ecomerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyPartnerRequest {
    private boolean approved; // true = APPROVE, false = REJECT
    private String rejectionReason;

    // Compatibility method for existing code
    public String getAction() {
        return approved ? "APPROVE" : "REJECT";
    }
}
