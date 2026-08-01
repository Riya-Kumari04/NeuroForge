package com.neuroforge.backend.bug.service;

import com.neuroforge.backend.bug.dto.IncidentAlert;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BugWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void publish(IncidentAlert alert) {

        messagingTemplate.convertAndSend(
                "/topic/incidents",
                alert);
    }
}