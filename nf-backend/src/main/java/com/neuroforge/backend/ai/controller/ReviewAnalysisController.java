package com.neuroforge.backend.ai.controller;

import com.neuroforge.backend.ai.dto.AnalyzeReviewRequest;
import com.neuroforge.backend.ai.dto.AnalyzeReviewResponse;
import com.neuroforge.backend.ai.service.ReviewAnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewAnalysisController {

    private final ReviewAnalysisService reviewAnalysisService;

    @PostMapping("/analyze")
    @PreAuthorize("hasAuthority('ROLE_DEVELOPER')")
    public ResponseEntity<AnalyzeReviewResponse> analyzeReview(@Valid @RequestBody AnalyzeReviewRequest request) {
        AnalyzeReviewResponse response = reviewAnalysisService.analyzeCode(request);
        return ResponseEntity.ok(response);
    }
}
