package com.springboard.auth_service.feign;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("EMAIL-SERVICE")
public interface EmailFeign {

    void sendEmail(String email,
                   String subject,
                   String body);
}
