package com.neuroforge.backend.project.dto;

import com.neuroforge.backend.project.entity.Sprint;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SprintDto {

    private Long id;

    private String sprintName;

    private String goal;

    private String status;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    // Module 5: Actual start date
    private LocalDateTime actualStartDate;

    // Module 5: Actual end date
    private LocalDateTime actualEndDate;

    private Long projectId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static SprintDto from(Sprint sprint) {

        return SprintDto.builder()
                .id(sprint.getId())
                .sprintName(sprint.getSprintName())
                .goal(sprint.getGoal())
                .status(sprint.getStatus())
                .startDate(sprint.getStartDate())
                .endDate(sprint.getEndDate())
                .actualStartDate(sprint.getActualStartDate())
                .actualEndDate(sprint.getActualEndDate())
                .projectId(sprint.getProject().getId())
                .createdAt(sprint.getCreatedAt())
                .updatedAt(sprint.getUpdatedAt())
                .build();
    }
}