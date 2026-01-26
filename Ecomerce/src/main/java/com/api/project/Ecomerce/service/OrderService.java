package com.api.project.Ecomerce.service;
import com.api.project.Ecomerce.dto.*;
import com.api.project.Ecomerce.entity.*;
import com.api.project.Ecomerce.entity.enums.OrderStatus;
import com.api.project.Ecomerce.entity.enums.PaymentStatus;
import com.api.project.Ecomerce.exception.ApiException;
import com.api.project.Ecomerce.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
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

        log.debug("OrderService - Resolving current user from SecurityContext: {}", email);

        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("OrderService - User not found: {}", email);
                    return new ApiException("User not found",
                            HttpStatus.NOT_FOUND);
                });
    }

    // ================= PLACE ORDER =================
    @Transactional
    public OrderResponse placeOrder() {

        User user = getCurrentUser();
        log.info("OrderService - placeOrder called: userId={}", user.getId());

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> {
                    log.warn("OrderService - Cart not found for userId={}", user.getId());
                    return new ApiException("Cart not found",
                            HttpStatus.BAD_REQUEST);
                });

        List<CartItem> cartItems = cartItemRepository.findByCart(cart);

        if (cartItems.isEmpty()) {
            log.warn("OrderService - Cart is empty for userId={}", user.getId());
            throw new ApiException("Cart is empty",
                    HttpStatus.BAD_REQUEST);
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {

            Product product = productRepository.findById(
                            cartItem.getProduct().getId())
                    .orElseThrow(() -> {
                        log.warn("OrderService - Product not found while placing order: productId={}", cartItem.getProduct().getId());
                        return new ApiException("Product not found",
                                HttpStatus.NOT_FOUND);
                    });

            if (cartItem.getQuantity() > product.getStock()) {
                log.warn("OrderService - Insufficient stock for productId={} requested={} stock={}", product.getId(), cartItem.getQuantity(), product.getStock());
                throw new ApiException(
                        "Insufficient stock for " + product.getName(),
                        HttpStatus.BAD_REQUEST);
            }

            // Deduct stock immediately (BLOCK quantity)
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);
            log.debug("OrderService - Deducted stock for productId={} newStock={}", product.getId(), product.getStock());

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
                .shippingAddress(user.getAddress())
                .items(new ArrayList<>())
                .build();

        Order savedOrder = orderRepository.save(order);
        log.info("OrderService - Order created: orderId={}, userId={}, itemsCount={}, total={}", savedOrder.getId(), user.getId(), orderItems.size(), totalAmount);

        for (OrderItem item : orderItems) {
            item.setOrder(savedOrder);
            item.setPaymentReference(savedOrder.getPaymentReference());
            item.setPaymentStatus(savedOrder.getPaymentStatus());
            item.setStatus(savedOrder.getStatus());
        }

        orderItemRepository.saveAll(orderItems);
        log.debug("OrderService - Order items saved for orderId={}", savedOrder.getId());

        // Clear cart
        cartItemRepository.deleteAll(cartItems);
        log.info("OrderService - Cleared cart after order placement: userId={}, removedItems={}", user.getId(), cartItems.size());

        return mapToResponse(savedOrder, orderItems);
    }

    // ================= GET MY ORDERS =================
    public List<OrderResponse> getMyOrders() {

        User user = getCurrentUser();
        log.info("OrderService - getMyOrders called: userId={}", user.getId());

        List<Order> orders = orderRepository.findByUser(user);

        List<OrderResponse> responses = new ArrayList<>();

        for (Order order : orders) {
            responses.add(mapToResponse(order, order.getItems()));
        }

        log.debug("OrderService - getMyOrders returned count={} for userId={}", responses.size(), user.getId());
        return responses;
    }

    // ================= GET ORDER DETAILS =================
    public OrderResponse getOrderDetails(Long orderId) {

        User user = getCurrentUser();
        log.info("OrderService - getOrderDetails called: orderId={}, userId={}", orderId, user.getId());

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("OrderService - Order not found: orderId={}", orderId);
                    return new ApiException("Order not found",
                            HttpStatus.NOT_FOUND);
                });

        if (!order.getUser().getId().equals(user.getId())) {
            log.warn("OrderService - Access denied for userId={} on orderId={}", user.getId(), orderId);
            throw new ApiException("Access denied",
                    HttpStatus.FORBIDDEN);
        }

        OrderResponse resp = mapToResponse(order, order.getItems());
        log.debug("OrderService - getOrderDetails returning for orderId={} itemsCount={}", orderId, resp.getItems() == null ? 0 : resp.getItems().size());
        return resp;
    }

    // ================= ADMIN GET ALL =================
    public List<OrderResponse> getAllOrders() {
        log.info("OrderService - getAllOrders called");

        List<Order> orders = orderRepository.findAll();

        List<OrderResponse> responses = new ArrayList<>();

        for (Order order : orders) {
            responses.add(mapToResponse(order, order.getItems()));
        }

        log.debug("OrderService - getAllOrders returned count={}", responses.size());
        return responses;
    }

    // ================= ADMIN UPDATE STATUS =================
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId,
                                           UpdateOrderStatusRequest request) {

        log.info("OrderService - updateOrderStatus called: orderId={}, requestedStatus={}", orderId, request == null ? "null" : request.getStatus());

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("OrderService - Order not found for update: orderId={}", orderId);
                    return new ApiException("Order not found",
                            HttpStatus.NOT_FOUND);
                });

        OrderStatus current = order.getStatus();
        OrderStatus target;

        try {
            target = OrderStatus.valueOf(request.getStatus());
        } catch (Exception e) {
            log.warn("OrderService - Invalid order status provided for orderId={}: {}", orderId, request.getStatus());
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
            log.warn("OrderService - Invalid status transition for orderId={}: {} -> {}", orderId, current, target);
            throw new ApiException(
                    "Invalid status transition from " + current + " to " + target,
                    HttpStatus.BAD_REQUEST);
        }

        order.setStatus(target);

        Order updated = orderRepository.save(order);
        log.info("OrderService - Order status updated: orderId={}, from={} to={}", updated.getId(), current, target);

        return mapToResponse(updated, updated.getItems());
    }

    @Transactional
    public OrderResponse payOrder(Long orderId, boolean forceSuccess) {

        User user = getCurrentUser();
        log.info("OrderService - payOrder called: orderId={}, userId={}, forceSuccess={}", orderId, user.getId(), forceSuccess);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("OrderService - Order not found for payment: orderId={}", orderId);
                    return new ApiException("Order not found",
                            HttpStatus.NOT_FOUND);
                });

        if (!order.getUser().getId().equals(user.getId())) {
            log.warn("OrderService - Access denied for payment: userId={} orderId={}", user.getId(), orderId);
            throw new ApiException("Access denied",
                    HttpStatus.FORBIDDEN);
        }

        // Only allow payment if order is pending
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            log.warn("OrderService - Order not in payment phase: orderId={} status={}", orderId, order.getStatus());
            throw new ApiException("Order is not in payment phase",
                    HttpStatus.BAD_REQUEST);
        }

        if (order.getPaymentStatus() != PaymentStatus.PENDING) {
            log.warn("OrderService - Payment already processed for orderId={} paymentStatus={}", orderId, order.getPaymentStatus());
            throw new ApiException("Payment already processed",
                    HttpStatus.BAD_REQUEST);
        }

        if (forceSuccess) {

            order.setPaymentStatus(PaymentStatus.SUCCESS);
            order.setStatus(OrderStatus.PLACED);

            Order updated = orderRepository.save(order);

            for (OrderItem item : order.getItems()) {
                item.setPaymentStatus(PaymentStatus.SUCCESS);
                item.setStatus(OrderStatus.PLACED);
                orderItemRepository.save(item);
            }

            log.info("OrderService - Payment success for orderId={}", orderId);
            return mapToResponse(updated, updated.getItems());

        } else {

            // Payment failed → restore stock
            int restored = 0;
            for (OrderItem item : order.getItems()) {

                Product product = productRepository.findById(
                                item.getProductId())
                        .orElseThrow(() -> {
                            log.warn("OrderService - Product not found while restoring stock: productId={}", item.getProductId());
                            return new ApiException("Product not found",
                                    HttpStatus.NOT_FOUND);
                        });

                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);
                restored += item.getQuantity();
            }

            order.setPaymentStatus(PaymentStatus.FAILED);
            order.setStatus(OrderStatus.PAYMENT_FAILED);

            Order updated = orderRepository.save(order);
            log.info("OrderService - Payment failed for orderId={}, restoredStockCount={}", orderId, restored);

            return mapToResponse(updated, updated.getItems());
        }
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId) {

        User user = getCurrentUser();
        log.info("OrderService - cancelOrder called: orderId={}, userId={}", orderId, user.getId());

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("OrderService - Order not found for cancel: orderId={}", orderId);
                    return new ApiException("Order not found",
                            HttpStatus.NOT_FOUND);
                });

        if (!order.getUser().getId().equals(user.getId())) {
            log.warn("OrderService - Access denied for cancel: userId={} orderId={}", user.getId(), orderId);
            throw new ApiException("Access denied",
                    HttpStatus.FORBIDDEN);
        }

        if (order.getStatus() == OrderStatus.SHIPPED ||
                order.getStatus() == OrderStatus.DELIVERED ||
                order.getStatus() == OrderStatus.CANCELLED) {

            log.warn("OrderService - Order cannot be cancelled due to status: orderId={}, status={}", orderId, order.getStatus());
            throw new ApiException("Order cannot be cancelled",
                    HttpStatus.BAD_REQUEST);
        }

        // Restore stock
        int restored = 0;
        for (OrderItem item : order.getItems()) {

            Product product = productRepository.findById(
                            item.getProductId())
                    .orElseThrow(() -> {
                        log.warn("OrderService - Product not found while restoring stock on cancel: productId={}", item.getProductId());
                        return new ApiException("Product not found",
                                HttpStatus.NOT_FOUND);
                    });

            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
            restored += item.getQuantity();
        }

        // Update status
        order.setStatus(OrderStatus.CANCELLED);

        if (order.getPaymentStatus() == PaymentStatus.SUCCESS) {
            order.setPaymentStatus(PaymentStatus.REFUNDED);
        } else {
            order.setPaymentStatus(PaymentStatus.FAILED);
        }

        Order updated = orderRepository.save(order);
        log.info("OrderService - Order cancelled: orderId={}, restoredStockCount={}, paymentStatus={}", updated.getId(), restored, updated.getPaymentStatus());

        return mapToResponse(updated, updated.getItems());
    }

    // ================= MAPPER =================
    private OrderResponse mapToResponse(Order order,
                                        List<OrderItem> items) {

        log.debug("OrderService - Mapping order to response: orderId={}, itemsCount={}", order.getId(), items == null ? 0 : items.size());

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