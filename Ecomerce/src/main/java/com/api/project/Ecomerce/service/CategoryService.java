package com.api.project.Ecomerce.service;


import com.api.project.Ecomerce.dto.CategoryRequest;
import com.api.project.Ecomerce.dto.CategoryResponse;
import com.api.project.Ecomerce.entity.Category;
import com.api.project.Ecomerce.exception.ApiException;
import com.api.project.Ecomerce.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // ================= CREATE =================
    public CategoryResponse createCategory(CategoryRequest request) {

        if (categoryRepository.existsByName(request.getName())) {
            throw new ApiException("Category already exists",
                    HttpStatus.BAD_REQUEST);
        }

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        Category saved = categoryRepository.save(category);

        return mapToResponse(saved);
    }

    // ================= UPDATE =================
    public CategoryResponse updateCategory(Long id,
                                           CategoryRequest request) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ApiException("Category not found",
                                HttpStatus.NOT_FOUND));

        // Check duplicate name (if changed)
        if (!category.getName().equals(request.getName())
                && categoryRepository.existsByName(request.getName())) {
            throw new ApiException("Category name already exists",
                    HttpStatus.BAD_REQUEST);
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        Category updated = categoryRepository.save(category);

        return mapToResponse(updated);
    }

    // ================= SOFT DELETE =================
    public void deleteCategory(Long id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ApiException("Category not found",
                                HttpStatus.NOT_FOUND));

        category.setStatus("INACTIVE");

        categoryRepository.save(category);
    }

    // ================= GET ALL (ADMIN) =================
    public List<CategoryResponse> getAllCategories() {

        return categoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ================= GET ACTIVE (CUSTOMER) =================
    public List<CategoryResponse> getActiveCategories() {

        return categoryRepository.findByStatus("ACTIVE")
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ================= MAPPER =================
    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .status(category.getStatus())
                .build();
    }
}

