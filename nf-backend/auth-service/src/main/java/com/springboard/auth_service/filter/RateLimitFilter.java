package com.springboard.auth_service.filter;

import com.springboard.auth_service.dto.RateLimitResult;
import com.springboard.auth_service.exception.RateLimitExceededException;
import com.springboard.auth_service.service.impl.RateLimitService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final HandlerExceptionResolver resolver;

    public RateLimitFilter(
            RateLimitService rateLimitService,
            @Qualifier("handlerExceptionResolver")
            HandlerExceptionResolver resolver) {

        this.rateLimitService = rateLimitService;
        this.resolver = resolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        RateLimitResult result = rateLimitService.validateRequest(request);

        if (result.isAllowed()) {
            filterChain.doFilter(request, response);
            return;
        }

        resolver.resolveException(
                request,
                response,
                null,
                new RateLimitExceededException(result.getMessage())
        );
    }
}