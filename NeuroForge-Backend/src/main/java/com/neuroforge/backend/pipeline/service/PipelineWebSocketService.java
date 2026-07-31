package com.neuroforge.backend.pipeline.service;

import com.neuroforge.backend.pipeline.dto.PipelineStageUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PipelineWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void publish(PipelineStageUpdate update) {

        messagingTemplate.convertAndSend(
                "/topic/pipeline/" + update.getRunId(),
                update
        );
    }
}