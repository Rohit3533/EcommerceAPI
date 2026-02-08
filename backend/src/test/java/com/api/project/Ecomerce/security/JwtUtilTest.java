//package com.api.project.Ecomerce.security;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.runner.RunWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.MockitoJUnitRunner;
//
//
//@RunWith(MockitoJUnitRunner.class)
//class JwtUtilTest {
//
//    @Mock
//    private JwtUtil jwtUtil;
//
//    @BeforeEach
//    void setup() {
//        jwtUtil = new JwtUtil();
//    }
//   @Test
//    public void testJwt() {
//        String token = jwtUtil.generateToken("test@gmail.com", "CUSTOMER");
//        System.out.println("TOKEN: " + token);
//        System.out.println("EMAIL: " + jwtUtil.extractEmail(token));
//    }
//
//
//}