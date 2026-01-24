package com.api.project.Ecomerce.controller;


import com.api.project.Ecomerce.dto.CategoryRequest;
import com.api.project.Ecomerce.dto.CategoryResponse;
import com.api.project.Ecomerce.response.ApiResponse;
import com.api.project.Ecomerce.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // ================= ADMIN CREATE =================
    @PostMapping("/api/admin/categories")
    public ApiResponse<CategoryResponse> createCategory(
            @RequestBody CategoryRequest request) {

        CategoryResponse response =
                categoryService.createCategory(request);

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

        CategoryResponse response =
                categoryService.updateCategory(id, request);

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

        categoryService.deleteCategory(id);

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Category deactivated successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }
    // ================= ADMIN REACTIVATED (SOFT) =================
    @PostMapping("/api/admin/categories/{id}")
    public ApiResponse<Void> ActivateCategory(@PathVariable Long id) {

        categoryService.activateCategory(id);

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Category Reactivated successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }


    // ================= ADMIN GET ALL =================
    @GetMapping("/api/admin/categories")
    public ApiResponse<List<CategoryResponse>> getAllCategories() {

        List<CategoryResponse> categories =
                categoryService.getAllCategories();

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

        List<CategoryResponse> categories =
                categoryService.getActiveCategories();

        return ApiResponse.<List<CategoryResponse>>builder()
                .success(true)
                .message("Active categories fetched")
                .data(categories)
                .timestamp(LocalDateTime.now())
                .build();
    }
}

