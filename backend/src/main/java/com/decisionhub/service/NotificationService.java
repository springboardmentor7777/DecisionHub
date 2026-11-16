
package com.decisionhub.service;

import com.decisionhub.entity.*;
import com.decisionhub.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public Notification createNotification(Long userId, String message) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        return notificationRepository.save(notification);
    }

    public List<Notification> getNotifications(Long userId) {
        return notificationRepository.findByUser_UserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * D2 — Marks a notification as read only if the authenticated user owns it.
     *
     * @param notificationId  ID of the notification to mark as read.
     * @param ownerEmail      Email of the authenticated user (from Spring Security principal).
     * @throws ResponseStatusException 403 FORBIDDEN when the notification belongs to a different user.
     * @throws RuntimeException        404 when the notification does not exist.
     */
    public Notification markAsRead(Long notificationId, String ownerEmail) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        // D2 — Ownership check: reject if the notification belongs to a different user.
        if (notification.getUser() == null
                || !notification.getUser().getEmail().equals(ownerEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You are not allowed to mark this notification as read");
        }

        notification.setIsRead(true);
        return notificationRepository.save(notification);
    }
}
