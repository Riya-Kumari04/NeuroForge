package com.springboard.auth_service.service.impl;

import com.springboard.auth_service.dto.RateLimitResult;
import com.springboard.auth_service.dto.RateLimitRule;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final BucketService bucketService;

    private final Map<String, RateLimitRule> rateLimitRules;
    public RateLimitResult validateRequest(HttpServletRequest request) {

        RateLimitRule rule =
                rateLimitRules.get(request.getRequestURI());

        if (rule == null) {
            return new RateLimitResult(true, null);
        }

        String bucketKey =
                rule.getKeyGenerator().apply(request);

        Bucket bucket =
                bucketService.resolveBucket(
                        bucketKey,
                        rule.getBandwidth()
                );

        if (bucket.tryConsume(1)) {

            return new RateLimitResult(true, null);

        }

        return new RateLimitResult(
                false,
                rule.getMessage()
        );

    }
}