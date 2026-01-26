package com.api.project.Ecomerce.controller;


import com.api.project.Ecomerce.dto.CategoryRequest;
import com.api.project.Ecomerce.dto.CategoryResponse;
import com.api.project.Ecomerce.response.ApiResponse;
import com.api.project.Ecomerce.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    // ================= ADMIN CREATE =================
    @PostMapping("/api/admin/categories")
    public ApiResponse<CategoryResponse> createCategory(
            @RequestBody CategoryRequest request) {

        log.info("CategoryController - createCategory called: requestType={}", request == null ? "null" : request.getClass().getSimpleName());

        CategoryResponse response =
                categoryService.createCategory(request);

        log.info("CategoryController - Category created successfully: categoryId={}", response == null ? "null" : response.getId());

        return ApiResponse.<CategoryResponse>builder()
                .success(true)
                .message("Category created successfully")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ================= ADMIN UPDATE =================
    @PutMapping("/api/admin/categories/{id}")
    public ApiResponse<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @RequestBody CategoryRequest request) {

        log.info("CategoryController - updateCategory called: id={}, requestType={}", id, request == null ? "null" : request.getClass().getSimpleName());

        CategoryResponse response =
                categoryService.updateCategory(id, request);

        log.info("CategoryController - Category updated successfully: id={}", id);

        return ApiResponse.<CategoryResponse>builder()
                .success(true)
                .message("Category updated successfully")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ================= ADMIN DELETE (SOFT) =================
    @DeleteMapping("/api/admin/categories/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {

        log.info("CategoryController - deleteCategory called: id={}", id);

        categoryService.deleteCategory(id);

        log.info("CategoryController - Category deactivated: id={}", id);

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Category deactivated successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }
    // ================= ADMIN REACTIVATED (SOFT) =================
    @PostMapping("/api/admin/categories/{id}")
    public ApiResponse<Void> ActivateCategory(@PathVariable Long id) {

        log.info("CategoryController - ActivateCategory called: id={}", id);

        categoryService.activateCategory(id);

        log.info("CategoryController - Category reactivated: id={}", id);

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Category Reactivated successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }


    // ================= ADMIN GET ALL =================
    @GetMapping("/api/admin/categories")
    public ApiResponse<List<CategoryResponse>> getAllCategories() {

        log.info("CategoryController - getAllCategories called");

        List<CategoryResponse> categories =
                categoryService.getAllCategories();

        log.info("CategoryController - getAllCategories returned count={}", categories == null ? 0 : categories.size());

        return ApiResponse.<List<CategoryResponse>>builder()
                .success(true)
                .message("All categories fetched")
                .data(categories)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ================= CUSTOMER GET ACTIVE =================
    @GetMapping("/api/categories")
    public ApiResponse<List<CategoryResponse>> getActiveCategories() {

        log.info("CategoryController - getActiveCategories called");

        List<CategoryResponse> categories =
                categoryService.getActiveCategories();

        log.info("CategoryController - getActiveCategories returned count={}", categories == null ? 0 : categories.size());

        return ApiResponse.<List<CategoryResponse>>builder()
                .success(true)
                .message("Active categories fetched")
                .data(categories)
                .timestamp(LocalDateTime.now())
                .build();
    }
}

