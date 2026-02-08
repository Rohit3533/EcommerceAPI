package com.api.project.Ecomerce.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_partners")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryPartner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link to User (for authentication)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String phone;

    @Column(unique = true)
    private String email;

    // ================= VEHICLE INFO =================
    @Column(name = "vehicle_number")
    private String vehicleNumber;

    @Column(name = "vehicle_type")
    private String vehicleType; // BIKE, VAN, TRUCK

    // ================= IDENTITY DOCUMENTS =================
    @Column(name = "identity_doc_type")
    private String identityDocType; // AADHAAR or PAN

    @Column(name = "identity_doc_number")
    private String identityDocNumber;

    @Column(name = "identity_doc_url")
    private String identityDocUrl;

    // ================= DRIVING LICENSE (COMPULSORY) =================
    @Column(name = "driving_license_number")
    private String drivingLicenseNumber;

    @Column(name = "driving_license_url")
    private String drivingLicenseUrl;

    // ================= VERIFICATION STATUS =================
    @Column(name = "verification_status", nullable = false)
    @Builder.Default
    private String verificationStatus = "PENDING"; // PENDING, APPROVED, REJECTED

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "verified_by")
    private String verifiedBy; // Admin email who verified

    // ================= GPS LOCATION =================
    @Column(name = "current_latitude")
    private Double currentLatitude;

    @Column(name = "current_longitude")
    private Double currentLongitude;

    @Column(name = "last_location_update")
    private LocalDateTime lastLocationUpdate;

    @Column(name = "is_online")
    @Builder.Default
    private Boolean isOnline = false;

    // ================= STATUS & STATS =================
    @Column(nullable = false)
    @Builder.Default
    private String status = "ACTIVE"; // ACTIVE, INACTIVE

    @Column(name = "current_orders")
    @Builder.Default
    private Integer currentOrders = 0;

    @Column(name = "total_deliveries")
    @Builder.Default
    private Integer totalDeliveries = 0;

    @Column(name = "average_rating")
    @Builder.Default
    private Double averageRating = 0.0;

    // ================= TIMESTAMPS =================
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Helper method to check if verified
    public boolean isVerified() {
        return "APPROVED".equals(this.verificationStatus);
    }
}
