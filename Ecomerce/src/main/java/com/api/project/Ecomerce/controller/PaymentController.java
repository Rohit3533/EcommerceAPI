package com.api.project.Ecomerce.controller;

import com.api.project.Ecomerce.dto.PaymentIntentResponse;
import com.api.project.Ecomerce.response.ApiResponse;
import com.api.project.Ecomerce.service.StripePaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/payments")
public class PaymentController {

    private final StripePaymentService stripePaymentService;

    /**
     * Creates a Stripe PaymentIntent for the given order.
     * Returns the client secret needed by the frontend to complete payment.
     */
    @PostMapping("/create-intent/{orderId}")
    public ApiResponse<PaymentIntentResponse> createPaymentIntent(@PathVariable Long orderId) {

        log.info("PaymentController - createPaymentIntent called: orderId={}", orderId);

        PaymentIntentResponse response = stripePaymentService.createPaymentIntent(orderId);

        log.info("PaymentController - PaymentIntent created: orderId={}, paymentIntentId={}",
                orderId, response.getPaymentIntentId());

        return ApiResponse.<PaymentIntentResponse>builder()
                .success(true)
                .message("Payment intent created")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Confirms that payment was successful after frontend completes the payment
     * flow.
     */
    @PostMapping("/confirm/{orderId}")
    public ApiResponse<Void> confirmPayment(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> request) {

        String paymentIntentId = request.get("paymentIntentId");
        log.info("PaymentController - confirmPayment called: orderId={}, paymentIntentId={}", orderId, paymentIntentId);

        stripePaymentService.confirmPayment(orderId, paymentIntentId);

        log.info("PaymentController - Payment confirmed: orderId={}", orderId);

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Payment confirmed successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }
}
