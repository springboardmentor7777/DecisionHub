package com.decisionhub.service;

import com.decisionhub.dto.ApiDtos.NotificationResponse;
import com.decisionhub.entity.UserNotification;
import com.decisionhub.repository.UserNotificationRepository;
import com.decisionhub.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {
    private final UserNotificationRepository notifications;
    private final UserRepository users;

    public NotificationService(UserNotificationRepository notifications, UserRepository users) {
        this.notifications = notifications;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> list(String email) {
        Long userId = userId(email);
        return notifications.findAllByUser_IdOrderByCreatedAtDesc(userId).stream().map(this::response).toList();
    }

    @Transactional
    public NotificationResponse markRead(Long id, String email) {
        UserNotification notification = notifications.findByIdAndUser_Id(id, userId(email))
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        notification.markRead();
        return response(notification);
    }

    private Long userId(String email) {
        return users.findByEmailIgnoreCase(email).orElseThrow(() -> new ResourceNotFoundException("User not found")).getId();
    }

    private NotificationResponse response(UserNotification notification) {
        return new NotificationResponse(notification.getId(), notification.getMessage(),
                notification.getLink(), notification.isRead(), notification.getCreatedAt());
    }
}
