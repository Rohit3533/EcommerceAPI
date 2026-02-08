package com.api.project.Ecomerce.entity;

import com.api.project.Ecomerce.entity.enums.OrderStatus;
import com.api.project.Ecomerce.entity.enums.PaymentMethod;
import com.api.project.Ecomerce.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ================= RELATION =================
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status; // PLACED, CANCELLED, SHIPPED, DELIVERED

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @Column(name = "payment_reference")
    private String paymentReference;

    @Column(name = "shipping_address", nullable = false, length = 500)
    private String shippingAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    // ================= DELIVERY TRACKING =================
    @Column(name = "order_number", unique = true)
    private String orderNumber; // Format: ORD-YYYYMMDD-XXXX

    @Column(name = "tracking_id", unique = true)
    private String trackingId; // Format: DEL-XXXXXXXX

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_partner_id")
    private DeliveryPartner deliveryPartner;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "out_for_delivery_at")
    private LocalDateTime outForDeliveryAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "estimated_delivery")
    private LocalDateTime estimatedDelivery;

    @Column(name = "picked_up_at")
    private LocalDateTime pickedUpAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = OrderStatus.PENDING_PAYMENT;
        }
        if (this.paymentStatus == null) {
            this.paymentStatus = PaymentStatus.PENDING;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Helper methods for delivery partner
    public Long getDeliveryPartnerId() {
        return deliveryPartner != null ? deliveryPartner.getId() : null;
    }

    public String getDeliveryPartnerName() {
        return deliveryPartner != null ? deliveryPartner.getName() : null;
    }

    public void setDeliveryPartnerId(Long partnerId) {
        // This is handled via the deliveryPartner relationship
    }

    public void setDeliveryPartnerName(String name) {
        // This is handled via the deliveryPartner relationship
    }

    // Convenience method to get order ID
    public Long getOrderId() {
        return this.id;
    }
}