package com.neuroforge.backend.specification.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neuroforge.backend.specification.dto.response.GenerateSpecificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeminiService {

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3-flash-preview:generateContent";
    private static final String GENERATED_BY = "Gemini";
    private static final double TEMPERATURE = 0.2;
    private static final int MAX_RETRIES = 1;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final PromptLoader promptLoader;
    private final JSONValidationService jsonValidationService;
    private final HallucinationDetectionService hallucinationDetectionService;

    public GenerateSpecificationResponse generateSpecification(String prompt) {
        long startTime = System.currentTimeMillis();
        String promptVersion = promptLoader.getPromptVersion();
        
        log.info("Generation started - Prompt version: {}, Model: gemini-3-flash-preview", promptVersion);
        
        int attempt = 0;
        RuntimeException lastException = null;
        
        while (attempt <= MAX_RETRIES) {
            attempt++;
            log.info("Attempt {}/{} for specification generation", attempt, MAX_RETRIES + 1);
            
            try {
                GenerateSpecificationResponse response = attemptGeneration(prompt, promptVersion);
                
                // Hallucination detection
                HallucinationDetectionService.HallucinationResult hallucinationResult = 
                    hallucinationDetectionService.detectHallucination(prompt, response);
                
                if (hallucinationResult.hasHallucination()) {
                    log.warn("Hallucination detected on attempt {}. Forbidden terms: {}. Retrying...", 
                        attempt, hallucinationResult.getDetectedTerms());
                    
                    if (attempt <= MAX_RETRIES) {
                        lastException = new RuntimeException("Hallucination detected: " + hallucinationResult.getDetectedTerms());
                        continue;
                    } else {
                        throw new RuntimeException("Unable to generate specification. AI invented technologies not mentioned in requirements: " + 
                            hallucinationResult.getDetectedTerms());
                    }
                }
                
                long duration = System.currentTimeMillis() - startTime;
                log.info("Generation completed successfully in {} ms", duration);
                return response;
                
            } catch (RuntimeException e) {
                log.error("Attempt {} failed: {}", attempt, e.getMessage());
                lastException = e;
                
                if (attempt <= MAX_RETRIES) {
                    log.info("Retrying...");
                }
            }
        }
        
        long duration = System.currentTimeMillis() - startTime;
        log.error("Generation failed after {} attempts in {} ms", MAX_RETRIES + 1, duration);
        throw new RuntimeException("Unable to generate specification. Please try again.", lastException);
    }

    private GenerateSpecificationResponse attemptGeneration(String prompt, String promptVersion) {
        try {
            String systemPrompt = promptLoader.loadSystemPrompt();
            String requestBody = buildRequestBody(systemPrompt, prompt);
            String url = GEMINI_API_URL + "?key=" + geminiApiKey;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            
            log.debug("Calling Gemini API");
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return parseGeminiResponse(response.getBody(), promptVersion);
            } else {
                throw new RuntimeException("Gemini API returned non-OK status: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("Error calling Gemini API", e);
            throw new RuntimeException("Failed to generate specification using Gemini: " + e.getMessage(), e);
        }
    }

    private String buildRequestBody(String systemPrompt, String userPrompt) {
        try {
            Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                    Map.of(
                        "parts", List.of(
                            Map.of("text", systemPrompt + "\n\nUser Prompt: " + userPrompt)
                        )
                    )
                ),
                "generationConfig", Map.of(
                    "temperature", TEMPERATURE,
                    "responseMimeType", "application/json"
                )
            );
            
            return objectMapper.writeValueAsString(requestBody);
        } catch (Exception e) {
            log.error("Error building Gemini request body", e);
            throw new RuntimeException("Failed to build request body", e);
        }
    }

    private GenerateSpecificationResponse parseGeminiResponse(String responseBody, String promptVersion) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode candidates = root.path("candidates");
            
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).path("content");
                JsonNode parts = content.path("parts");
                
                if (parts.isArray() && parts.size() > 0) {
                    String jsonText = parts.get(0).path("text").asText();
                    
                    // Remove markdown code blocks if present
                    jsonText = jsonText.replaceAll("```json\\s*", "").replaceAll("```\\s*$", "").trim();
                    
                    log.debug("Extracted JSON text from response");
                    
                    // Validate JSON structure
                    jsonValidationService.validateJsonStructure(jsonText);
                    
                    JsonNode jsonNode = objectMapper.readTree(jsonText);
                    
                    // Validate required fields and types
                    jsonValidationService.validate(jsonText, jsonNode);
                    log.debug("JSON validation successful");
                    
                    GenerateSpecificationResponse response = buildResponseFromJson(jsonNode);
                    
                    log.info("Successfully parsed Gemini response with title: {}", response.getTitle());
                    return response;
                }
            }
            
            throw new RuntimeException("Invalid Gemini response format: No candidates or parts found");
            
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("JSON parsing error in Gemini response", e);
            throw new RuntimeException("Failed to parse JSON from Gemini response: Invalid JSON format", e);
        } catch (Exception e) {
            log.error("Error parsing Gemini response", e);
            throw new RuntimeException("Failed to parse Gemini response: " + e.getMessage(), e);
        }
    }

    private GenerateSpecificationResponse buildResponseFromJson(JsonNode jsonNode) {
        GenerateSpecificationResponse response = new GenerateSpecificationResponse();
        response.setTitle(jsonNode.has("title") ? jsonNode.path("title").asText() : "Untitled Specification");
        response.setDescription(jsonNode.path("description").asText());
        
        List<String> userStories = new ArrayList<>();
        JsonNode userStoriesNode = jsonNode.path("userStories");
        if (userStoriesNode.isArray()) {
            userStoriesNode.forEach(node -> userStories.add(node.asText()));
        }
        response.setUserStories(userStories);
        
        List<String> acceptanceCriteria = new ArrayList<>();
        JsonNode acceptanceCriteriaNode = jsonNode.path("acceptanceCriteria");
        if (acceptanceCriteriaNode.isArray()) {
            acceptanceCriteriaNode.forEach(node -> acceptanceCriteria.add(node.asText()));
        }
        response.setAcceptanceCriteria(acceptanceCriteria);
        
        List<String> functionalRequirements = new ArrayList<>();
        JsonNode functionalRequirementsNode = jsonNode.path("functionalRequirements");
        if (functionalRequirementsNode.isArray()) {
            functionalRequirementsNode.forEach(node -> functionalRequirements.add(node.asText()));
        }
        response.setFunctionalRequirements(functionalRequirements);
        
        List<String> nonFunctionalRequirements = new ArrayList<>();
        JsonNode nonFunctionalRequirementsNode = jsonNode.path("nonFunctionalRequirements");
        if (nonFunctionalRequirementsNode.isArray()) {
            nonFunctionalRequirementsNode.forEach(node -> nonFunctionalRequirements.add(node.asText()));
        }
        response.setNonFunctionalRequirements(nonFunctionalRequirements);
        
        return response;
    }

    public String getGeneratedBy() {
        return GENERATED_BY;
    }

    public String getModel() {
        return "gemini-3-flash-preview";
    }
}
