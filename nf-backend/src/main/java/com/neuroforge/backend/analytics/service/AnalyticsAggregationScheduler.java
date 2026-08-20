package com.neuroforge.backend.analytics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnalyticsAggregationScheduler {

    private final MetricsSnapshotService metricsSnapshotService;
    private final VelocityHistoryService velocityHistoryService;

    @Scheduled(cron = "${analytics.snapshot.cron:0 0 2 * * *}")
    public void aggregateAnalytics() {
        log.info("Starting analytics snapshot aggregation");
        try {
            LocalDate today = LocalDate.now();
            metricsSnapshotService.createOrUpdateSnapshot(today);
            velocityHistoryService.refreshVelocityHistory();
            log.info("Analytics snapshot aggregation completed");
        } catch (Exception e) {
            log.error("Analytics snapshot aggregation failed", e);
            throw e;
        }
    }
}
