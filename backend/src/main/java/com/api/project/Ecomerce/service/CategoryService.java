package com.api.project.Ecomerce.service;

import com.api.project.Ecomerce.dto.CategoryRequest;
import com.api.project.Ecomerce.dto.CategoryResponse;
import com.api.project.Ecomerce.entity.Category;
import com.api.project.Ecomerce.exception.ApiException;
import com.api.project.Ecomerce.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // ================= CREATE =================
    public CategoryResponse createCategory(CategoryRequest request) {
        log.info("CategoryService - createCategory called: name={}", request == null ? "null" : request.getName());

        if (categoryRepository.existsByName(request.getName())) {
            log.warn("CategoryService - Category already exists: name={}", request.getName());
            throw new ApiException("Category already exists",
                    HttpStatus.BAD_REQUEST);
        }

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        Category saved = categoryRepository.save(category);

        log.info("CategoryService - Category created: id={}, name={}", saved.getId(), saved.getName());

        return mapToResponse(saved);
    }

    // ================= UPDATE =================
    public CategoryResponse updateCategory(Long id,
                                           CategoryRequest request) {

        log.info("CategoryService - updateCategory called: id={}, name={}", id, request == null ? "null" : request.getName());

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("CategoryService - Category not found for update: id={}", id);
                    return new ApiException("Category not found",
                            HttpStatus.NOT_FOUND);
                });

        // Check duplicate name (if changed)
        if (!category.getName().equals(request.getName())
                && categoryRepository.existsByName(request.getName())) {
            log.warn("CategoryService - Category name already exists on update: id={}, newName={}", id, request.getName());
            throw new ApiException("Category name already exists",
                    HttpStatus.BAD_REQUEST);
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        Category updated = categoryRepository.save(category);

        log.info("CategoryService - Category updated: id={}, name={}", updated.getId(), updated.getName());

        return mapToResponse(updated);
    }

    // ================= SOFT DELETE =================
    public void deleteCategory(Long id) {
        log.info("CategoryService - deleteCategory called: id={}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("CategoryService - Category not found for delete: id={}", id);
                    return new ApiException("Category not found",
                            HttpStatus.NOT_FOUND);
                });

        category.setStatus("INACTIVE");

        Category saved = categoryRepository.save(category);

        log.info("CategoryService - Category deactivated: id={}, name={}", saved.getId(), saved.getName());
    }

    public void activateCategory(Long id) {
        log.info("CategoryService - activateCategory called: id={}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("CategoryService - Category not found for activate: id={}", id);
                    return new ApiException("Category not found",
                            HttpStatus.NOT_FOUND);
                });

        category.setStatus("ACTIVE");

        Category saved = categoryRepository.save(category);

        log.info("CategoryService - Category reactivated: id={}, name={}", saved.getId(), saved.getName());
    }

    // ================= GET ALL (ADMIN) =================
    public List<CategoryResponse> getAllCategories() {
        log.info("CategoryService - getAllCategories called");

        List<CategoryResponse> responses = categoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        log.debug("CategoryService - getAllCategories returned count={}", responses == null ? 0 : responses.size());

        return responses;
    }

    // ================= GET ACTIVE (CUSTOMER) =================
    public List<CategoryResponse> getActiveCategories() {
        log.info("CategoryService - getActiveCategories called");

        List<CategoryResponse> responses = categoryRepository.findByStatus("ACTIVE")
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        log.debug("CategoryService - getActiveCategories returned count={}", responses == null ? 0 : responses.size());

        return responses;
    }

    // ================= MAPPER =================
    private CategoryResponse mapToResponse(Category category) {
        if (category == null) {
            log.debug("CategoryService - mapToResponse called with null");
            return null;
        }
        log.debug("CategoryService - Mapping category to response: id={}, name={}", category.getId(), category.getName());
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .status(category.getStatus())
                .build();
    }
}
