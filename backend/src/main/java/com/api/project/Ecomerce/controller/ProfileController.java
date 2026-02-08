package com.api.project.Ecomerce.controller;

import com.api.project.Ecomerce.dto.AddressResponse;
import com.api.project.Ecomerce.dto.UpdateProfileRequest;
import com.api.project.Ecomerce.dto.UserProfileResponse;
import com.api.project.Ecomerce.entity.User;
import com.api.project.Ecomerce.exception.ApiException;
import com.api.project.Ecomerce.repository.UserRepository;
import com.api.project.Ecomerce.response.ApiResponse;
import com.api.project.Ecomerce.service.AddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
public class ProfileController {

    private final UserRepository userRepository;
    private final AddressService addressService;

    // ================= GET PROFILE =================
    @GetMapping
    public ApiResponse<UserProfileResponse> getProfile() {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        log.info("ProfileController - getProfile called for {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("ProfileController - User not found: {}", email);
                    return new ApiException("User not found", HttpStatus.NOT_FOUND);
                });

        return ApiResponse.<UserProfileResponse>builder()
                .success(true)
                .message("Profile fetched successfully")
                .data(mapToProfileResponse(user))
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ================= UPDATE PROFILE =================
    @PutMapping
    public ApiResponse<UserProfileResponse> updateProfile(
            @RequestBody UpdateProfileRequest request) {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        log.info("ProfileController - updateProfile called for {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("ProfileController - User not found for update: {}", email);
                    return new ApiException("User not found", HttpStatus.NOT_FOUND);
                });

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName().trim());
        }

        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            user.setPhone(request.getPhone().trim());
        }

        User saved = userRepository.save(user);

        log.info("ProfileController - Profile updated for userId={}", saved.getId());

        return ApiResponse.<UserProfileResponse>builder()
                .success(true)
                .message("Profile updated successfully")
                .data(mapToProfileResponse(saved))
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ================= HELPER =================
    private UserProfileResponse mapToProfileResponse(User user) {
        List<AddressResponse> addresses = addressService.getAddresses();

        return UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .status(user.getStatus())
                .emailVerified(user.getEmailVerified())
                .addresses(addresses)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
