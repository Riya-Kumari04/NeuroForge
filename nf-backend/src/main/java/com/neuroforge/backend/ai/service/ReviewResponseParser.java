package com.neuroforge.backend.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neuroforge.backend.ai.dto.AnalyzeReviewResponse;
import org.springframework.stereotype.Service;

@Service
public class ReviewResponseParser {

    private final ObjectMapper objectMapper;

    public ReviewResponseParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public AnalyzeReviewResponse parse(String json) {
        if (json == null || json.isBlank()) {
            throw new IllegalArgumentException("Gemini response cannot be null or blank.");
        }

        AnalyzeReviewResponse response;
        try {
            response = objectMapper.readValue(json, AnalyzeReviewResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse Gemini review response.", e);
        }

        if (response.getOverallScore() == null) {
            throw new IllegalStateException("Parsed review response missing required overallScore.");
        }

        if (response.getSummary() == null) {
            throw new IllegalStateException("Parsed review response missing required summary.");
        }

        if (response.getIssues() == null) {
            throw new IllegalStateException("Parsed review response missing required issues.");
        }

        return response;
    }
}
