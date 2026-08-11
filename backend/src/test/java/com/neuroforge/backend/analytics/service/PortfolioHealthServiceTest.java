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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioHealthServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private SprintRepository sprintRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private PortfolioHealthServiceImpl portfolioHealthService;

    private UUID orgId;
    private Organization organization;

    @BeforeEach
    void setUp() {
        orgId = UUID.randomUUID();
        organization = Organization.builder()
                .id(orgId)
                .name("Acme Corp")
                .slug("acme-corp")
                .build();
    }

    @Test
    void getPortfolioHealth_throwsResourceNotFoundExceptionWhenOrgMissing() {
        UUID nonExistentId = UUID.randomUUID();
        when(organizationRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> portfolioHealthService.getPortfolioHealth(nonExistentId)
        );

        assertEquals("Organization not found with id: " + nonExistentId, exception.getMessage());
        verify(teamRepository, never()).findByOrganizationId(any());
    }

    @Test
    void getPortfolioHealth_returnsEmptyPortfolioWhenOrgHasNoTeams() {
        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organization));
        when(teamRepository.findByOrganizationId(orgId)).thenReturn(Collections.emptyList());

        PortfolioHealthResponse response = portfolioHealthService.getPortfolioHealth(orgId);

        assertNotNull(response);
        assertEquals(orgId, response.getOrganizationId());
        assertEquals("Acme Corp", response.getOrganizationName());
        assertEquals(0, response.getTotalProjects());
        assertEquals(0, response.getHealthyProjects());
        assertEquals(0, response.getAtRiskProjects());
        assertEquals(0, response.getCriticalProjects());
        assertEquals(0.0, response.getOverallCompletionPercentage());
        assertEquals(0, response.getTotalStoryPoints());
        assertEquals(0, response.getCompletedStoryPoints());
        assertEquals(0L, response.getTotalTasks());
        assertEquals(0L, response.getCompletedTasks());
        assertTrue(response.getProjects().isEmpty());
    }

    @Test
    void getPortfolioHealth_returnsHealthyProject() {
        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organization));

        UUID teamId = UUID.randomUUID();
        Team team = Team.builder().id(teamId).name("Team Alpha").organization(organization).build();
        when(teamRepository.findByOrganizationId(orgId)).thenReturn(List.of(team));

        UUID sprintId = UUID.randomUUID();
        Sprint sprint = Sprint.builder().id(sprintId).team(team).status(SprintStatus.ACTIVE).build();
        when(sprintRepository.findByTeamId(teamId)).thenReturn(List.of(sprint));

        when(taskRepository.countBySprintId(sprintId)).thenReturn(10L);
        when(taskRepository.countBySprintIdAndStatus(sprintId, TaskStatus.DONE)).thenReturn(8L);
        when(taskRepository.getTotalStoryPointsBySprint(sprintId)).thenReturn(20);
        when(taskRepository.getStoryPointsBySprintAndStatus(sprintId, TaskStatus.DONE)).thenReturn(16);

        PortfolioHealthResponse response = portfolioHealthService.getPortfolioHealth(orgId);

        assertNotNull(response);
        assertEquals(1, response.getTotalProjects());
        assertEquals(1, response.getHealthyProjects());
        assertEquals(0, response.getAtRiskProjects());
        assertEquals(0, response.getCriticalProjects());

        ProjectHealthSummary project = response.getProjects().get(0);
        assertEquals(teamId, project.getProjectId());
        assertEquals("Team Alpha", project.getProjectName());
        assertEquals("HEALTHY", project.getHealthStatus());
        assertEquals(80.0, project.getCompletionPercentage());
    }

    @Test
    void getPortfolioHealth_returnsAtRiskProject() {
        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organization));

        UUID teamId = UUID.randomUUID();
        Team team = Team.builder().id(teamId).name("Team Beta").organization(organization).build();
        when(teamRepository.findByOrganizationId(orgId)).thenReturn(List.of(team));

        UUID sprintId = UUID.randomUUID();
        Sprint sprint = Sprint.builder().id(sprintId).team(team).status(SprintStatus.ACTIVE).build();
        when(sprintRepository.findByTeamId(teamId)).thenReturn(List.of(sprint));

        when(taskRepository.countBySprintId(sprintId)).thenReturn(10L);
        when(taskRepository.countBySprintIdAndStatus(sprintId, TaskStatus.DONE)).thenReturn(5L);

        PortfolioHealthResponse response = portfolioHealthService.getPortfolioHealth(orgId);

        assertNotNull(response);
        assertEquals(1, response.getAtRiskProjects());
        assertEquals("AT_RISK", response.getProjects().get(0).getHealthStatus());
    }

    @Test
    void getPortfolioHealth_returnsCriticalProject() {
        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organization));

        UUID teamId = UUID.randomUUID();
        Team team = Team.builder().id(teamId).name("Team Gamma").organization(organization).build();
        when(teamRepository.findByOrganizationId(orgId)).thenReturn(List.of(team));

        UUID sprintId = UUID.randomUUID();
        Sprint sprint = Sprint.builder().id(sprintId).team(team).status(SprintStatus.ACTIVE).build();
        when(sprintRepository.findByTeamId(teamId)).thenReturn(List.of(sprint));

        when(taskRepository.countBySprintId(sprintId)).thenReturn(10L);
        when(taskRepository.countBySprintIdAndStatus(sprintId, TaskStatus.DONE)).thenReturn(2L);

        PortfolioHealthResponse response = portfolioHealthService.getPortfolioHealth(orgId);

        assertNotNull(response);
        assertEquals(1, response.getCriticalProjects());
        assertEquals("CRITICAL", response.getProjects().get(0).getHealthStatus());
    }

    @Test
    void getPortfolioHealth_zeroTasksWithActiveSprintReturnsAtRisk() {
        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organization));

        UUID teamId = UUID.randomUUID();
        Team team = Team.builder().id(teamId).name("Team Delta").organization(organization).build();
        when(teamRepository.findByOrganizationId(orgId)).thenReturn(List.of(team));

        UUID sprintId = UUID.randomUUID();
        Sprint sprint = Sprint.builder().id(sprintId).team(team).status(SprintStatus.ACTIVE).build();
        when(sprintRepository.findByTeamId(teamId)).thenReturn(List.of(sprint));

        when(taskRepository.countBySprintId(sprintId)).thenReturn(0L);
        when(taskRepository.countBySprintIdAndStatus(sprintId, TaskStatus.DONE)).thenReturn(0L);

        PortfolioHealthResponse response = portfolioHealthService.getPortfolioHealth(orgId);

        assertNotNull(response);
        assertEquals(1, response.getTotalProjects());
        assertEquals(1, response.getAtRiskProjects());
        assertEquals(0, response.getHealthyProjects());
        assertEquals("AT_RISK", response.getProjects().get(0).getHealthStatus());
    }

    @Test
    void getPortfolioHealth_zeroTasksNoActiveSprintReturnsHealthy() {
        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organization));

        UUID teamId = UUID.randomUUID();
        Team team = Team.builder().id(teamId).name("Team Epsilon").organization(organization).build();
        when(teamRepository.findByOrganizationId(orgId)).thenReturn(List.of(team));

        when(sprintRepository.findByTeamId(teamId)).thenReturn(Collections.emptyList());

        PortfolioHealthResponse response = portfolioHealthService.getPortfolioHealth(orgId);

        assertNotNull(response);
        assertEquals(1, response.getTotalProjects());
        assertEquals(1, response.getHealthyProjects());
        assertEquals(0, response.getAtRiskProjects());
        assertEquals("HEALTHY", response.getProjects().get(0).getHealthStatus());
    }

    @Test
    void getPortfolioHealth_aggregatesMultipleTeamsIntoPortfolio() {
        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organization));

        UUID teamId1 = UUID.randomUUID();
        Team team1 = Team.builder().id(teamId1).name("Alpha").organization(organization).build();
        UUID teamId2 = UUID.randomUUID();
        Team team2 = Team.builder().id(teamId2).name("Beta").organization(organization).build();

        when(teamRepository.findByOrganizationId(orgId)).thenReturn(List.of(team2, team1));

        UUID s1 = UUID.randomUUID();
        Sprint sprint1 = Sprint.builder().id(s1).team(team1).status(SprintStatus.ACTIVE).build();
        when(sprintRepository.findByTeamId(teamId1)).thenReturn(List.of(sprint1));
        when(taskRepository.countBySprintId(s1)).thenReturn(10L);
        when(taskRepository.countBySprintIdAndStatus(s1, TaskStatus.DONE)).thenReturn(8L);
        when(taskRepository.getTotalStoryPointsBySprint(s1)).thenReturn(20);
        when(taskRepository.getStoryPointsBySprintAndStatus(s1, TaskStatus.DONE)).thenReturn(16);

        UUID s2 = UUID.randomUUID();
        Sprint sprint2 = Sprint.builder().id(s2).team(team2).status(SprintStatus.COMPLETED).build();
        when(sprintRepository.findByTeamId(teamId2)).thenReturn(List.of(sprint2));
        when(taskRepository.countBySprintId(s2)).thenReturn(10L);
        when(taskRepository.countBySprintIdAndStatus(s2, TaskStatus.DONE)).thenReturn(3L);
        when(taskRepository.getTotalStoryPointsBySprint(s2)).thenReturn(10);
        when(taskRepository.getStoryPointsBySprintAndStatus(s2, TaskStatus.DONE)).thenReturn(3);

        PortfolioHealthResponse response = portfolioHealthService.getPortfolioHealth(orgId);

        assertNotNull(response);
        assertEquals(2, response.getTotalProjects());
        assertEquals(1, response.getHealthyProjects());
        assertEquals(0, response.getAtRiskProjects());
        assertEquals(1, response.getCriticalProjects());

        assertEquals(20L, response.getTotalTasks());
        assertEquals(11L, response.getCompletedTasks());
        assertEquals(30, response.getTotalStoryPoints());
        assertEquals(19, response.getCompletedStoryPoints());
        assertEquals(55.0, response.getOverallCompletionPercentage());

        assertEquals("Alpha", response.getProjects().get(0).getProjectName());
        assertEquals("Beta", response.getProjects().get(1).getProjectName());

        assertEquals(1, response.getProjects().get(0).getActiveSprints());
        assertEquals(0, response.getProjects().get(0).getCompletedSprints());
        assertEquals(0, response.getProjects().get(1).getActiveSprints());
        assertEquals(1, response.getProjects().get(1).getCompletedSprints());
    }
}
