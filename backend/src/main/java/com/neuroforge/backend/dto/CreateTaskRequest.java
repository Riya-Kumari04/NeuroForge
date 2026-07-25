package com.neuroforge.backend.dto;

import com.neuroforge.backend.enums.TaskPriority;
import com.neuroforge.backend.enums.TaskStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskRequest {

    @NotBlank(message = "Task title is required")
    private String title;

    private String description;

    private TaskStatus status;

    private TaskPriority priority;

    @Min(value = 0, message = "Story points must be greater than or equal to 0")
    private Integer storyPoints;

    private String labels;

    private UUID sprintId;

    private UUID assigneeId;
}
