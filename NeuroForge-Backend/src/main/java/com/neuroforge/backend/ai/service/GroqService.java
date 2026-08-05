package com.neuroforge.backend.ai.service;

import com.neuroforge.backend.ai.dto.ReleaseNotesRequest;
import com.neuroforge.backend.ai.dto.ReleaseNotesResponse;

public interface GroqService {

        ReleaseNotesResponse generateReleaseNotes(
                        ReleaseNotesRequest request);

        boolean isDuplicate(
                        String existingBug,
                        String newBug);

        String chat(String prompt);
}