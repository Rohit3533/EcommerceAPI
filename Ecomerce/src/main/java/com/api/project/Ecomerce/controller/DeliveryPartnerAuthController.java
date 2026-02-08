package com.api.project.Ecomerce.controller;

import com.api.project.Ecomerce.dto.*;
import com.api.project.Ecomerce.response.ApiResponse;
import com.api.project.Ecomerce.service.AuthService;
import com.api.project.Ecomerce.service.DeliveryPartnerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/delivery")
@RequiredArgsConstructor
@Slf4j
public class DeliveryPartnerAuthController {

    private final DeliveryPartnerService deliveryPartnerService;
    private final AuthService authService;

    // ================= REGISTER =================
    @PostMapping("/register")
    public ApiResponse<DeliveryPartnerResponse> register(@RequestBody DeliveryPartnerRegisterRequest request) {
        log.info("DeliveryPartnerAuthController - register: {}", request.getEmail());

        DeliveryPartnerResponse response = deliveryPartnerService.registerPartner(request);

        return ApiResponse.<DeliveryPartnerResponse>builder()
                .success(true)
                .message("Registration successful. Please upload documents for verification.")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ================= LOGIN (uses existing auth) =================
    @PostMapping("/login")
    public ApiResponse<String> login(@RequestBody LoginRequest request) {
        log.info("DeliveryPartnerAuthController - login: {}", request.getEmail());

        String token = authService.login(request.getEmail(), request.getPassword());

        return ApiResponse.<String>builder()
                .success(true)
                .message("Login successful")
                .data(token)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ================= GET PROFILE =================
    @GetMapping("/profile")
    @PreAuthorize("hasRole('DELIVERY_PARTNER')")
    public ApiResponse<DeliveryPartnerResponse> getProfile() {
        log.info("DeliveryPartnerAuthController - getProfile");

        DeliveryPartnerResponse response = deliveryPartnerService.getMyProfile();

        return ApiResponse.<DeliveryPartnerResponse>builder()
                .success(true)
                .message("Profile fetched successfully")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ================= UPLOAD DOCUMENTS =================
    @PostMapping("/documents")
    @PreAuthorize("hasRole('DELIVERY_PARTNER')")
    public ApiResponse<DeliveryPartnerResponse> uploadDocuments(@RequestBody DeliveryPartnerDocumentRequest request) {
        log.info("DeliveryPartnerAuthController - uploadDocuments");

        DeliveryPartnerResponse response = deliveryPartnerService.uploadDocuments(request);

        return ApiResponse.<DeliveryPartnerResponse>builder()
                .success(true)
                .message("Documents uploaded successfully. Awaiting admin verification.")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ================= UPDATE LOCATION =================
    @PutMapping("/location")
    @PreAuthorize("hasRole('DELIVERY_PARTNER')")
    public ApiResponse<DeliveryPartnerResponse> updateLocation(@RequestBody LocationUpdateRequest request) {
        log.info("DeliveryPartnerAuthController - updateLocation");

        DeliveryPartnerResponse response = deliveryPartnerService.updateLocation(request);

        return ApiResponse.<DeliveryPartnerResponse>builder()
                .success(true)
                .message("Location updated")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
