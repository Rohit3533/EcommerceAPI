package com.api.project.Ecomerce.service;

import com.api.project.Ecomerce.dto.ProductRequest;
import com.api.project.Ecomerce.dto.ProductResponse;
import com.api.project.Ecomerce.entity.Category;
import com.api.project.Ecomerce.entity.Product;
import com.api.project.Ecomerce.exception.ApiException;
import com.api.project.Ecomerce.repository.CategoryRepository;
import com.api.project.Ecomerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    // ================= CREATE =================
    public ProductResponse createProduct(ProductRequest request) {

        log.info("Creating product with name: {}", request.getName());

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> {
                    log.warn("Category not found with id: {}", request.getCategoryId());
                    return new ApiException("Category not found", HttpStatus.NOT_FOUND);
                });

        if (request.getPrice().doubleValue() <= 0) {
            log.warn("Invalid price provided for product: {}", request.getPrice());
            throw new ApiException("Price must be greater than zero",
                    HttpStatus.BAD_REQUEST);
        }

        if (request.getStock() < 0) {
            log.warn("Invalid stock provided for product: {}", request.getStock());
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

        log.info("Product created successfully with id: {}", saved.getId());
        log.debug("Created product details: {}", saved);

        return mapToResponse(saved);
    }

    // ================= UPDATE =================
    public ProductResponse updateProduct(Long id, ProductRequest request) {

        log.info("Updating product with id: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Product not found with id: {}", id);
                    return new ApiException("Product not found", HttpStatus.NOT_FOUND);
                });

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> {
                    log.warn("Category not found with id: {}", request.getCategoryId());
                    return new ApiException("Category not found", HttpStatus.NOT_FOUND);
                });

        if (request.getPrice().doubleValue() <= 0) {
            log.warn("Invalid price while updating product id {}: {}", id, request.getPrice());
            throw new ApiException("Price must be greater than zero",
                    HttpStatus.BAD_REQUEST);
        }

        if (request.getStock() < 0) {
            log.warn("Invalid stock while updating product id {}: {}", id, request.getStock());
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

        log.info("Product updated successfully with id: {}", updated.getId());
        log.debug("Updated product details: {}", updated);

        return mapToResponse(updated);
    }

    // ================= SOFT DELETE =================
    public void deleteProduct(Long id) {

        log.info("Soft deleting product with id: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Product not found for deletion with id: {}", id);
                    return new ApiException("Product not found", HttpStatus.NOT_FOUND);
                });

        product.setStock(0); // triggers INACTIVE via @PreUpdate
        productRepository.save(product);

        log.info("Product soft deleted (set to INACTIVE) with id: {}", id);
    }

    // ================= ADMIN GET ALL =================
    public Page<ProductResponse> getAllProducts(Pageable pageable) {

        log.debug("Fetching all products. Page: {}, Size: {}",
                pageable.getPageNumber(),
                pageable.getPageSize());

        return productRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    // ================= PUBLIC GET ALL (ACTIVE) =================
    public Page<ProductResponse> getActiveProducts(Pageable pageable) {

        log.debug("Fetching active products. Page: {}, Size: {}",
                pageable.getPageNumber(),
                pageable.getPageSize());

        return productRepository
                .findByStatus("ACTIVE", pageable)
                .map(this::mapToResponse);
    }

    // ================= PUBLIC GET BY ID =================
    public ProductResponse getProductById(Long id) {

        log.debug("Fetching product by id: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Product not found with id: {}", id);
                    return new ApiException("Product not found", HttpStatus.NOT_FOUND);
                });

        if (!"ACTIVE".equals(product.getStatus())) {
            log.warn("Attempt to access inactive product with id: {}", id);
            throw new ApiException("Product not available",
                    HttpStatus.NOT_FOUND);
        }

        return mapToResponse(product);
    }

    // ================= PUBLIC GET BY CATEGORY =================
    public Page<ProductResponse> getProductsByCategory(Long categoryId,
                                                       Pageable pageable) {

        log.debug("Fetching active products by category id: {} | Page: {}, Size: {}",
                categoryId,
                pageable.getPageNumber(),
                pageable.getPageSize());

        return productRepository
                .findByCategoryIdAndStatus(categoryId,
                        "ACTIVE",
                        pageable)
                .map(this::mapToResponse);
    }

    // ================= MAPPER =================
    private ProductResponse mapToResponse(Product product) {

        log.debug("Mapping product entity to response. Product id: {}", product.getId());

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