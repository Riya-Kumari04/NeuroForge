package com.springboard.auth_service.service;

import com.springboard.auth_service.dto.*;
import com.springboard.auth_service.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

public interface AuthService {
    void register(RegistrationDTO register);

    @Transactional
    void sendOtp(String email);

    @Transactional
    void sendForgotPasswordOtp(String email);


    @Transactional
    void setPassword(HttpServletRequest request,String password);

    @Transactional
    void resetPassword(ForgotPasswordDTO dto);

    LoginResponseDTO login(LoginRequestDTO request);

    ApiResponseDTO<LoginResponseDTO> refreshToken(
            RefreshTokenRequestDTO request);

    void sendSetPasswordMail(
            String email,
            String token
    );
}
