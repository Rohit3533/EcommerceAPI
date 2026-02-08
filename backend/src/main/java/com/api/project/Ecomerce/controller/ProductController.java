package com.api.project.Ecomerce.controller;

import com.api.project.Ecomerce.dto.ProductRequest;
import com.api.project.Ecomerce.dto.ProductResponse;
import com.api.project.Ecomerce.response.ApiResponse;
import com.api.project.Ecomerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
public class ProductController {

        private final ProductService productService;

        // ================= ADMIN CREATE =================
        @PostMapping("/api/admin/products")
        public ApiResponse<ProductResponse> createProduct(
                        @RequestBody ProductRequest request) {

                log.info("ProductController - createProduct called: requestType={}",
                                request == null ? "null" : request.getClass().getSimpleName());

                ProductResponse response = productService.createProduct(request);

                log.info("ProductController - Product created: productId={}",
                                response == null ? "null" : response.getId());

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

                log.info("ProductController - updateProduct called: id={}, requestType={}", id,
                                request == null ? "null" : request.getClass().getSimpleName());

                ProductResponse response = productService.updateProduct(id, request);

                log.info("ProductController - Product updated: productId={}", response == null ? id : response.getId());

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

                log.info("ProductController - deleteProduct called: id={}", id);

                productService.deleteProduct(id);

                log.info("ProductController - Product deactivated: id={}", id);

                return ApiResponse.<Void>builder()
                                .success(true)
                                .message("Product deactivated successfully")
                                .timestamp(LocalDateTime.now())
                                .build();
        }

        // ================= ADMIN GET ALL =================
        @GetMapping("/api/admin/products")
        public ApiResponse<Page<ProductResponse>> getAllProducts(
                        @RequestParam(name = "page", defaultValue = "0") int page,
                        @RequestParam(name = "size", defaultValue = "25") int size) {

                log.info("ProductController - getAllProducts called: page={}, size={}", page, size);

                Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

                Page<ProductResponse> products = productService.getAllProducts(pageable);

                log.info("ProductController - getAllProducts returned: itemsCount={}, totalElements={}",
                                products == null ? 0 : products.getNumberOfElements(),
                                products == null ? 0 : products.getTotalElements());

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
                        @RequestParam(name = "page", defaultValue = "0") int page,
                        @RequestParam(name = "size", defaultValue = "10") int size) {

                log.info("ProductController - getActiveProducts called: page={}, size={}", page, size);

                Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

                Page<ProductResponse> products = productService.getActiveProducts(pageable);

                log.info("ProductController - getActiveProducts returned: itemsCount={}, totalElements={}",
                                products == null ? 0 : products.getNumberOfElements(),
                                products == null ? 0 : products.getTotalElements());

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

                log.info("ProductController - getProductById called: id={}", id);

                ProductResponse product = productService.getProductById(id);

                log.info("ProductController - getProductById returned: productId={}",
                                product == null ? id : product.getId());

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
                        @RequestParam(name = "page", defaultValue = "0") int page,
                        @RequestParam(name = "size", defaultValue = "10") int size) {

                log.info("ProductController - getProductsByCategory called: categoryId={}, page={}, size={}",
                                categoryId, page, size);

                Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

                Page<ProductResponse> products = productService.getProductsByCategory(categoryId, pageable);

                log.info("ProductController - getProductsByCategory returned: itemsCount={}, totalElements={}",
                                products == null ? 0 : products.getNumberOfElements(),
                                products == null ? 0 : products.getTotalElements());

                return ApiResponse.<Page<ProductResponse>>builder()
                                .success(true)
                                .message("Products fetched by category")
                                .data(products)
                                .timestamp(LocalDateTime.now())
                                .build();
        }
}
