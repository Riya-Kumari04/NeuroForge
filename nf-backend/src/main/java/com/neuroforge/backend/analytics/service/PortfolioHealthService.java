package com.neuroforge.backend.analytics.service;

import com.neuroforge.backend.analytics.dto.PortfolioHealthResponse;

public interface PortfolioHealthService {

    PortfolioHealthResponse getPortfolioHealth(Long organizationId);
}
