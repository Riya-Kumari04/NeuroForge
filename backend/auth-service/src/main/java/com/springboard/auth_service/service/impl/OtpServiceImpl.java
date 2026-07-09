package com.springboard.auth_service.service.impl;

import com.springboard.auth_service.dto.ForgotPasswordDTO;
import com.springboard.auth_service.entity.Otp;
import com.springboard.auth_service.entity.User;
import com.springboard.auth_service.exception.InvalidOtpException;
import com.springboard.auth_service.exception.InvalidUserException;
import com.springboard.auth_service.repository.OtpRepository;
import com.springboard.auth_service.repository.UserRepository;
import com.springboard.auth_service.service.OtpService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final JavaMailSender mailSender;
    private final OtpRepository otpRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;




}