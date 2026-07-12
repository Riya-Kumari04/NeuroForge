package com.springboard.auth_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;


@Schema(description = "Standard error response")
@Data
@Builder
public class ApiErrorDTO {

    @Schema(example = "400")
    private int status;

    @Schema(example = "BAD_REQUEST")
    private String error;

    @Schema(example = "Invalid OTP")
    private String message;

    private LocalDateTime timestamp;
}