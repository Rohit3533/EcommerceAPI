package com.api.project.Ecomerce.controller;

import com.api.project.Ecomerce.response.ApiResponse;
import com.api.project.Ecomerce.dto.DeliveryPartnerRequest;
import com.api.project.Ecomerce.dto.DeliveryPartnerResponse;
import com.api.project.Ecomerce.service.DeliveryPartnerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/delivery-partners")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class DeliveryPartnerController {

    private final DeliveryPartnerService deliveryPartnerService;

    @GetMapping
    public ApiResponse<List<DeliveryPartnerResponse>> getAll() {
        log.info("Admin fetching all delivery partners");
        List<DeliveryPartnerResponse> partners = deliveryPartnerService.getAll();
        return ApiResponse.<List<DeliveryPartnerResponse>>builder()
                .success(true)
                .message("Delivery partners fetched successfully")
                .data(partners)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @GetMapping("/active")
    public ApiResponse<List<DeliveryPartnerResponse>> getActive() {
        log.info("Fetching active delivery partners");
        List<DeliveryPartnerResponse> partners = deliveryPartnerService.getActive();
        return ApiResponse.<List<DeliveryPartnerResponse>>builder()
                .success(true)
                .message("Active delivery partners fetched")
                .data(partners)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<DeliveryPartnerResponse> getById(@PathVariable Long id) {
        log.info("Fetching delivery partner: id={}", id);
        DeliveryPartnerResponse partner = deliveryPartnerService.getById(id);
        return ApiResponse.<DeliveryPartnerResponse>builder()
                .success(true)
                .message("Delivery partner fetched")
                .data(partner)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @PostMapping
    public ApiResponse<DeliveryPartnerResponse> create(@RequestBody DeliveryPartnerRequest request) {
        log.info("Creating delivery partner: {}", request.getName());
        DeliveryPartnerResponse partner = deliveryPartnerService.create(request);
        return ApiResponse.<DeliveryPartnerResponse>builder()
                .success(true)
                .message("Delivery partner created successfully")
                .data(partner)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<DeliveryPartnerResponse> update(
            @PathVariable Long id,
            @RequestBody DeliveryPartnerRequest request) {
        log.info("Updating delivery partner: id={}", id);
        DeliveryPartnerResponse partner = deliveryPartnerService.update(id, request);
        return ApiResponse.<DeliveryPartnerResponse>builder()
                .success(true)
                .message("Delivery partner updated successfully")
                .data(partner)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        log.info("Deleting delivery partner: id={}", id);
        deliveryPartnerService.delete(id);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Delivery partner deleted successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ================= PENDING VERIFICATIONS =================
    @GetMapping("/pending")
    public ApiResponse<List<DeliveryPartnerResponse>> getPending() {
        log.info("Fetching pending verifications");
        List<DeliveryPartnerResponse> partners = deliveryPartnerService.getPendingVerifications();
        return ApiResponse.<List<DeliveryPartnerResponse>>builder()
                .success(true)
                .message("Pending verifications fetched")
                .data(partners)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ================= VERIFY PARTNER =================
    @PutMapping("/{id}/verify")
    public ApiResponse<DeliveryPartnerResponse> verifyPartner(
            @PathVariable Long id,
            @RequestBody com.api.project.Ecomerce.dto.VerifyPartnerRequest request) {
        String adminEmail = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();
        log.info("Admin {} verifying partner: id={}, approved={}", adminEmail, id, request.isApproved());
        DeliveryPartnerResponse partner = deliveryPartnerService.verifyPartner(id, request, adminEmail);
        return ApiResponse.<DeliveryPartnerResponse>builder()
                .success(true)
                .message(request.isApproved() ? "Partner approved successfully" : "Partner rejected")
                .data(partner)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
