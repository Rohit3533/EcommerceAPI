package com.api.project.Ecomerce.repository;

import com.api.project.Ecomerce.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}