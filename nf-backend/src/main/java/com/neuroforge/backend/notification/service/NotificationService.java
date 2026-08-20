package com.neuroforge.backend.notification.service;

import com.neuroforge.backend.dto.ApiResponse;
import com.neuroforge.backend.entity.User;
import com.neuroforge.backend.notification.controller.NotificationWebSocketController;
import com.neuroforge.backend.notification.dto.NotificationDto;
import com.neuroforge.backend.notification.entity.Notification;
import com.neuroforge.backend.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repo;
    private final NotificationWebSocketController webSocketController;

    @Transactional(readOnly = true)
    public ApiResponse<List<NotificationDto>> getAll(User user) {
        List<NotificationDto> list = repo.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream().map(NotificationDto::from).toList();
        return ApiResponse.ok("Notifications retrieved", list);
    }

    @Transactional(readOnly = true)
    public ApiResponse<Map<String, Long>> getUnreadCount(User user) {
        long count = repo.countByUserIdAndReadFalse(user.getId());
        return ApiResponse.ok("Unread count", Map.of("count", count));
    }

    @Transactional
    public ApiResponse<Void> markRead(Long id, User user) {
        repo.findById(id).ifPresent(n -> {
            if (n.getUser().getId().equals(user.getId())) {
                n.setRead(true);
                repo.save(n);
                // Send updated count via WebSocket
                long unreadCount = repo.countByUserIdAndReadFalse(user.getId());
                webSocketController.sendNotificationCountUpdate(user.getId(), unreadCount);
            }
        });
        return ApiResponse.ok("Notification marked as read");
    }

    @Transactional
    public ApiResponse<Void> markAllRead(User user) {
        repo.markAllReadByUserId(user.getId());
        // Send updated count via WebSocket
        webSocketController.sendNotificationCountUpdate(user.getId(), 0L);
        return ApiResponse.ok("All notifications marked as read");
    }

    @Transactional
    public Notification create(User user, String title, String message, String type) {
        Notification notification = repo.save(Notification.builder()
                .user(user).title(title).message(message).type(type).build());

        // Send notification via WebSocket for real-time delivery
        NotificationDto dto = NotificationDto.from(notification);
        webSocketController.sendNotificationToUser(user.getId(), dto);

        // Send updated count via WebSocket
        long unreadCount = repo.countByUserIdAndReadFalse(user.getId());
        webSocketController.sendNotificationCountUpdate(user.getId(), unreadCount);

        log.info("Created notification for user {} and sent via WebSocket: {}", user.getId(), title);
        return notification;
    }
}
