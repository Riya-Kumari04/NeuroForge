package com.neuroforge.backend.ai.controller;

import com.neuroforge.backend.ai.dto.ReleaseNotesRequest;
import com.neuroforge.backend.ai.dto.ReleaseNotesResponse;
import com.neuroforge.backend.ai.service.GroqService;
// import com.neuroforge.backend.controller.RestController;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final GroqService groqService;

    @PostMapping("/release-notes")
public ReleaseNotesResponse generateReleaseNotes(
        @RequestBody ReleaseNotesRequest request) {

    System.out.println("AI API HIT");

    return groqService.generateReleaseNotes(request);
}
}