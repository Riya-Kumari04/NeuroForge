package com.neuroforge.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 30, message = "Username must be 3–30 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    /**
     * Role is optional for normal registration (required if no invitation).
     * Role validation is performed in the service layer to distinguish between
     * invitation-based registration (allows admin roles) and normal registration
     * (only allows DEVELOPER, QA, CLIENT).
     */
    private String role;

    private Long organizationId;

    /** OTP entered by the user to confirm email ownership */
    private String otp;
}
