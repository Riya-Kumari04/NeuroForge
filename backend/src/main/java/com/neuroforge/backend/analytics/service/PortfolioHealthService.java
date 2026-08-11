package com.neuroforge.backend.analytics.service;

import com.neuroforge.backend.analytics.dto.PortfolioHealthResponse;

import java.util.UUID;

public interface PortfolioHealthService {

    PortfolioHealthResponse getPortfolioHealth(UUID organizationId);
}
