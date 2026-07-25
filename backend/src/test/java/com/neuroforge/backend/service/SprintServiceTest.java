package com.neuroforge.backend.service;

import com.neuroforge.backend.dto.BurndownPointResponse;
import com.neuroforge.backend.dto.SprintProgressResponse;
import com.neuroforge.backend.dto.SprintResponse;
import com.neuroforge.backend.dto.SprintStatisticsResponse;
import com.neuroforge.backend.dto.SprintSummaryResponse;
import com.neuroforge.backend.dto.SprintVelocityResponse;
import com.neuroforge.backend.dto.TaskDistributionResponse;
import com.neuroforge.backend.entity.Sprint;
import com.neuroforge.backend.entity.Task;
import com.neuroforge.backend.entity.Team;
import com.neuroforge.backend.enums.SprintStatus;
import com.neuroforge.backend.enums.TaskPriority;
import com.neuroforge.backend.enums.TaskStatus;
import com.neuroforge.backend.exception.InvalidSprintStateException;
import com.neuroforge.backend.exception.ResourceNotFoundException;
import com.neuroforge.backend.repository.SprintRepository;
import com.neuroforge.backend.repository.TaskRepository;
import com.neuroforge.backend.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SprintServiceTest {

    @Mock
    private SprintRepository sprintRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private SprintService sprintService;

    private UUID sprintId;
    private UUID teamId;
    private Team team;
    private Sprint sprint;

    @BeforeEach
    void setUp() {
        sprintId = UUID.randomUUID();
        teamId = UUID.randomUUID();
        team = Team.builder().id(teamId).name("Engineering").build();

        sprint = Sprint.builder()
                .id(sprintId)
                .name("Sprint 1")
                .goal("Initial Setup")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(14))
                .status(SprintStatus.PLANNED)
                .team(team)
                .build();
    }

    @Test
    void startSprint_Success() {
        when(sprintRepository.findById(sprintId)).thenReturn(Optional.of(sprint));
        when(sprintRepository.existsByTeamIdAndStatus(teamId, SprintStatus.ACTIVE)).thenReturn(false);
        when(sprintRepository.save(any(Sprint.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SprintResponse response = sprintService.startSprint(sprintId);

        assertNotNull(response);
        assertEquals(SprintStatus.ACTIVE, response.getStatus());
        assertEquals(LocalDate.now(), response.getActualStartDate());
        verify(sprintRepository).save(sprint);
    }

    @Test
    void startSprint_InvalidState_ThrowsException() {
        sprint.setStatus(SprintStatus.ACTIVE);
        when(sprintRepository.findById(sprintId)).thenReturn(Optional.of(sprint));

        assertThrows(InvalidSprintStateException.class, () -> sprintService.startSprint(sprintId));
        verify(sprintRepository, never()).save(any());
    }

    @Test
    void startSprint_ActiveSprintAlreadyExists_ThrowsException() {
        when(sprintRepository.findById(sprintId)).thenReturn(Optional.of(sprint));
        when(sprintRepository.existsByTeamIdAndStatus(teamId, SprintStatus.ACTIVE)).thenReturn(true);

        assertThrows(InvalidSprintStateException.class, () -> sprintService.startSprint(sprintId));
        verify(sprintRepository, never()).save(any());
    }

    @Test
    void completeSprint_Success() {
        sprint.setStatus(SprintStatus.ACTIVE);
        sprint.setActualStartDate(LocalDate.now().minusDays(10));
        when(sprintRepository.findById(sprintId)).thenReturn(Optional.of(sprint));
        when(sprintRepository.save(any(Sprint.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SprintResponse response = sprintService.completeSprint(sprintId);

        assertNotNull(response);
        assertEquals(SprintStatus.COMPLETED, response.getStatus());
        assertEquals(LocalDate.now(), response.getActualEndDate());
        verify(sprintRepository).save(sprint);
    }

    @Test
    void completeSprint_InvalidState_ThrowsException() {
        when(sprintRepository.findById(sprintId)).thenReturn(Optional.of(sprint));

        assertThrows(InvalidSprintStateException.class, () -> sprintService.completeSprint(sprintId));
        verify(sprintRepository, never()).save(any());
    }

    @Test
    void getActiveSprint_Success() {
        sprint.setStatus(SprintStatus.ACTIVE);
        when(sprintRepository.findFirstByStatus(SprintStatus.ACTIVE)).thenReturn(Optional.of(sprint));

        SprintResponse response = sprintService.getActiveSprint();

        assertNotNull(response);
        assertEquals(sprintId, response.getId());
        assertEquals(SprintStatus.ACTIVE, response.getStatus());
    }

    @Test
    void getActiveSprint_NotFound_ThrowsException() {
        when(sprintRepository.findFirstByStatus(SprintStatus.ACTIVE)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> sprintService.getActiveSprint());
    }

    @Test
    void getSprintSummary_Success() {
        when(sprintRepository.findById(sprintId)).thenReturn(Optional.of(sprint));

        Task task1 = Task.builder().id(UUID.randomUUID()).title("Task 1").status(TaskStatus.DONE).storyPoints(5).priority(TaskPriority.HIGH).build();
        Task task2 = Task.builder().id(UUID.randomUUID()).title("Task 2").status(TaskStatus.TODO).storyPoints(3).priority(TaskPriority.MEDIUM).build();

        when(taskRepository.findBySprintId(sprintId)).thenReturn(Arrays.asList(task1, task2));

        SprintSummaryResponse summary = sprintService.getSprintSummary(sprintId);

        assertNotNull(summary);
        assertEquals(sprintId, summary.getId());
        assertEquals(2, summary.getTotalTasks());
        assertEquals(1, summary.getCompletedTasks());
        assertEquals(1, summary.getRemainingTasks());
        assertEquals(50.0, summary.getCompletionPercentage());
        assertEquals(8, summary.getTotalStoryPoints());
        assertEquals(5, summary.getCompletedStoryPoints());
        assertEquals(3, summary.getRemainingStoryPoints());
    }

    @Test
    void getSprintStatistics_Success() {
        when(sprintRepository.findById(sprintId)).thenReturn(Optional.of(sprint));

        Task task1 = Task.builder().id(UUID.randomUUID()).title("Task 1").status(TaskStatus.DONE).storyPoints(5).priority(TaskPriority.CRITICAL).build();
        Task task2 = Task.builder().id(UUID.randomUUID()).title("Task 2").status(TaskStatus.IN_PROGRESS).storyPoints(3).priority(TaskPriority.HIGH).build();
        Task task3 = Task.builder().id(UUID.randomUUID()).title("Task 3").status(TaskStatus.TODO).storyPoints(4).priority(TaskPriority.LOW).build();

        when(taskRepository.findBySprintId(sprintId)).thenReturn(Arrays.asList(task1, task2, task3));

        SprintStatisticsResponse stats = sprintService.getSprintStatistics(sprintId);

        assertNotNull(stats);
        assertEquals(1, stats.getTodoTasks());
        assertEquals(1, stats.getInProgressTasks());
        assertEquals(0, stats.getTestingTasks());
        assertEquals(0, stats.getCodeReviewTasks());
        assertEquals(1, stats.getDoneTasks());
        assertEquals(1, stats.getHighPriorityTasks());
        assertEquals(1, stats.getCriticalPriorityTasks());
        assertEquals(4.0, stats.getAverageStoryPoints());
        assertEquals(33.33, stats.getCompletionPercentage());
    }

    @Test
    void getSprintProgress_Success() {
        when(sprintRepository.findById(sprintId)).thenReturn(Optional.of(sprint));

        Task task1 = Task.builder().id(UUID.randomUUID()).title("Task 1").status(TaskStatus.DONE).storyPoints(5).build();
        Task task2 = Task.builder().id(UUID.randomUUID()).title("Task 2").status(TaskStatus.TODO).storyPoints(3).build();

        when(taskRepository.findBySprintId(sprintId)).thenReturn(Arrays.asList(task1, task2));

        SprintProgressResponse progress = sprintService.getSprintProgress(sprintId);

        assertNotNull(progress);
        assertEquals(sprintId, progress.getSprintId());
        assertEquals("Sprint 1", progress.getSprintName());
        assertEquals(2, progress.getTotalTasks());
        assertEquals(1, progress.getCompletedTasks());
        assertEquals(1, progress.getRemainingTasks());
        assertEquals(8, progress.getTotalStoryPoints());
        assertEquals(5, progress.getCompletedStoryPoints());
        assertEquals(3, progress.getRemainingStoryPoints());
        assertEquals(50.0, progress.getCompletionPercentage());
        assertEquals(SprintStatus.PLANNED, progress.getCurrentSprintStatus());
    }

    @Test
    void getSprintBurndown_Success() {
        when(sprintRepository.findById(sprintId)).thenReturn(Optional.of(sprint));

        Task task1 = Task.builder().id(UUID.randomUUID()).title("Task 1").status(TaskStatus.DONE).storyPoints(5).build();
        when(taskRepository.findBySprintId(sprintId)).thenReturn(Collections.singletonList(task1));

        List<BurndownPointResponse> burndown = sprintService.getSprintBurndown(sprintId);

        assertNotNull(burndown);
        assertFalse(burndown.isEmpty());
        assertEquals(15, burndown.size()); // startDate to startDate + 14 days
    }

    @Test
    void getSprintVelocity_Success() {
        when(sprintRepository.findById(sprintId)).thenReturn(Optional.of(sprint));

        Task task1 = Task.builder().id(UUID.randomUUID()).title("Task 1").status(TaskStatus.DONE).storyPoints(6).build();
        Task task2 = Task.builder().id(UUID.randomUUID()).title("Task 2").status(TaskStatus.DONE).storyPoints(4).build();
        Task task3 = Task.builder().id(UUID.randomUUID()).title("Task 3").status(TaskStatus.TODO).storyPoints(5).build();

        when(taskRepository.findBySprintId(sprintId)).thenReturn(Arrays.asList(task1, task2, task3));

        SprintVelocityResponse velocity = sprintService.getSprintVelocity(sprintId);

        assertNotNull(velocity);
        assertEquals(10, velocity.getCompletedStoryPoints());
        assertEquals(2, velocity.getCompletedTasks());
        assertEquals(5.0, velocity.getAverageStoryPointsPerTask());
        assertEquals(66.67, velocity.getCompletionPercentage());
    }

    @Test
    void getTaskDistribution_Success() {
        when(sprintRepository.findById(sprintId)).thenReturn(Optional.of(sprint));

        Task task1 = Task.builder().id(UUID.randomUUID()).title("Task 1").status(TaskStatus.DONE).priority(TaskPriority.HIGH).build();
        Task task2 = Task.builder().id(UUID.randomUUID()).title("Task 2").status(TaskStatus.TODO).priority(TaskPriority.LOW).build();

        when(taskRepository.findBySprintId(sprintId)).thenReturn(Arrays.asList(task1, task2));

        TaskDistributionResponse distribution = sprintService.getTaskDistribution(sprintId);

        assertNotNull(distribution);
        assertEquals(1L, distribution.getByStatus().get(TaskStatus.DONE));
        assertEquals(1L, distribution.getByStatus().get(TaskStatus.TODO));
        assertEquals(1L, distribution.getByPriority().get(TaskPriority.HIGH));
        assertEquals(1L, distribution.getByPriority().get(TaskPriority.LOW));
        assertEquals(2L, distribution.getByAssignee().get("Unassigned"));
    }
}
