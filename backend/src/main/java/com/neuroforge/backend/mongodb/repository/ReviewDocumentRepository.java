package com.neuroforge.backend.mongodb.repository;

import com.neuroforge.backend.mongodb.document.ReviewDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewDocumentRepository extends MongoRepository<ReviewDocument, String> {

    Optional<ReviewDocument> findByReviewId(UUID reviewId);

    List<ReviewDocument> findByTaskId(UUID taskId);
}
