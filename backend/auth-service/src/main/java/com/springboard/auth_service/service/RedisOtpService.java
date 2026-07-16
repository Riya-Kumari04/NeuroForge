package com.springboard.auth_service.service;


import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisOtpService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final Duration OTP_EXPIRY = Duration.ofMinutes(5);

    public void saveOtp(String email, String otp) {

        redisTemplate.opsForValue()
                .set("otp:" + email.toLowerCase(), otp, OTP_EXPIRY);
    }

    public String getOtp(String email) {

        Object otp = redisTemplate.opsForValue()
                .get("otp:" + email.toLowerCase());

        return otp == null ? null : otp.toString();
    }

    public void deleteOtp(String email) {

        redisTemplate.delete("otp:" + email);
    }
}