package com.api.project.Ecomerce.repository;

import com.api.project.Ecomerce.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {

    Optional<OtpVerification> findByEmail(String email);

    void deleteByEmail(String email);

    void deleteByExpiresAtBefore(LocalDateTime dateTime);

    boolean existsByEmail(String email);
}
