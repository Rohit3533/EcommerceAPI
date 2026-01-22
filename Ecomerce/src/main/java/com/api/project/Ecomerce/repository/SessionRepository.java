package com.api.project.Ecomerce.repository;

import com.api.project.Ecomerce.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {

    Optional<Session> findByTokenAndIsActiveTrue(String token);

    void deleteByUserId(Long userId);
}

