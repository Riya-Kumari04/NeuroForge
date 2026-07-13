package com.neuroforge.backend.service;

import com.neuroforge.backend.dto.*;

public interface AuthService {
    ApiResponse<Void>          sendOtp(OtpRequest request);
    ApiResponse<Void>          register(RegisterRequest request);
    ApiResponse<LoginResponse> login(LoginRequest request);
    ApiResponse<Void>          logout(String email);
    ApiResponse<Void>          sendForgotPasswordOtp(OtpRequest request);
    ApiResponse<Void>          verifyOtp(VerifyOtpRequest request);
    ApiResponse<Void>          resetPassword(ResetPasswordRequest request);
    ApiResponse<LoginResponse> refreshToken(RefreshTokenRequest request);
    ApiResponse<UserDTO>       me(String email);
}
