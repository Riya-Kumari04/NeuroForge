package com.neuroforge.backend.controller;

import com.neuroforge.backend.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/oauth2")
@RequiredArgsConstructor
public class OAuthController {

    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String googleClientId;

    @GetMapping("/google/config")
    public ApiResponse<Map<String, String>> getGoogleOAuthConfig() {
        if (googleClientId == null || googleClientId.isEmpty()) {
            return ApiResponse.fail("Google OAuth is not configured");
        }
        return ApiResponse.ok("Google OAuth configured", Map.of(
                "clientId", googleClientId,
                "authUrl", "https://accounts.google.com/o/oauth2/v2/auth",
                "redirectUri", "http://localhost:5000/oauth2/callback/google"
        ));
    }
}
