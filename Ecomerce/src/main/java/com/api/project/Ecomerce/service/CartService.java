package com.api.project.Ecomerce.service;


import com.api.project.Ecomerce.dto.*;
import com.api.project.Ecomerce.entity.*;
import com.api.project.Ecomerce.exception.ApiException;
import com.api.project.Ecomerce.repository.*;
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

        log.debug("CartService - Resolving current user from SecurityContext: {}", email);

        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("CartService - User not found: {}", email);
                    return new ApiException("User not found",
                            HttpStatus.NOT_FOUND);
                });
    }

    // ================= GET OR CREATE CART =================
    private Cart getOrCreateCart(User user) {
        log.debug("CartService - Fetching cart for user: {} (id={})", user.getEmail(), user.getId());

        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    log.info("CartService - Creating new cart for user: {} (id={})", user.getEmail(), user.getId());
                    Cart newCart = Cart.builder()
                            .user(user)
                            .items(new ArrayList<>())
                            .build();
                    Cart saved = cartRepository.save(newCart);
                    log.debug("CartService - New cart created with id={}", saved.getId());
                    return saved;
                });
    }

    // ================= ADD TO CART =================
    public CartResponse addToCart(AddToCartRequest request) {
        log.info("CartService - addToCart called: productId={}, quantity={}", request.getProductId(), request.getQuantity());

        if (request.getQuantity() <= 0) {
            log.warn("CartService - Invalid quantity requested: {}", request.getQuantity());
            throw new ApiException("Quantity must be greater than zero",
                    HttpStatus.BAD_REQUEST);
        }

        User user = getCurrentUser();
        Cart cart = getOrCreateCart(user);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> {
                    log.warn("CartService - Product not found: id={}", request.getProductId());
                    return new ApiException("Product not found",
                            HttpStatus.NOT_FOUND);
                });

        if (!"ACTIVE".equals(product.getStatus())) {
            log.warn("CartService - Attempt to add inactive product: id={}, status={}", product.getId(), product.getStatus());
            throw new ApiException("Product not available",
                    HttpStatus.BAD_REQUEST);
        }

        if (request.getQuantity() > product.getStock()) {
            log.warn("CartService - Requested quantity exceeds stock: productId={}, requested={}, stock={}",
                    product.getId(), request.getQuantity(), product.getStock());
            throw new ApiException("Requested quantity exceeds stock",
                    HttpStatus.BAD_REQUEST);
        }

        CartItem cartItem = cartItemRepository
                .findByCartAndProduct(cart, product)
                .orElse(null);

        if (cartItem != null) {
            int oldQty = cartItem.getQuantity();
            int newQuantity = oldQty + request.getQuantity();

            if (newQuantity > product.getStock()) {
                log.warn("CartService - New quantity exceeds stock on update: productId={}, newQuantity={}, stock={}",
                        product.getId(), newQuantity, product.getStock());
                throw new ApiException("Requested quantity exceeds stock",
                        HttpStatus.BAD_REQUEST);
            }

            cartItem.setQuantity(newQuantity);
            log.debug("CartService - Updated cart item quantity: productId={}, from={} to={}", product.getId(), oldQty, newQuantity);

        } else {

            cartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .priceAtAddition(product.getPrice())
                    .build();

            log.debug("CartService - Creating new cart item for productId={} quantity={}", product.getId(), request.getQuantity());
        }

        cartItemRepository.save(cartItem);
        log.info("CartService - Cart item saved for productId={} userId={}", product.getId(), user.getId());

        CartResponse response = buildCartResponse(cart);
        log.info("CartService - addToCart completed: userId={}, itemsCount={}", user.getId(), response.getItems().size());

        return response;
    }

    // ================= UPDATE CART =================
    public CartResponse updateCart(UpdateCartRequest request) {
        log.info("CartService - updateCart called: productId={}, quantity={}", request.getProductId(), request.getQuantity());

        if (request.getQuantity() < 0) {
            log.warn("CartService - Negative quantity provided: {}", request.getQuantity());
            throw new ApiException("Quantity cannot be negative",
                    HttpStatus.BAD_REQUEST);
        }

        User user = getCurrentUser();
        Cart cart = getOrCreateCart(user);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> {
                    log.warn("CartService - Product not found for update: id={}", request.getProductId());
                    return new ApiException("Product not found",
                            HttpStatus.NOT_FOUND);
                });

        CartItem cartItem = cartItemRepository
                .findByCartAndProduct(cart, product)
                .orElseThrow(() -> {
                    log.warn("CartService - Product not in cart for update: productId={}, userId={}", product.getId(), user.getId());
                    return new ApiException("Product not in cart",
                            HttpStatus.NOT_FOUND);
                });

        if (request.getQuantity() == 0) {
            cartItemRepository.delete(cartItem);
            log.info("CartService - Removed cart item (quantity set to 0): productId={}, userId={}", product.getId(), user.getId());
            CartResponse resp = buildCartResponse(cart);
            log.debug("CartService - updateCart completed after deletion: userId={}, itemsCount={}", user.getId(), resp.getItems().size());
            return resp;
        }

        if (request.getQuantity() > product.getStock()) {
            log.warn("CartService - Requested quantity exceeds stock on update: productId={}, requested={}, stock={}",
                    product.getId(), request.getQuantity(), product.getStock());
            throw new ApiException("Requested quantity exceeds stock",
                    HttpStatus.BAD_REQUEST);
        }

        int oldQty = cartItem.getQuantity();
        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);
        log.info("CartService - Cart item updated: productId={}, from={} to={}, userId={}", product.getId(), oldQty, request.getQuantity(), user.getId());

        CartResponse response = buildCartResponse(cart);
        log.debug("CartService - updateCart completed: userId={}, itemsCount={}", user.getId(), response.getItems().size());

        return response;
    }

    // ================= REMOVE ITEM =================
    public CartResponse removeItem(Long productId) {
        log.info("CartService - removeItem called: productId={}", productId);

        User user = getCurrentUser();
        Cart cart = getOrCreateCart(user);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("CartService - Product not found for removal: id={}", productId);
                    return new ApiException("Product not found",
                            HttpStatus.NOT_FOUND);
                });

        CartItem cartItem = cartItemRepository
                .findByCartAndProduct(cart, product)
                .orElseThrow(() -> {
                    log.warn("CartService - Product not in cart for removal: productId={}, userId={}", product.getId(), user.getId());
                    return new ApiException("Product not in cart",
                            HttpStatus.NOT_FOUND);
                });

        cartItemRepository.delete(cartItem);
        log.info("CartService - Cart item deleted: productId={}, userId={}", product.getId(), user.getId());

        CartResponse response = buildCartResponse(cart);
        log.debug("CartService - removeItem completed: userId={}, itemsCount={}", user.getId(), response.getItems().size());

        return response;
    }

    // ================= CLEAR CART =================
    public void clearCart() {
        log.info("CartService - clearCart called");

        User user = getCurrentUser();
        Cart cart = getOrCreateCart(user);

        List<CartItem> items = cartItemRepository.findByCart(cart);
        int count = items.size();

        cartItemRepository.deleteAll(items);
        log.info("CartService - Cleared cart for userId={}, removedItems={}", user.getId(), count);
    }

    // ================= GET CART =================
    public CartResponse getCart() {
        log.info("CartService - getCart called");

        User user = getCurrentUser();
        Cart cart = getOrCreateCart(user);

        CartResponse response = buildCartResponse(cart);
        log.debug("CartService - getCart completed: userId={}, itemsCount={}", user.getId(), response.getItems().size());

        return response;
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

        log.debug("CartService - Built cart response: cartId={}, itemsCount={}, total={}", cart.getId(), responseItems.size(), total);

        return CartResponse.builder()
                .items(responseItems)
                .totalAmount(total)
                .build();
    }
}
