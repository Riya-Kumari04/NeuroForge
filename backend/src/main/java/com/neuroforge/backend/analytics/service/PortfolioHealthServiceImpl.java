package com.neuroforge.backend.analytics.service;

import com.neuroforge.backend.analytics.dto.PortfolioHealthResponse;
import com.neuroforge.backend.analytics.dto.ProjectHealthSummary;
import com.neuroforge.backend.entity.Organization;
import com.neuroforge.backend.entity.Sprint;
import com.neuroforge.backend.entity.Team;
import com.neuroforge.backend.enums.SprintStatus;
import com.neuroforge.backend.enums.TaskStatus;
import com.neuroforge.backend.exception.ResourceNotFoundException;
import com.neuroforge.backend.repository.OrganizationRepository;
import com.neuroforge.backend.repository.SprintRepository;
import com.neuroforge.backend.repository.TaskRepository;
import com.neuroforge.backend.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioHealthServiceImpl implements PortfolioHealthService {

    public static final double HEALTHY_THRESHOLD = 70.0;
    public static final double AT_RISK_THRESHOLD = 40.0;

    private final OrganizationRepository organizationRepository;
    private final TeamRepository teamRepository;
    private final SprintRepository sprintRepository;
    private final TaskRepository taskRepository;

    @Override
    public PortfolioHealthResponse getPortfolioHealth(UUID organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id: " + organizationId));

        List<Team> teams = teamRepository.findByOrganizationId(organizationId);

        List<Team> validTeams = new ArrayList<>();
        if (teams != null) {
            for (Team team : teams) {
                if (team != null && team.getId() != null) {
                    validTeams.add(team);
                }
            }
        }

        if (validTeams.isEmpty()) {
            return PortfolioHealthResponse.builder()
                    .organizationId(organizationId)
                    .organizationName(organization.getName())
                    .totalProjects(0)
                    .healthyProjects(0)
                    .atRiskProjects(0)
                    .criticalProjects(0)
                    .overallCompletionPercentage(0.0)
                    .totalStoryPoints(0)
                    .completedStoryPoints(0)
                    .totalTasks(0L)
                    .completedTasks(0L)
                    .projects(Collections.emptyList())
                    .build();
        }

        validTeams.sort(Comparator.comparing(t -> t.getName() == null ? "" : t.getName(), String::compareToIgnoreCase));

        int healthyProjects = 0;
        int atRiskProjects = 0;
        int criticalProjects = 0;

        long overallTotalTasks = 0;
        long overallCompletedTasks = 0;
        int overallTotalStoryPoints = 0;
        int overallCompletedStoryPoints = 0;

        List<ProjectHealthSummary> projectSummaries = new ArrayList<>();

        for (Team team : validTeams) {
            if (team == null || team.getId() == null) {
                continue;
            }

            List<Sprint> teamSprints = sprintRepository.findByTeamId(team.getId());
            if (teamSprints == null) {
                teamSprints = Collections.emptyList();
            }

            int activeSprints = (int) teamSprints.stream()
                    .filter(s -> s != null && s.getStatus() == SprintStatus.ACTIVE)
                    .count();

            int completedSprints = (int) teamSprints.stream()
                    .filter(s -> s != null && s.getStatus() == SprintStatus.COMPLETED)
                    .count();

            long teamTotalTasks = 0;
            long teamCompletedTasks = 0;
            int teamTotalStoryPoints = 0;
            int teamCompletedStoryPoints = 0;

            for (Sprint sprint : teamSprints) {
                if (sprint != null && sprint.getId() != null) {
                    teamTotalTasks += taskRepository.countBySprintId(sprint.getId());
                    teamCompletedTasks += taskRepository.countBySprintIdAndStatus(sprint.getId(), TaskStatus.DONE);

                    Integer totalPts = taskRepository.getTotalStoryPointsBySprint(sprint.getId());
                    if (totalPts != null) {
                        teamTotalStoryPoints += totalPts;
                    }

                    Integer compPts = taskRepository.getStoryPointsBySprintAndStatus(sprint.getId(), TaskStatus.DONE);
                    if (compPts != null) {
                        teamCompletedStoryPoints += compPts;
                    }
                }
            }

            double teamCompletionPercentage = teamTotalTasks > 0
                    ? Math.round(((teamCompletedTasks * 100.0) / teamTotalTasks) * 100.0) / 100.0
                    : 0.0;

            String healthStatus;
            if (teamTotalTasks > 0) {
                if (teamCompletionPercentage >= HEALTHY_THRESHOLD) {
                    healthStatus = "HEALTHY";
                    healthyProjects++;
                } else if (teamCompletionPercentage >= AT_RISK_THRESHOLD) {
                    healthStatus = "AT_RISK";
                    atRiskProjects++;
                } else {
                    healthStatus = "CRITICAL";
                    criticalProjects++;
                }
            } else {
                if (activeSprints > 0) {
                    healthStatus = "AT_RISK";
                    atRiskProjects++;
                } else {
                    healthStatus = "HEALTHY";
                    healthyProjects++;
                }
            }

            projectSummaries.add(ProjectHealthSummary.builder()
                    .projectId(team.getId())
                    .projectName(team.getName())
                    .teamId(team.getId())
                    .healthStatus(healthStatus)
                    .totalTasks(teamTotalTasks)
                    .completedTasks(teamCompletedTasks)
                    .completionPercentage(teamCompletionPercentage)
                    .totalStoryPoints(teamTotalStoryPoints)
                    .completedStoryPoints(teamCompletedStoryPoints)
                    .activeSprints(activeSprints)
                    .completedSprints(completedSprints)
                    .build());

            overallTotalTasks += teamTotalTasks;
            overallCompletedTasks += teamCompletedTasks;
            overallTotalStoryPoints += teamTotalStoryPoints;
            overallCompletedStoryPoints += teamCompletedStoryPoints;
        }

        double overallCompletionPercentage = overallTotalTasks > 0
                ? Math.round(((overallCompletedTasks * 100.0) / overallTotalTasks) * 100.0) / 100.0
                : 0.0;

        return PortfolioHealthResponse.builder()
                .organizationId(organizationId)
                .organizationName(organization.getName())
                .totalProjects(projectSummaries.size())
                .healthyProjects(healthyProjects)
                .atRiskProjects(atRiskProjects)
                .criticalProjects(criticalProjects)
                .overallCompletionPercentage(overallCompletionPercentage)
                .totalStoryPoints(overallTotalStoryPoints)
                .completedStoryPoints(overallCompletedStoryPoints)
                .totalTasks(overallTotalTasks)
                .completedTasks(overallCompletedTasks)
                .projects(projectSummaries)
                .build();
    }
}
