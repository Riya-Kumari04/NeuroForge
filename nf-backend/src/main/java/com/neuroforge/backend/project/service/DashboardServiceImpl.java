package com.neuroforge.backend.project.service;

import com.neuroforge.backend.dto.ApiResponse;
import com.neuroforge.backend.project.dto.DashboardDto;
import com.neuroforge.backend.project.repository.ProjectRepository;
import com.neuroforge.backend.project.repository.SprintRepository;
import com.neuroforge.backend.project.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ProjectRepository projectRepository;
    private final SprintRepository sprintRepository;
    private final TaskRepository taskRepository;

    @Override
    public ApiResponse<DashboardDto> getDashboard() {

        long totalProjects = projectRepository.count();

        long activeProjects = projectRepository.countByStatus("ACTIVE");

        long completedProjects = projectRepository.countByStatus("COMPLETED");

        long totalSprints = sprintRepository.count();

        long totalTasks = taskRepository.count();

        long completedTasks = taskRepository.countByStatus("DONE");

        long pendingTasks = totalTasks - completedTasks;

        double overallProgress = 0;

        if (totalTasks > 0) {
            overallProgress = (completedTasks * 100.0) / totalTasks;
        }

        DashboardDto dashboard = DashboardDto.builder()
                .totalProjects(totalProjects)
                .activeProjects(activeProjects)
                .completedProjects(completedProjects)
                .totalSprints(totalSprints)
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .pendingTasks(pendingTasks)
                .overallProgress(overallProgress)
                .build();

        return ApiResponse.ok("Dashboard fetched successfully", dashboard);
    }
}