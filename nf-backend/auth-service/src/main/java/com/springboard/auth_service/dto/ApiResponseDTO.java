package com.springboard.auth_service.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Schema(description = "Standard success response")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponseDTO<T> {

    @Schema(example = "true")
    private boolean success;

    @Schema(example = "OTP sent successfully")
    private String message;

    private T data;
}