package com.neuroforge.backend.analytics.service;

import com.neuroforge.backend.analytics.dto.VelocityHistoryResponse;

public interface VelocityHistoryService {

    void refreshVelocityHistory();

    VelocityHistoryResponse getVelocityHistory();
}
