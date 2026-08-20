package com.neuroforge.backend.specification.service.impl;

import com.neuroforge.backend.specification.dto.request.GenerateSpecificationRequest;
import com.neuroforge.backend.specification.dto.response.GenerateSpecificationResponse;
import com.neuroforge.backend.specification.entity.AISpecification;
import com.neuroforge.backend.specification.repository.AISpecificationRepository;
import com.neuroforge.backend.specification.service.GeminiService;
import com.neuroforge.backend.specification.service.PromptLoader;
import com.neuroforge.backend.specification.service.SpecificationAIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpecificationAIServiceImpl implements SpecificationAIService {

    private final GeminiService geminiService;
    private final AISpecificationRepository aiSpecificationRepository;
    private final PromptLoader promptLoader;

    @Override
    public GenerateSpecificationResponse generateSpecification(GenerateSpecificationRequest request) {
        log.info("Generating specification for prompt: {}", request.getPrompt());

        GenerateSpecificationResponse specificationResponse = geminiService.generateSpecification(request.getPrompt());
        
        // Store in MongoDB for history
        saveAISpecification(request.getPrompt(), specificationResponse);

        return specificationResponse;
    }

    private void saveAISpecification(String prompt, GenerateSpecificationResponse response) {
        try {
            AISpecification aiSpec = AISpecification.builder()
                    .prompt(prompt)
                    .title(response.getTitle())
                    .description(response.getDescription())
                    .userStories(response.getUserStories())
                    .acceptanceCriteria(response.getAcceptanceCriteria())
                    .functionalRequirements(response.getFunctionalRequirements())
                    .nonFunctionalRequirements(response.getNonFunctionalRequirements())
                    .aiModel(geminiService.getModel())
                    .generatedBy(geminiService.getGeneratedBy())
                    .promptVersion(geminiService.getModel() + "-" + promptLoader.getPromptVersion())
                    .generatedAt(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            
            aiSpecificationRepository.save(aiSpec);
            log.info("Saved AI specification to MongoDB with ID: {}, Model: {}, PromptVersion: {}", 
                aiSpec.getId(), aiSpec.getAiModel(), aiSpec.getPromptVersion());
            
        } catch (Exception e) {
            log.error("Failed to save AI specification to MongoDB", e);
            // Don't throw - this is non-critical for the main flow
        }
    }
}
