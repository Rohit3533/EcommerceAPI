package com.api.project.Ecomerce.service;

import com.api.project.Ecomerce.dto.ProductRequest;
import com.api.project.Ecomerce.dto.ProductResponse;
import com.api.project.Ecomerce.entity.Category;
import com.api.project.Ecomerce.entity.Product;
import com.api.project.Ecomerce.exception.ApiException;
import com.api.project.Ecomerce.repository.CategoryRepository;
import com.api.project.Ecomerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    // ================= CREATE =================
    public ProductResponse createProduct(ProductRequest request) {

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() ->
                        new ApiException("Category not found",
                                HttpStatus.NOT_FOUND));

        if (request.getPrice().doubleValue() <= 0) {
            throw new ApiException("Price must be greater than zero",
                    HttpStatus.BAD_REQUEST);
        }

        if (request.getStock() < 0) {
            throw new ApiException("Stock cannot be negative",
                    HttpStatus.BAD_REQUEST);
        }

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .imageUrl(request.getImageUrl())
                .category(category)
                .build();

        Product saved = productRepository.save(product);

        return mapToResponse(saved);
    }

    // ================= UPDATE =================
    public ProductResponse updateProduct(Long id,
                                         ProductRequest request) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ApiException("Product not found",
                                HttpStatus.NOT_FOUND));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() ->
                        new ApiException("Category not found",
                                HttpStatus.NOT_FOUND));

        if (request.getPrice().doubleValue() <= 0) {
            throw new ApiException("Price must be greater than zero",
                    HttpStatus.BAD_REQUEST);
        }

        if (request.getStock() < 0) {
            throw new ApiException("Stock cannot be negative",
                    HttpStatus.BAD_REQUEST);
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setImageUrl(request.getImageUrl());
        product.setCategory(category);

        Product updated = productRepository.save(product);

        return mapToResponse(updated);
    }

    // ================= SOFT DELETE =================
    public void deleteProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ApiException("Product not found",
                                HttpStatus.NOT_FOUND));

        product.setStock(0); // triggers INACTIVE via @PreUpdate
        productRepository.save(product);
    }

    // ================= ADMIN GET ALL =================
    public Page<ProductResponse> getAllProducts(Pageable pageable) {

        return productRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    // ================= PUBLIC GET ALL (ACTIVE) =================
    public Page<ProductResponse> getActiveProducts(Pageable pageable) {

        return productRepository
                .findByStatus("ACTIVE", pageable)
                .map(this::mapToResponse);
    }

    // ================= PUBLIC GET BY ID =================
    public ProductResponse getProductById(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ApiException("Product not found",
                                HttpStatus.NOT_FOUND));

        if (!product.getStatus().equals("ACTIVE")) {
            throw new ApiException("Product not available",
                    HttpStatus.NOT_FOUND);
        }

        return mapToResponse(product);
    }

    // ================= PUBLIC GET BY CATEGORY =================
    public Page<ProductResponse> getProductsByCategory(Long categoryId,
                                                       Pageable pageable) {

        return productRepository
                .findByCategoryIdAndStatus(categoryId,
                        "ACTIVE",
                        pageable)
                .map(this::mapToResponse);
    }

    // ================= MAPPER =================
    private ProductResponse mapToResponse(Product product) {

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .imageUrl(product.getImageUrl())
                .status(product.getStatus())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .build();
    }
}