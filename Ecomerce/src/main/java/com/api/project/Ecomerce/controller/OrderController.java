package com.api.project.Ecomerce.controller;
import com.api.project.Ecomerce.dto.*;
import com.api.project.Ecomerce.response.ApiResponse;
import com.api.project.Ecomerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ================= CUSTOMER PLACE ORDER =================
    @PostMapping("/api/orders/place")
    public ApiResponse<OrderResponse> placeOrder() {

        OrderResponse response = orderService.placeOrder();

        return ApiResponse.<OrderResponse>builder()
                .success(true)
                .message("Order placed successfully")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ================= CUSTOMER GET MY ORDERS =================
    @GetMapping("/api/orders/my")
    public ApiResponse<List<OrderResponse>> getMyOrders() {

        List<OrderResponse> responses = orderService.getMyOrders();

        return ApiResponse.<List<OrderResponse>>builder()
                .success(true)
                .message("Orders fetched successfully")
                .data(responses)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ================= CUSTOMER GET ORDER DETAILS =================
    @GetMapping("/api/orders/{orderId}")
    public ApiResponse<OrderResponse> getOrderDetails(
            @PathVariable Long orderId) {

        OrderResponse response =
                orderService.getOrderDetails(orderId);

        return ApiResponse.<OrderResponse>builder()
                .success(true)
                .message("Order details fetched successfully")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ================= ADMIN GET ALL ORDERS =================
    @GetMapping("/api/admin/orders")
    public ApiResponse<List<OrderResponse>> getAllOrders() {

        List<OrderResponse> responses = orderService.getAllOrders();

        return ApiResponse.<List<OrderResponse>>builder()
                .success(true)
                .message("All orders fetched successfully")
                .data(responses)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ================= ADMIN UPDATE STATUS =================
    @PutMapping("/api/admin/orders/{orderId}/status")
    public ApiResponse<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody UpdateOrderStatusRequest request) {

        OrderResponse response =
                orderService.updateOrderStatus(orderId, request);

        return ApiResponse.<OrderResponse>builder()
                .success(true)
                .message("Order status updated successfully")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @PostMapping("/api/orders/{orderId}/pay")
    public ApiResponse<OrderResponse> payOrder(
            @PathVariable Long orderId,
            @RequestBody PaymentRequest request) {

        OrderResponse response =
                orderService.payOrder(orderId, request.isForceSuccess());

        return ApiResponse.<OrderResponse>builder()
                .success(true)
                .message("Payment processed")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @PostMapping("/api/orders/{orderId}/cancel")
    public ApiResponse<OrderResponse> cancelOrder(
            @PathVariable Long orderId) {

        OrderResponse response =
                orderService.cancelOrder(orderId);

        return ApiResponse.<OrderResponse>builder()
                .success(true)
                .message("Order cancelled successfully")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build();
    }
}