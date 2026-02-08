package com.api.project.Ecomerce.repository;


import com.api.project.Ecomerce.entity.Cart;
import com.api.project.Ecomerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUser(User user);
}
