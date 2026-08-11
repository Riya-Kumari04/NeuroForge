package com.neuroforge.backend.analytics.service;

import java.util.UUID;

public interface SprintReportPdfService {

    byte[] generateSprintReportPdf(UUID sprintId);
}
