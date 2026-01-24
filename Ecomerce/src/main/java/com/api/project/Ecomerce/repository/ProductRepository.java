package com.api.project.Ecomerce.repository;

import com.api.project.Ecomerce.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // Public listing (only ACTIVE)
    Page<Product> findByStatus(String status, Pageable pageable);

    // Public listing by category (only ACTIVE)
    Page<Product> findByCategoryIdAndStatus(Long categoryId,
                                            String status,
                                            Pageable pageable);
}