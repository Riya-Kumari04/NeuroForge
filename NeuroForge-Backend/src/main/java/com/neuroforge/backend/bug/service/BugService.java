package com.neuroforge.backend.bug.service;

import java.util.List;

import com.neuroforge.backend.bug.dto.BugResponse;
import com.neuroforge.backend.bug.dto.IncidentResponse;
import com.neuroforge.backend.bug.dto.CreateBugRequest;
import com.neuroforge.backend.dto.ApiResponse;

import com.neuroforge.backend.bug.dto.SlaTimerResponse;
import com.neuroforge.backend.bug.dto.DuplicateCheckResponse;
import com.neuroforge.backend.bug.dto.UpdateBugStatusRequest;

public interface BugService {

        ApiResponse<BugResponse> createBug(CreateBugRequest request);

        ApiResponse<List<BugResponse>> getAllBugs();

        ApiResponse<BugResponse> getBugById(Long bugId);

        ApiResponse<BugResponse> updateBugStatus(
                        Long bugId,
                        UpdateBugStatusRequest request);

        ApiResponse<List<IncidentResponse>> getAllIncidents();

        ApiResponse<SlaTimerResponse> getSlaTimer(Long incidentId);

        ApiResponse<IncidentResponse> resolveIncident(Long incidentId);


        ApiResponse<DuplicateCheckResponse> checkDuplicate(
                        CreateBugRequest request);

}