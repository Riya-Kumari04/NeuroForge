package com.neuroforge.backend.analytics.service;

import com.neuroforge.backend.analytics.dto.VelocityHistoryResponse;
import com.neuroforge.backend.analytics.dto.VelocityPointResponse;
import com.neuroforge.backend.analytics.entity.VelocityHistory;
import com.neuroforge.backend.analytics.repository.VelocityHistoryRepository;
import com.neuroforge.backend.project.entity.Sprint;
import com.neuroforge.backend.project.repository.SprintRepository;
import com.neuroforge.backend.project.repository.TaskRepository;
import com.neuroforge.backend.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VelocityHistoryServiceImpl implements VelocityHistoryService {

    private final VelocityHistoryRepository velocityHistoryRepository;
    private final SprintRepository sprintRepository;
    private final TaskRepository taskRepository;

    @Override
    public void refreshVelocityHistory() {
        List<Sprint> sprints = sprintRepository.findAllByOrderByStartDateAsc();

        if (sprints == null || sprints.isEmpty()) {
            return;
        }

        for (Sprint sprint : sprints) {
            if (sprint == null || sprint.getId() == null || !"COMPLETED".equals(sprint.getStatus())) {
                continue;
            }

            Long sprintId = sprint.getId();
            String sprintName = sprint.getSprintName();
            Integer completedPointsObj = taskRepository.getStoryPointsBySprintAndStatus(sprintId, "DONE");
            int completedStoryPoints = completedPointsObj != null ? completedPointsObj : 0;
            long completedTasks = taskRepository.countBySprintIdAndStatus(sprintId, "DONE");
            LocalDate sprintEndDate = sprint.getActualEndDate() != null ? sprint.getActualEndDate().toLocalDate() : (sprint.getEndDate() != null ? sprint.getEndDate().toLocalDate() : null);

            VelocityHistory history = velocityHistoryRepository.findBySprintId(sprintId)
                    .orElseGet(() -> VelocityHistory.builder().sprintId(sprintId).build());

            history.setSprintName(sprintName);
            history.setCompletedStoryPoints(completedStoryPoints);
            history.setCompletedTasks(completedTasks);
            history.setSprintEndDate(sprintEndDate);

            velocityHistoryRepository.save(history);
        }
    }

    @Override
    public VelocityHistoryResponse getVelocityHistory() {
        // Role-based data filtering: Non-super-admin users only see their organization's velocity data
        Long currentOrgId = SecurityUtils.getCurrentUserOrganizationId().orElse(null);
        boolean isSuperAdmin = SecurityUtils.isSuperAdmin();
        
        List<VelocityHistory> histories;
        if (isSuperAdmin || currentOrgId == null) {
            // Super admin sees all velocity history
            histories = velocityHistoryRepository.findAllByOrderBySprintEndDateAsc();
        } else {
            // Other roles only see their organization's velocity history
            // Filter by sprint organization ID
            List<VelocityHistory> allHistories = velocityHistoryRepository.findAllByOrderBySprintEndDateAsc();
            histories = allHistories.stream()
                    .filter(vh -> {
                        Sprint sprint = sprintRepository.findById(vh.getSprintId()).orElse(null);
                        return sprint != null && currentOrgId.equals(sprint.getOrganizationId());
                    })
                    .collect(Collectors.toList());
        }

        List<VelocityPointResponse> points = histories.stream()
                .map(vh -> VelocityPointResponse.builder()
                        .sprintId(vh.getSprintId())
                        .sprintName(vh.getSprintName())
                        .completedStoryPoints(vh.getCompletedStoryPoints())
                        .completedTasks(vh.getCompletedTasks())
                        .sprintEndDate(vh.getSprintEndDate())
                        .build())
                .collect(Collectors.toList());

        return VelocityHistoryResponse.builder()
                .sprints(points)
                .build();
    }
}
