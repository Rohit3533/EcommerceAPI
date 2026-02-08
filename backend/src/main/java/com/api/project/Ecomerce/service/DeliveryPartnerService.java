package com.api.project.Ecomerce.service;

import com.api.project.Ecomerce.dto.*;
import com.api.project.Ecomerce.entity.DeliveryPartner;
import com.api.project.Ecomerce.entity.User;
import com.api.project.Ecomerce.exception.ApiException;
import com.api.project.Ecomerce.repository.DeliveryPartnerRepository;
import com.api.project.Ecomerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DeliveryPartnerService {

    private final DeliveryPartnerRepository deliveryPartnerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ================= SELF REGISTRATION =================
    @Transactional
    public DeliveryPartnerResponse registerPartner(DeliveryPartnerRegisterRequest request) {
        log.info("Registering delivery partner: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException("Email already registered", HttpStatus.BAD_REQUEST);
        }

        if (deliveryPartnerRepository.existsByPhone(request.getPhone())) {
            throw new ApiException("Phone number already registered", HttpStatus.BAD_REQUEST);
        }

        // Create User with DELIVERY_PARTNER role
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role("DELIVERY_PARTNER")
                .status("ACTIVE")
                .emailVerified(false)
                .build();
        User savedUser = userRepository.save(user);

        // Create DeliveryPartner linked to User
        DeliveryPartner partner = DeliveryPartner.builder()
                .user(savedUser)
                .name(request.getName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .vehicleType(request.getVehicleType())
                .vehicleNumber(request.getVehicleNumber())
                .verificationStatus("PENDING")
                .status("ACTIVE")
                .isOnline(false)
                .build();

        DeliveryPartner saved = deliveryPartnerRepository.save(partner);
        log.info("Delivery partner registered: id={}, userId={}", saved.getId(), savedUser.getId());

        return mapToResponse(saved);
    }

    // ================= GET CURRENT PARTNER PROFILE =================
    public DeliveryPartnerResponse getMyProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        DeliveryPartner partner = deliveryPartnerRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("Delivery partner profile not found", HttpStatus.NOT_FOUND));
        return mapToResponse(partner);
    }

    // ================= UPLOAD DOCUMENTS =================
    @Transactional
    public DeliveryPartnerResponse uploadDocuments(DeliveryPartnerDocumentRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        DeliveryPartner partner = deliveryPartnerRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("Delivery partner not found", HttpStatus.NOT_FOUND));

        partner.setIdentityDocType(request.getIdentityDocType());
        partner.setIdentityDocNumber(request.getIdentityDocNumber());
        partner.setIdentityDocUrl(request.getIdentityDocUrl());
        partner.setDrivingLicenseNumber(request.getDrivingLicenseNumber());
        partner.setDrivingLicenseUrl(request.getDrivingLicenseUrl());

        DeliveryPartner updated = deliveryPartnerRepository.save(partner);
        log.info("Documents uploaded for partner: id={}", updated.getId());

        return mapToResponse(updated);
    }

    // ================= UPDATE LOCATION =================
    @Transactional
    public DeliveryPartnerResponse updateLocation(LocationUpdateRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        DeliveryPartner partner = deliveryPartnerRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("Delivery partner not found", HttpStatus.NOT_FOUND));

        partner.setCurrentLatitude(request.getLatitude());
        partner.setCurrentLongitude(request.getLongitude());
        partner.setLastLocationUpdate(LocalDateTime.now());

        if (request.getIsOnline() != null) {
            partner.setIsOnline(request.getIsOnline());
        }

        DeliveryPartner updated = deliveryPartnerRepository.save(partner);
        return mapToResponse(updated);
    }

    // ================= ADMIN: GET PENDING VERIFICATIONS =================
    public List<DeliveryPartnerResponse> getPendingVerifications() {
        return deliveryPartnerRepository.findByVerificationStatus("PENDING").stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ================= ADMIN: VERIFY PARTNER =================
    @Transactional
    public DeliveryPartnerResponse verifyPartner(Long partnerId, VerifyPartnerRequest request, String adminEmail) {
        DeliveryPartner partner = deliveryPartnerRepository.findById(partnerId)
                .orElseThrow(() -> new ApiException("Delivery partner not found", HttpStatus.NOT_FOUND));

        if ("APPROVE".equalsIgnoreCase(request.getAction())) {
            partner.setVerificationStatus("APPROVED");
            partner.setRejectionReason(null);
        } else if ("REJECT".equalsIgnoreCase(request.getAction())) {
            partner.setVerificationStatus("REJECTED");
            partner.setRejectionReason(request.getRejectionReason());
        } else {
            throw new ApiException("Invalid action. Use APPROVE or REJECT", HttpStatus.BAD_REQUEST);
        }

        partner.setVerifiedAt(LocalDateTime.now());
        partner.setVerifiedBy(adminEmail);

        DeliveryPartner updated = deliveryPartnerRepository.save(partner);
        log.info("Partner {} verification: {} by admin {}", partnerId, request.getAction(), adminEmail);

        return mapToResponse(updated);
    }

    // ================= ADMIN: CREATE (Legacy) =================
    @Transactional
    public DeliveryPartnerResponse create(DeliveryPartnerRequest request) {
        log.info("Creating delivery partner: {}", request.getName());

        if (deliveryPartnerRepository.existsByPhone(request.getPhone())) {
            throw new ApiException("Phone number already registered", HttpStatus.BAD_REQUEST);
        }

        if (request.getEmail() != null && deliveryPartnerRepository.existsByEmail(request.getEmail())) {
            throw new ApiException("Email already registered", HttpStatus.BAD_REQUEST);
        }

        DeliveryPartner partner = DeliveryPartner.builder()
                .name(request.getName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .vehicleNumber(request.getVehicleNumber())
                .vehicleType(request.getVehicleType())
                .status("ACTIVE")
                .verificationStatus("APPROVED") // Admin-created partners are pre-approved
                .build();

        DeliveryPartner saved = deliveryPartnerRepository.save(partner);
        log.info("Delivery partner created: id={}", saved.getId());

        return mapToResponse(saved);
    }

    // ================= UPDATE =================
    @Transactional
    public DeliveryPartnerResponse update(Long id, DeliveryPartnerRequest request) {
        log.info("Updating delivery partner: id={}", id);

        DeliveryPartner partner = deliveryPartnerRepository.findById(id)
                .orElseThrow(() -> new ApiException("Delivery partner not found", HttpStatus.NOT_FOUND));

        partner.setName(request.getName());
        partner.setPhone(request.getPhone());
        partner.setEmail(request.getEmail());
        partner.setVehicleNumber(request.getVehicleNumber());
        partner.setVehicleType(request.getVehicleType());

        if (request.getStatus() != null) {
            partner.setStatus(request.getStatus());
        }

        DeliveryPartner updated = deliveryPartnerRepository.save(partner);
        log.info("Delivery partner updated: id={}", updated.getId());

        return mapToResponse(updated);
    }

    // ================= DELETE =================
    @Transactional
    public void delete(Long id) {
        log.info("Deleting delivery partner: id={}", id);

        if (!deliveryPartnerRepository.existsById(id)) {
            throw new ApiException("Delivery partner not found", HttpStatus.NOT_FOUND);
        }

        deliveryPartnerRepository.deleteById(id);
        log.info("Delivery partner deleted: id={}", id);
    }

    // ================= GET ALL =================
    public List<DeliveryPartnerResponse> getAll() {
        log.debug("Fetching all delivery partners");
        return deliveryPartnerRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ================= GET ACTIVE & VERIFIED =================
    public List<DeliveryPartnerResponse> getActive() {
        log.debug("Fetching active delivery partners");
        return deliveryPartnerRepository.findByStatusAndVerificationStatus("ACTIVE", "APPROVED").stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ================= GET BY ID =================
    public DeliveryPartnerResponse getById(Long id) {
        DeliveryPartner partner = deliveryPartnerRepository.findById(id)
                .orElseThrow(() -> new ApiException("Delivery partner not found", HttpStatus.NOT_FOUND));
        return mapToResponse(partner);
    }

    // ================= MAPPER =================
    private DeliveryPartnerResponse mapToResponse(DeliveryPartner partner) {
        return DeliveryPartnerResponse.builder()
                .id(partner.getId())
                .userId(partner.getUser() != null ? partner.getUser().getId() : null)
                .name(partner.getName())
                .phone(partner.getPhone())
                .email(partner.getEmail())
                .vehicleNumber(partner.getVehicleNumber())
                .vehicleType(partner.getVehicleType())
                .status(partner.getStatus())
                .verificationStatus(partner.getVerificationStatus())
                .rejectionReason(partner.getRejectionReason())
                .verifiedAt(partner.getVerifiedAt())
                .identityDocType(partner.getIdentityDocType())
                .identityDocNumber(partner.getIdentityDocNumber())
                .hasIdentityDoc(partner.getIdentityDocUrl() != null)
                .hasDrivingLicense(partner.getDrivingLicenseUrl() != null)
                .currentLatitude(partner.getCurrentLatitude())
                .currentLongitude(partner.getCurrentLongitude())
                .isOnline(partner.getIsOnline())
                .lastLocationUpdate(partner.getLastLocationUpdate())
                .currentOrders(partner.getCurrentOrders())
                .totalDeliveries(partner.getTotalDeliveries())
                .averageRating(partner.getAverageRating())
                .createdAt(partner.getCreatedAt())
                .build();
    }
}
