package com.neuroforge.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private Long   userId;
    private String name;
    private String email;
    private String username;
    private String role;
    private Long   organizationId;
}
