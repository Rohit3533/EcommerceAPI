package com.api.project.Ecomerce.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${spring.application.name:ShopHub}")
    private String appName;

    /**
     * Send OTP verification email with styled HTML body
     */
    @Async
    public void sendOtpEmail(String toEmail, String name, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("🔐 Verify Your Email - " + appName);
            helper.setText(buildOtpEmailHtml(name, otp), true);

            mailSender.send(message);
            log.info("EmailService - OTP email sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("EmailService - Failed to send OTP email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    /**
     * Send welcome email after successful registration
     */
    @Async
    public void sendWelcomeEmail(String toEmail, String name) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("🎉 Welcome to " + appName + "!");
            helper.setText(buildWelcomeEmailHtml(name), true);

            mailSender.send(message);
            log.info("EmailService - Welcome email sent to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("EmailService - Failed to send welcome email to {}: {}", toEmail, e.getMessage());
        }
    }

    private String buildOtpEmailHtml(String name, String otp) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                </head>
                <body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #1a1a2e;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 40px 20px;">
                        <!-- Header -->
                        <div style="text-align: center; margin-bottom: 40px;">
                            <h1 style="color: #8b5cf6; font-size: 32px; margin: 0;">🛒 %s</h1>
                        </div>

                        <!-- Main Card -->
                        <div style="background: linear-gradient(145deg, #16213e 0%%, #1a1a2e 100%%); border-radius: 16px; padding: 40px; border: 1px solid rgba(139, 92, 246, 0.2); box-shadow: 0 20px 40px rgba(0,0,0,0.3);">
                            <h2 style="color: #ffffff; font-size: 24px; margin: 0 0 20px 0; text-align: center;">
                                Verify Your Email Address
                            </h2>

                            <p style="color: #a0a0a0; font-size: 16px; line-height: 1.6; margin: 0 0 30px 0; text-align: center;">
                                Hi <strong style="color: #ffffff;">%s</strong>,<br><br>
                                Welcome to %s! To complete your registration, please use the verification code below:
                            </p>

                            <!-- OTP Box -->
                            <div style="background: linear-gradient(135deg, #8b5cf6 0%%, #7c3aed 100%%); border-radius: 12px; padding: 24px; text-align: center; margin: 0 0 30px 0;">
                                <p style="color: rgba(255,255,255,0.8); font-size: 14px; margin: 0 0 10px 0; letter-spacing: 1px;">YOUR VERIFICATION CODE</p>
                                <p style="color: #ffffff; font-size: 36px; font-weight: bold; margin: 0; letter-spacing: 8px; font-family: 'Courier New', monospace;">%s</p>
                            </div>

                            <p style="color: #a0a0a0; font-size: 14px; line-height: 1.6; margin: 0 0 20px 0; text-align: center;">
                                ⏰ This code expires in <strong style="color: #f59e0b;">10 minutes</strong>
                            </p>

                            <div style="background: rgba(239, 68, 68, 0.1); border: 1px solid rgba(239, 68, 68, 0.2); border-radius: 8px; padding: 16px; margin-top: 20px;">
                                <p style="color: #ef4444; font-size: 13px; margin: 0; text-align: center;">
                                    ⚠️ If you didn't request this code, please ignore this email.
                                </p>
                            </div>
                        </div>

                        <!-- Footer -->
                        <div style="text-align: center; margin-top: 40px;">
                            <p style="color: #666; font-size: 12px; margin: 0;">
                                © 2024 %s. All rights reserved.
                            </p>
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(appName, name, appName, otp, appName);
    }

    private String buildWelcomeEmailHtml(String name) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                </head>
                <body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #1a1a2e;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 40px 20px;">
                        <div style="text-align: center; margin-bottom: 40px;">
                            <h1 style="color: #8b5cf6; font-size: 32px; margin: 0;">🛒 %s</h1>
                        </div>

                        <div style="background: linear-gradient(145deg, #16213e 0%%, #1a1a2e 100%%); border-radius: 16px; padding: 40px; border: 1px solid rgba(139, 92, 246, 0.2);">
                            <div style="text-align: center; font-size: 48px; margin-bottom: 20px;">🎉</div>
                            <h2 style="color: #ffffff; font-size: 24px; margin: 0 0 20px 0; text-align: center;">
                                Welcome to %s!
                            </h2>

                            <p style="color: #a0a0a0; font-size: 16px; line-height: 1.6; margin: 0 0 30px 0; text-align: center;">
                                Hi <strong style="color: #ffffff;">%s</strong>,<br><br>
                                Your account has been successfully created. You can now start shopping and enjoy exclusive deals!
                            </p>

                            <div style="text-align: center;">
                                <p style="color: #8b5cf6; font-size: 14px; margin: 0;">
                                    Happy Shopping! 🛍️
                                </p>
                            </div>
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(appName, appName, name);
    }
}
