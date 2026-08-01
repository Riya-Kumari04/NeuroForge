package com.neuroforge.backend.service;

import com.neuroforge.backend.dto.CodeReviewResponse;
import com.neuroforge.backend.dto.CreateCodeReviewRequest;
import com.neuroforge.backend.dto.UpdateCodeReviewStatusRequest;
import com.neuroforge.backend.entity.CodeReview;
import com.neuroforge.backend.entity.Task;
import com.neuroforge.backend.entity.User;
import com.neuroforge.backend.enums.CodeReviewStatus;
import com.neuroforge.backend.exception.InvalidTaskStateException;
import com.neuroforge.backend.exception.ResourceNotFoundException;
import com.neuroforge.backend.repository.CodeReviewRepository;
import com.neuroforge.backend.repository.TaskRepository;
import com.neuroforge.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CodeReviewService {

    private final CodeReviewRepository codeReviewRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Transactional
    public CodeReviewResponse createCodeReview(CreateCodeReviewRequest request) {
        CodeReview saved = createCodeReviewEntity(request);
        return mapToResponse(saved);
    }

    @Transactional
    protected CodeReview createCodeReviewEntity(CreateCodeReviewRequest request) {
        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + request.getTaskId()));

        User requestedBy = userRepository.findById(request.getRequestedBy())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getRequestedBy()));

        CodeReview codeReview = CodeReview.builder()
                .task(task)
                .requestedBy(requestedBy)
                .status(CodeReviewStatus.REQUESTED)
                .reviewSource(request.getReviewSource())
                .sourceReference(request.getSourceReference())
                .overallScore(null)
                .summary(null)
                .approvedBy(null)
                .build();

        return codeReviewRepository.save(codeReview);
    }

    @Transactional(readOnly = true)
    public List<CodeReviewResponse> getAllCodeReviews() {
        return codeReviewRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CodeReviewResponse getCodeReviewById(UUID id) {
        CodeReview review = codeReviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Code review not found with ID: " + id));
        return mapToResponse(review);
    }

    @Transactional(readOnly = true)
    public List<CodeReviewResponse> getCodeReviewsByTask(UUID taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Task not found with ID: " + taskId);
        }
        return codeReviewRepository.findByTaskId(taskId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CodeReviewResponse> getCodeReviewsByStatus(CodeReviewStatus status) {
        return codeReviewRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CodeReviewResponse getLatestReviewForTask(UUID taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Task not found with ID: " + taskId);
        }
        CodeReview review = codeReviewRepository.findTopByTaskIdOrderByCreatedAtDesc(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("No code review found for task with ID: " + taskId));
        return mapToResponse(review);
    }

    @Transactional
    public CodeReviewResponse updateReviewStatus(UUID reviewId, UpdateCodeReviewStatusRequest request) {
        CodeReview review = codeReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Code review not found with ID: " + reviewId));

        CodeReviewStatus currentStatus = review.getStatus();
        CodeReviewStatus newStatus = request.getStatus();

        if (!isValidStatusTransition(currentStatus, newStatus)) {
            throw new InvalidTaskStateException(
                    "Invalid status transition from " + currentStatus + " to " + newStatus);
        }

        review.setStatus(newStatus);
        if (request.getOverallScore() != null) {
            review.setOverallScore(request.getOverallScore());
        }
        if (request.getSummary() != null) {
            review.setSummary(request.getSummary());
        }

        CodeReview updated = codeReviewRepository.save(review);
        return mapToResponse(updated);
    }

    @Transactional
    public CodeReviewResponse approveReview(UUID reviewId) {
        CodeReview review = codeReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Code review not found with ID: " + reviewId));

        if (review.getStatus() != CodeReviewStatus.COMPLETED) {
            throw new InvalidTaskStateException(
                    "Invalid status transition from " + review.getStatus() + " to " + CodeReviewStatus.ACCEPTED);
        }

        review.setStatus(CodeReviewStatus.ACCEPTED);
        CodeReview updated = codeReviewRepository.save(review);
        return mapToResponse(updated);
    }

    @Transactional
    public CodeReviewResponse rejectReview(UUID reviewId) {
        CodeReview review = codeReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Code review not found with ID: " + reviewId));

        if (review.getStatus() != CodeReviewStatus.COMPLETED) {
            throw new InvalidTaskStateException(
                    "Invalid status transition from " + review.getStatus() + " to " + CodeReviewStatus.REJECTED);
        }

        review.setStatus(CodeReviewStatus.REJECTED);
        CodeReview updated = codeReviewRepository.save(review);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteCodeReview(UUID reviewId) {
        CodeReview review = codeReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Code review not found with ID: " + reviewId));
        codeReviewRepository.delete(review);
    }

    private boolean isValidStatusTransition(CodeReviewStatus currentStatus, CodeReviewStatus newStatus) {
        if (currentStatus == null || newStatus == null) {
            return false;
        }
        return (currentStatus == CodeReviewStatus.REQUESTED && newStatus == CodeReviewStatus.IN_PROGRESS)
                || (currentStatus == CodeReviewStatus.IN_PROGRESS && newStatus == CodeReviewStatus.COMPLETED)
                || (currentStatus == CodeReviewStatus.COMPLETED && newStatus == CodeReviewStatus.ACCEPTED)
                || (currentStatus == CodeReviewStatus.COMPLETED && newStatus == CodeReviewStatus.REJECTED);
    }

    private CodeReviewResponse mapToResponse(CodeReview review) {
        return CodeReviewResponse.builder()
                .id(review.getId())
                .taskId(review.getTask() != null ? review.getTask().getId() : null)
                .requestedBy(review.getRequestedBy() != null ? review.getRequestedBy().getId() : null)
                .approvedBy(review.getApprovedBy() != null ? review.getApprovedBy().getId() : null)
                .status(review.getStatus())
                .reviewSource(review.getReviewSource())
                .overallScore(review.getOverallScore())
                .summary(review.getSummary())
                .sourceReference(review.getSourceReference())
                .createdAt(review.getCreatedAt())
                .createdBy(review.getCreatedBy())
                .updatedAt(review.getUpdatedAt())
                .updatedBy(review.getUpdatedBy())
                .build();
    }
}
