package com.neuroforge.backend.integration.repository;

import com.neuroforge.backend.integration.entity.RepositoryConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositoryConnectionRepository extends JpaRepository<RepositoryConnection, Long> {

    boolean existsByRepositoryUrl(String repositoryUrl);

    List<RepositoryConnection> findByProjectId(Long projectId);

}
