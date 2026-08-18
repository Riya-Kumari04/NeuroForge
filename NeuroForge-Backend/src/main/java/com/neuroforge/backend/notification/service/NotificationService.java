package com.neuroforge.backend.notification.service;

import com.neuroforge.backend.dto.ApiResponse;
import com.neuroforge.backend.entity.User;
import com.neuroforge.backend.notification.dto.NotificationDto;
import com.neuroforge.backend.notification.entity.Notification;
import com.neuroforge.backend.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repo;

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
            }
        });
        return ApiResponse.ok("Notification marked as read");
    }

    @Transactional
    public ApiResponse<Void> markAllRead(User user) {
        repo.markAllReadByUserId(user.getId());
        return ApiResponse.ok("All notifications marked as read");
    }

    @Transactional
    public Notification create(User user, String title, String message, String type) {
        return repo.save(Notification.builder()
                .user(user).title(title).message(message).type(type).build());
    }
}
