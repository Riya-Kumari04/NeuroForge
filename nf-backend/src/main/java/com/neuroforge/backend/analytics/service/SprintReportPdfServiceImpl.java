package com.neuroforge.backend.analytics.service;

import com.lowagie.text.Cell;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.PdfWriter;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SprintReportPdfServiceImpl implements SprintReportPdfService {

    private final SprintRepository sprintRepository;
    private final TaskRepository taskRepository;
    private final TaskStatusHistoryRepository taskStatusHistoryRepository;
    private final ReviewDocumentRepository reviewDocumentRepository;
    private final DeploymentRecordRepository deploymentRecordRepository;
    private final SprintHealthSummaryService sprintHealthSummaryService;

    @Override
    public byte[] generateSprintReportPdf(Long sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found with id: " + sprintId));

        // 1. Gather Metrics
        String sprintName = sprint.getSprintName();
        String statusStr = sprint.getStatus() != null ? sprint.getStatus() : "N/A";
        LocalDate startDate = sprint.getStartDate() != null ? sprint.getStartDate().toLocalDate() : null;
        LocalDate endDate = sprint.getEndDate() != null ? sprint.getEndDate().toLocalDate() : null;

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

        // Cycle Time
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
        double averageCycleTimeHours = measuredCount > 0 ? Math.round(((totalMinutes / (double) measuredCount) / 60.0) * 100.0) / 100.0 : 0.0;

        // Code Review Issues
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

        // Deployment Metrics
        long productionDeploymentAttempts = 0;
        long successfulProductionDeployments = 0;
        long failedProductionDeployments = 0;
        double deploymentFrequencyPerDay = 0.0;
        double changeFailureRate = 0.0;

        long sprintDays = (startDate != null && endDate != null && !endDate.isBefore(startDate))
                ? ChronoUnit.DAYS.between(startDate, endDate) + 1
                : 1;

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
                deploymentFrequencyPerDay = Math.round((successfulProductionDeployments / (double) sprintDays) * 100.0) / 100.0;
            }

            if (productionDeploymentAttempts > 0) {
                double rawRate = (failedProductionDeployments * 100.0) / (double) productionDeploymentAttempts;
                changeFailureRate = Math.round(rawRate * 100.0) / 100.0;
            }
        }

        // AI Sprint Health Summary
        SprintHealthSummaryResponse aiSummary = null;
        try {
            aiSummary = sprintHealthSummaryService.generateSummary(sprintId);
        } catch (Exception e) {
            log.warn("AI Sprint Health Summary generation failed during PDF export for sprint {}", sprintId);
        }

        // 2. Generate PDF Document using OpenPDF
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, new Color(0, 102, 204));
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);

            // Title
            Paragraph title = new Paragraph("NeuroForge SDLC - Sprint Analytics Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(15);
            document.add(title);

            Paragraph timestampPara = new Paragraph("Report Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), normalFont);
            timestampPara.setAlignment(Element.ALIGN_RIGHT);
            timestampPara.setSpacingAfter(15);
            document.add(timestampPara);

            // Section 1: Sprint Information
            document.add(new Paragraph("1. Sprint Information", headerFont));
            Table infoTable = new Table(2);
            infoTable.setWidth(100);
            infoTable.setPadding(3);
            infoTable.setSpacing(5);

            addTableRow(infoTable, "Sprint Name", sprintName, boldFont, normalFont);
            addTableRow(infoTable, "Sprint ID", sprintId.toString(), boldFont, normalFont);
            addTableRow(infoTable, "Start Date", startDate != null ? startDate.toString() : "N/A", boldFont, normalFont);
            addTableRow(infoTable, "End Date", endDate != null ? endDate.toString() : "N/A", boldFont, normalFont);
            addTableRow(infoTable, "Status", statusStr, boldFont, normalFont);

            document.add(infoTable);
            document.add(new Paragraph(" "));

            // Section 2: Sprint Performance
            document.add(new Paragraph("2. Sprint Performance", headerFont));
            Table perfTable = new Table(2);
            perfTable.setWidth(100);
            perfTable.setPadding(3);
            perfTable.setSpacing(5);

            addTableRow(perfTable, "Total Tasks", String.valueOf(totalTasks), boldFont, normalFont);
            addTableRow(perfTable, "Completed Tasks", String.valueOf(completedTasks), boldFont, normalFont);
            addTableRow(perfTable, "Remaining Tasks", String.valueOf(remainingTasks), boldFont, normalFont);
            addTableRow(perfTable, "Total Story Points", String.valueOf(totalStoryPoints), boldFont, normalFont);
            addTableRow(perfTable, "Completed Story Points", String.valueOf(completedStoryPoints), boldFont, normalFont);
            addTableRow(perfTable, "Remaining Story Points", String.valueOf(remainingStoryPoints), boldFont, normalFont);
            addTableRow(perfTable, "Completion Percentage", String.format("%.2f%%", completionPercentage), boldFont, normalFont);

            document.add(perfTable);
            document.add(new Paragraph(" "));

            // Section 3: Analytics Metrics
            document.add(new Paragraph("3. Velocity, Cycle Time & Quality Metrics", headerFont));
            Table analyticsTable = new Table(2);
            analyticsTable.setWidth(100);
            analyticsTable.setPadding(3);
            analyticsTable.setSpacing(5);

            addTableRow(analyticsTable, "Sprint Velocity (Completed Story Points)", String.valueOf(completedStoryPoints), boldFont, normalFont);
            addTableRow(analyticsTable, "Average Cycle Time", String.format("%.2f hours", averageCycleTimeHours), boldFont, normalFont);
            addTableRow(analyticsTable, "Total Code Review Issues", String.valueOf(totalIssues), boldFont, normalFont);
            addTableRow(analyticsTable, "HIGH Severity Issues", String.valueOf(highIssues), boldFont, normalFont);
            addTableRow(analyticsTable, "MEDIUM Severity Issues", String.valueOf(mediumIssues), boldFont, normalFont);
            addTableRow(analyticsTable, "LOW Severity Issues", String.valueOf(lowIssues), boldFont, normalFont);
            addTableRow(analyticsTable, "INFO Severity Issues", String.valueOf(infoIssues), boldFont, normalFont);

            document.add(analyticsTable);
            document.add(new Paragraph(" "));

            // Section 4: DORA Metrics
            document.add(new Paragraph("4. DORA-Style Delivery Metrics", headerFont));
            Table doraTable = new Table(2);
            doraTable.setWidth(100);
            doraTable.setPadding(3);
            doraTable.setSpacing(5);

            addTableRow(doraTable, "Production Deployment Attempts", String.valueOf(productionDeploymentAttempts), boldFont, normalFont);
            addTableRow(doraTable, "Successful Deployments", String.valueOf(successfulProductionDeployments), boldFont, normalFont);
            addTableRow(doraTable, "Failed Deployments", String.valueOf(failedProductionDeployments), boldFont, normalFont);
            addTableRow(doraTable, "Deployment Frequency (per day)", String.format("%.2f", deploymentFrequencyPerDay), boldFont, normalFont);
            addTableRow(doraTable, "Change Failure Rate", String.format("%.2f%%", changeFailureRate), boldFont, normalFont);

            document.add(doraTable);
            document.add(new Paragraph(" "));

            // Section 5: AI Sprint Health Summary
            document.add(new Paragraph("5. AI Sprint Health Summary", headerFont));
            if (aiSummary != null) {
                Table healthTable = new Table(2);
                healthTable.setWidth(100);
                healthTable.setPadding(3);
                healthTable.setSpacing(5);

                addTableRow(healthTable, "Overall Health", aiSummary.getOverallHealth() != null ? aiSummary.getOverallHealth() : "N/A", boldFont, normalFont);
                document.add(healthTable);

                document.add(new Paragraph("Summary:", boldFont));
                document.add(new Paragraph(aiSummary.getSummary() != null ? aiSummary.getSummary() : "N/A", normalFont));

                document.add(new Paragraph("Risks:", boldFont));
                if (aiSummary.getRisks() != null && !aiSummary.getRisks().isEmpty()) {
                    for (String risk : aiSummary.getRisks()) {
                        document.add(new Paragraph("- " + risk, normalFont));
                    }
                } else {
                    document.add(new Paragraph("None reported", normalFont));
                }

                document.add(new Paragraph("Recommendations:", boldFont));
                if (aiSummary.getRecommendations() != null && !aiSummary.getRecommendations().isEmpty()) {
                    for (String rec : aiSummary.getRecommendations()) {
                        document.add(new Paragraph("- " + rec, normalFont));
                    }
                } else {
                    document.add(new Paragraph("None reported", normalFont));
                }
            } else {
                document.add(new Paragraph("AI Sprint Health Summary is currently unavailable for this sprint.", normalFont));
            }

            document.close();
        } catch (DocumentException e) {
            log.error("Failed to generate PDF document for sprint {}", sprintId, e);
            throw new RuntimeException("Failed to generate PDF report", e);
        }

        return out.toByteArray();
    }

    private void addTableRow(Table table, String label, String value, Font labelFont, Font valueFont) throws DocumentException {
        Cell labelCell = new Cell(new Paragraph(label, labelFont));
        labelCell.setBackgroundColor(new Color(240, 240, 240));
        table.addCell(labelCell);

        Cell valueCell = new Cell(new Paragraph(value, valueFont));
        table.addCell(valueCell);
    }
}
