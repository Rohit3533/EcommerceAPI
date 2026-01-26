package com.api.project.Ecomerce.service;

import com.api.project.Ecomerce.entity.Session;
import com.api.project.Ecomerce.entity.User;
import com.api.project.Ecomerce.exception.ApiException;
import com.api.project.Ecomerce.repository.SessionRepository;
import com.api.project.Ecomerce.repository.UserRepository;
import com.api.project.Ecomerce.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Value("${session.timeout.minutes}")
    private long sessionTimeout;

    // ================= REGISTER =================
    public void register(String name, String email, String password) {
        log.info("AuthService - Registering user: {} (name={})", email, name);

        if (userRepository.existsByEmail(email)) {
            log.warn("AuthService - Registration failed, email already registered: {}", email);
            throw new ApiException("Email already registered",
                    HttpStatus.BAD_REQUEST);
        }

        User user = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role("CUSTOMER")
                .status("ACTIVE")
                .build();

        userRepository.save(user);

        log.info("AuthService - User registered successfully: {}", email);
    }

    // ================= LOGIN =================
    public String login(String email, String password) {
        log.info("AuthService - Login attempt for: {}", email);

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
        } catch (AuthenticationException ex) {
            log.warn("AuthService - Authentication failed for {}: {}", email, ex.getMessage());
            throw new ApiException("Invalid credentials",
                    HttpStatus.UNAUTHORIZED);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("AuthService - User not found during login: {}", email);
                    return new ApiException("User not found", HttpStatus.NOT_FOUND);
                });

        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole()
        );

        LocalDateTime now = LocalDateTime.now();

        Session session = Session.builder()
                .user(user)
                .token(token)
                .lastActivityTime(now)
                .expiryTime(now.plusMinutes(sessionTimeout))
                .isActive(true)
                .build();

        sessionRepository.save(session);

        String masked = maskToken(token);
        log.info("AuthService - Login successful for {}: role={}, tokenEnding={}, expiry={}",
                email, user.getRole(), masked, session.getExpiryTime());

        return token;
    }

    // ================= LOGOUT =================
    public void logout(String token) {
        String masked = maskToken(token);
        log.info("AuthService - Logout requested for token ending: {}", masked);

        Session session = sessionRepository
                .findByTokenAndIsActiveTrue(token)
                .orElseThrow(() -> {
                    log.warn("AuthService - Invalid or inactive session for token ending: {}", masked);
                    return new ApiException("Invalid session", HttpStatus.UNAUTHORIZED);
                });

        log.info("AuthService - Found active session for user: {}", session.getUser().getEmail());

        session.setIsActive(false);
        sessionRepository.save(session);

        log.info("AuthService - Session invalidated for user: {} tokenEnding: {}", session.getUser().getEmail(), masked);
    }

    private String maskToken(String token) {
        if (token == null) return "***";
        return token.length() > 4 ? ("***" + token.substring(token.length() - 4)) : "***";
    }
}
