package com.neuroforge.backend.project.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Module 5: Story Point Snapshot Entity
 * 
 * Stores daily snapshots of remaining story points for sprints.
 * This enables accurate burndown chart calculation based on historical data
 * rather than dynamic calculation from task timestamps.
 */
@Entity
@Table(name = "story_point_snapshots")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoryPointSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_id", nullable = false)
    private Sprint sprint;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "remaining_story_points", nullable = false)
    private Integer remainingStoryPoints;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
