package com.api.project.Ecomerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {
    private String label;
    private String fullName;
    private String phone;
    private String streetAddress;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private Boolean isDefault;
}
