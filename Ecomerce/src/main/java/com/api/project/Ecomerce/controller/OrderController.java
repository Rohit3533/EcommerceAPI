package com.api.project.Ecomerce.controller;
import com.api.project.Ecomerce.dto.*;
import com.api.project.Ecomerce.response.ApiResponse;
import com.api.project.Ecomerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    // ================= CUSTOMER PLACE ORDER =================
    @PostMapping("/api/orders/place")
    public ApiResponse<OrderResponse> placeOrder() {
        log.info("OrderController - placeOrder called");

        OrderResponse response = orderService.placeOrder();

        log.info("OrderController - placeOrder successful: orderId={}", response == null ? "null" : response.getOrderId());

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
        log.info("OrderController - getMyOrders called");

        List<OrderResponse> responses = orderService.getMyOrders();

        log.info("OrderController - getMyOrders successful: count={}", responses == null ? 0 : responses.size());

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

        log.info("OrderController - getOrderDetails called: orderId={}", orderId);

        OrderResponse response =
                orderService.getOrderDetails(orderId);

        log.info("OrderController - getOrderDetails successful: orderId={}", response == null ? orderId : response.getOrderId());

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
        log.info("OrderController - getAllOrders called");

        List<OrderResponse> responses = orderService.getAllOrders();

        log.info("OrderController - getAllOrders successful: count={}", responses == null ? 0 : responses.size());

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

        log.info("OrderController - updateOrderStatus called: orderId={}, requestType={}", orderId, request == null ? "null" : request.getClass().getSimpleName());

        OrderResponse response =
                orderService.updateOrderStatus(orderId, request);

        log.info("OrderController - updateOrderStatus successful: orderId={}, newStatus={}", orderId, response == null ? "null" : response.getStatus());

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

        log.info("OrderController - payOrder called: orderId={}, requestType={}, forceSuccess={}", orderId,
                request == null ? "null" : request.getClass().getSimpleName(),
                request == null ? "false" : request.isForceSuccess());

        OrderResponse response =
                orderService.payOrder(orderId, request.isForceSuccess());

        log.info("OrderController - payOrder processed: orderId={}, status={}", orderId, response == null ? "null" : response.getStatus());

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

        log.info("OrderController - cancelOrder called: orderId={}", orderId);

        OrderResponse response =
                orderService.cancelOrder(orderId);

        log.info("OrderController - cancelOrder successful: orderId={}, status={}", orderId, response == null ? "null" : response.getStatus());

        return ApiResponse.<OrderResponse>builder()
                .success(true)
                .message("Order cancelled successfully")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
