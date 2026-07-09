package com.springboard.auth_service.dto;

import io.github.bucket4j.Bandwidth;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.function.Function;


@Getter
@Builder
@AllArgsConstructor
public class RateLimitRule {

    private final Bandwidth bandwidth;

    private final Function<HttpServletRequest,String> keyGenerator;

    private final String message;

}