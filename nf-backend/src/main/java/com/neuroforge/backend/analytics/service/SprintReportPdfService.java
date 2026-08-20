package com.neuroforge.backend.analytics.service;

public interface SprintReportPdfService {

    byte[] generateSprintReportPdf(Long sprintId);
}
