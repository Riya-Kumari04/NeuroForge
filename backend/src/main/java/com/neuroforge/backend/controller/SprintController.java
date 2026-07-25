package com.neuroforge.backend.controller;

import com.neuroforge.backend.dto.BurndownPointResponse;
import com.neuroforge.backend.dto.CreateSprintRequest;
import com.neuroforge.backend.dto.SprintProgressResponse;
import com.neuroforge.backend.dto.SprintResponse;
import com.neuroforge.backend.dto.SprintStatisticsResponse;
import com.neuroforge.backend.dto.SprintSummaryResponse;
import com.neuroforge.backend.dto.SprintVelocityResponse;
import com.neuroforge.backend.dto.TaskDistributionResponse;
import com.neuroforge.backend.service.SprintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sprints")
@RequiredArgsConstructor
public class SprintController {

    private final SprintService sprintService;

    @PostMapping
    public ResponseEntity<SprintResponse> createSprint(@Valid @RequestBody CreateSprintRequest request) {
        SprintResponse response = sprintService.createSprint(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<SprintResponse>> getAllSprints() {
        List<SprintResponse> response = sprintService.getAllSprints();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    public ResponseEntity<SprintResponse> getActiveSprint() {
        SprintResponse response = sprintService.getActiveSprint();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SprintResponse> getSprintById(@PathVariable UUID id) {
        SprintResponse response = sprintService.getSprintById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SprintResponse> updateSprint(
            @PathVariable UUID id,
            @Valid @RequestBody CreateSprintRequest request) {
        SprintResponse response = sprintService.updateSprint(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSprint(@PathVariable UUID id) {
        sprintService.deleteSprint(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/start")
    public ResponseEntity<SprintResponse> startSprint(@PathVariable UUID id) {
        SprintResponse response = sprintService.startSprint(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<SprintResponse> completeSprint(@PathVariable UUID id) {
        SprintResponse response = sprintService.completeSprint(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<SprintSummaryResponse> getSprintSummary(@PathVariable UUID id) {
        SprintSummaryResponse response = sprintService.getSprintSummary(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/statistics")
    public ResponseEntity<SprintStatisticsResponse> getSprintStatistics(@PathVariable UUID id) {
        SprintStatisticsResponse response = sprintService.getSprintStatistics(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/progress")
    public ResponseEntity<SprintProgressResponse> getSprintProgress(@PathVariable UUID id) {
        SprintProgressResponse response = sprintService.getSprintProgress(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/burndown")
    public ResponseEntity<List<BurndownPointResponse>> getSprintBurndown(@PathVariable UUID id) {
        List<BurndownPointResponse> response = sprintService.getSprintBurndown(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/velocity")
    public ResponseEntity<SprintVelocityResponse> getSprintVelocity(@PathVariable UUID id) {
        SprintVelocityResponse response = sprintService.getSprintVelocity(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/distribution")
    public ResponseEntity<TaskDistributionResponse> getTaskDistribution(@PathVariable UUID id) {
        TaskDistributionResponse response = sprintService.getTaskDistribution(id);
        return ResponseEntity.ok(response);
    }
}
