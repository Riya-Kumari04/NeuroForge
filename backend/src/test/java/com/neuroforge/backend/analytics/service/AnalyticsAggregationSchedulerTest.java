package com.neuroforge.backend.analytics.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AnalyticsAggregationSchedulerTest {

    @Mock
    private MetricsSnapshotService metricsSnapshotService;

    @Mock
    private VelocityHistoryService velocityHistoryService;

    @InjectMocks
    private AnalyticsAggregationScheduler scheduler;

    @Test
    void aggregateAnalytics_executesSnapshotAndVelocityHistoryRefresh() {
        scheduler.aggregateAnalytics();

        verify(metricsSnapshotService).createOrUpdateSnapshot(eq(LocalDate.now()));
        verify(velocityHistoryService).refreshVelocityHistory();
    }
}
