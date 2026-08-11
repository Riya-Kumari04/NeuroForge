package com.neuroforge.backend.analytics.service;

import com.neuroforge.backend.analytics.dto.VelocityPointResponse;
import com.neuroforge.backend.analytics.dto.VelocityResponse;
import com.neuroforge.backend.entity.Sprint;
import com.neuroforge.backend.enums.SprintStatus;
import com.neuroforge.backend.enums.TaskStatus;
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
import java.util.Collections;
import java.util.List;
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
}
