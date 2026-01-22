package com.api.project.Ecomerce.service;

import com.api.project.Ecomerce.entity.Session;
import com.api.project.Ecomerce.entity.User;
import com.api.project.Ecomerce.exception.ApiException;
import com.api.project.Ecomerce.repository.SessionRepository;
import com.api.project.Ecomerce.repository.UserRepository;
import com.api.project.Ecomerce.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
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

        if (userRepository.existsByEmail(email)) {
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
    }

    // ================= LOGIN =================
    public String login(String email, String password) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
        } catch (AuthenticationException ex) {
            throw new ApiException("Invalid credentials",
                    HttpStatus.UNAUTHORIZED);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ApiException("User not found",
                                HttpStatus.NOT_FOUND));

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

        return token;
    }

    // ================= LOGOUT =================
    public void logout(String token) {

        Session session = sessionRepository
                .findByTokenAndIsActiveTrue(token)
                .orElseThrow(() ->
                        new ApiException("Invalid session",
                                HttpStatus.UNAUTHORIZED));

        session.setIsActive(false);
        sessionRepository.save(session);
    }
}

