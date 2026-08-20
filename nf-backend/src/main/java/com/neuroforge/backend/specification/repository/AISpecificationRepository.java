package com.neuroforge.backend.specification.repository;

import com.neuroforge.backend.specification.entity.AISpecification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AISpecificationRepository extends MongoRepository<AISpecification, String> {

    Optional<AISpecification> findBySpecificationId(UUID specificationId);

    Optional<AISpecification> findByVersionId(UUID versionId);

    List<AISpecification> findBySpecificationIdOrderByCreatedAtDesc(UUID specificationId);

    void deleteBySpecificationId(UUID specificationId);
}
