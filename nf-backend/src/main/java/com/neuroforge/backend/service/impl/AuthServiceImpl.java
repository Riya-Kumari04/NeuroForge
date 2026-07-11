package com.neuroforge.backend.service.impl;

import com.neuroforge.backend.dto.*;
import com.neuroforge.backend.entity.Otp;
import com.neuroforge.backend.entity.User;
import com.neuroforge.backend.exception.AppException;
import com.neuroforge.backend.repository.OtpRepository;
import com.neuroforge.backend.repository.UserRepository;
import com.neuroforge.backend.security.JwtUtil;
import com.neuroforge.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository     userRepository;
    private final OtpRepository      otpRepository;
    private final PasswordEncoder    passwordEncoder;
    private final JwtUtil            jwtUtil;
    private final AuthenticationManager authManager;
    private final JavaMailSender     mailSender;

    // ── Send OTP for registration ─────────────────────────────────────────────

    @Override
    @Transactional
    public ApiResponse<Void> sendOtp(OtpRequest request) {
        String email = request.getEmail();
        if (userRepository.existsByEmail(email)) {
            throw AppException.conflict("Email is already registered");
        }
        generateAndSendOtp(email, "NeuroForge — Email Verification",
                "Your registration OTP is: %s\nThis code expires in 5 minutes.");
        return ApiResponse.ok("OTP sent to " + email);
    }

    // ── Register ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ApiResponse<Void> register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw AppException.conflict("Email is already registered");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw AppException.conflict("Username is already taken");
        }

        verifyOtpCode(request.getEmail(), request.getOtp());

        User user = User.builder()
                .name(request.getName())
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .organizationId(request.getOrganizationId())
                .enabled(true)   // verified via OTP
                .build();

        userRepository.save(user);
        otpRepository.deleteAllByEmail(request.getEmail());

        return ApiResponse.ok("Registration successful. You can now log in.");
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @Override
    public ApiResponse<LoginResponse> login(LoginRequest request) {
        // Throws BadCredentialsException / DisabledException — caught by GlobalExceptionHandler
        authManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> AppException.notFound("User not found"));

        LoginResponse resp = LoginResponse.builder()
                .accessToken(jwtUtil.generateAccessToken(user))
                .refreshToken(jwtUtil.generateRefreshToken(user))
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .username(user.getUsername())
                .role(user.getRole())
                .organizationId(user.getOrganizationId())
                .build();

        return ApiResponse.ok("Login successful", resp);
    }

    // ── Logout (stateless — just an ack) ─────────────────────────────────────

    @Override
    public ApiResponse<Void> logout(String email) {
        return ApiResponse.ok("Logged out successfully");
    }

    // ── Forgot password OTP ───────────────────────────────────────────────────

    @Override
    @Transactional
    public ApiResponse<Void> sendForgotPasswordOtp(OtpRequest request) {
        String email = request.getEmail();
        if (!userRepository.existsByEmail(email)) {
            // Don't reveal whether the email exists
            return ApiResponse.ok("If that email is registered, an OTP has been sent.");
        }
        generateAndSendOtp(email, "NeuroForge — Password Reset",
                "Your password reset OTP is: %s\nThis code expires in 5 minutes.");
        return ApiResponse.ok("OTP sent to " + email);
    }

    // ── Verify OTP only (for multi-step flows) ────────────────────────────────

    @Override
    public ApiResponse<Void> verifyOtp(VerifyOtpRequest request) {
        verifyOtpCode(request.getEmail(), request.getOtp());
        return ApiResponse.ok("OTP verified successfully");
    }

    // ── Reset password ────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ApiResponse<Void> resetPassword(ResetPasswordRequest request) {
        verifyOtpCode(request.getEmail(), request.getOtp());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> AppException.notFound("User not found"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        otpRepository.deleteAllByEmail(request.getEmail());

        return ApiResponse.ok("Password reset successful. You can now log in.");
    }

    // ── Refresh token ─────────────────────────────────────────────────────────

    @Override
    public ApiResponse<LoginResponse> refreshToken(RefreshTokenRequest request) {
        String token = request.getRefreshToken();
        if (!jwtUtil.isTokenValid(token)) {
            throw AppException.unauthorized("Invalid or expired refresh token");
        }
        String email = jwtUtil.extractEmail(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> AppException.notFound("User not found"));

        LoginResponse resp = LoginResponse.builder()
                .accessToken(jwtUtil.generateAccessToken(user))
                .refreshToken(jwtUtil.generateRefreshToken(user))
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .username(user.getUsername())
                .role(user.getRole())
                .organizationId(user.getOrganizationId())
                .build();

        return ApiResponse.ok("Token refreshed", resp);
    }

    // ── Me ────────────────────────────────────────────────────────────────────

    @Override
    public ApiResponse<UserDTO> me(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> AppException.notFound("User not found"));
        return ApiResponse.ok("User details", UserDTO.from(user));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void generateAndSendOtp(String email, String subject, String bodyTemplate) {
        otpRepository.deleteAllByEmail(email);

        String code = String.format("%06d", new Random().nextInt(1_000_000));
        Otp otp = Otp.builder()
                .email(email)
                .otp(code)
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .build();
        otpRepository.save(otp);

        sendEmail(email, subject, String.format(bodyTemplate, code));
        log.info("OTP sent to {}", email);
    }

    private void verifyOtpCode(String email, String code) {
        Otp otp = otpRepository.findTopByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> AppException.badRequest("No OTP found for this email. Please request one."));

        if (otp.isUsed())    throw AppException.badRequest("OTP has already been used");
        if (otp.isExpired()) throw AppException.badRequest("OTP has expired. Please request a new one.");
        if (!otp.getOtp().equals(code)) throw AppException.badRequest("Invalid OTP");

        otp.setUsed(true);
        otpRepository.save(otp);
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            throw AppException.badRequest("Failed to send email. Please try again.");
        }
    }
}
