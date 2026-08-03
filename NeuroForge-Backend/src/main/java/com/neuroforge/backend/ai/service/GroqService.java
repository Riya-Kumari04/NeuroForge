package com.neuroforge.backend.ai.service;

import com.neuroforge.backend.ai.dto.ReleaseNotesRequest;
import com.neuroforge.backend.ai.dto.ReleaseNotesResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GroqService {

        private final RestTemplate restTemplate;

        @Value("${groq.api.key}")
        private String apiKey;

        @Value("${groq.model}")
        private String model;

        public ReleaseNotesResponse generateReleaseNotes(ReleaseNotesRequest request) {

                String prompt = """
                                Generate professional software release notes from the following completed tasks.

                                Tasks:
                                %s

                                Format:
                                - New Features
                                - Improvements
                                - Bug Fixes
                                - Technical Changes
                                """.formatted(String.join("\n", request.getTasks()));

                Map<String, Object> body = Map.of(
                                "model", model,
                                "messages", List.of(
                                                Map.of(
                                                                "role", "user",
                                                                "content", prompt)),
                                "temperature", 0.3);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(apiKey);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

                String url = "https://api.groq.com/openai/v1/chat/completions";

                ResponseEntity<String> response = restTemplate.exchange(
                                url,
                                HttpMethod.POST,
                                entity,
                                String.class);

                ObjectMapper mapper = new ObjectMapper();

                JsonNode root;

                try {
                        root = mapper.readTree(response.getBody());
                } catch (Exception e) {
                        throw new RuntimeException("Failed to parse Groq response", e);
                }

                String releaseNotes = root
                                .path("choices")
                                .get(0)
                                .path("message")
                                .path("content")
                                .asText();

                return new ReleaseNotesResponse(releaseNotes);
        }
}