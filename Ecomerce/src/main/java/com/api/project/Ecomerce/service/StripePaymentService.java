package com.api.project.Ecomerce.service;

import com.api.project.Ecomerce.dto.PaymentIntentResponse;
import com.api.project.Ecomerce.entity.Order;
import com.api.project.Ecomerce.entity.User;
import com.api.project.Ecomerce.entity.enums.OrderStatus;
import com.api.project.Ecomerce.entity.enums.PaymentStatus;
import com.api.project.Ecomerce.exception.ApiException;
import com.api.project.Ecomerce.repository.OrderRepository;
import com.api.project.Ecomerce.repository.UserRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripePaymentService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
    }

    /**
     * Creates a Stripe PaymentIntent for the given order.
     * This returns a client secret that the frontend uses to complete payment.
     */
    @Transactional
    public PaymentIntentResponse createPaymentIntent(Long orderId) {

        User user = getCurrentUser();
        log.info("StripePaymentService - createPaymentIntent called: orderId={}, userId={}", orderId, user.getId());

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("StripePaymentService - Order not found: orderId={}", orderId);
                    return new ApiException("Order not found", HttpStatus.NOT_FOUND);
                });

        // Verify ownership
        if (!order.getUser().getId().equals(user.getId())) {
            log.warn("StripePaymentService - Access denied: userId={} orderId={}", user.getId(), orderId);
            throw new ApiException("Access denied", HttpStatus.FORBIDDEN);
        }

        // Verify order status
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            log.warn("StripePaymentService - Invalid order status: orderId={} status={}", orderId, order.getStatus());
            throw new ApiException("Order is not in payment phase", HttpStatus.BAD_REQUEST);
        }

        // Convert to cents (Stripe uses smallest currency unit)
        long amountInCents = order.getTotalAmount().multiply(java.math.BigDecimal.valueOf(100)).longValue();

        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency("usd")
                    .setDescription("Order #" + orderId + " - Ecommerce Demo")
                    .putMetadata("orderId", String.valueOf(orderId))
                    .putMetadata("userId", String.valueOf(user.getId()))
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build())
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            // Store payment intent ID in order
            order.setPaymentReference(paymentIntent.getId());
            orderRepository.save(order);

            log.info("StripePaymentService - PaymentIntent created: orderId={}, paymentIntentId={}",
                    orderId, paymentIntent.getId());

            return PaymentIntentResponse.builder()
                    .clientSecret(paymentIntent.getClientSecret())
                    .paymentIntentId(paymentIntent.getId())
                    .orderId(orderId)
                    .amount(amountInCents)
                    .currency("usd")
                    .build();

        } catch (StripeException e) {
            log.error("StripePaymentService - Stripe error creating payment intent: orderId={} error={}",
                    orderId, e.getMessage());
            throw new ApiException("Payment processing error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Confirms payment was successful (called after frontend completes payment).
     * This verifies the payment with Stripe and updates order status.
     */
    @Transactional
    public void confirmPayment(Long orderId, String paymentIntentId) {

        User user = getCurrentUser();
        log.info("StripePaymentService - confirmPayment called: orderId={}, paymentIntentId={}", orderId,
                paymentIntentId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException("Order not found", HttpStatus.NOT_FOUND));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new ApiException("Access denied", HttpStatus.FORBIDDEN);
        }

        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            if ("succeeded".equals(paymentIntent.getStatus())) {
                order.setPaymentStatus(PaymentStatus.SUCCESS);
                order.setStatus(OrderStatus.PLACED);
                orderRepository.save(order);

                log.info("StripePaymentService - Payment confirmed: orderId={}", orderId);
            } else {
                log.warn("StripePaymentService - Payment not succeeded: orderId={} status={}",
                        orderId, paymentIntent.getStatus());
                throw new ApiException("Payment not completed", HttpStatus.BAD_REQUEST);
            }
        } catch (StripeException e) {
            log.error("StripePaymentService - Error confirming payment: orderId={} error={}", orderId, e.getMessage());
            throw new ApiException("Could not verify payment: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
