package com.neuroforge.backend.service;

import com.neuroforge.backend.dto.CreateTaskRequest;
import com.neuroforge.backend.dto.TaskResponse;
import com.neuroforge.backend.dto.TaskStatusHistoryResponse;
import com.neuroforge.backend.dto.UpdateTaskStatusRequest;
import com.neuroforge.backend.entity.Sprint;
import com.neuroforge.backend.entity.Task;
import com.neuroforge.backend.entity.TaskStatusHistory;
import com.neuroforge.backend.entity.User;
import com.neuroforge.backend.enums.TaskPriority;
import com.neuroforge.backend.enums.TaskStatus;
import com.neuroforge.backend.exception.InvalidTaskStateException;
import com.neuroforge.backend.exception.ResourceNotFoundException;
import com.neuroforge.backend.repository.SprintRepository;
import com.neuroforge.backend.repository.TaskRepository;
import com.neuroforge.backend.repository.TaskStatusHistoryRepository;
import com.neuroforge.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final SprintRepository sprintRepository;
    private final UserRepository userRepository;
    private final TaskStatusHistoryRepository taskStatusHistoryRepository;
    private final CurrentUserContextService currentUserContextService;

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

    @Transactional(readOnly = true)
    public List<TaskResponse> getBacklogTasks() {
        return taskRepository.findBySprintIsNull().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TaskResponse assignSprint(UUID taskId, UUID sprintId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found with ID: " + sprintId));

        task.setSprint(sprint);
        Task updated = taskRepository.save(task);
        return mapToResponse(updated);
    }

    @Transactional
    public TaskResponse removeSprint(UUID taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

        task.setSprint(null);
        Task updated = taskRepository.save(task);
        return mapToResponse(updated);
    }

    @Transactional
    public TaskResponse assignUser(UUID taskId, UUID userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        task.setAssignee(user);
        Task updated = taskRepository.save(task);
        return mapToResponse(updated);
    }

    @Transactional
    public TaskResponse removeUser(UUID taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

        task.setAssignee(null);
        Task updated = taskRepository.save(task);
        return mapToResponse(updated);
    }

    @Transactional
    public TaskResponse updateTaskStatus(UUID taskId, UpdateTaskStatusRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

        TaskStatus currentStatus = task.getStatus();
        TaskStatus newStatus = request.getStatus();

        if (!isValidStatusTransition(currentStatus, newStatus)) {
            throw new InvalidTaskStateException(
                    "Invalid task status transition from " + currentStatus + " to " + newStatus);
        }

        task.setStatus(newStatus);
        Task updated = taskRepository.save(task);

        String changedBy = getChangedBy();
        TaskStatusHistory history = TaskStatusHistory.builder()
                .task(updated)
                .previousStatus(currentStatus)
                .newStatus(newStatus)
                .changedBy(changedBy)
                .changedAt(LocalDateTime.now())
                .build();
        taskStatusHistoryRepository.save(history);

        return mapToResponse(updated);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByStatus(TaskStatus status) {
        return taskRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByPriority(TaskPriority priority) {
        return taskRepository.findByPriority(priority).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksBySprint(UUID sprintId) {
        if (!sprintRepository.existsById(sprintId)) {
            throw new ResourceNotFoundException("Sprint not found with ID: " + sprintId);
        }
        return taskRepository.findBySprintId(sprintId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByAssignee(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
        return taskRepository.findByAssigneeId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> searchTasks(String keyword) {
        String searchKw = keyword != null ? keyword : "";
        return taskRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(searchKw, searchKw).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private boolean isValidStatusTransition(TaskStatus currentStatus, TaskStatus newStatus) {
        if (currentStatus == null || newStatus == null) {
            return false;
        }
        return (currentStatus == TaskStatus.TODO && newStatus == TaskStatus.IN_PROGRESS)
                || (currentStatus == TaskStatus.IN_PROGRESS && newStatus == TaskStatus.CODE_REVIEW)
                || (currentStatus == TaskStatus.CODE_REVIEW && newStatus == TaskStatus.TESTING)
                || (currentStatus == TaskStatus.TESTING && newStatus == TaskStatus.DONE);
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

    @Transactional(readOnly = true)
    public List<TaskStatusHistoryResponse> getTaskStatusHistory(UUID taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Task not found with ID: " + taskId);
        }
        return taskStatusHistoryRepository.findByTaskIdOrderByChangedAtAsc(taskId).stream()
                .map(this::mapToHistoryResponse)
                .collect(Collectors.toList());
    }

    private String getChangedBy() {
        if (currentUserContextService != null) {
            try {
                String email = currentUserContextService.getCurrentUserEmail();
                if (email != null && !email.isBlank()) {
                    return email;
                }
            } catch (Exception ignored) {
            }
        }
        return "SYSTEM";
    }

    private TaskStatusHistoryResponse mapToHistoryResponse(TaskStatusHistory history) {
        return TaskStatusHistoryResponse.builder()
                .id(history.getId())
                .taskId(history.getTask() != null ? history.getTask().getId() : null)
                .previousStatus(history.getPreviousStatus())
                .newStatus(history.getNewStatus())
                .changedBy(history.getChangedBy())
                .changedAt(history.getChangedAt())
                .build();
    }
}
