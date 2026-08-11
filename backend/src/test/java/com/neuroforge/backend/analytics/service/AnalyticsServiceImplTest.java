package com.neuroforge.backend.analytics.service;

import com.neuroforge.backend.analytics.dto.BurndownPointResponse;
import com.neuroforge.backend.analytics.dto.BurndownResponse;
import com.neuroforge.backend.analytics.dto.VelocityPointResponse;
import com.neuroforge.backend.analytics.dto.VelocityResponse;
import com.neuroforge.backend.entity.Sprint;
import com.neuroforge.backend.entity.Task;
import com.neuroforge.backend.enums.SprintStatus;
import com.neuroforge.backend.enums.TaskStatus;
import com.neuroforge.backend.exception.ResourceNotFoundException;
import com.neuroforge.backend.repository.CodeReviewRepository;
import com.neuroforge.backend.repository.SprintRepository;
import com.neuroforge.backend.repository.TaskRepository;
import com.neuroforge.backend.repository.TaskStatusHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private SprintRepository sprintRepository;

    @Mock
    private TaskStatusHistoryRepository taskStatusHistoryRepository;

    @Mock
    private CodeReviewRepository codeReviewRepository;

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    private Sprint sprint1;
    private Sprint sprint2;
    private Sprint sprint3;

    @BeforeEach
    void setUp() {
        sprint1 = Sprint.builder()
                .id(UUID.randomUUID())
                .name("Sprint 1")
                .status(SprintStatus.COMPLETED)
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 1, 14))
                .actualEndDate(LocalDate.of(2026, 1, 15))
                .build();

        sprint2 = Sprint.builder()
                .id(UUID.randomUUID())
                .name("Sprint 2")
                .status(SprintStatus.ACTIVE)
                .startDate(LocalDate.of(2026, 1, 15))
                .endDate(LocalDate.of(2026, 1, 28))
                .build();

        sprint3 = Sprint.builder()
                .id(UUID.randomUUID())
                .name("Sprint 3")
                .status(SprintStatus.COMPLETED)
                .startDate(LocalDate.of(2026, 2, 1))
                .endDate(LocalDate.of(2026, 2, 14))
                .actualEndDate(null)
                .build();
    }

    @Test
    void getVelocity_returnsVelocityPointsForCompletedSprints() {
        when(sprintRepository.findAllByOrderByStartDateAsc()).thenReturn(List.of(sprint1, sprint2, sprint3));

        when(taskRepository.getStoryPointsBySprintAndStatus(sprint1.getId(), TaskStatus.DONE)).thenReturn(21);
        when(taskRepository.countBySprintIdAndStatus(sprint1.getId(), TaskStatus.DONE)).thenReturn(5L);

        when(taskRepository.getStoryPointsBySprintAndStatus(sprint3.getId(), TaskStatus.DONE)).thenReturn(15);
        when(taskRepository.countBySprintIdAndStatus(sprint3.getId(), TaskStatus.DONE)).thenReturn(4L);

        VelocityResponse response = analyticsService.getVelocity();

        assertNotNull(response);
        assertNotNull(response.getSprints());
        assertEquals(2, response.getSprints().size());

        VelocityPointResponse point1 = response.getSprints().get(0);
        assertEquals(sprint1.getId(), point1.getSprintId());
        assertEquals("Sprint 1", point1.getSprintName());
        assertEquals(21, point1.getCompletedStoryPoints());
        assertEquals(5L, point1.getCompletedTasks());
        assertEquals(LocalDate.of(2026, 1, 15), point1.getSprintEndDate());

        VelocityPointResponse point2 = response.getSprints().get(1);
        assertEquals(sprint3.getId(), point2.getSprintId());
        assertEquals("Sprint 3", point2.getSprintName());
        assertEquals(15, point2.getCompletedStoryPoints());
        assertEquals(4L, point2.getCompletedTasks());
        assertEquals(LocalDate.of(2026, 2, 14), point2.getSprintEndDate());

        verify(sprintRepository).findAllByOrderByStartDateAsc();
        verify(taskRepository, times(1)).getStoryPointsBySprintAndStatus(sprint1.getId(), TaskStatus.DONE);
        verify(taskRepository, times(1)).countBySprintIdAndStatus(sprint1.getId(), TaskStatus.DONE);
        verify(taskRepository, times(1)).getStoryPointsBySprintAndStatus(sprint3.getId(), TaskStatus.DONE);
        verify(taskRepository, times(1)).countBySprintIdAndStatus(sprint3.getId(), TaskStatus.DONE);
        verifyNoMoreInteractions(taskRepository);
    }

    @Test
    void getVelocity_returnsEmptyList_whenNoCompletedSprintsExist() {
        when(sprintRepository.findAllByOrderByStartDateAsc()).thenReturn(List.of(sprint2));

        VelocityResponse response = analyticsService.getVelocity();

        assertNotNull(response);
        assertNotNull(response.getSprints());
        assertTrue(response.getSprints().isEmpty());
    }

    @Test
    void getVelocity_returnsEmptyList_whenNoSprintsExist() {
        when(sprintRepository.findAllByOrderByStartDateAsc()).thenReturn(Collections.emptyList());

        VelocityResponse response = analyticsService.getVelocity();

        assertNotNull(response);
        assertNotNull(response.getSprints());
        assertTrue(response.getSprints().isEmpty());
    }

    @Test
    void getBurndown_returnsDailyPointsForActiveSprint() {
        Sprint activeSprint = Sprint.builder()
                .id(UUID.randomUUID())
                .name("Active Sprint")
                .status(SprintStatus.ACTIVE)
                .startDate(LocalDate.of(2026, 3, 1))
                .endDate(LocalDate.of(2026, 3, 5))
                .build();

        Task task1 = Task.builder()
                .id(UUID.randomUUID())
                .title("Task 1")
                .status(TaskStatus.DONE)
                .storyPoints(5)
                .updatedAt(LocalDateTime.of(2026, 3, 2, 10, 0))
                .build();

        Task task2 = Task.builder()
                .id(UUID.randomUUID())
                .title("Task 2")
                .status(TaskStatus.IN_PROGRESS)
                .storyPoints(3)
                .build();

        Task task3 = Task.builder()
                .id(UUID.randomUUID())
                .title("Task 3")
                .status(TaskStatus.DONE)
                .storyPoints(2)
                .updatedAt(LocalDateTime.of(2026, 3, 4, 14, 0))
                .build();

        when(sprintRepository.findFirstByStatus(SprintStatus.ACTIVE)).thenReturn(Optional.of(activeSprint));
        when(taskRepository.findBySprintId(activeSprint.getId())).thenReturn(List.of(task1, task2, task3));

        BurndownResponse response = analyticsService.getBurndown();

        assertNotNull(response);
        assertEquals(activeSprint.getId(), response.getSprintId());
        assertEquals("Active Sprint", response.getSprintName());
        assertEquals(LocalDate.of(2026, 3, 1), response.getStartDate());
        assertEquals(LocalDate.of(2026, 3, 5), response.getEndDate());
        assertEquals(10, response.getTotalStoryPoints());

        assertNotNull(response.getPoints());
        assertEquals(5, response.getPoints().size());

        // Day 1: 2026-03-01 -> completed: 0, remaining: 10
        BurndownPointResponse day1 = response.getPoints().get(0);
        assertEquals(LocalDate.of(2026, 3, 1), day1.getDate());
        assertEquals(0, day1.getCompletedStoryPoints());
        assertEquals(10, day1.getRemainingStoryPoints());

        // Day 2: 2026-03-02 -> completed: 5, remaining: 5
        BurndownPointResponse day2 = response.getPoints().get(1);
        assertEquals(LocalDate.of(2026, 3, 2), day2.getDate());
        assertEquals(5, day2.getCompletedStoryPoints());
        assertEquals(5, day2.getRemainingStoryPoints());

        // Day 3: 2026-03-03 -> completed: 5, remaining: 5
        BurndownPointResponse day3 = response.getPoints().get(2);
        assertEquals(LocalDate.of(2026, 3, 3), day3.getDate());
        assertEquals(5, day3.getCompletedStoryPoints());
        assertEquals(5, day3.getRemainingStoryPoints());

        // Day 4: 2026-03-04 -> completed: 7, remaining: 3
        BurndownPointResponse day4 = response.getPoints().get(3);
        assertEquals(LocalDate.of(2026, 3, 4), day4.getDate());
        assertEquals(7, day4.getCompletedStoryPoints());
        assertEquals(3, day4.getRemainingStoryPoints());

        // Day 5: 2026-03-05 -> completed: 7, remaining: 3
        BurndownPointResponse day5 = response.getPoints().get(4);
        assertEquals(LocalDate.of(2026, 3, 5), day5.getDate());
        assertEquals(7, day5.getCompletedStoryPoints());
        assertEquals(3, day5.getRemainingStoryPoints());
    }

    @Test
    void getBurndown_returnsZeroPointsForSprintWithNoTasks() {
        Sprint activeSprint = Sprint.builder()
                .id(UUID.randomUUID())
                .name("Empty Active Sprint")
                .status(SprintStatus.ACTIVE)
                .startDate(LocalDate.of(2026, 3, 1))
                .endDate(LocalDate.of(2026, 3, 3))
                .build();

        when(sprintRepository.findFirstByStatus(SprintStatus.ACTIVE)).thenReturn(Optional.of(activeSprint));
        when(taskRepository.findBySprintId(activeSprint.getId())).thenReturn(Collections.emptyList());

        BurndownResponse response = analyticsService.getBurndown();

        assertNotNull(response);
        assertEquals(activeSprint.getId(), response.getSprintId());
        assertEquals(0, response.getTotalStoryPoints());
        assertNotNull(response.getPoints());
        assertEquals(3, response.getPoints().size());

        for (BurndownPointResponse point : response.getPoints()) {
            assertEquals(0, point.getCompletedStoryPoints());
            assertEquals(0, point.getRemainingStoryPoints());
        }
    }

    @Test
    void getBurndown_throwsExceptionWhenNoActiveSprintExists() {
        when(sprintRepository.findFirstByStatus(SprintStatus.ACTIVE)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> analyticsService.getBurndown()
        );

        assertEquals("No active sprint found.", exception.getMessage());
    }
}
