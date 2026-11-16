package com.decisionhub.controller;

import com.decisionhub.dto.NotificationResponse;
import com.decisionhub.entity.Notification;
import com.decisionhub.service.NotificationService;
import com.decisionhub.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    public NotificationController(NotificationService notificationService,
                                   UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    // GET /api/notifications — returns notifications for the authenticated user
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = userService.getUserByEmail(userDetails.getUsername()).getUserId();
        List<NotificationResponse> list = notificationService.getNotifications(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    // PATCH /api/notifications/{id}/read
    // D2 — @AuthenticationPrincipal injected so the service can verify ownership.
    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Notification notification = notificationService.markAsRead(id, userDetails.getUsername());
        return ResponseEntity.ok(toResponse(notification));
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getNotificationId(),
                n.getMessage(),
                n.getIsRead(),
                n.getCreatedAt());
    }
}
