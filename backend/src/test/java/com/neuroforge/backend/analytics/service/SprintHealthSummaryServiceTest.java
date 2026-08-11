package com.neuroforge.backend.analytics.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neuroforge.backend.analytics.dto.SprintHealthSummaryResponse;
import com.neuroforge.backend.analytics.entity.DeploymentRecord;
import com.neuroforge.backend.analytics.enums.DeploymentEnvironment;
import com.neuroforge.backend.analytics.enums.DeploymentStatus;
import com.neuroforge.backend.analytics.repository.DeploymentRecordRepository;
import com.neuroforge.backend.entity.Sprint;
import com.neuroforge.backend.entity.Task;
import com.neuroforge.backend.entity.TaskStatusHistory;
import com.neuroforge.backend.enums.IssueSeverity;
import com.neuroforge.backend.enums.SprintStatus;
import com.neuroforge.backend.enums.TaskStatus;
import com.neuroforge.backend.exception.ResourceNotFoundException;
import com.neuroforge.backend.mongodb.document.ReviewDocument;
import com.neuroforge.backend.mongodb.document.ReviewIssue;
import com.neuroforge.backend.mongodb.repository.ReviewDocumentRepository;
import com.neuroforge.backend.repository.SprintRepository;
import com.neuroforge.backend.repository.TaskRepository;
import com.neuroforge.backend.repository.TaskStatusHistoryRepository;
import com.neuroforge.backend.service.GeminiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SprintHealthSummaryServiceTest {

    @Mock
    private SprintRepository sprintRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskStatusHistoryRepository taskStatusHistoryRepository;

    @Mock
    private ReviewDocumentRepository reviewDocumentRepository;

    @Mock
    private DeploymentRecordRepository deploymentRecordRepository;

    @Mock
    private GeminiService geminiService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private SprintHealthSummaryServiceImpl sprintHealthSummaryService;

    private UUID sprintId;
    private Sprint sprint;

    @BeforeEach
    void setUp() {
        sprintId = UUID.randomUUID();
        sprint = Sprint.builder()
                .id(sprintId)
                .name("Sprint 10")
                .status(SprintStatus.ACTIVE)
                .startDate(LocalDate.now().minusDays(5))
                .endDate(LocalDate.now().plusDays(5))
                .build();
    }

    @Test
    void generateSummary_returnsHealthySummary() {
        when(sprintRepository.findById(sprintId)).thenReturn(Optional.of(sprint));
        when(taskRepository.countBySprintId(sprintId)).thenReturn(10L);
        when(taskRepository.countBySprintIdAndStatus(sprintId, TaskStatus.DONE)).thenReturn(9L);
        when(taskRepository.getTotalStoryPointsBySprint(sprintId)).thenReturn(20);
        when(taskRepository.getStoryPointsBySprintAndStatus(sprintId, TaskStatus.DONE)).thenReturn(18);
        when(taskRepository.findBySprintId(sprintId)).thenReturn(Collections.emptyList());

        String mockLlmJson = """
                {
                  "overallHealth": "HEALTHY",
                  "summary": "Sprint is on track with high completion percentage.",
                  "risks": ["Minor scope variation"],
                  "recommendations": ["Maintain current pace"]
                }
                """;

        when(geminiService.analyzeCode(anyString())).thenReturn(mockLlmJson);

        SprintHealthSummaryResponse response = sprintHealthSummaryService.generateSummary(sprintId);

        assertNotNull(response);
        assertEquals(sprintId, response.getSprintId());
        assertEquals("Sprint 10", response.getSprintName());
        assertEquals("HEALTHY", response.getOverallHealth());
        assertEquals("Sprint is on track with high completion percentage.", response.getSummary());
        assertEquals(1, response.getRisks().size());
        assertEquals("Minor scope variation", response.getRisks().get(0));
        assertEquals(1, response.getRecommendations().size());
        assertEquals("Maintain current pace", response.getRecommendations().get(0));
    }

    @Test
    void generateSummary_returnsAtRiskSummary() {
        when(sprintRepository.findById(sprintId)).thenReturn(Optional.of(sprint));
        when(taskRepository.countBySprintId(sprintId)).thenReturn(10L);
        when(taskRepository.countBySprintIdAndStatus(sprintId, TaskStatus.DONE)).thenReturn(5L);

        String mockLlmJson = """
                {
                  "overallHealth": "AT_RISK",
                  "summary": "Sprint completion is behind schedule.",
                  "risks": ["50% of tasks incomplete"],
                  "recommendations": ["Re-prioritize high priority tasks"]
                }
                """;

        when(geminiService.analyzeCode(anyString())).thenReturn(mockLlmJson);

        SprintHealthSummaryResponse response = sprintHealthSummaryService.generateSummary(sprintId);

        assertNotNull(response);
        assertEquals("AT_RISK", response.getOverallHealth());
        assertEquals("Sprint completion is behind schedule.", response.getSummary());
    }

    @Test
    void generateSummary_returnsCriticalSummary() {
        when(sprintRepository.findById(sprintId)).thenReturn(Optional.of(sprint));
        when(taskRepository.countBySprintId(sprintId)).thenReturn(10L);
        when(taskRepository.countBySprintIdAndStatus(sprintId, TaskStatus.DONE)).thenReturn(1L);

        String mockLlmJson = """
                {
                  "overallHealth": "CRITICAL",
                  "summary": "Severe completion delays and unresolved blockers.",
                  "risks": ["Critical scope slip"],
                  "recommendations": ["Escalate to delivery lead"]
                }
                """;

        when(geminiService.analyzeCode(anyString())).thenReturn(mockLlmJson);

        SprintHealthSummaryResponse response = sprintHealthSummaryService.generateSummary(sprintId);

        assertNotNull(response);
        assertEquals("CRITICAL", response.getOverallHealth());
        assertEquals("Severe completion delays and unresolved blockers.", response.getSummary());
    }

    @Test
    void generateSummary_throwsWhenSprintDoesNotExist() {
        UUID nonExistentId = UUID.randomUUID();
        when(sprintRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> sprintHealthSummaryService.generateSummary(nonExistentId)
        );

        assertEquals("Sprint not found with id: " + nonExistentId, exception.getMessage());
        verify(geminiService, never()).analyzeCode(anyString());
    }

    @Test
    void generateSummary_includesDeploymentMetricsInPrompt() {
        when(sprintRepository.findById(sprintId)).thenReturn(Optional.of(sprint));
        when(taskRepository.countBySprintId(sprintId)).thenReturn(10L);
        when(taskRepository.countBySprintIdAndStatus(sprintId, TaskStatus.DONE)).thenReturn(8L);

        DeploymentRecord record1 = DeploymentRecord.builder()
                .environment(DeploymentEnvironment.PRODUCTION)
                .status(DeploymentStatus.SUCCESS)
                .deployedAt(LocalDateTime.now())
                .build();
        DeploymentRecord record2 = DeploymentRecord.builder()
                .environment(DeploymentEnvironment.PRODUCTION)
                .status(DeploymentStatus.FAILED)
                .deployedAt(LocalDateTime.now())
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
        )).thenReturn(List.of(record1, record2));

        String mockLlmJson = """
                {
                  "overallHealth": "HEALTHY",
                  "summary": "Deployment metrics included successfully.",
                  "risks": [],
                  "recommendations": []
                }
                """;

        when(geminiService.analyzeCode(anyString())).thenReturn(mockLlmJson);

        sprintHealthSummaryService.generateSummary(sprintId);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(geminiService).analyzeCode(captor.capture());
        String prompt = captor.getValue();

        assertTrue(prompt.contains("Production Deployment Attempts: 2"));
        assertTrue(prompt.contains("Successful Production Deployments: 1"));
        assertTrue(prompt.contains("Failed Production Deployments: 1"));
        assertTrue(prompt.contains("Change Failure Rate: 50.00%"));
    }

    @Test
    void generateSummary_includesCycleTimeMetricsInPrompt() {
        when(sprintRepository.findById(sprintId)).thenReturn(Optional.of(sprint));

        Task task1 = Task.builder()
                .id(UUID.randomUUID())
                .title("Task 1")
                .status(TaskStatus.DONE)
                .build();

        TaskStatusHistory h1 = TaskStatusHistory.builder()
                .previousStatus(TaskStatus.TODO)
                .newStatus(TaskStatus.IN_PROGRESS)
                .changedAt(LocalDateTime.of(2026, 8, 1, 10, 0))
                .build();
        TaskStatusHistory h2 = TaskStatusHistory.builder()
                .previousStatus(TaskStatus.IN_PROGRESS)
                .newStatus(TaskStatus.DONE)
                .changedAt(LocalDateTime.of(2026, 8, 3, 10, 0))
                .build();

        when(taskRepository.findBySprintId(sprintId)).thenReturn(List.of(task1));
        when(taskStatusHistoryRepository.findByTaskIdOrderByChangedAtAsc(task1.getId())).thenReturn(List.of(h1, h2));

        String mockLlmJson = """
                {
                  "overallHealth": "HEALTHY",
                  "summary": "Cycle time included.",
                  "risks": [],
                  "recommendations": []
                }
                """;
        when(geminiService.analyzeCode(anyString())).thenReturn(mockLlmJson);

        sprintHealthSummaryService.generateSummary(sprintId);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(geminiService).analyzeCode(captor.capture());
        String prompt = captor.getValue();

        assertTrue(prompt.contains("Average Cycle Time (Hours): 48.00"));
    }

    @Test
    void generateSummary_calculatesSprintProgressCorrectly() {
        when(sprintRepository.findById(sprintId)).thenReturn(Optional.of(sprint));

        String mockLlmJson = """
                {
                  "overallHealth": "HEALTHY",
                  "summary": "Time context checked.",
                  "risks": [],
                  "recommendations": []
                }
                """;
        when(geminiService.analyzeCode(anyString())).thenReturn(mockLlmJson);

        sprintHealthSummaryService.generateSummary(sprintId);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(geminiService).analyzeCode(captor.capture());
        String prompt = captor.getValue();

        assertTrue(prompt.contains("Days Elapsed:"));
        assertTrue(prompt.contains("Days Remaining:"));
        assertTrue(prompt.contains("Sprint Progress:"));
        assertTrue(prompt.contains("Code Review Issues:"));
    }

    @Test
    void generateSummary_handlesZeroDeploymentsCase() {
        when(sprintRepository.findById(sprintId)).thenReturn(Optional.of(sprint));
        when(deploymentRecordRepository.findByEnvironmentAndDeployedAtBetween(
                eq(DeploymentEnvironment.PRODUCTION),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(Collections.emptyList());

        String mockLlmJson = """
                {
                  "overallHealth": "HEALTHY",
                  "summary": "Zero deployment case.",
                  "risks": [],
                  "recommendations": []
                }
                """;
        when(geminiService.analyzeCode(anyString())).thenReturn(mockLlmJson);

        sprintHealthSummaryService.generateSummary(sprintId);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(geminiService).analyzeCode(captor.capture());
        String prompt = captor.getValue();

        assertTrue(prompt.contains("Production Deployment Attempts: 0"));
        assertTrue(prompt.contains("Change Failure Rate: 0.00%"));
    }

    @Test
    void generateSummary_fallbackWithHighSeverityIssues() {
        when(sprintRepository.findById(sprintId)).thenReturn(Optional.of(sprint));

        Task task1 = Task.builder().id(UUID.randomUUID()).build();
        when(taskRepository.findBySprintId(sprintId)).thenReturn(List.of(task1));

        ReviewDocument doc = ReviewDocument.builder()
                .taskId(task1.getId())
                .issues(List.of(
                        ReviewIssue.builder().severity(IssueSeverity.HIGH).build(),
                        ReviewIssue.builder().severity(IssueSeverity.HIGH).build(),
                        ReviewIssue.builder().severity(IssueSeverity.HIGH).build()
                ))
                .build();
        when(reviewDocumentRepository.findAllByOrderByCreatedAtAsc()).thenReturn(List.of(doc));

        when(geminiService.analyzeCode(anyString())).thenThrow(new RuntimeException("LLM Unavailable"));

        SprintHealthSummaryResponse response = sprintHealthSummaryService.generateSummary(sprintId);

        assertNotNull(response);
        assertEquals("CRITICAL", response.getOverallHealth());
    }

    @Test
    void generateSummary_fallbackWithHighChangeFailureRate() {
        when(sprintRepository.findById(sprintId)).thenReturn(Optional.of(sprint));

        DeploymentRecord d1 = DeploymentRecord.builder()
                .environment(DeploymentEnvironment.PRODUCTION)
                .status(DeploymentStatus.FAILED)
                .deployedAt(LocalDateTime.now())
                .build();
        DeploymentRecord d2 = DeploymentRecord.builder()
                .environment(DeploymentEnvironment.PRODUCTION)
                .status(DeploymentStatus.FAILED)
                .deployedAt(LocalDateTime.now())
                .build();

        when(deploymentRecordRepository.findByEnvironmentAndDeployedAtBetween(
                eq(DeploymentEnvironment.PRODUCTION),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(List.of(d1, d2));

        when(geminiService.analyzeCode(anyString())).thenThrow(new RuntimeException("LLM Error"));

        SprintHealthSummaryResponse response = sprintHealthSummaryService.generateSummary(sprintId);

        assertNotNull(response);
        assertEquals("CRITICAL", response.getOverallHealth());
    }

    @Test
    void generateSummary_doesNotExposeSecretsOrRawErrors() {
        when(sprintRepository.findById(sprintId)).thenReturn(Optional.of(sprint));

        when(geminiService.analyzeCode(anyString())).thenThrow(new RuntimeException("Secret Key AIzaSyD999 failure"));

        SprintHealthSummaryResponse response = sprintHealthSummaryService.generateSummary(sprintId);

        assertNotNull(response);
        assertFalse(response.getSummary().contains("AIzaSyD999"));
        assertFalse(response.getRisks().get(0).contains("AIzaSyD999"));
    }
}
