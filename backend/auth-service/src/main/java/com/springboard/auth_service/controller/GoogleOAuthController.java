package com.springboard.auth_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Google OAuth")
public class GoogleOAuthController {

    @GetMapping("/google-login")
    @Operation(summary = "Login using Google")
    public String login() {
        return "redirect:/oauth2/authorization/google";
    }

}