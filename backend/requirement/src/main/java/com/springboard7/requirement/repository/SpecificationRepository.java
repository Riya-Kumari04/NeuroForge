package com.springboard7.requirement.repository;

import com.springboard7.requirement.entity.Specification;
import com.springboard7.requirement.enums.SpecificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface SpecificationRepository extends JpaRepository<Specification, UUID> {

    boolean existsByTitleIgnoreCase(String title);

    Optional<Specification> findBySpecificationKey(String specificationKey);

    Page<Specification> findByStatus(SpecificationStatus status, Pageable pageable);

    Page<Specification> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Specification> findByDeletedFalse(Pageable pageable);

    Optional<Specification> findByIdAndDeletedFalse(UUID id);

    Page<Specification> findByDeletedFalseAndTitleContainingIgnoreCase(
            String title,
            Pageable pageable
    );

    Page<Specification> findByDeletedFalseAndStatus(
            SpecificationStatus status,
            Pageable pageable
    );


    @Query(value = "SELECT nextval('specification_key_seq')", nativeQuery = true)
    Long getNextSpecificationSequence();


    Page<Specification> findByDeletedFalseAndTitleContainingIgnoreCaseAndStatus(
            String title,
            SpecificationStatus status,
            Pageable pageable
    );

}