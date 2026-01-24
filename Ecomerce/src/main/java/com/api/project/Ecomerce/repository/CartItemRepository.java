package com.api.project.Ecomerce.repository;


import com.api.project.Ecomerce.entity.Cart;
import com.api.project.Ecomerce.entity.CartItem;
import com.api.project.Ecomerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);

    List<CartItem> findByCart(Cart cart);
}