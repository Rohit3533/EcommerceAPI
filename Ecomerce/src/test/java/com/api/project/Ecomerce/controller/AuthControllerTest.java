package com.api.project.Ecomerce.controller;

import com.api.project.Ecomerce.dto.RegisterRequest;
import com.api.project.Ecomerce.repository.UserRepository;
import com.api.project.Ecomerce.security.JwtUtil;
import com.api.project.Ecomerce.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @InjectMocks
    private AuthController authController;
    @Mock
    private AuthService authService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtUtil jwtUtil;

    @Test
    public void testRegister() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Test User");
        request.setEmail("Test@gmail.com");
        request.setPassword("password123");
        assertDoesNotThrow(() -> {
            authController.register(request);
        });

    }
}