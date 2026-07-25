package com.neuroforge.backend.controller;

import com.neuroforge.backend.dto.CreateSprintRequest;
import com.neuroforge.backend.dto.SprintResponse;
import com.neuroforge.backend.service.SprintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
}
