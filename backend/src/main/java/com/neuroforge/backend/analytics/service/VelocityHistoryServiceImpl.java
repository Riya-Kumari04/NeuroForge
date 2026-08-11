package com.neuroforge.backend.analytics.service;

import com.neuroforge.backend.analytics.dto.VelocityHistoryResponse;
import com.neuroforge.backend.analytics.dto.VelocityPointResponse;
import com.neuroforge.backend.analytics.entity.VelocityHistory;
import com.neuroforge.backend.analytics.repository.VelocityHistoryRepository;
import com.neuroforge.backend.entity.Sprint;
import com.neuroforge.backend.enums.SprintStatus;
import com.neuroforge.backend.enums.TaskStatus;
import com.neuroforge.backend.repository.SprintRepository;
import com.neuroforge.backend.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
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
            if (sprint == null || sprint.getId() == null || sprint.getStatus() != SprintStatus.COMPLETED) {
                continue;
            }

            UUID sprintId = sprint.getId();
            String sprintName = sprint.getName();
            Integer completedPointsObj = taskRepository.getStoryPointsBySprintAndStatus(sprintId, TaskStatus.DONE);
            int completedStoryPoints = completedPointsObj != null ? completedPointsObj : 0;
            long completedTasks = taskRepository.countBySprintIdAndStatus(sprintId, TaskStatus.DONE);
            LocalDate sprintEndDate = sprint.getActualEndDate() != null ? sprint.getActualEndDate() : sprint.getEndDate();

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
        List<VelocityHistory> histories = velocityHistoryRepository.findAllByOrderBySprintEndDateAsc();

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
