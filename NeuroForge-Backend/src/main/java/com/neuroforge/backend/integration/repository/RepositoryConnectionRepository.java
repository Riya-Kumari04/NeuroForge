package com.neuroforge.backend.integration.repository;

import com.neuroforge.backend.integration.entity.RepositoryConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepositoryConnectionRepository extends JpaRepository<RepositoryConnection, Long> {

    Optional<RepositoryConnection> findByRepositoryUrl(String repositoryUrl);

    boolean existsByRepositoryUrl(String repositoryUrl);

}