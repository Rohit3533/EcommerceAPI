package com.api.project.Ecomerce.security;

import com.api.project.Ecomerce.entity.Session;
import com.api.project.Ecomerce.entity.User;
import com.api.project.Ecomerce.repository.SessionRepository;
import com.api.project.Ecomerce.repository.UserRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;

    @Value("${session.timeout.minutes}")
    private long sessionTimeout;

    public JwtAuthenticationFilter(JwtUtil jwtUtil,
                                   UserRepository userRepository,
                                   SessionRepository sessionRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.validateToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        String email = jwtUtil.extractEmail(token);

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            filterChain.doFilter(request, response);
            return;
        }

        Session session = sessionRepository
                .findByTokenAndIsActiveTrue(token)
                .orElse(null);

        if (session == null) {
            filterChain.doFilter(request, response);
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        // Idle timeout check
        if (now.isAfter(session.getExpiryTime())) {
            session.setIsActive(false);
            sessionRepository.save(session);
            filterChain.doFilter(request, response);
            return;
        }

        // Update activity time
        session.setLastActivityTime(now);
        session.setExpiryTime(now.plusMinutes(sessionTimeout));
        sessionRepository.save(session);

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singleton(() ->
                        "ROLE_" + user.getRole())
        );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}

