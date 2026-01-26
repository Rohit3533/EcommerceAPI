package com.api.project.Ecomerce.controller;

import com.api.project.Ecomerce.dto.*;
import com.api.project.Ecomerce.response.ApiResponse;
import com.api.project.Ecomerce.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart")
@Slf4j
public class CartController {

    private final CartService cartService;

    // ================= ADD TO CART =================
    @PostMapping("/add")
    public ApiResponse<CartResponse> addToCart(
            @RequestBody AddToCartRequest request) {

        log.info("CartController - addToCart called: requestType={}", request == null ? "null" : request.getClass().getSimpleName());

        CartResponse response = cartService.addToCart(request);

        log.info("CartController - addToCart successful");

        return ApiResponse.<CartResponse>builder()
                .success(true)
                .message("Product added to cart")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ================= UPDATE CART =================
    @PutMapping("/update")
    public ApiResponse<CartResponse> updateCart(
            @RequestBody UpdateCartRequest request) {

        log.info("CartController - updateCart called: requestType={}", request == null ? "null" : request.getClass().getSimpleName());

        CartResponse response = cartService.updateCart(request);

        log.info("CartController - updateCart successful");

        return ApiResponse.<CartResponse>builder()
                .success(true)
                .message("Cart updated successfully")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ================= REMOVE ITEM =================
    @DeleteMapping("/remove/{productId}")
    public ApiResponse<CartResponse> removeItem(
            @PathVariable Long productId) {

        log.info("CartController - removeItem called: productId={}", productId);

        CartResponse response = cartService.removeItem(productId);

        log.info("CartController - removeItem successful: productId={}", productId);

        return ApiResponse.<CartResponse>builder()
                .success(true)
                .message("Item removed from cart")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ================= CLEAR CART =================
    @DeleteMapping("/clear")
    public ApiResponse<Void> clearCart() {

        log.info("CartController - clearCart called");

        cartService.clearCart();

        log.info("CartController - clearCart successful");

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Cart cleared successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ================= GET CART =================
    @GetMapping
    public ApiResponse<CartResponse> getCart() {

        log.info("CartController - getCart called");

        CartResponse response = cartService.getCart();

        log.info("CartController - getCart successful");

        return ApiResponse.<CartResponse>builder()
                .success(true)
                .message("Cart fetched successfully")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build();
    }
}