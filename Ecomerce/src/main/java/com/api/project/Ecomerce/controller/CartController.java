package com.api.project.Ecomerce.controller;

import com.api.project.Ecomerce.dto.*;
import com.api.project.Ecomerce.response.ApiResponse;
import com.api.project.Ecomerce.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    // ================= ADD TO CART =================
    @PostMapping("/add")
    public ApiResponse<CartResponse> addToCart(
            @RequestBody AddToCartRequest request) {

        CartResponse response = cartService.addToCart(request);

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

        CartResponse response = cartService.updateCart(request);

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

        CartResponse response = cartService.removeItem(productId);

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

        cartService.clearCart();

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Cart cleared successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ================= GET CART =================
    @GetMapping
    public ApiResponse<CartResponse> getCart() {

        CartResponse response = cartService.getCart();

        return ApiResponse.<CartResponse>builder()
                .success(true)
                .message("Cart fetched successfully")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build();
    }
}