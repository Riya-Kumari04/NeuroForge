package com.neuroforge.backend.project.service;

import com.neuroforge.backend.dto.ApiResponse;
import com.neuroforge.backend.exception.AppException;
import com.neuroforge.backend.project.dto.*;
import com.neuroforge.backend.project.dto.TaskBoardEvent;
import com.neuroforge.backend.project.entity.Project;
import com.neuroforge.backend.project.entity.ProjectMember;
import com.neuroforge.backend.project.entity.Sprint;
import com.neuroforge.backend.project.entity.Task;
import com.neuroforge.backend.project.entity.TaskStatusHistory;
import com.neuroforge.backend.project.entity.CodeReview;
import com.neuroforge.backend.ai.enums.CodeReviewStatus;
import com.neuroforge.backend.project.repository.CodeReviewRepository;
import com.neuroforge.backend.project.repository.ProjectMemberRepository;
import com.neuroforge.backend.project.repository.ProjectRepository;
import com.neuroforge.backend.project.repository.SprintRepository;
import com.neuroforge.backend.project.repository.TaskRepository;
import com.neuroforge.backend.project.repository.TaskStatusHistoryRepository;
import com.neuroforge.backend.specification.repository.SpecificationRepository;
import com.neuroforge.backend.specification.repository.SpecificationVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final SprintRepository sprintRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskStatusHistoryRepository taskStatusHistoryRepository;
    private final CodeReviewRepository codeReviewRepository;
    private final SpecificationRepository specificationRepository;
    private final SpecificationVersionRepository specificationVersionRepository;
    private final BoardEventPublisher boardEventPublisher;

    @Override
    @Transactional
    public ApiResponse<TaskDto> createTask(CreateTaskRequest request) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> AppException.notFound("Project not found"));
        Sprint sprint = null;
        if (request.getSprintId() != null) {
            sprint = sprintRepository.findById(request.getSprintId())
                    .orElseThrow(() -> AppException.notFound("Sprint not found"));
        }
        ProjectMember member = null;
        if (request.getAssignedToId() != null) {
            member = projectMemberRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> AppException.notFound("Project Member not found"));
        }
        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority() == null ? "MEDIUM" : request.getPriority())
                .status(request.getStatus() == null ? "TODO" : request.getStatus())
                .storyPoints(request.getStoryPoints())
                .labels(request.getLabels())
                .specificationId(request.getSpecificationId())
                .specificationVersionId(request.getSpecificationVersionId())
                .project(project).sprint(sprint).assignedTo(member)
                .build();
        task = taskRepository.save(task);
        return ApiResponse.ok("Task created successfully", TaskDto.from(task, specificationRepository, specificationVersionRepository));
    }

    @Override
    public ApiResponse<List<TaskDto>> getAllTasks() {
        List<TaskDto> tasks = taskRepository.findAll()
                .stream().map(t -> TaskDto.from(t, specificationRepository, specificationVersionRepository)).collect(Collectors.toList());
        return ApiResponse.ok("Tasks retrieved successfully", tasks);
    }

    @Override
    public ApiResponse<List<TaskDto>> getTasksByProject(Long projectId) {
        List<TaskDto> tasks = taskRepository.findByProjectId(projectId)
                .stream().map(t -> TaskDto.from(t, specificationRepository, specificationVersionRepository)).collect(Collectors.toList());
        return ApiResponse.ok("Project tasks retrieved", tasks);
    }

    @Override
    public ApiResponse<List<TaskDto>> getTasksBySprint(Long sprintId) {
        List<TaskDto> tasks = taskRepository.findBySprintId(sprintId)
                .stream().map(t -> TaskDto.from(t, specificationRepository, specificationVersionRepository)).collect(Collectors.toList());
        return ApiResponse.ok("Sprint tasks retrieved", tasks);
    }

    // ── Module 3: Task Board (grouped by status) ──────────────────────────────

    @Override
    public ApiResponse<TaskBoardDto> getTaskBoard(Long projectId) {
        projectRepository.findById(projectId)
                .orElseThrow(() -> AppException.notFound("Project not found"));
        List<TaskDto> todo      = taskRepository.findByProjectIdAndStatus(projectId, "TODO")
                .stream().map(t -> TaskDto.from(t, specificationRepository, specificationVersionRepository)).collect(Collectors.toList());
        List<TaskDto> inProgress = taskRepository.findByProjectIdAndStatus(projectId, "IN_PROGRESS")
                .stream().map(t -> TaskDto.from(t, specificationRepository, specificationVersionRepository)).collect(Collectors.toList());
        List<TaskDto> done      = taskRepository.findByProjectIdAndStatus(projectId, "DONE")
                .stream().map(t -> TaskDto.from(t, specificationRepository, specificationVersionRepository)).collect(Collectors.toList());
        TaskBoardDto board = TaskBoardDto.builder()
                .todo(todo).inProgress(inProgress).done(done).build();
        return ApiResponse.ok("Task board retrieved", board);
    }

    @Override
    public ApiResponse<TaskDto> getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("Task not found"));
        return ApiResponse.ok("Task found", TaskDto.from(task, specificationRepository, specificationVersionRepository));
    }

    @Override
    @Transactional
    public ApiResponse<TaskDto> updateTask(Long id, UpdateTaskRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("Task not found"));

        String currentStatus = task.getStatus();
        String newStatus = request.getStatus();

        // Track status change if status is being updated
        if (newStatus != null && !newStatus.equals(currentStatus)) {
            // Module 5: Validate status transition
            if (!isValidStatusTransition(currentStatus, newStatus)) {
                throw AppException.badRequest(
                        "Invalid task status transition from " + currentStatus + " to " + newStatus);
            }

            // Module 5: QA-only restriction for moving tasks to DONE
            if ("DONE".equals(newStatus) && !hasRole("ROLE_QA")) {
                throw AppException.forbidden("Only QA users can move tasks to DONE status");
            }

            task.setStatus(newStatus);

            // Module 5: Track status history
            String currentUser = getCurrentUsername();
            TaskStatusHistory history = TaskStatusHistory.builder()
                    .task(task)
                    .previousStatus(currentStatus)
                    .newStatus(newStatus)
                    .changedBy(currentUser)
                    .changedAt(LocalDateTime.now())
                    .build();
            taskStatusHistoryRepository.save(history);

            // Module 5: Publish WebSocket event for real-time board synchronization
            TaskBoardEvent event = TaskBoardEvent.builder()
                    .taskId(task.getId())
                    .projectId(task.getProject().getId())
                    .previousStatus(currentStatus)
                    .newStatus(newStatus)
                    .changedBy(currentUser)
                    .timestamp(LocalDateTime.now())
                    .build();
            boardEventPublisher.publishTaskUpdate(event);
        }

        if (request.getTitle() != null)       task.setTitle(request.getTitle());
        if (request.getDescription() != null) task.setDescription(request.getDescription());
        if (request.getPriority() != null)    task.setPriority(request.getPriority());
        if (request.getStoryPoints() != null) task.setStoryPoints(request.getStoryPoints());
        if (request.getLabels() != null)      task.setLabels(request.getLabels());
        if (request.getSpecificationId() != null) task.setSpecificationId(request.getSpecificationId());
        if (request.getSpecificationVersionId() != null) task.setSpecificationVersionId(request.getSpecificationVersionId());
        if (request.getSprintId() != null) {
            Sprint sprint = sprintRepository.findById(request.getSprintId())
                    .orElseThrow(() -> AppException.notFound("Sprint not found"));
            task.setSprint(sprint);
        }
        if (request.getAssignedToId() != null) {
            ProjectMember member = projectMemberRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> AppException.notFound("Project Member not found"));
            task.setAssignedTo(member);
        }
        task = taskRepository.save(task);
        return ApiResponse.ok("Task updated successfully", TaskDto.from(task, specificationRepository, specificationVersionRepository));
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("Task not found"));
        
        // Clear relationships to avoid foreign key constraint issues
        task.setSprint(null);
        task.setAssignedTo(null);
        taskRepository.save(task);
        
        taskRepository.deleteById(id);
        return ApiResponse.ok("Task deleted successfully");
    }

    @Override
    @Transactional
    public ApiResponse<TaskDto> updateTaskStatus(Long taskId, UpdateTaskStatusRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> AppException.notFound("Task not found"));

        String currentStatus = task.getStatus();
        String newStatus = request.getStatus();

        if (!isValidStatusTransition(currentStatus, newStatus)) {
            throw AppException.badRequest(
                    "Invalid task status transition from " + currentStatus + " to " + newStatus);
        }

        // Module 5: QA-only restriction for moving tasks to DONE
        if ("DONE".equals(newStatus) && !hasRole("ROLE_QA")) {
            throw AppException.forbidden("Only QA users can move tasks to DONE status");
        }

        // Module 8: Workflow gate - prevent TESTING transition unless code review is ACCEPTED
        if ("CODE_REVIEW".equals(currentStatus) && "TESTING".equals(newStatus)) {
            CodeReview latestReview = codeReviewRepository.findTopByTaskIdOrderByCreatedAtDesc(task.getId())
                    .orElse(null);
            
            if (latestReview == null || latestReview.getStatus() != CodeReviewStatus.ACCEPTED) {
                String reviewStatus = latestReview != null ? latestReview.getStatus().name() : "NOT_SUBMITTED";
                throw AppException.forbidden(
                        "Cannot move task to TESTING status. Code review must be ACCEPTED. Current review status: " + reviewStatus);
            }
        }

        task.setStatus(newStatus);
        Task updated = taskRepository.save(task);

        // Module 5: Track status history
        String currentUser = getCurrentUsername();
        TaskStatusHistory history = TaskStatusHistory.builder()
                .task(updated)
                .previousStatus(currentStatus)
                .newStatus(newStatus)
                .changedBy(currentUser)
                .changedAt(LocalDateTime.now())
                .build();
        taskStatusHistoryRepository.save(history);

        // Module 5: Publish WebSocket event for real-time board synchronization
        TaskBoardEvent event = TaskBoardEvent.builder()
                .taskId(updated.getId())
                .projectId(updated.getProject().getId())
                .previousStatus(currentStatus)
                .newStatus(newStatus)
                .changedBy(currentUser)
                .timestamp(LocalDateTime.now())
                .build();
        boardEventPublisher.publishTaskUpdate(event);

        return ApiResponse.ok("Task status updated successfully", TaskDto.from(updated));
    }

    @Override
    public ApiResponse<List<TaskStatusHistoryResponse>> getTaskStatusHistory(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw AppException.notFound("Task not found");
        }
        List<TaskStatusHistory> history = taskStatusHistoryRepository.findByTaskIdOrderByChangedAtAsc(taskId);
        List<TaskStatusHistoryResponse> response = history.stream()
                .map(this::mapToHistoryResponse)
                .collect(Collectors.toList());
        return ApiResponse.ok("Task status history retrieved", response);
    }

    // Module 5: Workflow validation
    // Module 8: Workflow gate - prevent TESTING transition unless code review is ACCEPTED
    private boolean isValidStatusTransition(String currentStatus, String newStatus) {
        if (currentStatus == null || newStatus == null) {
            return false;
        }
        if (currentStatus.equals(newStatus)) {
            return true;
        }
        // Valid transitions: TODO -> IN_PROGRESS -> CODE_REVIEW -> TESTING -> DONE
        boolean validTransition = ("TODO".equals(currentStatus) && "IN_PROGRESS".equals(newStatus))
                || ("IN_PROGRESS".equals(currentStatus) && "CODE_REVIEW".equals(newStatus))
                || ("CODE_REVIEW".equals(currentStatus) && "TESTING".equals(newStatus))
                || ("TESTING".equals(currentStatus) && "DONE".equals(newStatus))
                || ("CODE_REVIEW".equals(currentStatus) && "IN_PROGRESS".equals(newStatus))
                || ("IN_PROGRESS".equals(currentStatus) && "TODO".equals(newStatus));

        // Module 8: Workflow gate - check code review status before allowing TESTING transition
        if (validTransition && "CODE_REVIEW".equals(currentStatus) && "TESTING".equals(newStatus)) {
            // This check will be done in the calling method with task context
            return true;
        }

        return validTransition;
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

    // Module 5: Helper methods for role-based authorization
    private boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return authorities.stream()
                .anyMatch(authority -> authority.getAuthority().equals(role));
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "SYSTEM";
        }
        return authentication.getName();
    }
}
