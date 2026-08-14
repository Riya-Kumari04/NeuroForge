package com.neuroforge.backend.notification.controller;

import com.neuroforge.backend.entity.User;
import com.neuroforge.backend.notification.dto.NotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

/**
 * WebSocket controller for real-time notifications
 * Handles broadcasting notifications to connected clients
 */
@Controller
@Slf4j
@RequiredArgsConstructor
public class NotificationWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Broadcast notification to a specific user
     * Called when a new notification is created
     */
    public void sendNotificationToUser(Long userId, NotificationDto notification) {
        String destination = "/topic/notifications/" + userId;
        log.info("Sending notification to user {} via {}", userId, destination);
        messagingTemplate.convertAndSend(destination, notification);
    }

    /**
     * Broadcast notification count update to a specific user
     */
    public void sendNotificationCountUpdate(Long userId, Long unreadCount) {
        String destination = "/topic/notifications/" + userId + "/count";
        log.info("Sending notification count update to user {}: {}", userId, unreadCount);
        messagingTemplate.convertAndSend(destination, unreadCount);
    }
}
