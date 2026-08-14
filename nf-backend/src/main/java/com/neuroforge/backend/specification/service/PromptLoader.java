package com.neuroforge.backend.specification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class PromptLoader {

    @Value("classpath:prompts/specification-prompt.txt")
    private Resource promptResource;

    private static final String PROMPT_VERSION = "2.4";

    public String loadSystemPrompt() {
        try {
            String prompt = new String(promptResource.getContentAsByteArray(), StandardCharsets.UTF_8);
            log.debug("Loaded system prompt from file, version: {}", PROMPT_VERSION);
            return prompt;
        } catch (IOException e) {
            log.error("Failed to load system prompt from file", e);
            throw new RuntimeException("Failed to load system prompt: " + e.getMessage(), e);
        }
    }

    public String getPromptVersion() {
        return PROMPT_VERSION;
    }
}
