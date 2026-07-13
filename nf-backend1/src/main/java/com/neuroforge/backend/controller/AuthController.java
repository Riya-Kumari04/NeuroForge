package com.neuroforge.backend.controller;

import com.neuroforge.backend.dto.*;
import com.neuroforge.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication APIs")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/send-otp")
    @Operation(summary = "Send OTP for registration")
    public ResponseEntity<ApiResponse<Void>> sendOtp(@Valid @RequestBody OtpRequest req) {
        return ResponseEntity.ok(authService.sendOtp(req));
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user (requires OTP verification)")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(authService.register(req));
    }

    @PostMapping("/login")
    @Operation(summary = "Login and receive JWT tokens")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout (client should discard tokens)")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(authService.logout(user.getUsername()));
    }

    @PostMapping("/forgot-password/send-otp")
    @Operation(summary = "Send OTP for password reset")
    public ResponseEntity<ApiResponse<Void>> forgotPasswordOtp(@Valid @RequestBody OtpRequest req) {
        return ResponseEntity.ok(authService.sendForgotPasswordOtp(req));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP code")
    public ResponseEntity<ApiResponse<Void>> verifyOtp(@Valid @RequestBody VerifyOtpRequest req) {
        return ResponseEntity.ok(authService.verifyOtp(req));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using OTP")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        return ResponseEntity.ok(authService.resetPassword(req));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh access token")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(@Valid @RequestBody RefreshTokenRequest req) {
        return ResponseEntity.ok(authService.refreshToken(req));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current logged-in user")
    public ResponseEntity<ApiResponse<UserDTO>> me(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(authService.me(user.getUsername()));
    }
}
