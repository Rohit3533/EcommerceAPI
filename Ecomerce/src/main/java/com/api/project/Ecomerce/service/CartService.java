package com.api.project.Ecomerce.service;


import com.api.project.Ecomerce.dto.*;
import com.api.project.Ecomerce.entity.*;
import com.api.project.Ecomerce.exception.ApiException;
import com.api.project.Ecomerce.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

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

    // ================= GET OR CREATE CART =================
    private Cart getOrCreateCart(User user) {

        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .user(user)
                            .items(new ArrayList<>())
                            .build();
                    return cartRepository.save(newCart);
                });
    }

    // ================= ADD TO CART =================
    public CartResponse addToCart(AddToCartRequest request) {

        if (request.getQuantity() <= 0) {
            throw new ApiException("Quantity must be greater than zero",
                    HttpStatus.BAD_REQUEST);
        }

        User user = getCurrentUser();
        Cart cart = getOrCreateCart(user);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() ->
                        new ApiException("Product not found",
                                HttpStatus.NOT_FOUND));

        if (!product.getStatus().equals("ACTIVE")) {
            throw new ApiException("Product not available",
                    HttpStatus.BAD_REQUEST);
        }

        if (request.getQuantity() > product.getStock()) {
            throw new ApiException("Requested quantity exceeds stock",
                    HttpStatus.BAD_REQUEST);
        }

        CartItem cartItem = cartItemRepository
                .findByCartAndProduct(cart, product)
                .orElse(null);

        if (cartItem != null) {

            int newQuantity = cartItem.getQuantity() + request.getQuantity();

            if (newQuantity > product.getStock()) {
                throw new ApiException("Requested quantity exceeds stock",
                        HttpStatus.BAD_REQUEST);
            }

            cartItem.setQuantity(newQuantity);

        } else {

            cartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .priceAtAddition(product.getPrice())
                    .build();
        }

        cartItemRepository.save(cartItem);

        return buildCartResponse(cart);
    }

    // ================= UPDATE CART =================
    public CartResponse updateCart(UpdateCartRequest request) {

        if (request.getQuantity() < 0) {
            throw new ApiException("Quantity cannot be negative",
                    HttpStatus.BAD_REQUEST);
        }

        User user = getCurrentUser();
        Cart cart = getOrCreateCart(user);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() ->
                        new ApiException("Product not found",
                                HttpStatus.NOT_FOUND));

        CartItem cartItem = cartItemRepository
                .findByCartAndProduct(cart, product)
                .orElseThrow(() ->
                        new ApiException("Product not in cart",
                                HttpStatus.NOT_FOUND));

        if (request.getQuantity() == 0) {
            cartItemRepository.delete(cartItem);
            return buildCartResponse(cart);
        }

        if (request.getQuantity() > product.getStock()) {
            throw new ApiException("Requested quantity exceeds stock",
                    HttpStatus.BAD_REQUEST);
        }

        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);

        return buildCartResponse(cart);
    }

    // ================= REMOVE ITEM =================
    public CartResponse removeItem(Long productId) {

        User user = getCurrentUser();
        Cart cart = getOrCreateCart(user);

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ApiException("Product not found",
                                HttpStatus.NOT_FOUND));

        CartItem cartItem = cartItemRepository
                .findByCartAndProduct(cart, product)
                .orElseThrow(() ->
                        new ApiException("Product not in cart",
                                HttpStatus.NOT_FOUND));

        cartItemRepository.delete(cartItem);

        return buildCartResponse(cart);
    }

    // ================= CLEAR CART =================
    public void clearCart() {

        User user = getCurrentUser();
        Cart cart = getOrCreateCart(user);

        List<CartItem> items = cartItemRepository.findByCart(cart);

        cartItemRepository.deleteAll(items);
    }

    // ================= GET CART =================
    public CartResponse getCart() {

        User user = getCurrentUser();
        Cart cart = getOrCreateCart(user);

        return buildCartResponse(cart);
    }

    // ================= BUILD RESPONSE =================
    private CartResponse buildCartResponse(Cart cart) {

        List<CartItem> items = cartItemRepository.findByCart(cart);

        List<CartItemResponse> responseItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem item : items) {

            BigDecimal subtotal =
                    item.getPriceAtAddition()
                            .multiply(BigDecimal.valueOf(item.getQuantity()));

            total = total.add(subtotal);

            responseItems.add(
                    CartItemResponse.builder()
                            .productId(item.getProduct().getId())
                            .productName(item.getProduct().getName())
                            .price(item.getPriceAtAddition())
                            .quantity(item.getQuantity())
                            .subtotal(subtotal)
                            .build()
            );
        }

        return CartResponse.builder()
                .items(responseItems)
                .totalAmount(total)
                .build();
    }
}
