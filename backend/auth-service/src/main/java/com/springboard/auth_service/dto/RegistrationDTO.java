package com.springboard.auth_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationDTO {

    @NotBlank(message = "Name cannot be empty")
    @Size(min = 3, max = 50,
            message = "Name must be between 3 and 50 characters")
    @Schema(example = "Sahil Kumar")
    private String name;

    @NotBlank(message = "OTP cannot be empty")
    @Pattern(
            regexp = "^\\d{6}$",
            message = "OTP must be 6 digits"
    )
    @Schema(example = "123456")
    private String otp;

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email")
    @Schema(example = "sahil@gmail.com")
    private String email;

    @NotBlank(message = "Password cannot be empty")
    @Size(
            min = 8,
            max = 20,
            message = "Password must be between 8 and 20 characters"
    )
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).*$",
            message = "Password must contain uppercase, lowercase, digit and special character"
    )
    @Schema(example = "Password@123")
    private String password;
}