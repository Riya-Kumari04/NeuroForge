package com.neuroforge.backend.analytics.service;

import com.neuroforge.backend.analytics.dto.MetricsSnapshotResponse;
import com.neuroforge.backend.analytics.entity.DeploymentRecord;
import com.neuroforge.backend.analytics.entity.MetricsSnapshot;
import com.neuroforge.backend.analytics.enums.DeploymentEnvironment;
import com.neuroforge.backend.analytics.enums.DeploymentStatus;
import com.neuroforge.backend.analytics.repository.DeploymentRecordRepository;
import com.neuroforge.backend.analytics.repository.MetricsSnapshotRepository;
import com.neuroforge.backend.enums.IssueSeverity;
import com.neuroforge.backend.enums.TaskStatus;
import com.neuroforge.backend.mongodb.document.ReviewDocument;
import com.neuroforge.backend.mongodb.document.ReviewIssue;
import com.neuroforge.backend.mongodb.repository.ReviewDocumentRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetricsSnapshotServiceTest {

    @Mock
    private MetricsSnapshotRepository metricsSnapshotRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskStatusHistoryRepository taskStatusHistoryRepository;

    @Mock
    private ReviewDocumentRepository reviewDocumentRepository;

    @Mock
    private DeploymentRecordRepository deploymentRecordRepository;

    @InjectMocks
    private MetricsSnapshotServiceImpl metricsSnapshotService;

    private LocalDate today;

    @BeforeEach
    void setUp() {
        today = LocalDate.of(2026, 8, 12);
    }

    @Test
    void createOrUpdateSnapshot_calculatesTaskMetrics() {
        when(taskRepository.count()).thenReturn(10L);
        when(taskRepository.countByStatus(TaskStatus.DONE)).thenReturn(7L);
        when(taskRepository.getTotalStoryPoints()).thenReturn(30);
        when(taskRepository.getStoryPointsByStatus(TaskStatus.DONE)).thenReturn(21);
        when(metricsSnapshotRepository.findBySnapshotDate(today)).thenReturn(Optional.empty());
        when(metricsSnapshotRepository.save(any(MetricsSnapshot.class))).thenAnswer(i -> i.getArgument(0));

        MetricsSnapshotResponse response = metricsSnapshotService.createOrUpdateSnapshot(today);

        assertNotNull(response);
        assertEquals(10L, response.getTotalTasks());
        assertEquals(7L, response.getCompletedTasks());
        assertEquals(3L, response.getRemainingTasks());
        assertEquals(30, response.getTotalStoryPoints());
        assertEquals(21, response.getCompletedStoryPoints());
        assertEquals(70.0, response.getCompletionPercentage());
    }

    @Test
    void createOrUpdateSnapshot_calculatesIssueMetrics() {
        ReviewDocument doc = ReviewDocument.builder()
                .reviewId(UUID.randomUUID())
                .createdAt(LocalDateTime.of(2026, 8, 1, 10, 0))
                .issues(List.of(
                        ReviewIssue.builder().severity(IssueSeverity.HIGH).build(),
                        ReviewIssue.builder().severity(IssueSeverity.MEDIUM).build(),
                        ReviewIssue.builder().severity(IssueSeverity.LOW).build(),
                        ReviewIssue.builder().severity(IssueSeverity.INFO).build()
                ))
                .build();

        when(reviewDocumentRepository.findAllByOrderByCreatedAtAsc()).thenReturn(List.of(doc));
        when(metricsSnapshotRepository.findBySnapshotDate(today)).thenReturn(Optional.empty());
        when(metricsSnapshotRepository.save(any(MetricsSnapshot.class))).thenAnswer(i -> i.getArgument(0));

        MetricsSnapshotResponse response = metricsSnapshotService.createOrUpdateSnapshot(today);

        assertNotNull(response);
        assertEquals(4L, response.getTotalIssues());
        assertEquals(1L, response.getHighIssues());
        assertEquals(1L, response.getMediumIssues());
        assertEquals(1L, response.getLowIssues());
        assertEquals(1L, response.getInfoIssues());
    }

    @Test
    void createOrUpdateSnapshot_calculatesDeploymentMetrics() {
        DeploymentRecord d1 = DeploymentRecord.builder()
                .environment(DeploymentEnvironment.PRODUCTION)
                .status(DeploymentStatus.SUCCESS)
                .deployedAt(LocalDateTime.of(2026, 8, 5, 10, 0))
                .build();
        DeploymentRecord d2 = DeploymentRecord.builder()
                .environment(DeploymentEnvironment.PRODUCTION)
                .status(DeploymentStatus.FAILED)
                .deployedAt(LocalDateTime.of(2026, 8, 8, 10, 0))
                .build();

        when(deploymentRecordRepository.countByEnvironmentAndStatusAndDeployedAtBetween(
                eq(DeploymentEnvironment.PRODUCTION),
                eq(DeploymentStatus.SUCCESS),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(1L);

        when(deploymentRecordRepository.findByEnvironmentAndDeployedAtBetween(
                eq(DeploymentEnvironment.PRODUCTION),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(List.of(d1, d2));

        when(metricsSnapshotRepository.findBySnapshotDate(today)).thenReturn(Optional.empty());
        when(metricsSnapshotRepository.save(any(MetricsSnapshot.class))).thenAnswer(i -> i.getArgument(0));

        MetricsSnapshotResponse response = metricsSnapshotService.createOrUpdateSnapshot(today);

        assertNotNull(response);
        assertEquals(1L, response.getSuccessfulDeployments());
        assertEquals(2L, response.getProductionDeploymentAttempts());
        assertEquals(1L, response.getFailedProductionDeployments());
        assertEquals(0.03, response.getDeploymentFrequencyPerDay());
        assertEquals(50.0, response.getChangeFailureRate());
    }

    @Test
    void createOrUpdateSnapshot_handlesZeroProductionDeployments() {
        when(deploymentRecordRepository.countByEnvironmentAndStatusAndDeployedAtBetween(
                eq(DeploymentEnvironment.PRODUCTION),
                eq(DeploymentStatus.SUCCESS),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(0L);

        when(deploymentRecordRepository.findByEnvironmentAndDeployedAtBetween(
                eq(DeploymentEnvironment.PRODUCTION),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(Collections.emptyList());

        when(metricsSnapshotRepository.findBySnapshotDate(today)).thenReturn(Optional.empty());
        when(metricsSnapshotRepository.save(any(MetricsSnapshot.class))).thenAnswer(i -> i.getArgument(0));

        MetricsSnapshotResponse response = metricsSnapshotService.createOrUpdateSnapshot(today);

        assertNotNull(response);
        assertEquals(0L, response.getSuccessfulDeployments());
        assertEquals(0L, response.getProductionDeploymentAttempts());
        assertEquals(0L, response.getFailedProductionDeployments());
        assertEquals(0.0, response.getDeploymentFrequencyPerDay());
        assertEquals(0.0, response.getChangeFailureRate());
    }

    @Test
    void createOrUpdateSnapshot_updatesExistingSnapshot() {
        MetricsSnapshot existing = MetricsSnapshot.builder()
                .id(UUID.randomUUID())
                .snapshotDate(today)
                .totalTasks(5L)
                .build();

        when(taskRepository.count()).thenReturn(15L);
        when(metricsSnapshotRepository.findBySnapshotDate(today)).thenReturn(Optional.of(existing));
        when(metricsSnapshotRepository.save(any(MetricsSnapshot.class))).thenAnswer(i -> i.getArgument(0));

        MetricsSnapshotResponse response = metricsSnapshotService.createOrUpdateSnapshot(today);

        assertNotNull(response);
        assertEquals(existing.getId(), response.getId());
        assertEquals(15L, response.getTotalTasks());
        verify(metricsSnapshotRepository).save(existing);
    }

    @Test
    void getSnapshots_returnsSnapshotsInChronologicalOrder() {
        LocalDate start = LocalDate.of(2026, 8, 1);
        LocalDate end = LocalDate.of(2026, 8, 5);

        MetricsSnapshot s1 = MetricsSnapshot.builder().id(UUID.randomUUID()).snapshotDate(start).build();
        MetricsSnapshot s2 = MetricsSnapshot.builder().id(UUID.randomUUID()).snapshotDate(end).build();

        when(metricsSnapshotRepository.findBySnapshotDateBetweenOrderBySnapshotDateAsc(start, end))
                .thenReturn(List.of(s1, s2));

        List<MetricsSnapshotResponse> responses = metricsSnapshotService.getSnapshots(start, end);

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals(start, responses.get(0).getSnapshotDate());
        assertEquals(end, responses.get(1).getSnapshotDate());
    }
}
