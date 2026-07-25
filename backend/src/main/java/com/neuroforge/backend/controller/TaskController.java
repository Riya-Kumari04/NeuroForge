package com.neuroforge.backend.controller;

import com.neuroforge.backend.dto.CreateTaskRequest;
import com.neuroforge.backend.dto.TaskResponse;
import com.neuroforge.backend.dto.UpdateTaskStatusRequest;
import com.neuroforge.backend.enums.TaskPriority;
import com.neuroforge.backend.enums.TaskStatus;
import com.neuroforge.backend.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request) {
        TaskResponse response = taskService.createTask(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAllTasks() {
        List<TaskResponse> response = taskService.getAllTasks();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/backlog")
    public ResponseEntity<List<TaskResponse>> getBacklogTasks() {
        List<TaskResponse> response = taskService.getBacklogTasks();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<TaskResponse>> searchTasks(@RequestParam(required = false) String keyword) {
        List<TaskResponse> response = taskService.searchTasks(keyword);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<TaskResponse>> getTasksByStatus(@PathVariable TaskStatus status) {
        List<TaskResponse> response = taskService.getTasksByStatus(status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<TaskResponse>> getTasksByPriority(@PathVariable TaskPriority priority) {
        List<TaskResponse> response = taskService.getTasksByPriority(priority);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sprint/{sprintId}")
    public ResponseEntity<List<TaskResponse>> getTasksBySprint(@PathVariable UUID sprintId) {
        List<TaskResponse> response = taskService.getTasksBySprint(sprintId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/assignee/{userId}")
    public ResponseEntity<List<TaskResponse>> getTasksByAssignee(@PathVariable UUID userId) {
        List<TaskResponse> response = taskService.getTasksByAssignee(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable UUID id) {
        TaskResponse response = taskService.getTaskById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable UUID id,
            @Valid @RequestBody CreateTaskRequest request) {
        TaskResponse response = taskService.updateTask(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{taskId}/assign-sprint/{sprintId}")
    public ResponseEntity<TaskResponse> assignSprint(@PathVariable UUID taskId, @PathVariable UUID sprintId) {
        TaskResponse response = taskService.assignSprint(taskId, sprintId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{taskId}/remove-sprint")
    public ResponseEntity<TaskResponse> removeSprint(@PathVariable UUID taskId) {
        TaskResponse response = taskService.removeSprint(taskId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{taskId}/assign-user/{userId}")
    public ResponseEntity<TaskResponse> assignUser(@PathVariable UUID taskId, @PathVariable UUID userId) {
        TaskResponse response = taskService.assignUser(taskId, userId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{taskId}/remove-user")
    public ResponseEntity<TaskResponse> removeUser(@PathVariable UUID taskId) {
        TaskResponse response = taskService.removeUser(taskId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{taskId}/status")
    public ResponseEntity<TaskResponse> updateTaskStatus(
            @PathVariable UUID taskId,
            @Valid @RequestBody UpdateTaskStatusRequest request) {
        TaskResponse response = taskService.updateTaskStatus(taskId, request);
        return ResponseEntity.ok(response);
    }
}
