package com.neuroforge.backend.service;

import com.neuroforge.backend.dto.AnalyzeReviewRequest;
import com.neuroforge.backend.dto.AnalyzeReviewResponse;
import com.neuroforge.backend.dto.CreateCodeReviewRequest;
import com.neuroforge.backend.entity.CodeReview;
import com.neuroforge.backend.enums.CodeReviewStatus;
import com.neuroforge.backend.integration.gemini.GeminiConstants;
import com.neuroforge.backend.mongodb.document.ReviewDocument;
import com.neuroforge.backend.mongodb.document.ReviewIssue;
import com.neuroforge.backend.mongodb.repository.ReviewDocumentRepository;
import com.neuroforge.backend.repository.CodeReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewAnalysisService {

    private final PromptBuilderService promptBuilderService;
    private final GeminiService geminiService;
    private final ReviewResponseParser reviewResponseParser;
    private final CodeReviewService codeReviewService;
    private final CodeReviewRepository codeReviewRepository;
    private final ReviewDocumentRepository reviewDocumentRepository;

    public ReviewAnalysisService(
            PromptBuilderService promptBuilderService,
            GeminiService geminiService,
            ReviewResponseParser reviewResponseParser,
            CodeReviewService codeReviewService,
            CodeReviewRepository codeReviewRepository,
            ReviewDocumentRepository reviewDocumentRepository) {
        this.promptBuilderService = promptBuilderService;
        this.geminiService = geminiService;
        this.reviewResponseParser = reviewResponseParser;
        this.codeReviewService = codeReviewService;
        this.codeReviewRepository = codeReviewRepository;
        this.reviewDocumentRepository = reviewDocumentRepository;
    }

    @Transactional
    public AnalyzeReviewResponse analyzeCode(AnalyzeReviewRequest request) {
        CreateCodeReviewRequest createRequest = CreateCodeReviewRequest.builder()
                .taskId(request.getTaskId())
                .requestedBy(request.getRequestedBy())
                .reviewSource(request.getReviewSource())
                .sourceReference(null)
                .build();

        CodeReview codeReview = codeReviewService.createCodeReviewEntity(createRequest);

        codeReview.setStatus(CodeReviewStatus.IN_PROGRESS);
        codeReview = codeReviewRepository.save(codeReview);

        String prompt = promptBuilderService.buildReviewPrompt(request.getLanguage(), request.getSourceCode());

        String jsonResponse = geminiService.analyzeCode(prompt);

        AnalyzeReviewResponse parsedResponse = reviewResponseParser.parse(jsonResponse);

        codeReview.setOverallScore(parsedResponse.getOverallScore());
        codeReview.setSummary(parsedResponse.getSummary());
        codeReview.setStatus(CodeReviewStatus.COMPLETED);
        codeReview = codeReviewRepository.save(codeReview);

        List<ReviewIssue> issues = null;
        if (parsedResponse.getIssues() != null) {
            issues = parsedResponse.getIssues().stream()
                    .map(issue -> ReviewIssue.builder()
                            .line(issue.getLine())
                            .severity(issue.getSeverity())
                            .category(issue.getCategory())
                            .description(issue.getDescription())
                            .suggestion(issue.getSuggestion())
                            .build())
                    .collect(Collectors.toList());
        }

        ReviewDocument reviewDocument = ReviewDocument.builder()
                .reviewId(codeReview.getId())
                .taskId(request.getTaskId())
                .model(GeminiConstants.MODEL)
                .language(request.getLanguage())
                .sourceCode(request.getSourceCode())
                .overallScore(parsedResponse.getOverallScore())
                .summary(parsedResponse.getSummary())
                .issues(issues)
                .createdAt(LocalDateTime.now())
                .build();

        reviewDocumentRepository.save(reviewDocument);

        parsedResponse.setReviewId(codeReview.getId());

        return parsedResponse;
    }
}
