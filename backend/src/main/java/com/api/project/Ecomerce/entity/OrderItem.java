package com.api.project.Ecomerce.entity;

import com.api.project.Ecomerce.entity.enums.OrderStatus;
import com.api.project.Ecomerce.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ================= RELATION =================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // ================= PRODUCT SNAPSHOT =================
    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "price_at_order",
            nullable = false,
            precision = 10,
            scale = 2)
    private BigDecimal priceAtOrder;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false,
            precision = 10,
            scale = 2)
    private BigDecimal subtotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @Column(name = "payment_reference")
    private String paymentReference;
}