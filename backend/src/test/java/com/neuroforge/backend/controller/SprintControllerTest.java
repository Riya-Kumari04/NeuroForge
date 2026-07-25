package com.neuroforge.backend.controller;

import com.neuroforge.backend.dto.BurndownPointResponse;
import com.neuroforge.backend.dto.SprintProgressResponse;
import com.neuroforge.backend.dto.SprintResponse;
import com.neuroforge.backend.dto.SprintStatisticsResponse;
import com.neuroforge.backend.dto.SprintSummaryResponse;
import com.neuroforge.backend.dto.SprintVelocityResponse;
import com.neuroforge.backend.dto.TaskDistributionResponse;
import com.neuroforge.backend.enums.SprintStatus;
import com.neuroforge.backend.service.SprintService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SprintControllerTest {

    @Mock
    private SprintService sprintService;

    @InjectMocks
    private SprintController sprintController;

    private UUID sprintId;

    @BeforeEach
    void setUp() {
        sprintId = UUID.randomUUID();
    }

    @Test
    void startSprint_ReturnsOk() {
        SprintResponse response = SprintResponse.builder()
                .id(sprintId)
                .name("Sprint 1")
                .status(SprintStatus.ACTIVE)
                .actualStartDate(LocalDate.now())
                .build();

        when(sprintService.startSprint(sprintId)).thenReturn(response);

        ResponseEntity<SprintResponse> result = sprintController.startSprint(sprintId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(SprintStatus.ACTIVE, result.getBody().getStatus());
    }

    @Test
    void completeSprint_ReturnsOk() {
        SprintResponse response = SprintResponse.builder()
                .id(sprintId)
                .name("Sprint 1")
                .status(SprintStatus.COMPLETED)
                .actualEndDate(LocalDate.now())
                .build();

        when(sprintService.completeSprint(sprintId)).thenReturn(response);

        ResponseEntity<SprintResponse> result = sprintController.completeSprint(sprintId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(SprintStatus.COMPLETED, result.getBody().getStatus());
    }

    @Test
    void getActiveSprint_ReturnsOk() {
        SprintResponse response = SprintResponse.builder()
                .id(sprintId)
                .name("Sprint 1")
                .status(SprintStatus.ACTIVE)
                .build();

        when(sprintService.getActiveSprint()).thenReturn(response);

        ResponseEntity<SprintResponse> result = sprintController.getActiveSprint();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(sprintId, result.getBody().getId());
    }

    @Test
    void getSprintSummary_ReturnsOk() {
        SprintSummaryResponse summary = SprintSummaryResponse.builder()
                .id(sprintId)
                .name("Sprint 1")
                .totalTasks(10)
                .completedTasks(7)
                .build();

        when(sprintService.getSprintSummary(sprintId)).thenReturn(summary);

        ResponseEntity<SprintSummaryResponse> result = sprintController.getSprintSummary(sprintId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(10, result.getBody().getTotalTasks());
    }

    @Test
    void getSprintStatistics_ReturnsOk() {
        SprintStatisticsResponse stats = SprintStatisticsResponse.builder()
                .doneTasks(5)
                .todoTasks(2)
                .build();

        when(sprintService.getSprintStatistics(sprintId)).thenReturn(stats);

        ResponseEntity<SprintStatisticsResponse> result = sprintController.getSprintStatistics(sprintId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(5, result.getBody().getDoneTasks());
    }

    @Test
    void getSprintProgress_ReturnsOk() {
        SprintProgressResponse progress = SprintProgressResponse.builder()
                .sprintId(sprintId)
                .sprintName("Sprint 1")
                .totalTasks(10)
                .completedTasks(6)
                .remainingTasks(4)
                .completionPercentage(60.0)
                .build();

        when(sprintService.getSprintProgress(sprintId)).thenReturn(progress);

        ResponseEntity<SprintProgressResponse> result = sprintController.getSprintProgress(sprintId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(sprintId, result.getBody().getSprintId());
        assertEquals(60.0, result.getBody().getCompletionPercentage());
    }

    @Test
    void getSprintBurndown_ReturnsOk() {
        BurndownPointResponse point = BurndownPointResponse.builder()
                .date(LocalDate.now())
                .remainingStoryPoints(15)
                .build();

        when(sprintService.getSprintBurndown(sprintId)).thenReturn(Collections.singletonList(point));

        ResponseEntity<List<BurndownPointResponse>> result = sprintController.getSprintBurndown(sprintId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());
        assertEquals(15, result.getBody().get(0).getRemainingStoryPoints());
    }

    @Test
    void getSprintVelocity_ReturnsOk() {
        SprintVelocityResponse velocity = SprintVelocityResponse.builder()
                .completedStoryPoints(20)
                .completedTasks(4)
                .averageStoryPointsPerTask(5.0)
                .completionPercentage(80.0)
                .build();

        when(sprintService.getSprintVelocity(sprintId)).thenReturn(velocity);

        ResponseEntity<SprintVelocityResponse> result = sprintController.getSprintVelocity(sprintId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(20, result.getBody().getCompletedStoryPoints());
        assertEquals(5.0, result.getBody().getAverageStoryPointsPerTask());
    }

    @Test
    void getTaskDistribution_ReturnsOk() {
        TaskDistributionResponse distribution = TaskDistributionResponse.builder()
                .byStatus(Collections.emptyMap())
                .byPriority(Collections.emptyMap())
                .byAssignee(Collections.emptyMap())
                .build();

        when(sprintService.getTaskDistribution(sprintId)).thenReturn(distribution);

        ResponseEntity<TaskDistributionResponse> result = sprintController.getTaskDistribution(sprintId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
    }
}
