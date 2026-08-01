package com.neuroforge.backend.controller;

import com.neuroforge.backend.dto.CodeReviewResponse;
import com.neuroforge.backend.dto.CreateCodeReviewRequest;
import com.neuroforge.backend.dto.UpdateCodeReviewStatusRequest;
import com.neuroforge.backend.enums.CodeReviewStatus;
import com.neuroforge.backend.service.CodeReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/code-reviews")
@RequiredArgsConstructor
public class CodeReviewController {

    private final CodeReviewService codeReviewService;

    @PostMapping
    public ResponseEntity<CodeReviewResponse> createCodeReview(@Valid @RequestBody CreateCodeReviewRequest request) {
        CodeReviewResponse response = codeReviewService.createCodeReview(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CodeReviewResponse>> getAllCodeReviews() {
        List<CodeReviewResponse> response = codeReviewService.getAllCodeReviews();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CodeReviewResponse> getCodeReviewById(@PathVariable UUID id) {
        CodeReviewResponse response = codeReviewService.getCodeReviewById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<CodeReviewResponse>> getCodeReviewsByTask(@PathVariable UUID taskId) {
        List<CodeReviewResponse> response = codeReviewService.getCodeReviewsByTask(taskId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<CodeReviewResponse>> getCodeReviewsByStatus(@PathVariable CodeReviewStatus status) {
        List<CodeReviewResponse> response = codeReviewService.getCodeReviewsByStatus(status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/task/{taskId}/latest")
    public ResponseEntity<CodeReviewResponse> getLatestReviewForTask(@PathVariable UUID taskId) {
        CodeReviewResponse response = codeReviewService.getLatestReviewForTask(taskId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{reviewId}/status")
    public ResponseEntity<CodeReviewResponse> updateReviewStatus(
            @PathVariable UUID reviewId,
            @Valid @RequestBody UpdateCodeReviewStatusRequest request) {
        CodeReviewResponse response = codeReviewService.updateReviewStatus(reviewId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{reviewId}/approve")
    public ResponseEntity<CodeReviewResponse> approveReview(@PathVariable UUID reviewId) {
        CodeReviewResponse response = codeReviewService.approveReview(reviewId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{reviewId}/reject")
    public ResponseEntity<CodeReviewResponse> rejectReview(@PathVariable UUID reviewId) {
        CodeReviewResponse response = codeReviewService.rejectReview(reviewId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteCodeReview(@PathVariable UUID reviewId) {
        codeReviewService.deleteCodeReview(reviewId);
        return ResponseEntity.noContent().build();
    }
}
