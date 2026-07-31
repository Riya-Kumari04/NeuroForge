package com.neuroforge.backend.specification.dto.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

public final class ApiResponseBuilder {

    private ApiResponseBuilder() {
    }

    public static <T> ResponseEntity<ApiResponse<T>> ok(
            String message,
            T data) {

        return ResponseEntity.ok(
                ApiResponse.<T>builder()
                        .success(true)
                        .message(message)
                        .data(data)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(
            String message,
            T data) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.<T>builder()
                                .success(true)
                                .message(message)
                                .data(data)
                                .timestamp(LocalDateTime.now())
                                .build()
                );
    }

    public static ResponseEntity<ApiResponse<Void>> ok(
            String message) {

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message(message)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

}
