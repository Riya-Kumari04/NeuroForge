package com.neuroforge.backend.project.dto;

import com.neuroforge.backend.project.entity.Task;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TaskDto {

    private Long id;

    private String title;

    private String description;

    private String priority;

    private String status;

    private Long projectId;

    private Long sprintId;

    private Long assignedToId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static TaskDto from(Task task) {

        return TaskDto.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .priority(task.getPriority())
                .status(task.getStatus())
                .projectId(task.getProject().getId())
                .sprintId(task.getSprint() != null ? task.getSprint().getId() : null)
                .assignedToId(task.getAssignedTo() != null ? task.getAssignedTo().getId() : null)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}