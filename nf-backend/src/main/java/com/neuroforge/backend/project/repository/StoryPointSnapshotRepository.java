package com.neuroforge.backend.project.repository;

import com.neuroforge.backend.project.entity.StoryPointSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Module 5: Story Point Snapshot Repository
 * 
 * Provides data access for story point snapshots used in burndown calculations.
 */
@Repository
public interface StoryPointSnapshotRepository extends JpaRepository<StoryPointSnapshot, Long> {

    /**
     * Find all snapshots for a specific sprint, ordered by snapshot date
     */
    List<StoryPointSnapshot> findBySprintIdOrderBySnapshotDateAsc(Long sprintId);

    /**
     * Find a snapshot for a specific sprint on a specific date
     */
    StoryPointSnapshot findBySprintIdAndSnapshotDate(Long sprintId, LocalDate snapshotDate);

    /**
     * Check if a snapshot exists for a specific sprint on a specific date
     */
    boolean existsBySprintIdAndSnapshotDate(Long sprintId, LocalDate snapshotDate);

    /**
     * Find the latest snapshot for a sprint
     */
    StoryPointSnapshot findFirstBySprintIdOrderBySnapshotDateDesc(Long sprintId);
}
