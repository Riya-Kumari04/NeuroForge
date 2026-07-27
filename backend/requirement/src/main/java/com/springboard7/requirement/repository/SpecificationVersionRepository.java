package com.springboard7.requirement.repository;

import com.springboard7.requirement.entity.SpecificationVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpecificationVersionRepository
        extends JpaRepository<SpecificationVersion, UUID> {

    List<SpecificationVersion> findBySpecificationIdOrderByVersionNumberDesc(UUID specificationId);

    Optional<SpecificationVersion> findBySpecificationIdAndVersionNumber(
            UUID specificationId,
            Integer versionNumber
    );

    Optional<SpecificationVersion> findTopBySpecificationIdOrderByVersionNumberDesc(
            UUID specificationId
    );

}