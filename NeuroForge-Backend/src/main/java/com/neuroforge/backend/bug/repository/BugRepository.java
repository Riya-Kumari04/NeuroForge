package com.neuroforge.backend.bug.repository;

import com.neuroforge.backend.bug.entity.Bug;

import com.neuroforge.backend.bug.entity.BugStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BugRepository extends JpaRepository<Bug, Long> {

    List<Bug> findByStatus(BugStatus status);

    List<Bug> findBySeverity(String severity);

    List<Bug> findByTitleContainingIgnoreCase(String title);
}