package com.neuroforge.backend.service;

import com.neuroforge.backend.integration.gemini.GeminiConstants;
import com.neuroforge.backend.integration.gemini.dto.GeminiCandidate;
import com.neuroforge.backend.integration.gemini.dto.GeminiContent;
import com.neuroforge.backend.integration.gemini.dto.GeminiPart;
import com.neuroforge.backend.integration.gemini.dto.GeminiRequest;
import com.neuroforge.backend.integration.gemini.dto.GeminiResponse;
import com.neuroforge.backend.integration.gemini.dto.GeminiResponsePart;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Service
public class GeminiService {

    private final RestClient restClient;
    private final String apiKey;
    private final String baseUrl;

    public GeminiService(
            RestClient.Builder restClientBuilder,
            @Value("${gemini.api.key}") String apiKey,
            @Value("${gemini.api.base-url}") String baseUrl) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    public String analyzeCode(String prompt) {
        GeminiPart part = GeminiPart.builder()
                .text(prompt)
                .build();

        GeminiContent content = GeminiContent.builder()
                .parts(List.of(part))
                .build();

        GeminiRequest request = GeminiRequest.builder()
                .contents(List.of(content))
                .build();

        GeminiResponse response;
        try {
            response = restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/{model}:generateContent")
                            .queryParam("key", apiKey)
                            .build(GeminiConstants.MODEL))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, resp) -> {
                        String responseBody = new String(resp.getBody().readAllBytes(), StandardCharsets.UTF_8);
                        log.error("Gemini API error. Status: {}, URL: {}, Response Body: {}",
                                resp.getStatusCode(), req.getURI(), responseBody);
                        throw new RuntimeException("Gemini API Error (" + resp.getStatusCode() + "): " + responseBody);
                    })
                    .body(GeminiResponse.class);
        } catch (RestClientResponseException e) {
            log.error("Gemini API error. Status: {}, Response Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Gemini API Error (" + e.getStatusCode() + "): " + e.getResponseBodyAsString(), e);
        } catch (RestClientException e) {
            log.error("Failed to communicate with Gemini API.", e);
            throw new RuntimeException("Failed to communicate with Gemini API: " + e.getMessage(), e);
        }

        if (response == null) {
            throw new IllegalStateException("Gemini API response was null.");
        }

        if (response.getCandidates() == null || response.getCandidates().isEmpty()) {
            throw new IllegalStateException("Gemini API response contained no candidates.");
        }

        GeminiCandidate candidate = response.getCandidates().get(0);
        if (candidate.getContent() == null) {
            throw new IllegalStateException("Gemini API candidate content was null.");
        }

        if (candidate.getContent().getParts() == null || candidate.getContent().getParts().isEmpty()) {
            throw new IllegalStateException("Gemini API candidate content contained no parts.");
        }

        GeminiResponsePart responsePart = candidate.getContent().getParts().get(0);
        if (responsePart.getText() == null) {
            throw new IllegalStateException("Gemini API response part text was null.");
        }

        return responsePart.getText();
    }
}
