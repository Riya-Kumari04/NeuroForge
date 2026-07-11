package com.springboard.auth_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponseDTO {

    @Schema(
            description = "JWT Access Token"
    )
    private String accessToken;

    @Schema(
            description = "JWT Refresh Token"
    )
    private String refreshToken;

    @Schema(
            description = "Token type",
            example = "Bearer"
    )
    private String tokenType;

    @Schema(
            description = "Access token expiry time in seconds",
            example = "3600"
    )
    private Long expiresIn;
}