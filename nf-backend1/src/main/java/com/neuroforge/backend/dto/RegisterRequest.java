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
     * Accepted values: ROLE_SUPER_ADMIN | ROLE_ORG_ADMIN | ROLE_PROJECT_MANAGER
     *                  ROLE_DEVELOPER   | ROLE_TESTER    | ROLE_CLIENT
     */
    @NotBlank(message = "Role is required")
    @Pattern(regexp = "ROLE_(SUPER_ADMIN|ORG_ADMIN|PROJECT_MANAGER|DEVELOPER|TESTER|CLIENT)",
             message = "Invalid role")
    private String role;

    private Long organizationId;

    /** OTP entered by the user to confirm email ownership */
    private String otp;
}
