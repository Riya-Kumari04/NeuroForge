package com.neuroforge.backend.project.repository;

import com.neuroforge.backend.ai.enums.CodeReviewStatus;
import com.neuroforge.backend.project.entity.CodeReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CodeReviewRepository extends JpaRepository<CodeReview, Long> {

    List<CodeReview> findByTaskId(Long taskId);

    List<CodeReview> findByStatus(CodeReviewStatus status);

    Optional<CodeReview> findTopByTaskIdOrderByCreatedAtDesc(Long taskId);
}
