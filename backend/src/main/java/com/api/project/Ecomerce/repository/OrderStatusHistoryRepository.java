package com.api.project.Ecomerce.repository;

import com.api.project.Ecomerce.entity.Order;
import com.api.project.Ecomerce.entity.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {

    List<OrderStatusHistory> findByOrderOrderByCreatedAtAsc(Order order);

    List<OrderStatusHistory> findByOrderIdOrderByCreatedAtAsc(Long orderId);
}
