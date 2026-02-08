package com.api.project.Ecomerce.controller;

import com.api.project.Ecomerce.dto.UpdateUserRoleRequest;
import com.api.project.Ecomerce.dto.UpdateUserStatusRequest;
import com.api.project.Ecomerce.dto.UserAdminResponse;
import com.api.project.Ecomerce.entity.User;
import com.api.project.Ecomerce.exception.ApiException;
import com.api.project.Ecomerce.repository.UserRepository;
import com.api.project.Ecomerce.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
public class AdminUserController {

    private final UserRepository userRepository;

    // ================= LIST ALL USERS =================
    @GetMapping
    public ApiResponse<List<UserAdminResponse>> getAllUsers() {

        log.info("AdminUserController - getAllUsers called");

        // #region agent log
        try {
            java.nio.file.Files.writeString(
                    java.nio.file.Paths.get("c:\\Users\\Rohit\\Downloads\\Ecomerce\\.cursor\\debug.log"),
                    "{\"sessionId\":\"debug-session\",\"runId\":\"pre-fix\",\"hypothesisId\":\"H1\",\"location\":\"AdminUserController.java:getAllUsers\",\"message\":\"entry\",\"data\":null,\"timestamp\":"
                            + System.currentTimeMillis() + "}\n",
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND);
        } catch (Exception ignored) {
        }
        // #endregion

        List<User> users = userRepository.findAll();

        List<UserAdminResponse> responses = users.stream()
                .map(this::mapToAdminResponse)
                .collect(Collectors.toList());

        log.info("AdminUserController - getAllUsers returned count={}", responses.size());

        return ApiResponse.<List<UserAdminResponse>>builder()
                .success(true)
                .message("Users fetched successfully")
                .data(responses)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ================= UPDATE ROLE =================
    @PutMapping("/{id}/role")
    public ApiResponse<UserAdminResponse> updateUserRole(
            @PathVariable Long id,
            @RequestBody UpdateUserRoleRequest request) {

        log.info("AdminUserController - updateUserRole called: userId={}, role={}", id,
                request == null ? "null" : request.getRole());

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("AdminUserController - User not found: id={}", id);
                    return new ApiException("User not found", HttpStatus.NOT_FOUND);
                });

        String role = normalize(request.getRole());
        if (!"ADMIN".equals(role) && !"CUSTOMER".equals(role)) {
            log.warn("AdminUserController - Invalid role provided for userId={}: {}", id, request.getRole());
            throw new ApiException("Role must be ADMIN or CUSTOMER", HttpStatus.BAD_REQUEST);
        }

        user.setRole(role);
        User saved = userRepository.save(user);

        log.info("AdminUserController - User role updated: id={}, role={}", saved.getId(), saved.getRole());

        return ApiResponse.<UserAdminResponse>builder()
                .success(true)
                .message("User role updated successfully")
                .data(mapToAdminResponse(saved))
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ================= UPDATE STATUS =================
    @PutMapping("/{id}/status")
    public ApiResponse<UserAdminResponse> updateUserStatus(
            @PathVariable Long id,
            @RequestBody UpdateUserStatusRequest request) {

        log.info("AdminUserController - updateUserStatus called: userId={}, status={}", id,
                request == null ? "null" : request.getStatus());

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("AdminUserController - User not found: id={}", id);
                    return new ApiException("User not found", HttpStatus.NOT_FOUND);
                });

        String status = normalize(request.getStatus());
        if (!"ACTIVE".equals(status) && !"BLOCKED".equals(status)) {
            log.warn("AdminUserController - Invalid status provided for userId={}: {}", id, request.getStatus());
            throw new ApiException("Status must be ACTIVE or BLOCKED", HttpStatus.BAD_REQUEST);
        }

        user.setStatus(status);
        User saved = userRepository.save(user);

        log.info("AdminUserController - User status updated: id={}, status={}", saved.getId(), saved.getStatus());

        return ApiResponse.<UserAdminResponse>builder()
                .success(true)
                .message("User status updated successfully")
                .data(mapToAdminResponse(saved))
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ================= HELPERS =================
    private UserAdminResponse mapToAdminResponse(User user) {
        return UserAdminResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .status(user.getStatus())
                .emailVerified(user.getEmailVerified())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
