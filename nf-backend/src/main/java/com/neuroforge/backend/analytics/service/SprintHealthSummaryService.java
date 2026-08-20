package com.neuroforge.backend.analytics.service;

import com.neuroforge.backend.analytics.dto.SprintHealthSummaryResponse;

public interface SprintHealthSummaryService {

    SprintHealthSummaryResponse generateSummary(Long sprintId);
}
