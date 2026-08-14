package com.neuroforge.backend.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Task Key for Module 7 commit linking (e.g., NF-123)
    @Column(name = "task_key", unique = true, nullable = false)
    private String taskKey;

    // Task Title
    @Column(nullable = false)
    private String title;

    // Task Description
    @Column(columnDefinition = "TEXT")
    private String description;

    // Priority (LOW, MEDIUM, HIGH)
    @Column(nullable = false)
    @Builder.Default
    private String priority = "MEDIUM";

    // Status (TODO, IN_PROGRESS, CODE_REVIEW, TESTING, DONE)
    @Column(nullable = false)
    @Builder.Default
    private String status = "TODO";

    // Module 5: Story Points
    @Column(name = "story_points")
    private Integer storyPoints;

    // Module 5: Labels
    @Column(name = "labels")
    private String labels;

    // Module 4: Specification Traceability
    @Column(name = "specification_id")
    private UUID specificationId;

    @Column(name = "specification_version_id")
    private UUID specificationVersionId;

    // Project
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // Sprint (optional)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_id")
    private Sprint sprint;

    // Assigned Project Member
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_member_id")
    private ProjectMember assignedTo;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}