package com.neuroforge.backend.project.controller;

import com.neuroforge.backend.dto.ApiResponse;
import com.neuroforge.backend.project.dto.CreateTaskRequest;
import com.neuroforge.backend.project.dto.TaskDto;
import com.neuroforge.backend.project.dto.UpdateTaskRequest;
import com.neuroforge.backend.project.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Task Management")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @Operation(summary = "Create Task")
    public ResponseEntity<ApiResponse<TaskDto>> createTask(
            @Valid @RequestBody CreateTaskRequest request) {

        return ResponseEntity.ok(taskService.createTask(request));
    }

    @GetMapping
    @Operation(summary = "Get All Tasks")
    public ResponseEntity<ApiResponse<List<TaskDto>>> getAllTasks() {

        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Task By Id")
    public ResponseEntity<ApiResponse<TaskDto>> getTaskById(
            @PathVariable Long id) {

        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Get Tasks By Project")
    public ResponseEntity<ApiResponse<List<TaskDto>>> getTasksByProject(
            @PathVariable Long projectId) {

        return ResponseEntity.ok(taskService.getTasksByProject(projectId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Task")
    public ResponseEntity<ApiResponse<TaskDto>> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskRequest request) {

        return ResponseEntity.ok(taskService.updateTask(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Task")
    public ResponseEntity<ApiResponse<Void>> deleteTask(
            @PathVariable Long id) {

        return ResponseEntity.ok(taskService.deleteTask(id));
    }
}