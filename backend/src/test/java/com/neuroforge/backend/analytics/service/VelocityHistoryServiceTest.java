package com.neuroforge.backend.analytics.service;

import com.neuroforge.backend.analytics.dto.VelocityHistoryResponse;
import com.neuroforge.backend.analytics.entity.VelocityHistory;
import com.neuroforge.backend.analytics.repository.VelocityHistoryRepository;
import com.neuroforge.backend.entity.Sprint;
import com.neuroforge.backend.enums.SprintStatus;
import com.neuroforge.backend.enums.TaskStatus;
import com.neuroforge.backend.repository.SprintRepository;
import com.neuroforge.backend.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VelocityHistoryServiceTest {

    @Mock
    private VelocityHistoryRepository velocityHistoryRepository;

    @Mock
    private SprintRepository sprintRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private VelocityHistoryServiceImpl velocityHistoryService;

    private Sprint completedSprint;
    private Sprint activeSprint;

    @BeforeEach
    void setUp() {
        completedSprint = Sprint.builder()
                .id(UUID.randomUUID())
                .name("Completed Sprint 1")
                .status(SprintStatus.COMPLETED)
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 1, 14))
                .actualEndDate(LocalDate.of(2026, 1, 15))
                .build();

        activeSprint = Sprint.builder()
                .id(UUID.randomUUID())
                .name("Active Sprint 2")
                .status(SprintStatus.ACTIVE)
                .startDate(LocalDate.of(2026, 1, 15))
                .endDate(LocalDate.of(2026, 1, 28))
                .build();
    }

    @Test
    void refreshVelocityHistory_savesCompletedSprints() {
        when(sprintRepository.findAllByOrderByStartDateAsc()).thenReturn(List.of(completedSprint, activeSprint));
        when(taskRepository.getStoryPointsBySprintAndStatus(completedSprint.getId(), TaskStatus.DONE)).thenReturn(18);
        when(taskRepository.countBySprintIdAndStatus(completedSprint.getId(), TaskStatus.DONE)).thenReturn(5L);
        when(velocityHistoryRepository.findBySprintId(completedSprint.getId())).thenReturn(Optional.empty());

        velocityHistoryService.refreshVelocityHistory();

        verify(velocityHistoryRepository, times(1)).save(any(VelocityHistory.class));
        verify(taskRepository, never()).getStoryPointsBySprintAndStatus(activeSprint.getId(), TaskStatus.DONE);
    }

    @Test
    void refreshVelocityHistory_ignoresActiveSprints() {
        when(sprintRepository.findAllByOrderByStartDateAsc()).thenReturn(List.of(activeSprint));

        velocityHistoryService.refreshVelocityHistory();

        verify(velocityHistoryRepository, never()).save(any(VelocityHistory.class));
    }

    @Test
    void refreshVelocityHistory_updatesExistingSprintHistory() {
        VelocityHistory existing = VelocityHistory.builder()
                .id(UUID.randomUUID())
                .sprintId(completedSprint.getId())
                .sprintName("Old Sprint Name")
                .completedStoryPoints(10)
                .completedTasks(3L)
                .sprintEndDate(LocalDate.of(2026, 1, 14))
                .build();

        when(sprintRepository.findAllByOrderByStartDateAsc()).thenReturn(List.of(completedSprint));
        when(taskRepository.getStoryPointsBySprintAndStatus(completedSprint.getId(), TaskStatus.DONE)).thenReturn(25);
        when(taskRepository.countBySprintIdAndStatus(completedSprint.getId(), TaskStatus.DONE)).thenReturn(7L);
        when(velocityHistoryRepository.findBySprintId(completedSprint.getId())).thenReturn(Optional.of(existing));

        velocityHistoryService.refreshVelocityHistory();

        verify(velocityHistoryRepository).save(existing);
        assertEquals("Completed Sprint 1", existing.getSprintName());
        assertEquals(25, existing.getCompletedStoryPoints());
        assertEquals(7L, existing.getCompletedTasks());
        assertEquals(LocalDate.of(2026, 1, 15), existing.getSprintEndDate());
    }

    @Test
    void refreshVelocityHistory_handlesNoCompletedSprints() {
        when(sprintRepository.findAllByOrderByStartDateAsc()).thenReturn(Collections.emptyList());

        velocityHistoryService.refreshVelocityHistory();

        verify(velocityHistoryRepository, never()).save(any(VelocityHistory.class));

        when(velocityHistoryRepository.findAllByOrderBySprintEndDateAsc()).thenReturn(Collections.emptyList());
        VelocityHistoryResponse response = velocityHistoryService.getVelocityHistory();

        assertNotNull(response);
        assertNotNull(response.getSprints());
        assertTrue(response.getSprints().isEmpty());
    }
}
