package com.springboard.auth_service.dto.temp;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "build")
public record DeveloperDetails(String message, Map<String,String> contactDetails, List<String> call) {
}
