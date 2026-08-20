package com.neuroforge.backend.analytics.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neuroforge.backend.analytics.dto.SprintHealthSummaryResponse;
import com.neuroforge.backend.analytics.entity.DeploymentRecord;
import com.neuroforge.backend.analytics.enums.DeploymentEnvironment;
import com.neuroforge.backend.analytics.enums.DeploymentStatus;
import com.neuroforge.backend.analytics.repository.DeploymentRecordRepository;
import com.neuroforge.backend.project.entity.Sprint;
import com.neuroforge.backend.project.entity.Task;
import com.neuroforge.backend.project.entity.TaskStatusHistory;
import com.neuroforge.backend.specification.exception.ResourceNotFoundException;
import com.neuroforge.backend.mongodb.document.ReviewDocument;
import com.neuroforge.backend.mongodb.document.ReviewIssue;
import com.neuroforge.backend.mongodb.repository.ReviewDocumentRepository;
import com.neuroforge.backend.project.repository.SprintRepository;
import com.neuroforge.backend.project.repository.TaskRepository;
import com.neuroforge.backend.project.repository.TaskStatusHistoryRepository;
import com.neuroforge.backend.ai.service.GeminiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SprintHealthSummaryServiceImpl implements SprintHealthSummaryService {

    private final SprintRepository sprintRepository;
    private final TaskRepository taskRepository;
    private final TaskStatusHistoryRepository taskStatusHistoryRepository;
    private final ReviewDocumentRepository reviewDocumentRepository;
    private final DeploymentRecordRepository deploymentRecordRepository;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    @Override
    public SprintHealthSummaryResponse generateSummary(Long sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found with id: " + sprintId));

        String sprintName = sprint.getSprintName();
        String sprintStatus = sprint.getStatus();
        LocalDate startDate = sprint.getStartDate() != null ? sprint.getStartDate().toLocalDate() : null;
        LocalDate endDate = sprint.getEndDate() != null ? sprint.getEndDate().toLocalDate() : null;

        // 1. Sprint Time Context
        LocalDate today = LocalDate.now();
        long totalSprintDays = (startDate != null && endDate != null && !endDate.isBefore(startDate))
                ? ChronoUnit.DAYS.between(startDate, endDate) + 1
                : 1;

        long daysElapsed;
        long daysRemaining;
        double sprintProgressPercentage;

        if (startDate == null || endDate == null) {
            daysElapsed = 0;
            daysRemaining = 0;
            sprintProgressPercentage = 0.0;
        } else if (today.isBefore(startDate)) {
            daysElapsed = 0;
            daysRemaining = totalSprintDays;
            sprintProgressPercentage = 0.0;
        } else if (today.isAfter(endDate)) {
            daysElapsed = totalSprintDays;
            daysRemaining = 0;
            sprintProgressPercentage = 100.0;
        } else {
            daysElapsed = ChronoUnit.DAYS.between(startDate, today) + 1;
            daysRemaining = Math.max(0, ChronoUnit.DAYS.between(today, endDate));
            sprintProgressPercentage = Math.round(((daysElapsed * 100.0) / (double) totalSprintDays) * 100.0) / 100.0;
        }

        // 2. Sprint Work Metrics
        long totalTasks = taskRepository.countBySprintId(sprintId);
        long completedTasks = taskRepository.countBySprintIdAndStatus(sprintId, "DONE");
        long remainingTasks = totalTasks - completedTasks;
        double completionPercentage = totalTasks > 0 ? Math.round(((completedTasks * 100.0) / totalTasks) * 100.0) / 100.0 : 0.0;

        Integer totalPointsObj = taskRepository.getTotalStoryPointsBySprint(sprintId);
        int totalStoryPoints = totalPointsObj != null ? totalPointsObj : 0;
        Integer completedPointsObj = taskRepository.getStoryPointsBySprintAndStatus(sprintId, "DONE");
        int completedStoryPoints = completedPointsObj != null ? completedPointsObj : 0;
        int remainingStoryPoints = totalStoryPoints - completedStoryPoints;

        List<Task> sprintTasks = taskRepository.findBySprintId(sprintId);

        // 3. Cycle Time for Sprint Tasks
        long measuredCount = 0;
        double totalMinutes = 0.0;

        if (sprintTasks != null) {
            for (Task task : sprintTasks) {
                if (task == null || task.getId() == null || task.getStatus() != "DONE") {
                    continue;
                }
                List<TaskStatusHistory> historyList = taskStatusHistoryRepository.findByTaskIdOrderByChangedAtAsc(task.getId());
                if (historyList == null || historyList.isEmpty()) {
                    continue;
                }

                LocalDateTime startedAt = null;
                LocalDateTime completedAt = null;

                for (TaskStatusHistory history : historyList) {
                    if (history == null || history.getChangedAt() == null || history.getNewStatus() == null) {
                        continue;
                    }

                    if (startedAt == null && history.getNewStatus() == "IN_PROGRESS") {
                        startedAt = history.getChangedAt();
                    } else if (startedAt != null && history.getNewStatus() == "DONE") {
                        if (!history.getChangedAt().isBefore(startedAt)) {
                            completedAt = history.getChangedAt();
                            break;
                        }
                    }
                }

                if (startedAt != null && completedAt != null && !completedAt.isBefore(startedAt)) {
                    long minutes = Duration.between(startedAt, completedAt).toMinutes();
                    totalMinutes += minutes;
                    measuredCount++;
                }
            }
        }

        double averageCycleTimeHours = 0.0;
        if (measuredCount > 0) {
            double avgHours = (totalMinutes / (double) measuredCount) / 60.0;
            averageCycleTimeHours = Math.round(avgHours * 100.0) / 100.0;
        }

        // 4. Code Review Issues
        Set<Long> taskIds = sprintTasks != null ? sprintTasks.stream().map(Task::getId).collect(Collectors.toSet()) : Set.of();
        long highIssues = 0;
        long mediumIssues = 0;
        long lowIssues = 0;
        long infoIssues = 0;

        if (!taskIds.isEmpty()) {
            List<ReviewDocument> reviewDocs = reviewDocumentRepository.findAllByOrderByCreatedAtAsc();
            if (reviewDocs != null) {
                for (ReviewDocument doc : reviewDocs) {
                    if (doc != null && doc.getTaskId() != null && taskIds.contains(doc.getTaskId()) && doc.getIssues() != null) {
                        for (ReviewIssue issue : doc.getIssues()) {
                            if (issue != null && issue.getSeverity() != null) {
                                switch (issue.getSeverity()) {
                                    case HIGH -> highIssues++;
                                    case MEDIUM -> mediumIssues++;
                                    case LOW -> lowIssues++;
                                    case INFO -> infoIssues++;
                                }
                            }
                        }
                    }
                }
            }
        }
        long totalIssues = highIssues + mediumIssues + lowIssues + infoIssues;

        // 5. Deployment Metrics for Sprint Period
        long productionDeploymentAttempts = 0;
        long successfulProductionDeployments = 0;
        long failedProductionDeployments = 0;
        double deploymentFrequencyPerDay = 0.0;
        double changeFailureRate = 0.0;

        if (startDate != null && endDate != null && !endDate.isBefore(startDate)) {
            LocalDateTime startTimestamp = startDate.atStartOfDay();
            LocalDateTime endTimestamp = endDate.atTime(23, 59, 59, 999_999_999);

            successfulProductionDeployments = deploymentRecordRepository.countByEnvironmentAndStatusAndDeployedAtBetween(
                    DeploymentEnvironment.PRODUCTION,
                    DeploymentStatus.SUCCESS,
                    startTimestamp,
                    endTimestamp
            );

            List<DeploymentRecord> prodDeployments = deploymentRecordRepository.findByEnvironmentAndDeployedAtBetween(
                    DeploymentEnvironment.PRODUCTION,
                    startTimestamp,
                    endTimestamp
            );

            if (prodDeployments != null) {
                productionDeploymentAttempts = prodDeployments.size();
                failedProductionDeployments = prodDeployments.stream()
                        .filter(d -> d.getStatus() == DeploymentStatus.FAILED)
                        .count();
            }

            if (successfulProductionDeployments > 0) {
                deploymentFrequencyPerDay = Math.round((successfulProductionDeployments / (double) totalSprintDays) * 100.0) / 100.0;
            }

            if (productionDeploymentAttempts > 0) {
                double rawRate = (failedProductionDeployments * 100.0) / (double) productionDeploymentAttempts;
                changeFailureRate = Math.round(rawRate * 100.0) / 100.0;
            }
        }

        // 6. Build Gemini Prompt
        String prompt = String.format("""
                You are an engineering delivery analytics assistant.
                Analyze the supplied sprint metrics and return an executive summary in valid JSON format.

                Sprint Time Context:
                - Start Date: %s
                - End Date: %s
                - Days Elapsed: %d
                - Days Remaining: %d
                - Sprint Progress: %.2f%%

                Sprint Work Metrics:
                - Sprint Name: %s
                - Status: %s
                - Total Tasks: %d
                - Completed Tasks: %d
                - Remaining Tasks: %d
                - Completion Percentage: %.2f%%
                - Total Story Points: %d
                - Completed Story Points: %d
                - Remaining Story Points: %d
                - Average Cycle Time (Hours): %.2f

                Quality & Code Review Metrics:
                - Code Review Issues: %d (HIGH: %d, MEDIUM: %d, LOW: %d, INFO: %d)

                Deployment Metrics (Sprint Window):
                - Production Deployment Attempts: %d
                - Successful Production Deployments: %d
                - Failed Production Deployments: %d
                - Deployment Frequency (Per Day): %.2f
                - Change Failure Rate: %.2f%%

                Return ONLY a JSON object (no markdown, no ```json formatting, no extra text) with the following structure:
                {
                  "overallHealth": "HEALTHY",
                  "summary": "Plain language executive summary...",
                  "risks": [
                    "Risk 1",
                    "Risk 2"
                  ],
                  "recommendations": [
                    "Recommendation 1",
                    "Recommendation 2"
                  ]
                }

                Rules:
                1. "overallHealth" must be exactly one of: "HEALTHY", "AT_RISK", "CRITICAL".
                   - HEALTHY: Sprint is progressing normally with no major delivery risk.
                   - AT_RISK: One or more meaningful delivery risks exist.
                   - CRITICAL: Severe completion, quality, or delivery problems exist.
                2. "summary" must be a non-blank string summarizing sprint health.
                3. "risks" must be a list of string risk factors.
                4. "recommendations" must be a list of actionable string recommendations.
                """,
                startDate, endDate, daysElapsed, daysRemaining, sprintProgressPercentage,
                sprintName, sprintStatus,
                totalTasks, completedTasks, remainingTasks, completionPercentage,
                totalStoryPoints, completedStoryPoints, remainingStoryPoints, averageCycleTimeHours,
                totalIssues, highIssues, mediumIssues, lowIssues, infoIssues,
                productionDeploymentAttempts, successfulProductionDeployments, failedProductionDeployments,
                deploymentFrequencyPerDay, changeFailureRate);

        String overallHealth = null;
        String summary = null;
        List<String> risks = null;
        List<String> recommendations = null;

        try {
            String rawResponse = geminiService.analyzeCode(prompt);
            if (rawResponse != null) {
                String cleaned = rawResponse.trim();
                if (cleaned.startsWith("```json")) {
                    cleaned = cleaned.substring(7);
                }
                if (cleaned.startsWith("```")) {
                    cleaned = cleaned.substring(3);
                }
                if (cleaned.endsWith("```")) {
                    cleaned = cleaned.substring(0, cleaned.length() - 3);
                }
                cleaned = cleaned.trim();

                JsonNode rootNode = objectMapper.readTree(cleaned);
                if (rootNode != null && rootNode.isObject()) {
                    if (rootNode.has("overallHealth") && !rootNode.get("overallHealth").isNull()) {
                        String healthStr = rootNode.get("overallHealth").asText().trim();
                        if ("HEALTHY".equalsIgnoreCase(healthStr) || "AT_RISK".equalsIgnoreCase(healthStr) || "CRITICAL".equalsIgnoreCase(healthStr)) {
                            overallHealth = healthStr.toUpperCase();
                        }
                    }

                    if (rootNode.has("summary") && !rootNode.get("summary").isNull()) {
                        String sumStr = rootNode.get("summary").asText().trim();
                        if (!sumStr.isEmpty()) {
                            summary = sumStr;
                        }
                    }

                    if (rootNode.has("risks") && rootNode.get("risks").isArray()) {
                        List<String> riskList = new ArrayList<>();
                        for (JsonNode node : rootNode.get("risks")) {
                            if (node != null && !node.isNull() && !node.asText().isBlank()) {
                                riskList.add(node.asText());
                            }
                        }
                        risks = riskList;
                    }

                    if (rootNode.has("recommendations") && rootNode.get("recommendations").isArray()) {
                        List<String> recList = new ArrayList<>();
                        for (JsonNode node : rootNode.get("recommendations")) {
                            if (node != null && !node.isNull() && !node.asText().isBlank()) {
                                recList.add(node.asText());
                            }
                        }
                        recommendations = recList;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Gemini Service invocation or parsing failed for sprint {}", sprintId);
        }

        // 7. Deterministic Fallback Health Calculation
        if (overallHealth == null || summary == null || risks == null || recommendations == null) {
            if (completionPercentage < (sprintProgressPercentage - 40.0)
                    || highIssues >= 3
                    || changeFailureRate >= 50.0
                    || (sprintProgressPercentage >= 90.0 && remainingStoryPoints > 0 && completionPercentage < 50.0)) {
                overallHealth = "CRITICAL";
            } else if (completionPercentage < (sprintProgressPercentage - 20.0)
                    || highIssues > 0
                    || changeFailureRate >= 30.0) {
                overallHealth = "AT_RISK";
            } else {
                overallHealth = "HEALTHY";
            }

            summary = "Unable to generate the AI explanation automatically. Manual review of sprint completion and issues is recommended.";
            risks = List.of("AI summary generation unavailable or produced invalid output");
            recommendations = List.of("Review sprint completion, remaining story points, and code review issues manually");
        }

        return SprintHealthSummaryResponse.builder()
                .sprintId(sprintId)
                .sprintName(sprintName)
                .generatedAt(LocalDate.now())
                .overallHealth(overallHealth)
                .summary(summary)
                .risks(risks)
                .recommendations(recommendations)
                .build();
    }
}
