package com.neuroforge.backend.organization.repository;

import com.neuroforge.backend.entity.User;
import com.neuroforge.backend.organization.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    Optional<Organization> findBySlug(String slug);
    List<Organization> findByCreatedBy(User createdBy);
    boolean existsBySlug(String slug);
}
