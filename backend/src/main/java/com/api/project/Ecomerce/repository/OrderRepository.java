package com.api.project.Ecomerce.repository;

import com.api.project.Ecomerce.entity.DeliveryPartner;
import com.api.project.Ecomerce.entity.Order;
import com.api.project.Ecomerce.entity.User;
import com.api.project.Ecomerce.entity.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUser(User user);

    Optional<Order> findByOrderNumber(String orderNumber);

    Optional<Order> findByTrackingId(String trackingId);

    List<Order> findByDeliveryPartner(DeliveryPartner deliveryPartner);

    List<Order> findByDeliveryPartnerIsNullAndStatusIn(List<OrderStatus> statuses);
}