package com.api.project.Ecomerce.controller;

import com.api.project.Ecomerce.dto.*;
import com.api.project.Ecomerce.entity.User;
import com.api.project.Ecomerce.repository.UserRepository;
import com.api.project.Ecomerce.response.ApiResponse;
import com.api.project.Ecomerce.security.JwtUtil;
import com.api.project.Ecomerce.service.AuthService;
import com.api.project.Ecomerce.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

        private final AuthService authService;
        private final OtpService otpService;
        private final UserRepository userRepository;
        private final JwtUtil jwtUtil;

        // ================= INITIATE REGISTRATION (NEW - with OTP) =================
        @PostMapping("/register/initiate")
        public ApiResponse<Void> initiateRegistration(@RequestBody InitiateRegistrationRequest request) {
                log.info("AuthController - Initiating registration for: {}", request.getEmail());
                otpService.initiateRegistration(request);
                return ApiResponse.<Void>builder()
                                .success(true)
                                .message("Verification code sent to your email")
                                .timestamp(LocalDateTime.now())
                                .build();
        }

        // ================= VERIFY OTP AND COMPLETE REGISTRATION =================
        @PostMapping("/register/verify")
        public ApiResponse<Void> verifyOtpAndRegister(@RequestBody VerifyOtpRequest request) {
                log.info("AuthController - Verifying OTP for: {}", request.getEmail());
                otpService.verifyOtpAndRegister(request.getEmail(), request.getOtp());
                return ApiResponse.<Void>builder()
                                .success(true)
                                .message("Email verified! Registration complete.")
                                .timestamp(LocalDateTime.now())
                                .build();
        }

        // ================= RESEND OTP =================
        @PostMapping("/register/resend-otp")
        public ApiResponse<Void> resendOtp(@RequestBody Map<String, String> request) {
                String email = request.get("email");
                log.info("AuthController - Resending OTP to: {}", email);
                otpService.resendOtp(email);
                return ApiResponse.<Void>builder()
                                .success(true)
                                .message("New verification code sent")
                                .timestamp(LocalDateTime.now())
                                .build();
        }

        // ================= LEGACY REGISTER (Keep for backward compatibility)
        // =================
        @PostMapping("/register")
        public ApiResponse<Void> register(@RequestBody RegisterRequest request) {
                log.info("AuthController - Legacy registration for: {}", request.getEmail());
                authService.register(
                                request.getName(),
                                request.getEmail(),
                                request.getPassword(),
                                request.getPhone());
                log.info("AuthController - User registered successfully: {}", request.getEmail());
                return ApiResponse.<Void>builder()
                                .success(true)
                                .message("User registered successfully")
                                .timestamp(LocalDateTime.now())
                                .build();
        }

        // ================= LOGIN =================
        @PostMapping("/login")
        public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
                log.info("AuthController - Login attempt: {}", request.getEmail());

                String token = authService.login(
                                request.getEmail(),
                                request.getPassword());

                User user = userRepository.findByEmail(request.getEmail()).get();

                String expiresAt = jwtUtil.extractExpiration(token).toString();

                LoginResponse response = LoginResponse.builder()
                                .token(token)
                                .role(user.getRole())
                                .expiresAt(expiresAt)
                                .build();

                log.info("AuthController - Login successful for {}: role={}, expiresAt={}",
                                request.getEmail(), user.getRole(), expiresAt);

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
                String masked = token.length() > 4 ? ("***" + token.substring(token.length() - 4)) : "***";

                log.info("AuthController - Logout requested for token ending: {}", masked);

                authService.logout(token);

                log.info("AuthController - Logged out token ending: {}", masked);

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

                log.info("AuthController - Fetching current user from SecurityContext: {}", email);

                User user = userRepository.findByEmail(email).orElseThrow();

                user.setPassword(null); // never expose password

                log.info("AuthController - User details fetched for: {}", email);

                return ApiResponse.<User>builder()
                                .success(true)
                                .message("User details fetched")
                                .data(user)
                                .timestamp(LocalDateTime.now())
                                .build();
        }
}
