package com.api.project.Ecomerce.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(nullable = false)
    private String status; // ACTIVE / INACTIVE

    @Column(name = "allowed_payment_methods", nullable = false)
    @Builder.Default
    private String allowedPaymentMethods = "CARD"; // Comma-separated: COD,UPI,CARD

    // ================= RELATION =================
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ================= AUTO FIELDS =================
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        updateStatusBasedOnStock();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        updateStatusBasedOnStock();
    }

    private void updateStatusBasedOnStock() {
        if (this.stock != null && this.stock > 0) {
            this.status = "ACTIVE";
        } else {
            this.status = "INACTIVE";
        }
    }
}