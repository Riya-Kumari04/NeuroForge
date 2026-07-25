package com.neuroforge.backend.service;

import com.neuroforge.backend.dto.CreateTaskRequest;
import com.neuroforge.backend.dto.TaskResponse;
import com.neuroforge.backend.entity.Sprint;
import com.neuroforge.backend.entity.Task;
import com.neuroforge.backend.entity.User;
import com.neuroforge.backend.enums.TaskPriority;
import com.neuroforge.backend.enums.TaskStatus;
import com.neuroforge.backend.exception.ResourceNotFoundException;
import com.neuroforge.backend.repository.SprintRepository;
import com.neuroforge.backend.repository.TaskRepository;
import com.neuroforge.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final SprintRepository sprintRepository;
    private final UserRepository userRepository;

    @Transactional
    public TaskResponse createTask(CreateTaskRequest request) {
        Sprint sprint = null;
        if (request.getSprintId() != null) {
            sprint = sprintRepository.findById(request.getSprintId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sprint not found with ID: " + request.getSprintId()));
        }

        User assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getAssigneeId()));
        }

        TaskStatus status = request.getStatus() != null ? request.getStatus() : TaskStatus.TODO;
        TaskPriority priority = request.getPriority() != null ? request.getPriority() : TaskPriority.MEDIUM;

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(status)
                .priority(priority)
                .storyPoints(request.getStoryPoints())
                .labels(request.getLabels())
                .sprint(sprint)
                .assignee(assignee)
                .build();

        Task saved = taskRepository.save(task);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(UUID id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + id));
        return mapToResponse(task);
    }

    @Transactional
    public TaskResponse updateTask(UUID id, CreateTaskRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + id));

        if (request.getSprintId() != null) {
            Sprint sprint = sprintRepository.findById(request.getSprintId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sprint not found with ID: " + request.getSprintId()));
            task.setSprint(sprint);
        } else {
            task.setSprint(null);
        }

        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getAssigneeId()));
            task.setAssignee(assignee);
        } else {
            task.setAssignee(null);
        }

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        task.setStoryPoints(request.getStoryPoints());
        task.setLabels(request.getLabels());

        Task updated = taskRepository.save(task);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteTask(UUID id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + id));
        taskRepository.delete(task);
    }

    private TaskResponse mapToResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .storyPoints(task.getStoryPoints())
                .labels(task.getLabels())
                .sprintId(task.getSprint() != null ? task.getSprint().getId() : null)
                .assigneeId(task.getAssignee() != null ? task.getAssignee().getId() : null)
                .createdAt(task.getCreatedAt())
                .createdBy(task.getCreatedBy())
                .updatedAt(task.getUpdatedAt())
                .updatedBy(task.getUpdatedBy())
                .build();
    }
}
