package com.neuroforge.backend.analytics.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "velocity_history", indexes = {
    @Index(name = "idx_sprint_id", columnList = "sprint_id", unique = true),
    @Index(name = "idx_sprint_end_date", columnList = "sprint_end_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VelocityHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "sprint_id", nullable = false, unique = true)
    private Long sprintId;

    @Column(name = "sprint_name")
    private String sprintName;

    @Column(name = "completed_story_points")
    private Integer completedStoryPoints;

    @Column(name = "completed_tasks")
    private Long completedTasks;

    @Column(name = "sprint_end_date")
    private LocalDate sprintEndDate;
}
