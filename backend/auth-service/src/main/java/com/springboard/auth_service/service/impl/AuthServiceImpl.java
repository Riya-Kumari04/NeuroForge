package com.springboard.auth_service.service.impl;

import com.springboard.auth_service.config.JwtService;
import com.springboard.auth_service.dto.*;
import com.springboard.auth_service.entity.Role;
import com.springboard.auth_service.entity.User;
import com.springboard.auth_service.exception.InvalidOtpException;
import com.springboard.auth_service.exception.InvalidTokenException;
import com.springboard.auth_service.exception.InvalidUserException;
import com.springboard.auth_service.exception.UserAlreadyExistsException;
import com.springboard.auth_service.repository.UserRepository;
import com.springboard.auth_service.service.AuthService;
import com.springboard.auth_service.service.RedisOtpService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {



    @Value("${app.frontend-url}")
    private String frontendUrl;
    private final JavaMailSender mailSender;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisOtpService redisOtpService;

    @Override
    @Transactional
    public void register(RegistrationDTO registrationDetails) {
        log.info("Registration request received for email={}", registrationDetails.getEmail());
        if (registrationDetails.getInviteToken() != null &&
                !registrationDetails.getInviteToken().isBlank()) {

            registerByInvite(registrationDetails);

        } else {

            registerByOtp(registrationDetails);

        }

        log.info("User registered successfully. email={}", registrationDetails.getEmail());
    }

    private void registerByOtp(RegistrationDTO dto) {

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new UserAlreadyExistsException("User already exists");
        }
        String storedOtp = redisOtpService.getOtp(dto.getEmail());

        if(storedOtp == null)
            throw new InvalidOtpException("OTP expired");

        if(!storedOtp.equals(dto.getOtp()))
            throw new InvalidOtpException("Invalid OTP");


        User user = User.builder()
                .name(dto.getName())
                .password(passwordEncoder.encode(dto.getPassword()))
                .email(dto.getEmail())
                .enabled(true)
                .role(Role.ROLE_USER)
                .build();

        userRepository.save(user);

        redisOtpService.deleteOtp(dto.getEmail());
    }

    private void registerByInvite(RegistrationDTO dto) {

        if (!jwtService.isTokenValid(
                dto.getInviteToken(),
                "INVITE")) {

            throw new InvalidTokenException(
                    "Invalid or expired invitation."
            );
        }

        String email =
                jwtService.extractUsername(
                        dto.getInviteToken());

        String role =
                jwtService.extractClaim(
                        dto.getInviteToken(),
                        claims -> claims.get(
                                "role",
                                String.class
                        )
                );

        if (userRepository.existsByEmail(email)) {

            throw new UserAlreadyExistsException(
                    "User already exists."
            );
        }

        Role userRole = Role.valueOf(role);

        User user = User.builder()
                .name(dto.getName())
                .email(email)
                .password(
                        passwordEncoder.encode(
                                dto.getPassword()))
                .role(userRole)
                .enabled(true)
                .build();

        userRepository.save(user);
    }

    @Transactional
    @Override
    public void sendOtp(String email) {
        log.info("Generating registration OTP for email={}", email);
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException(
                    "User already registered");
        }
        String otp = createAndSaveOtp(email);

        sendEmail(
                email,
                "OTP Verification",
                "Your OTP is: " + otp + "\nValid for 5 minutes."
        );
        log.info("Otp sent to {}",email);
    }

    @Transactional
    @Override
    public void sendForgotPasswordOtp(String email) {
        log.info("Password reset OTP requested for email={}", email);
        userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new InvalidUserException("User not found"));

        String otp = createAndSaveOtp(email);

        sendEmail(
                email,
                "Password Reset OTP",
                "Your OTP for password reset is: "
                        + otp +
                        "\nValid for 5 minutes."
        );
        log.info("Password reset OTP sent successfully for email={}", email);
    }


    @Override
    @Transactional
    public void setPassword(
            HttpServletRequest request,
            String password
    ){

        String token = request
                .getHeader("Authorization")
                .substring(7);

        if(!jwtService.isSetPasswordToken(token)){
            throw new InvalidTokenException(
                    "Invalid token"
            );
        }

        String email =
                jwtService.extractUsername(token);

        User user =
                userRepository.findByEmail(email)
                        .orElseThrow();

        if(user.getPassword()!=null){
            throw new RuntimeException(
                    "Password already set"
            );
        }

        user.setPassword(
                passwordEncoder.encode(password)
        );
    }

    @Transactional
    @Override
    public void resetPassword(ForgotPasswordDTO dto) {
        log.info("Password reset requested for email={}", dto.getEmail());

        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() ->
                        new InvalidUserException("User not found"));

        String storedOtp = redisOtpService.getOtp(dto.getEmail());

        if (storedOtp == null) {
            throw new InvalidOtpException("OTP expired");
        }

        if (!storedOtp.equals(dto.getOtp())) {
            throw new InvalidOtpException("Invalid OTP");
        }

        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        userRepository.save(user);

        redisOtpService.deleteOtp(dto.getEmail());
        log.info("Password reset completed successfully for email={}", dto.getEmail());
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {

        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = (User) authentication.getPrincipal();
        if (!user.isEnabled()) {
            throw new DisabledException("Account is disabled.");
        }

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getACCESS_TOKEN_EXPIRATION())
                .build();

    }

    @Override
    public ApiResponseDTO<LoginResponseDTO> refreshToken(
            RefreshTokenRequestDTO request) {

        String refreshToken = request.getRefreshToken();

        if (!jwtService.isTokenValid(refreshToken,"REFRESH")) {
            throw new InvalidTokenException("Refresh token is invalid.");
        }
        String email = jwtService.extractUsername(refreshToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidUserException("User not found"));

        String accessToken = jwtService.generateToken(user);

        LoginResponseDTO response = LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getACCESS_TOKEN_EXPIRATION())
                .build();

        return ApiResponseDTO.<LoginResponseDTO>builder()
                .success(true)
                .message("Access token refreshed successfully.")
                .data(response)
                .build();
    }


    
    @Override
    public void sendSetPasswordMail(
            String email,
            String token
    ) {

        String link =
                "http://localhost:3000/set-password?token=" + token;

        sendEmail(
                email,
                "Set Your Password",
                """
                Welcome!
    
                Your Google account has been linked successfully.
    
                Click the link below to set your password:
    
                %s
    
                This link expires in 15 minutes.
                """.formatted(link)
        );
    }

    @Override
    @Transactional
    public void inviteUser(InviteRequest dto) {

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new UserAlreadyExistsException(
                    "User already exists"
            );
        }

        String token = jwtService.generateInviteToken(
                dto.getEmail(),
                dto.getRole()
        );

        String link =
                frontendUrl+ "/register?token=" + token;

        sendEmail(
                dto.getEmail(),
                "Invitation",
                """
                Hello,
    
                You have been invited to register.
    
                Click the link below to complete your registration.
    
                %s
    
                This invitation expires in 24 hours.
                """.formatted(link)
        );
    }

    private String createAndSaveOtp(String email) {
        String otp = generateOtp();
        redisOtpService.saveOtp(email, otp);
        return otp;
    }

    private String generateOtp() {
        return String.format(
                "%06d",
                new Random().nextInt(1000000)
        );
    }

    private void sendEmail(
            String email,
            String subject,
            String body
    ) {

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setTo(email);
        message.setSubject(subject);
        message.setText(body);

        try {
            mailSender.send(message);
            log.info("Email sent successfully to {}", email);
        } catch (Exception ex) {
            log.error("Failed to send email to {}", email, ex);
            throw ex;
        }

    }


}