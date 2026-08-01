package com.neuroforge.backend.bug.controller;

import com.neuroforge.backend.bug.dto.BugResponse;
import com.neuroforge.backend.bug.dto.CreateBugRequest;
import com.neuroforge.backend.bug.service.BugService;
// import com.neuroforge.backend.controller.RestController;
// import com.neuroforge.backend.controller.RestController;
// import com.neuroforge.backend.controller.RestController;
import com.neuroforge.backend.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import com.neuroforge.backend.bug.dto.SlaTimerResponse;

import com.neuroforge.backend.bug.dto.UpdateBugStatusRequest;

import java.util.List;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bugs")
@RequiredArgsConstructor
public class BugController {

    private final BugService bugService;

    @PostMapping
    public ApiResponse<BugResponse> createBug(
            @RequestBody CreateBugRequest request) {

        return bugService.createBug(request);
    }

    @GetMapping
    public ApiResponse<List<BugResponse>> getAllBugs() {

        return bugService.getAllBugs();
    }

    @GetMapping("/{bugId}")
    public ApiResponse<BugResponse> getBugById(
            @PathVariable Long bugId) {

        return bugService.getBugById(bugId);
    }

    @PutMapping("/{bugId}/status")
    public ApiResponse<BugResponse> updateBugStatus(
            @PathVariable Long bugId,
            @RequestBody UpdateBugStatusRequest request) {

        return bugService.updateBugStatus(
                bugId,
                request);
    }

    @GetMapping("/incidents/{incidentId}/sla")
    public ApiResponse<SlaTimerResponse> getSlaTimer(
            @PathVariable Long incidentId) {

        return bugService.getSlaTimer(incidentId);
    }

}