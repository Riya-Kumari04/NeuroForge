package com.springboard.auth_service.service.impl;

import com.springboard.auth_service.config.JwtService;
import com.springboard.auth_service.entity.User;
import com.springboard.auth_service.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

import java.io.IOException;
@Service
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException {

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");

        User user = userRepository.findByEmail(email).orElse(null);

        if(user==null){

            user = User.builder()
                    .email(email)
                    .name(name)
                    .enabled(true)
                    .role("ROLE_USER")
                    .password(null)
                    .build();

            userRepository.save(user);
        }

        String jwt = jwtService.generateToken(user);

        ResponseCookie cookie = ResponseCookie.from("access_token", jwt)
                .httpOnly(true)
                .secure(true)      // true in production (HTTPS)
                .path("/")
                .sameSite("Lax")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        response.sendRedirect(frontendUrl + "/dashboard");
//        emailService.sendSetPasswordMail(
//                user.getEmail(),
//                token
//        );

        response.getWriter().write(
                "A password setup link has been sent to your email."
        );
    }
}