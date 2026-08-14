package com.neuroforge.backend.integration.repository;

import com.neuroforge.backend.integration.entity.PullRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PullRequestRepository extends JpaRepository<PullRequest, Long> {

    Optional<PullRequest> findByPrNumberAndRepositoryConnectionId(Integer prNumber, Long repositoryId);

}
