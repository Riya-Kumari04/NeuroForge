package com.neuroforge.backend.project.service;

import com.neuroforge.backend.project.dto.TaskBoardEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Module 5: Board Event Publisher
 * 
 * Publishes task board events via WebSocket to enable real-time
 * synchronization across all clients viewing the same project.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BoardEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Publishes a task status update event to all subscribers of the project's board topic.
     * 
     * @param event The task board event to publish
     */
    public void publishTaskUpdate(TaskBoardEvent event) {
        String destination = "/topic/project/" + event.getProjectId() + "/board";
        log.info("Publishing task update to {}: taskId={}, previousStatus={}, newStatus={}, changedBy={}",
                destination, event.getTaskId(), event.getPreviousStatus(), event.getNewStatus(), event.getChangedBy());
        
        messagingTemplate.convertAndSend(destination, event);
    }
}
