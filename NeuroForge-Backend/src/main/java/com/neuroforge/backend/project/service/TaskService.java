package com.neuroforge.backend.project.service;

import com.neuroforge.backend.dto.ApiResponse;
import com.neuroforge.backend.project.dto.CreateTaskRequest;
import com.neuroforge.backend.project.dto.TaskBoardDto;
import com.neuroforge.backend.project.dto.TaskDto;
import com.neuroforge.backend.project.dto.UpdateTaskRequest;

import java.util.List;

public interface TaskService {

    ApiResponse<TaskDto> createTask(CreateTaskRequest request);

    ApiResponse<List<TaskDto>> getAllTasks();

    ApiResponse<List<TaskDto>> getTasksByProject(Long projectId);

    // Module 3: task board view grouped by status
    ApiResponse<TaskBoardDto> getTaskBoard(Long projectId);

    ApiResponse<TaskDto> getTaskById(Long id);

    ApiResponse<TaskDto> updateTask(Long id, UpdateTaskRequest request);

    ApiResponse<Void> deleteTask(Long id);
}
