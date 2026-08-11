package com.neuroforge.backend.analytics.service;

import com.lowagie.text.pdf.PdfReader;
import com.neuroforge.backend.analytics.dto.SprintHealthSummaryResponse;
import com.neuroforge.backend.analytics.repository.DeploymentRecordRepository;
import com.neuroforge.backend.entity.Sprint;
import com.neuroforge.backend.enums.SprintStatus;
import com.neuroforge.backend.enums.TaskStatus;
import com.neuroforge.backend.exception.ResourceNotFoundException;
import com.neuroforge.backend.mongodb.repository.ReviewDocumentRepository;
import com.neuroforge.backend.repository.SprintRepository;
import com.neuroforge.backend.repository.TaskRepository;
import com.neuroforge.backend.repository.TaskStatusHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SprintReportPdfServiceTest {

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
    private SprintHealthSummaryService sprintHealthSummaryService;

    @InjectMocks
    private SprintReportPdfServiceImpl sprintReportPdfService;

    private UUID sprintId;
    private Sprint sprint;

    @BeforeEach
    void setUp() {
        sprintId = UUID.randomUUID();
        sprint = Sprint.builder()
                .id(sprintId)
                .name("Sprint 12")
                .status(SprintStatus.ACTIVE)
                .startDate(LocalDate.of(2026, 8, 1))
                .endDate(LocalDate.of(2026, 8, 14))
                .build();
    }

    @Test
    void generateSprintReportPdf_generatesPdfSuccessfully() throws Exception {
        when(sprintRepository.findById(sprintId)).thenReturn(Optional.of(sprint));
        when(taskRepository.countBySprintId(sprintId)).thenReturn(10L);
        when(taskRepository.countBySprintIdAndStatus(sprintId, TaskStatus.DONE)).thenReturn(8L);
        when(taskRepository.getTotalStoryPointsBySprint(sprintId)).thenReturn(25);
        when(taskRepository.getStoryPointsBySprintAndStatus(sprintId, TaskStatus.DONE)).thenReturn(20);
        when(taskRepository.findBySprintId(sprintId)).thenReturn(Collections.emptyList());

        SprintHealthSummaryResponse aiResponse = SprintHealthSummaryResponse.builder()
                .sprintId(sprintId)
                .sprintName("Sprint 12")
                .generatedAt(LocalDate.now())
                .overallHealth("HEALTHY")
                .summary("Sprint is progressing well.")
                .risks(List.of("Low risk"))
                .recommendations(List.of("Continue sprint execution"))
                .build();

        when(sprintHealthSummaryService.generateSummary(sprintId)).thenReturn(aiResponse);

        byte[] pdfBytes = sprintReportPdfService.generateSprintReportPdf(sprintId);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);

        String signature = new String(pdfBytes, 0, 4, StandardCharsets.UTF_8);
        assertTrue(signature.startsWith("%PDF"), "PDF header should start with %PDF");

        PdfReader reader = new PdfReader(pdfBytes);
        assertTrue(reader.getNumberOfPages() > 0, "PDF should have at least 1 page");
        byte[] pageContent = reader.getPageContent(1);
        assertNotNull(pageContent);
        assertTrue(pageContent.length > 0);
        reader.close();
    }

    @Test
    void generateSprintReportPdf_throwsResourceNotFoundExceptionWhenSprintMissing() {
        UUID nonExistentId = UUID.randomUUID();
        when(sprintRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> sprintReportPdfService.generateSprintReportPdf(nonExistentId)
        );

        assertEquals("Sprint not found with id: " + nonExistentId, exception.getMessage());
        verify(sprintHealthSummaryService, never()).generateSummary(any());
    }

    @Test
    void generateSprintReportPdf_completesWhenAiHealthSummaryFails() throws Exception {
        when(sprintRepository.findById(sprintId)).thenReturn(Optional.of(sprint));
        when(taskRepository.countBySprintId(sprintId)).thenReturn(5L);

        when(sprintHealthSummaryService.generateSummary(sprintId))
                .thenThrow(new RuntimeException("AI Summary Service Failure"));

        byte[] pdfBytes = sprintReportPdfService.generateSprintReportPdf(sprintId);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);

        String signature = new String(pdfBytes, 0, 4, StandardCharsets.UTF_8);
        assertTrue(signature.startsWith("%PDF"), "PDF header should start with %PDF");
    }
}
