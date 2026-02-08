package com.api.project.Ecomerce.controller;

import com.api.project.Ecomerce.entity.DeliveryPartner;
import com.api.project.Ecomerce.entity.Order;
import com.api.project.Ecomerce.entity.enums.OrderStatus;
import com.api.project.Ecomerce.exception.ApiException;
import com.api.project.Ecomerce.repository.DeliveryPartnerRepository;
import com.api.project.Ecomerce.repository.OrderRepository;
import com.api.project.Ecomerce.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/delivery/orders")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('DELIVERY_PARTNER')")
public class DeliveryPartnerOrderController {

    private final OrderRepository orderRepository;
    private final DeliveryPartnerRepository deliveryPartnerRepository;

    // ================= GET MY ASSIGNED ORDERS =================
    @GetMapping("/my")
    public ApiResponse<List<Map<String, Object>>> getMyOrders() {
        DeliveryPartner partner = getCurrentPartner();
        log.info("Fetching orders for partner: {}", partner.getId());

        List<Order> orders = orderRepository.findByDeliveryPartner(partner);

        List<Map<String, Object>> orderData = orders.stream()
                .map(this::mapOrderToSimple)
                .collect(Collectors.toList());

        return ApiResponse.<List<Map<String, Object>>>builder()
                .success(true)
                .message("Orders fetched successfully")
                .data(orderData)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ================= GET AVAILABLE ORDERS (Near location) =================
    @GetMapping("/available")
    public ApiResponse<List<Map<String, Object>>> getAvailableOrders() {
        DeliveryPartner partner = getCurrentPartner();
        log.info("Fetching available orders for partner: {}", partner.getId());

        // Show all unassigned orders that are ready for delivery
        List<Order> orders = orderRepository.findByDeliveryPartnerIsNullAndStatusIn(
                List.of(OrderStatus.CONFIRMED, OrderStatus.PROCESSING, OrderStatus.SHIPPED));

        List<Map<String, Object>> orderData = orders.stream()
                .map(this::mapOrderToSimple)
                .collect(Collectors.toList());

        return ApiResponse.<List<Map<String, Object>>>builder()
                .success(true)
                .message("Available orders fetched")
                .data(orderData)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ================= ACCEPT ORDER =================
    @PutMapping("/{orderId}/accept")
    public ApiResponse<Map<String, Object>> acceptOrder(@PathVariable Long orderId) {
        DeliveryPartner partner = getCurrentPartner();
        log.info("Partner {} accepting order {}", partner.getId(), orderId);

        if (!"APPROVED".equals(partner.getVerificationStatus())) {
            throw new ApiException("You must be verified to accept orders", HttpStatus.FORBIDDEN);
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException("Order not found", HttpStatus.NOT_FOUND));

        if (order.getDeliveryPartner() != null) {
            throw new ApiException("Order already assigned", HttpStatus.BAD_REQUEST);
        }

        order.setDeliveryPartner(partner);
        order.setAssignedAt(LocalDateTime.now());
        order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
        orderRepository.save(order);

        partner.setCurrentOrders(partner.getCurrentOrders() + 1);
        deliveryPartnerRepository.save(partner);

        return ApiResponse.<Map<String, Object>>builder()
                .success(true)
                .message("Order accepted successfully")
                .data(mapOrderToSimple(order))
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ================= REJECT/RELEASE ORDER =================
    @PutMapping("/{orderId}/reject")
    public ApiResponse<Void> rejectOrder(@PathVariable Long orderId) {
        DeliveryPartner partner = getCurrentPartner();
        log.info("Partner {} rejecting order {}", partner.getId(), orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException("Order not found", HttpStatus.NOT_FOUND));

        if (order.getDeliveryPartner() == null || !partner.getId().equals(order.getDeliveryPartner().getId())) {
            throw new ApiException("This order is not assigned to you", HttpStatus.BAD_REQUEST);
        }

        order.setDeliveryPartner(null);
        order.setAssignedAt(null);
        order.setStatus(OrderStatus.SHIPPED);
        orderRepository.save(order);

        partner.setCurrentOrders(Math.max(0, partner.getCurrentOrders() - 1));
        deliveryPartnerRepository.save(partner);

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Order released successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ================= MARK PICKED UP =================
    @PutMapping("/{orderId}/pickup")
    public ApiResponse<Map<String, Object>> markPickedUp(@PathVariable Long orderId) {
        DeliveryPartner partner = getCurrentPartner();
        Order order = getMyOrder(orderId, partner);

        order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
        order.setPickedUpAt(LocalDateTime.now());
        orderRepository.save(order);

        log.info("Order {} marked as picked up by partner {}", orderId, partner.getId());

        return ApiResponse.<Map<String, Object>>builder()
                .success(true)
                .message("Order marked as picked up")
                .data(mapOrderToSimple(order))
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ================= MARK DELIVERED =================
    @PutMapping("/{orderId}/deliver")
    public ApiResponse<Map<String, Object>> markDelivered(@PathVariable Long orderId) {
        DeliveryPartner partner = getCurrentPartner();
        Order order = getMyOrder(orderId, partner);

        order.setStatus(OrderStatus.DELIVERED);
        order.setDeliveredAt(LocalDateTime.now());
        orderRepository.save(order);

        partner.setCurrentOrders(Math.max(0, partner.getCurrentOrders() - 1));
        partner.setTotalDeliveries(partner.getTotalDeliveries() + 1);
        deliveryPartnerRepository.save(partner);

        log.info("Order {} marked as delivered by partner {}", orderId, partner.getId());

        return ApiResponse.<Map<String, Object>>builder()
                .success(true)
                .message("Order delivered successfully! 🎉")
                .data(mapOrderToSimple(order))
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ================= HELPER METHODS =================
    private DeliveryPartner getCurrentPartner() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return deliveryPartnerRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("Delivery partner not found", HttpStatus.NOT_FOUND));
    }

    private Order getMyOrder(Long orderId, DeliveryPartner partner) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException("Order not found", HttpStatus.NOT_FOUND));

        if (order.getDeliveryPartner() == null || !partner.getId().equals(order.getDeliveryPartner().getId())) {
            throw new ApiException("This order is not assigned to you", HttpStatus.FORBIDDEN);
        }

        return order;
    }

    private Map<String, Object> mapOrderToSimple(Order order) {
        Map<String, Object> map = new HashMap<>();
        map.put("orderId", order.getId());
        map.put("orderNumber", order.getOrderNumber());
        map.put("status", order.getStatus().name());
        map.put("totalAmount", order.getTotalAmount());
        map.put("customerName", order.getUser() != null ? order.getUser().getName() : "N/A");
        map.put("customerPhone", order.getUser() != null ? order.getUser().getPhone() : "N/A");
        map.put("shippingAddress", order.getShippingAddress());
        map.put("createdAt", order.getCreatedAt());
        map.put("pickedUpAt", order.getPickedUpAt());
        map.put("deliveredAt", order.getDeliveredAt());
        return map;
    }
}
