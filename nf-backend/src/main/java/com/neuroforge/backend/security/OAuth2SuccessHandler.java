package com.neuroforge.backend.security;

import com.neuroforge.backend.entity.User;
import com.neuroforge.backend.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "spring.security.oauth2.client.registration.google", name = "client-id")
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oauthUser = oauthToken.getPrincipal();

        Map<String, Object> attributes = oauthUser.getAttributes();
        String googleId = attributes.get("sub") != null ? attributes.get("sub").toString() : null;
        String email = attributes.get("email") != null ? attributes.get("email").toString() : null;
        String name = attributes.get("name") != null ? attributes.get("name").toString() : null;
        String picture = attributes.get("picture") != null ? attributes.get("picture").toString() : null;

        log.info("OAuth2 login attempt - Google ID: {}, Email: {}", googleId, email);

        if (email == null) {
            log.error("OAuth2 login failed: No email in Google response");
            response.sendRedirect("http://localhost:5000/login?error=no_email");
            return;
        }

        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null) {
            // Existing user - authenticate them
            if (!"GOOGLE".equals(user.getAuthenticationProvider()) && !googleId.equals(user.getGoogleProviderId())) {
                // User exists with LOCAL auth and different Google ID - prevent account takeover
                log.warn("OAuth2 login failed: Email {} already registered with LOCAL auth", email);
                response.sendRedirect("http://localhost:5000/login?error=account_exists_local");
                return;
            }

            // Check approval status for existing users
            String approvalStatus = user.getApprovalStatus();
            if ("PENDING".equals(approvalStatus)) {
                log.warn("OAuth2 login failed: User {} has PENDING approval status", email);
                response.sendRedirect("http://localhost:5000/login?error=pending_approval");
                return;
            }
            if ("REJECTED".equals(approvalStatus)) {
                log.warn("OAuth2 login failed: User {} has REJECTED approval status", email);
                response.sendRedirect("http://localhost:5000/login?error=rejected");
                return;
            }

            // Update Google provider info if not set
            if (user.getGoogleProviderId() == null) {
                user.setGoogleProviderId(googleId);
                user.setAuthenticationProvider("GOOGLE");
                userRepository.save(user);
            }

            log.info("OAuth2 login successful for existing user: {}", email);
            generateTokenAndRedirect(response, user);
        } else {
            // New user - create account
            log.info("Creating new user from OAuth2: {}", email);
            User newUser = User.builder()
                    .username(email.split("@")[0] + "_" + System.currentTimeMillis())
                    .email(email)
                    .name(name != null ? name : email.split("@")[0])
                    .password("") // OAuth users don't need password
                    .role("ROLE_DEVELOPER") // Default role for OAuth users
                    .authenticationProvider("GOOGLE")
                    .googleProviderId(googleId)
                    .avatarUrl(picture)
                    .enabled(true)
                    .approvalStatus("APPROVED") // OAuth users are auto-approved
                    .build();

            user = userRepository.save(newUser);
            log.info("New OAuth user created: {}", email);
            generateTokenAndRedirect(response, user);
        }
    }

    private void generateTokenAndRedirect(HttpServletResponse response, User user) throws IOException {
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        String redirectUrl = String.format(
                "http://localhost:5000/oauth2/success?token=%s&refresh=%s&userId=%s&name=%s&email=%s&role=%s",
                accessToken,
                refreshToken,
                user.getId(),
                java.net.URLEncoder.encode(user.getName(), "UTF-8"),
                java.net.URLEncoder.encode(user.getEmail(), "UTF-8"),
                user.getRole()
        );

        response.sendRedirect(redirectUrl);
    }
}
