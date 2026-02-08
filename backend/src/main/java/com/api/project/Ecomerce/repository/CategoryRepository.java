package com.api.project.Ecomerce.repository;


import com.api.project.Ecomerce.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByName(String name);

    Optional<Category> findByName(String name);

    List<Category> findByStatus(String status);
}

