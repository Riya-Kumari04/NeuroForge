package com.neuroforge.backend.analytics.service;

import com.neuroforge.backend.analytics.dto.MetricsSnapshotResponse;

import java.time.LocalDate;
import java.util.List;

public interface MetricsSnapshotService {

    MetricsSnapshotResponse createOrUpdateSnapshot(LocalDate snapshotDate);

    MetricsSnapshotResponse getSnapshot(LocalDate snapshotDate);

    List<MetricsSnapshotResponse> getSnapshots(LocalDate startDate, LocalDate endDate);
}
