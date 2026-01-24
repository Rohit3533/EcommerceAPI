package com.api.project.Ecomerce.controller;

import com.api.project.Ecomerce.dto.ProductRequest;
import com.api.project.Ecomerce.dto.ProductResponse;
import com.api.project.Ecomerce.response.ApiResponse;
import com.api.project.Ecomerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // ================= ADMIN CREATE =================
    @PostMapping("/api/admin/products")
    public ApiResponse<ProductResponse> createProduct(
            @RequestBody ProductRequest request) {

        ProductResponse response =
                productService.createProduct(request);

        return ApiResponse.<ProductResponse>builder()
                .success(true)
                .message("Product created successfully")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ================= ADMIN UPDATE =================
    @PutMapping("/api/admin/products/{id}")
    public ApiResponse<ProductResponse> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductRequest request) {

        ProductResponse response =
                productService.updateProduct(id, request);

        return ApiResponse.<ProductResponse>builder()
                .success(true)
                .message("Product updated successfully")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ================= ADMIN DELETE =================
    @DeleteMapping("/api/admin/products/{id}")
    public ApiResponse<Void> deleteProduct(@PathVariable Long id) {

        productService.deleteProduct(id);

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Product deactivated successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ================= ADMIN GET ALL =================
    @GetMapping("/api/admin/products")
    public ApiResponse<Page<ProductResponse>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<ProductResponse> products =
                productService.getAllProducts(pageable);

        return ApiResponse.<Page<ProductResponse>>builder()
                .success(true)
                .message("All products fetched")
                .data(products)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ================= PUBLIC GET ALL ACTIVE =================
    @GetMapping("/api/products")
    public ApiResponse<Page<ProductResponse>> getActiveProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<ProductResponse> products =
                productService.getActiveProducts(pageable);

        return ApiResponse.<Page<ProductResponse>>builder()
                .success(true)
                .message("Active products fetched")
                .data(products)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ================= PUBLIC GET BY ID =================
    @GetMapping("/api/products/{id}")
    public ApiResponse<ProductResponse> getProductById(
            @PathVariable Long id) {

        ProductResponse product =
                productService.getProductById(id);

        return ApiResponse.<ProductResponse>builder()
                .success(true)
                .message("Product fetched")
                .data(product)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ================= PUBLIC GET BY CATEGORY =================
    @GetMapping("/api/products/category/{categoryId}")
    public ApiResponse<Page<ProductResponse>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<ProductResponse> products =
                productService.getProductsByCategory(categoryId, pageable);

        return ApiResponse.<Page<ProductResponse>>builder()
                .success(true)
                .message("Products fetched by category")
                .data(products)
                .timestamp(LocalDateTime.now())
                .build();
    }
}