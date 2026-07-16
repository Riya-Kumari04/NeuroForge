package com.springboard.auth_service.config;


import com.springboard.auth_service.constants.ApiEndpoints;
import com.springboard.auth_service.dto.RateLimitRule;
import io.github.bucket4j.Bandwidth;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class RateLimitConfig {


    @Bean
    public Map<String, RateLimitRule> rateLimitRules() {

        Map<String, RateLimitRule> rules = new HashMap<>();

        rules.put(
                ApiEndpoints.LOGIN,
                RateLimitRule.builder()
                        .bandwidth(
                                Bandwidth.builder()
                                        .capacity(5)
                                        .refillGreedy(5,Duration.ofMinutes(1))
                                        .build()
                        )
                        .message("Too many login attempts. Please try again after 1 minute.")
                        .keyGenerator(request -> "login:" + request.getRemoteAddr())
                        .build()
        );
        rules.put(
                ApiEndpoints.REGISTER,
                RateLimitRule.builder()
                        .bandwidth(
                                Bandwidth.builder()
                                        .capacity(10)
                                        .refillGreedy(2,Duration.ofMinutes(10))
                                        .build()
                        )
                        .message("Registration limit exceeded. Please try again after 10 minutes.")
                        .keyGenerator(request -> "register:" + request.getRemoteAddr())
                        .build()
        );
        rules.put(
                ApiEndpoints.SEND_OTP,
                RateLimitRule.builder()
                        .bandwidth(
                                Bandwidth.builder()
                                        .capacity(3)
                                        .refillGreedy(3, Duration.ofMinutes(10))
                                        .build()
                        )
                        .message("OTP request limit exceeded. Please wait 10 minutes before requesting another OTP.")
                        .keyGenerator(request -> "otp:"+ request.getParameter("email"))
                        .build()
        );

        rules.put(
                ApiEndpoints.FORGOT_PASSWORD_SEND_OTP,
                RateLimitRule.builder()
                        .bandwidth(
                                Bandwidth.builder()
                                        .capacity(3)
                                        .refillGreedy(3, Duration.ofMinutes(10))
                                        .build()
                        )
                        .message("OTP request limit exceeded. Please wait 10 minutes before requesting another OTP.")
                        .keyGenerator(request ->
                                "forgot-otp:" + request.getParameter("email")
                        )
                        .build()
        );

        rules.put(
                ApiEndpoints.SET_PASSWORD,
                RateLimitRule.builder()
                        .bandwidth(
                                Bandwidth.builder()
                                        .capacity(5)
                                        .refillGreedy(5, Duration.ofMinutes(10))
                                        .build()
                        )
                        .message("Limit exceeded. Please wait")
                        .keyGenerator(request ->
                                "set-password:" + request.getParameter("email")
                        )
                        .build()
        );

        rules.put(
                ApiEndpoints.RESET_PASSWORD,
                RateLimitRule.builder()
                        .bandwidth(
                                Bandwidth.builder()
                                        .capacity(5)
                                        .refillGreedy(5, Duration.ofMinutes(10))
                                        .build()
                        )
                        .message("Limit exceeded. Please wait")
                        .keyGenerator(request ->
                                "reset-password:" + request.getParameter("email")
                        )
                        .build()
        );

        rules.put(
                "/api/auth/refresh-token",
                RateLimitRule.builder()
                        .bandwidth(
                                Bandwidth.builder()
                                        .capacity(20)
                                        .refillGreedy(20,Duration.ofMinutes(1))
                                        .build()
                        )
                        .keyGenerator(request -> "refresh:"+request.getParameter("email"))
                        .build()


        );
        return rules;
    }

}