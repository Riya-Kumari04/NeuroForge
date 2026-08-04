package com.neuroforge.backend.ai.service;

import com.neuroforge.backend.ai.dto.ReleaseNotesRequest;
import com.neuroforge.backend.ai.dto.ReleaseNotesResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

// import java.net.http.HttpHeaders;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GroqServiceImpl implements GroqService {

        @Value("${groq.api.key}")
        private String apiKey;

        @Value("${groq.model}")
        private String model;

        private final RestTemplate restTemplate = new RestTemplate();

        @Override
        public ReleaseNotesResponse generateReleaseNotes(ReleaseNotesRequest request) {

                String prompt = """
                                Generate professional release notes for the following completed tasks:

                                %s
                                """.formatted(String.join("\n", request.getTasks()));

                String notes = callGroq(prompt);

                return ReleaseNotesResponse.builder()
                                .releaseNotes(notes)
                                .build();
        }

        @Override
        public boolean isDuplicate(String existingBug, String newBug) {

                String prompt = """
                                You are an AI assistant for bug tracking.

                                Compare the following two bug reports.

                                Return ONLY one word:
                                YES - if they describe the same underlying issue.
                                NO - if they describe different issues.

                                Do not provide any explanation.

                                Existing Bug:
                                %s

                                New Bug:
                                %s
                                """.formatted(existingBug, newBug);

                String result = callGroq(prompt);

                System.out.println("Groq Response: " + result);

                return result.trim().equalsIgnoreCase("YES");
        }

        private String callGroq(String prompt) {

                String url = "https://api.groq.com/openai/v1/chat/completions";

                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(apiKey);
                headers.setContentType(MediaType.APPLICATION_JSON);

                Map<String, Object> body = new HashMap<>();
                body.put("model", model);

                body.put("messages", List.of(
                                Map.of(
                                                "role", "user",
                                                "content", prompt)));

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

                ResponseEntity<Map> response = restTemplate.exchange(
                                url,
                                HttpMethod.POST,
                                entity,
                                Map.class);

                Map choice = (Map) ((List<?>) response.getBody().get("choices")).get(0);
                Map message = (Map) choice.get("message");

                return message.get("content").toString();
        }
}