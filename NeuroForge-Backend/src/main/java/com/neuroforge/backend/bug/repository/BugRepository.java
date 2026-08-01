package com.neuroforge.backend.bug.repository;

import com.neuroforge.backend.bug.entity.Bug;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BugRepository extends JpaRepository<Bug, Long> {

    List<Bug> findByStatus(String status);

    List<Bug> findBySeverity(String severity);
}