package com.springboard.auth_service.controller;


import com.springboard.auth_service.dto.*;
import com.springboard.auth_service.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Authentication APIs",
        description = "Registration, OTP verification and password reset operations"
)
@Validated
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Send OTP for registration")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "OTP sent successfully",
                    content = @Content(
                            examples = @ExampleObject(
                                    value = """
                        {
                          "success": true,
                          "message": "OTP sent successfully",
                          "data": null
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid email",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "User already exists",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorDTO.class)
                    )
            )
    })
    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponseDTO<Void>> sendOtp(
            @Email(message = "Invalid email")
            @RequestParam String email) {

        authService.sendOtp(email);

        return ResponseEntity.ok(
                ApiResponseDTO.<Void>builder()
                        .success(true)
                        .message("OTP sent successfully")
                        .build()
        );
    }

    @Operation(summary = "Register a new user")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiResponseDTO.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid OTP",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorDTO.class),
                            examples = @ExampleObject(
                                    value = """
                        {
                          "status": 400,
                          "error": "BAD_REQUEST",
                          "message": "Invalid OTP",
                          "timestamp": "2026-06-24T10:00:00"
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "User already exists",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorDTO.class
                            )
                    )
            )
    })
    @PostMapping(
            value = "/register",
            produces = "application/json"
    )
    public ResponseEntity<ApiResponseDTO<Void>> register(
            @Valid @RequestBody RegistrationDTO register) {

        authService.register(register);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponseDTO.<Void>builder()
                                .success(true)
                                .message("User registered successfully")
                                .build()
                );
    }

    @Operation(summary = "Send OTP for password reset")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "OTP sent successfully",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiResponseDTO.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorDTO.class),
                            examples = @ExampleObject(
                                    value = """
                        {
                          "status": 404,
                          "error": "NOT_FOUND",
                          "message": "User not found",
                          "timestamp": "2026-06-24T10:00:00"
                        }
                        """
                            )
                    )
            )
    })
    @PatchMapping("/forgot-password/send-otp")
    public ResponseEntity<ApiResponseDTO<Void>> sendForgotPasswordOtp(
            @Email(message = "Invalid email")
            @RequestParam String email) {
        authService.sendForgotPasswordOtp(email);
        return ResponseEntity.ok(
                ApiResponseDTO.<Void>builder()
                        .success(true)
                        .message("OTP sent successfully")
                        .build()
        );
    }

    @Operation(summary = "Reset user password")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Password reset successfully",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiResponseDTO.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid OTP",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorDTO.class),
                            examples = @ExampleObject(
                                    value = """
                        {
                          "status": 400,
                          "error": "BAD_REQUEST",
                          "message": "Invalid OTP",
                          "timestamp": "2026-06-24T10:00:00"
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorDTO.class),
                            examples = @ExampleObject(
                                    value = """
                        {
                          "status": 404,
                          "error": "NOT_FOUND",
                          "message": "User not found",
                          "timestamp": "2026-06-24T10:00:00"
                        }
                        """
                            )
                    )
            )
    })
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponseDTO<Void>> resetPassword(
            @Valid @RequestBody ForgotPasswordDTO forgotPasswordDTO) {
        authService.resetPassword(forgotPasswordDTO);
        return ResponseEntity.ok(
                ApiResponseDTO.<Void>builder()
                        .success(true)
                        .message("Password reset successfully")
                        .build()
        );
    }

    @Operation(
            summary = "User Login",
            description = "Authenticates a user using email and password and returns a JWT access token."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(
                            examples = @ExampleObject(
                                    value = """
                        {
                          "success": true,
                          "message": "Login successful",
                          "data": {
                            "accessToken":"eyJhbGc...",
                            "refreshToken":"eyJhbGc..."
                          }
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid email or password"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Email not verified or account disabled"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponseDTO<LoginResponseDTO>> login(
            @Valid @RequestBody LoginRequestDTO request) {

        LoginResponseDTO response = authService.login(request);

        return ResponseEntity.ok(
                ApiResponseDTO.<LoginResponseDTO>builder()
                        .success(true)
                        .message("Login successful")
                        .data(response)
                        .build()
        );
    }

    @Operation(
            summary = "Refresh Access Token",
            description = "Generates a new access token and refresh token using a valid refresh token."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Token refreshed successfully",
                    content = @Content(
                            examples = @ExampleObject(
                                    value = """
                                {
                                  "success": true,
                                  "message": "Token refreshed successfully",
                                  "data": {
                                    "accessToken":"eyJhbGc...",
                                    "refreshToken":"eyJhbGc..."
                                  }
                                }
                                """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid or expired refresh token",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorDTO.class)
                    )
            )
    })
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponseDTO<LoginResponseDTO>> refreshToken(
            @Valid @RequestBody RefreshTokenRequestDTO request) {

        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("/set-password")
    public ResponseEntity<?> setPassword(
            HttpServletRequest request,
            @RequestBody PasswordDTO dto
    ){

        authService.setPassword(
                request,
                dto.getPassword()
        );

        return ResponseEntity.ok(
                "Password set successfully"
        );
    }

}
