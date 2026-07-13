package com.neuroforge.backend.project.service;

import com.neuroforge.backend.dto.ApiResponse;
import com.neuroforge.backend.exception.AppException;
import com.neuroforge.backend.project.dto.CreateTaskRequest;
import com.neuroforge.backend.project.dto.TaskDto;
import com.neuroforge.backend.project.dto.UpdateTaskRequest;
import com.neuroforge.backend.project.entity.Project;
import com.neuroforge.backend.project.entity.ProjectMember;
import com.neuroforge.backend.project.entity.Sprint;
import com.neuroforge.backend.project.entity.Task;
import com.neuroforge.backend.project.repository.ProjectMemberRepository;
import com.neuroforge.backend.project.repository.ProjectRepository;
import com.neuroforge.backend.project.repository.SprintRepository;
import com.neuroforge.backend.project.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final SprintRepository sprintRepository;
    private final ProjectMemberRepository projectMemberRepository;

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
                .project(project)
                .sprint(sprint)
                .assignedTo(member)
                .build();

        task = taskRepository.save(task);

        return ApiResponse.ok("Task created successfully", TaskDto.from(task));
    }

    @Override
    public ApiResponse<List<TaskDto>> getAllTasks() {

        List<TaskDto> tasks = taskRepository.findAll()
                .stream()
                .map(TaskDto::from)
                .collect(Collectors.toList());

        return ApiResponse.ok("Tasks retrieved successfully", tasks);
    }

    @Override
    public ApiResponse<List<TaskDto>> getTasksByProject(Long projectId) {

        List<TaskDto> tasks = taskRepository.findByProjectId(projectId)
                .stream()
                .map(TaskDto::from)
                .collect(Collectors.toList());

        return ApiResponse.ok("Project tasks retrieved", tasks);
    }

    @Override
    public ApiResponse<TaskDto> getTaskById(Long id) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("Task not found"));

        return ApiResponse.ok("Task found", TaskDto.from(task));
    }

    @Override
    @Transactional
    public ApiResponse<TaskDto> updateTask(Long id, UpdateTaskRequest request) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("Task not found"));

        if (request.getTitle() != null)
            task.setTitle(request.getTitle());

        if (request.getDescription() != null)
            task.setDescription(request.getDescription());

        if (request.getPriority() != null)
            task.setPriority(request.getPriority());

        if (request.getStatus() != null)
            task.setStatus(request.getStatus());

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

        return ApiResponse.ok("Task updated successfully", TaskDto.from(task));
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteTask(Long id) {

        if (!taskRepository.existsById(id))
            throw AppException.notFound("Task not found");

        taskRepository.deleteById(id);

        return ApiResponse.ok("Task deleted successfully");
    }
}