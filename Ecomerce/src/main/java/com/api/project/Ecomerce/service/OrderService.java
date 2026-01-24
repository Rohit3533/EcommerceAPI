package com.api.project.Ecomerce.service;
import com.api.project.Ecomerce.dto.*;
import com.api.project.Ecomerce.entity.*;
import com.api.project.Ecomerce.entity.enums.OrderStatus;
import com.api.project.Ecomerce.entity.enums.PaymentStatus;
import com.api.project.Ecomerce.exception.ApiException;
import com.api.project.Ecomerce.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    // ================= GET CURRENT USER =================
    private User getCurrentUser() {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ApiException("User not found",
                                HttpStatus.NOT_FOUND));
    }

    // ================= PLACE ORDER =================
    @Transactional
    public OrderResponse placeOrder() {

        User user = getCurrentUser();

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() ->
                        new ApiException("Cart not found",
                                HttpStatus.BAD_REQUEST));

        List<CartItem> cartItems = cartItemRepository.findByCart(cart);

        if (cartItems.isEmpty()) {
            throw new ApiException("Cart is empty",
                    HttpStatus.BAD_REQUEST);
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {

            Product product = productRepository.findById(
                            cartItem.getProduct().getId())
                    .orElseThrow(() ->
                            new ApiException("Product not found",
                                    HttpStatus.NOT_FOUND));

            if (cartItem.getQuantity() > product.getStock()) {
                throw new ApiException(
                        "Insufficient stock for " + product.getName(),
                        HttpStatus.BAD_REQUEST);
            }

            // Deduct stock immediately (BLOCK quantity)
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);

            BigDecimal subtotal = cartItem.getPriceAtAddition()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));

            totalAmount = totalAmount.add(subtotal);

            OrderItem orderItem = OrderItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .priceAtOrder(cartItem.getPriceAtAddition())
                    .quantity(cartItem.getQuantity())
                    .subtotal(subtotal)
                    .build();

            orderItems.add(orderItem);
        }

        // Generate fake payment reference
        String paymentRef = "PAY-" + System.currentTimeMillis();

        Order order = Order.builder()
                .user(user)
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING_PAYMENT)
                .paymentStatus(PaymentStatus.PENDING)
                .paymentReference(paymentRef)
                .items(new ArrayList<>())
                .build();

        Order savedOrder = orderRepository.save(order);

        for (OrderItem item : orderItems) {
            item.setOrder(savedOrder);
            item.setPaymentReference(savedOrder.getPaymentReference());
            item.setPaymentStatus(savedOrder.getPaymentStatus());
            item.setStatus(savedOrder.getStatus());
        }

        orderItemRepository.saveAll(orderItems);

        // Clear cart
        cartItemRepository.deleteAll(cartItems);

        return mapToResponse(savedOrder, orderItems);
    }

    // ================= GET MY ORDERS =================
    public List<OrderResponse> getMyOrders() {

        User user = getCurrentUser();

        List<Order> orders = orderRepository.findByUser(user);

        List<OrderResponse> responses = new ArrayList<>();

        for (Order order : orders) {
            responses.add(mapToResponse(order, order.getItems()));
        }

        return responses;
    }

    // ================= GET ORDER DETAILS =================
    public OrderResponse getOrderDetails(Long orderId) {

        User user = getCurrentUser();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ApiException("Order not found",
                                HttpStatus.NOT_FOUND));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new ApiException("Access denied",
                    HttpStatus.FORBIDDEN);
        }

        return mapToResponse(order, order.getItems());
    }

    // ================= ADMIN GET ALL =================
    public List<OrderResponse> getAllOrders() {

        List<Order> orders = orderRepository.findAll();

        List<OrderResponse> responses = new ArrayList<>();

        for (Order order : orders) {
            responses.add(mapToResponse(order, order.getItems()));
        }

        return responses;
    }

    // ================= ADMIN UPDATE STATUS =================
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId,
                                           UpdateOrderStatusRequest request) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ApiException("Order not found",
                                HttpStatus.NOT_FOUND));

        OrderStatus current = order.getStatus();
        OrderStatus target;

        try {
            target = OrderStatus.valueOf(request.getStatus());
        } catch (Exception e) {
            throw new ApiException("Invalid order status",
                    HttpStatus.BAD_REQUEST);
        }

        // ================= TRANSITION VALIDATION =================

        boolean valid = false;

        switch (current) {

            case PLACED:
                if (target == OrderStatus.SHIPPED ||
                        target == OrderStatus.CANCELLED) {
                    valid = true;
                }
                break;

            case SHIPPED:
                if (target == OrderStatus.DELIVERED) {
                    valid = true;
                }
                break;

            default:
                valid = false;
        }

        if (!valid) {
            throw new ApiException(
                    "Invalid status transition from " + current + " to " + target,
                    HttpStatus.BAD_REQUEST);
        }

        order.setStatus(target);

        Order updated = orderRepository.save(order);

        return mapToResponse(updated, updated.getItems());
    }
    @Transactional
    public OrderResponse payOrder(Long orderId, boolean forceSuccess) {

        User user = getCurrentUser();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ApiException("Order not found",
                                HttpStatus.NOT_FOUND));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new ApiException("Access denied",
                    HttpStatus.FORBIDDEN);
        }

        // Only allow payment if order is pending
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new ApiException("Order is not in payment phase",
                    HttpStatus.BAD_REQUEST);
        }

        if (order.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new ApiException("Payment already processed",
                    HttpStatus.BAD_REQUEST);
        }

        if (forceSuccess) {

            order.setPaymentStatus(PaymentStatus.SUCCESS);
            order.setStatus(OrderStatus.PLACED);

            Order updated = orderRepository.save(order);

            for( OrderItem item : order.getItems()) {
                item.setPaymentStatus(PaymentStatus.SUCCESS);
                item.setStatus(OrderStatus.PLACED);
                orderItemRepository.save(item);
            }

            return mapToResponse(updated, updated.getItems());

        } else {

            // Payment failed → restore stock
            for (OrderItem item : order.getItems()) {

                Product product = productRepository.findById(
                                item.getProductId())
                        .orElseThrow(() ->
                                new ApiException("Product not found",
                                        HttpStatus.NOT_FOUND));

                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);
            }

            order.setPaymentStatus(PaymentStatus.FAILED);
            order.setStatus(OrderStatus.PAYMENT_FAILED);

            Order updated = orderRepository.save(order);

            return mapToResponse(updated, updated.getItems());
        }
    }
    @Transactional
    public OrderResponse cancelOrder(Long orderId) {

        User user = getCurrentUser();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ApiException("Order not found",
                                HttpStatus.NOT_FOUND));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new ApiException("Access denied",
                    HttpStatus.FORBIDDEN);
        }

        if (order.getStatus() == OrderStatus.SHIPPED ||
                order.getStatus() == OrderStatus.DELIVERED ||
                order.getStatus() == OrderStatus.CANCELLED) {

            throw new ApiException("Order cannot be cancelled",
                    HttpStatus.BAD_REQUEST);
        }

        // Restore stock
        for (OrderItem item : order.getItems()) {

            Product product = productRepository.findById(
                            item.getProductId())
                    .orElseThrow(() ->
                            new ApiException("Product not found",
                                    HttpStatus.NOT_FOUND));

            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }

        // Update status
        order.setStatus(OrderStatus.CANCELLED);

        if (order.getPaymentStatus() == PaymentStatus.SUCCESS) {
            order.setPaymentStatus(PaymentStatus.REFUNDED);
        } else {
            order.setPaymentStatus(PaymentStatus.FAILED);
        }

        Order updated = orderRepository.save(order);

        return mapToResponse(updated, updated.getItems());
    }

    // ================= MAPPER =================
    private OrderResponse mapToResponse(Order order,
                                        List<OrderItem> items) {

        List<OrderItemResponse> itemResponses = new ArrayList<>();

        for (OrderItem item : items) {

            itemResponses.add(
                    OrderItemResponse.builder()
                            .productId(item.getProductId())
                            .productName(item.getProductName())
                            .priceAtOrder(item.getPriceAtOrder())
                            .quantity(item.getQuantity())
                            .subtotal(item.getSubtotal())
                            .build()
            );
        }

        return OrderResponse.builder()
                .orderId(order.getId())
                .totalAmount(order.getTotalAmount())
                .status(String.valueOf(order.getStatus()))
                .createdAt(order.getCreatedAt())
                .items(itemResponses)
                .build();
    }
}