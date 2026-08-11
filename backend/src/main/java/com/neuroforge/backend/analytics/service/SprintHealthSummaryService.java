package com.neuroforge.backend.analytics.service;

import com.neuroforge.backend.analytics.dto.SprintHealthSummaryResponse;

import java.util.UUID;

public interface SprintHealthSummaryService {

    SprintHealthSummaryResponse generateSummary(UUID sprintId);
}
