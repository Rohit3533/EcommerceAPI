package com.api.project.Ecomerce.controller;

import com.api.project.Ecomerce.dto.*;
import com.api.project.Ecomerce.entity.User;
import com.api.project.Ecomerce.repository.UserRepository;
import com.api.project.Ecomerce.response.ApiResponse;
import com.api.project.Ecomerce.security.JwtUtil;
import com.api.project.Ecomerce.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    // ================= REGISTER =================
    @PostMapping("/register")
    public ApiResponse<Void> register(@RequestBody RegisterRequest request) {

        authService.register(
                request.getName(),
                request.getEmail(),
                request.getPassword()
        );

        return ApiResponse.<Void>builder()
                .success(true)
                .message("User registered successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ================= LOGIN =================
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {

        String token = authService.login(
                request.getEmail(),
                request.getPassword()
        );

        User user = userRepository.findByEmail(request.getEmail()).get();

        LoginResponse response = LoginResponse.builder()
                .token(token)
                .role(user.getRole())
                .expiresAt(jwtUtil.extractExpiration(token).toString())
                .build();

        return ApiResponse.<LoginResponse>builder()
                .success(true)
                .message("Login successful")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ================= LOGOUT =================
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);

        authService.logout(token);

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Logged out successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ================= GET CURRENT USER =================
    @GetMapping("/me")
    public ApiResponse<User> getCurrentUser() {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email).orElseThrow();

        user.setPassword(null); // never expose password

        return ApiResponse.<User>builder()
                .success(true)
                .message("User details fetched")
                .data(user)
                .timestamp(LocalDateTime.now())
                .build();
    }
}

