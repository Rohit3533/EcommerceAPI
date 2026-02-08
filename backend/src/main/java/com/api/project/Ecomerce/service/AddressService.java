package com.api.project.Ecomerce.service;

import com.api.project.Ecomerce.dto.AddressRequest;
import com.api.project.Ecomerce.dto.AddressResponse;
import com.api.project.Ecomerce.entity.Address;
import com.api.project.Ecomerce.entity.User;
import com.api.project.Ecomerce.exception.ApiException;
import com.api.project.Ecomerce.repository.AddressRepository;
import com.api.project.Ecomerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
    }

    /**
     * Get all addresses for current user
     */
    public List<AddressResponse> getAddresses() {
        User user = getCurrentUser();
        log.info("AddressService - Getting addresses for userId={}", user.getId());

        return addressRepository.findByUserOrderByIsDefaultDescCreatedAtDesc(user)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get single address by ID
     */
    public AddressResponse getAddress(Long id) {
        User user = getCurrentUser();
        Address address = addressRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ApiException("Address not found", HttpStatus.NOT_FOUND));
        return mapToResponse(address);
    }

    /**
     * Add new address
     */
    @Transactional
    public AddressResponse addAddress(AddressRequest request) {
        User user = getCurrentUser();
        log.info("AddressService - Adding address for userId={}", user.getId());

        // If this is the first address or marked as default, handle default logic
        boolean isFirstAddress = addressRepository.countByUser(user) == 0;
        boolean shouldBeDefault = isFirstAddress || Boolean.TRUE.equals(request.getIsDefault());

        if (shouldBeDefault) {
            // Unset any existing default
            addressRepository.findByUserAndIsDefaultTrue(user)
                    .ifPresent(addr -> {
                        addr.setIsDefault(false);
                        addressRepository.save(addr);
                    });
        }

        Address address = Address.builder()
                .user(user)
                .label(request.getLabel())
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .streetAddress(request.getStreetAddress())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .isDefault(shouldBeDefault)
                .build();

        Address saved = addressRepository.save(address);
        log.info("AddressService - Address added: id={}", saved.getId());

        return mapToResponse(saved);
    }

    /**
     * Update existing address
     */
    @Transactional
    public AddressResponse updateAddress(Long id, AddressRequest request) {
        User user = getCurrentUser();
        log.info("AddressService - Updating address id={} for userId={}", id, user.getId());

        Address address = addressRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ApiException("Address not found", HttpStatus.NOT_FOUND));

        // Handle default address change
        if (Boolean.TRUE.equals(request.getIsDefault()) && !Boolean.TRUE.equals(address.getIsDefault())) {
            addressRepository.findByUserAndIsDefaultTrue(user)
                    .ifPresent(addr -> {
                        addr.setIsDefault(false);
                        addressRepository.save(addr);
                    });
        }

        address.setLabel(request.getLabel());
        address.setFullName(request.getFullName());
        address.setPhone(request.getPhone());
        address.setStreetAddress(request.getStreetAddress());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());
        address.setIsDefault(request.getIsDefault());

        Address updated = addressRepository.save(address);
        log.info("AddressService - Address updated: id={}", updated.getId());

        return mapToResponse(updated);
    }

    /**
     * Delete address
     */
    @Transactional
    public void deleteAddress(Long id) {
        User user = getCurrentUser();
        log.info("AddressService - Deleting address id={} for userId={}", id, user.getId());

        Address address = addressRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ApiException("Address not found", HttpStatus.NOT_FOUND));

        boolean wasDefault = Boolean.TRUE.equals(address.getIsDefault());
        addressRepository.delete(address);

        // If deleted address was default, set another as default
        if (wasDefault) {
            List<Address> remaining = addressRepository.findByUserOrderByIsDefaultDescCreatedAtDesc(user);
            if (!remaining.isEmpty()) {
                remaining.get(0).setIsDefault(true);
                addressRepository.save(remaining.get(0));
            }
        }

        log.info("AddressService - Address deleted: id={}", id);
    }

    /**
     * Set address as default
     */
    @Transactional
    public AddressResponse setDefaultAddress(Long id) {
        User user = getCurrentUser();
        log.info("AddressService - Setting default address id={} for userId={}", id, user.getId());

        // Unset current default
        addressRepository.findByUserAndIsDefaultTrue(user)
                .ifPresent(addr -> {
                    addr.setIsDefault(false);
                    addressRepository.save(addr);
                });

        // Set new default
        Address address = addressRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ApiException("Address not found", HttpStatus.NOT_FOUND));

        address.setIsDefault(true);
        Address updated = addressRepository.save(address);

        log.info("AddressService - Default address set: id={}", id);
        return mapToResponse(updated);
    }

    private AddressResponse mapToResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .label(address.getLabel())
                .fullName(address.getFullName())
                .phone(address.getPhone())
                .streetAddress(address.getStreetAddress())
                .city(address.getCity())
                .state(address.getState())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .isDefault(address.getIsDefault())
                .build();
    }
}
