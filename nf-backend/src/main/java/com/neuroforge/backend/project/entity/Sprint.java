package com.neuroforge.backend.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "sprints")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sprint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sprintName;

    @Column(columnDefinition = "TEXT")
    private String goal;

    @Column(nullable = false)
    @Builder.Default
    private String status = "PLANNED";

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    // Module 5: Actual start date (when sprint was started)
    @Column(name = "actual_start_date")
    private LocalDateTime actualStartDate;

    // Module 5: Actual end date (when sprint was completed)
    @Column(name = "actual_end_date")
    private LocalDateTime actualEndDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @OneToMany(mappedBy = "sprint", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}