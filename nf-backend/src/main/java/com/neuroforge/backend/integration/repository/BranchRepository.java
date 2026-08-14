package com.neuroforge.backend.integration.repository;

import com.neuroforge.backend.integration.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BranchRepository extends JpaRepository<Branch, Long> {

    Optional<Branch> findByBranchNameAndRepositoryConnectionId(String branchName, Long repositoryId);

}
