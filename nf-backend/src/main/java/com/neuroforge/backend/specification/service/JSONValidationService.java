package com.neuroforge.backend.specification.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class JSONValidationService {

    private final ObjectMapper objectMapper;

    private static final List<String> REQUIRED_FIELDS = Arrays.asList(
            "title", "description", "userStories", "acceptanceCriteria", 
            "functionalRequirements", "nonFunctionalRequirements"
    );

    /**
     * Validates JSON structure before parsing
     */
    public void validateJsonStructure(String jsonText) throws RuntimeException {
        if (jsonText == null || jsonText.isEmpty()) {
            throw new RuntimeException("JSON response is empty");
        }

        jsonText = jsonText.trim();

        if (!jsonText.startsWith("{") || !jsonText.endsWith("}")) {
            throw new RuntimeException("Invalid JSON format: Response does not contain valid JSON object");
        }
    }

    /**
     * Validates required fields exist in parsed JSON
     */
    public void validateRequiredFields(JsonNode jsonNode) throws RuntimeException {
        for (String field : REQUIRED_FIELDS) {
            if (!jsonNode.has(field)) {
                throw new RuntimeException("Invalid JSON structure: Missing required field '" + field + "'");
            }
        }
    }

    /**
     * Validates field types
     */
    public void validateFieldTypes(JsonNode jsonNode) throws RuntimeException {
        // Validate string fields
        if (!jsonNode.get("title").isTextual()) {
            throw new RuntimeException("Invalid field type: 'title' must be a string");
        }
        if (!jsonNode.get("description").isTextual()) {
            throw new RuntimeException("Invalid field type: 'description' must be a string");
        }

        // Validate array fields
        if (!jsonNode.get("userStories").isArray()) {
            throw new RuntimeException("Invalid field type: 'userStories' must be an array");
        }
        if (!jsonNode.get("acceptanceCriteria").isArray()) {
            throw new RuntimeException("Invalid field type: 'acceptanceCriteria' must be an array");
        }
        if (!jsonNode.get("functionalRequirements").isArray()) {
            throw new RuntimeException("Invalid field type: 'functionalRequirements' must be an array");
        }
        if (!jsonNode.get("nonFunctionalRequirements").isArray()) {
            throw new RuntimeException("Invalid field type: 'nonFunctionalRequirements' must be an array");
        }
    }

    /**
     * Validates arrays are not empty
     */
    public void validateArraysNotEmpty(JsonNode jsonNode) throws RuntimeException {
        if (jsonNode.get("userStories").size() == 0) {
            log.warn("userStories array is empty");
        }
        if (jsonNode.get("acceptanceCriteria").size() == 0) {
            log.warn("acceptanceCriteria array is empty");
        }
        if (jsonNode.get("functionalRequirements").size() == 0) {
            log.warn("functionalRequirements array is empty");
        }
    }

    /**
     * Complete validation pipeline
     */
    public void validate(String jsonText, JsonNode jsonNode) throws RuntimeException {
        log.debug("Starting JSON validation");
        validateJsonStructure(jsonText);
        validateRequiredFields(jsonNode);
        validateFieldTypes(jsonNode);
        validateArraysNotEmpty(jsonNode);
        log.debug("JSON validation completed successfully");
    }
}
