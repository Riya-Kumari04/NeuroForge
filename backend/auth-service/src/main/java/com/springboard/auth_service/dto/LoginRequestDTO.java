package com.springboard.auth_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDTO {

    @Schema(
            description = "Registered email address",
            example = "john@gmail.com"
    )
    @NotBlank
    @Email
    private String email;

    @Schema(
            description = "User password",
            example = "Password@123"
    )
    @NotBlank
    private String password;
}