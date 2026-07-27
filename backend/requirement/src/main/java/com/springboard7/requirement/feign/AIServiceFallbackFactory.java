package com.springboard7.requirement.feign;


import com.springboard7.requirement.exception.AIServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AIServiceFallbackFactory
        implements FallbackFactory<AIServiceClient> {

    @Override
    public AIServiceClient create(Throwable cause) {

        return request -> {
            log.error("AI Service failed", cause);

            throw new AIServiceUnavailableException(
                    "AI Service is currently unavailable.",
                    cause
            );
        };
    }
}