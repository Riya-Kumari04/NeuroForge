package com.neuroforge.backend.repository;

import com.neuroforge.backend.entity.CodeReview;
import com.neuroforge.backend.enums.CodeReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CodeReviewRepository extends JpaRepository<CodeReview, UUID> {

    List<CodeReview> findByTaskId(UUID taskId);

    List<CodeReview> findByStatus(CodeReviewStatus status);

    Optional<CodeReview> findTopByTaskIdOrderByCreatedAtDesc(UUID taskId);
}
