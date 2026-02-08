package com.api.project.Ecomerce.service;

import com.api.project.Ecomerce.dto.InitiateRegistrationRequest;
import com.api.project.Ecomerce.entity.OtpVerification;
import com.api.project.Ecomerce.entity.User;
import com.api.project.Ecomerce.exception.ApiException;
import com.api.project.Ecomerce.repository.OtpVerificationRepository;
import com.api.project.Ecomerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final OtpVerificationRepository otpRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${otp.expiry.minutes:10}")
    private int otpExpiryMinutes;

    private static final SecureRandom random = new SecureRandom();

    /**
     * Generate a 6-digit OTP
     */
    public String generateOtp() {
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    /**
     * Initiate registration by storing temp data and sending OTP
     */
    @Transactional
    public void initiateRegistration(InitiateRegistrationRequest request) {
        log.info("OtpService - Initiating registration for: {}", request.getEmail());

        // Check if email already registered
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException("Email already registered", HttpStatus.BAD_REQUEST);
        }

        // Delete any existing pending verification for this email
        otpRepository.deleteByEmail(request.getEmail());

        // Generate OTP
        String otp = generateOtp();
        log.info("OtpService - Generated OTP for {}: {}", request.getEmail(), otp);

        // Create verification record
        OtpVerification verification = OtpVerification.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .emailOtp(otp)
                .emailVerified(false)
                .otpSentAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(otpExpiryMinutes))
                .build();

        otpRepository.save(verification);

        // Send OTP email
        emailService.sendOtpEmail(request.getEmail(), request.getName(), otp);

        log.info("OtpService - Registration initiated, OTP sent to: {}", request.getEmail());
    }

    /**
     * Resend OTP to email
     */
    @Transactional
    public void resendOtp(String email) {
        log.info("OtpService - Resending OTP to: {}", email);

        OtpVerification verification = otpRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("No pending registration found", HttpStatus.NOT_FOUND));

        // Generate new OTP
        String newOtp = generateOtp();
        verification.setEmailOtp(newOtp);
        verification.setOtpSentAt(LocalDateTime.now());
        verification.setExpiresAt(LocalDateTime.now().plusMinutes(otpExpiryMinutes));

        otpRepository.save(verification);

        // Send new OTP
        emailService.sendOtpEmail(email, verification.getName(), newOtp);

        log.info("OtpService - New OTP sent to: {}", email);
    }

    /**
     * Verify email OTP and complete registration
     */
    @Transactional
    public void verifyOtpAndRegister(String email, String otp) {
        log.info("OtpService - Verifying OTP for: {}", email);

        OtpVerification verification = otpRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("No pending registration found", HttpStatus.NOT_FOUND));

        // Check if expired
        if (verification.isExpired()) {
            log.warn("OtpService - OTP expired for: {}", email);
            throw new ApiException("OTP has expired. Please request a new one.", HttpStatus.BAD_REQUEST);
        }

        // Verify OTP
        if (!verification.getEmailOtp().equals(otp)) {
            log.warn("OtpService - Invalid OTP provided for: {}", email);
            throw new ApiException("Invalid OTP", HttpStatus.BAD_REQUEST);
        }

        // Create actual user
        User user = User.builder()
                .name(verification.getName())
                .email(verification.getEmail())
                .password(verification.getPassword()) // Already encoded
                .phone(verification.getPhone())
                .role("CUSTOMER")
                .status("ACTIVE")
                .emailVerified(true)
                .build();

        userRepository.save(user);

        // Delete verification record
        otpRepository.delete(verification);

        // Send welcome email
        emailService.sendWelcomeEmail(email, verification.getName());

        log.info("OtpService - Registration completed successfully for: {}", email);
    }

    /**
     * Cleanup expired verification records
     */
    @Transactional
    public void cleanupExpired() {
        otpRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        log.info("OtpService - Cleaned up expired OTP verifications");
    }
}
