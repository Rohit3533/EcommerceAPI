package com.api.project.Ecomerce.repository;

import com.api.project.Ecomerce.entity.DeliveryPartner;
import com.api.project.Ecomerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryPartnerRepository extends JpaRepository<DeliveryPartner, Long> {

    List<DeliveryPartner> findByStatus(String status);

    List<DeliveryPartner> findByVerificationStatus(String verificationStatus);

    List<DeliveryPartner> findByStatusAndVerificationStatus(String status, String verificationStatus);

    Optional<DeliveryPartner> findByPhone(String phone);

    Optional<DeliveryPartner> findByEmail(String email);

    Optional<DeliveryPartner> findByUser(User user);

    Optional<DeliveryPartner> findByUserId(Long userId);

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);
}
