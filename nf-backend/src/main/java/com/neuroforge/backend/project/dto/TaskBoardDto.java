package com.neuroforge.backend.project.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TaskBoardDto {

    private List<TaskDto> todo;

    private List<TaskDto> inProgress;

    private List<TaskDto> done;

}