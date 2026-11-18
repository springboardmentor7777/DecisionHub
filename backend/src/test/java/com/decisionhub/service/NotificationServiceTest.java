package com.decisionhub.service;

import com.decisionhub.entity.Notification;
import com.decisionhub.entity.Role;
import com.decisionhub.entity.User;
import com.decisionhub.repository.NotificationRepository;
import com.decisionhub.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NotificationService.
 *
 * Tests:
 *   createNotification() — 3 scenarios
 *     1. Success: user found → notification saved with isRead=false
 *     2. Field mapping: user, message, isRead, createdAt verified
 *     3. User not found → RuntimeException
 *   getNotifications() — 2 scenarios
 *     4. Returns list ordered by createdAt descending
 *     5. Returns empty list when user has no notifications
 *   markAsRead() — 5 scenarios  [D2 ownership fix]
 *     6. Owner marks own notification → isRead=true, saved and returned
 *     7. Notification not found → RuntimeException
 *     8. Already-read notification → isRead remains true (idempotent)
 *     9. Non-owner attempts markAsRead → ResponseStatusException FORBIDDEN
 *    10. Notification has no user (null) → ResponseStatusException FORBIDDEN
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService unit tests")
class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private UserRepository         userRepository;

    @InjectMocks
    private NotificationService notificationService;

    // ── Fixtures ──────────────────────────────────────────────────────────

    private static final String ALICE_EMAIL = "alice@decisionhub.com";
    private static final String BOB_EMAIL   = "bob@decisionhub.com";

    private User recipient;

    @BeforeEach
    void setUp() {
        Role role = new Role();
        role.setRoleId(1L);
        role.setRoleName("USER");

        recipient = new User();
        recipient.setUserId(1L);
        recipient.setEmail(ALICE_EMAIL);
        recipient.setRole(role);
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 1 — createNotification() success
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("createNotification() saves and returns Notification with isRead=false")
    void createNotification_userFound_savesNotification() {

        Notification saved = new Notification();
        saved.setNotificationId(1L);
        saved.setUser(recipient);
        saved.setMessage("You have a new vote!");
        saved.setIsRead(false);
        saved.setCreatedAt(LocalDateTime.now());

        when(userRepository.findById(1L)).thenReturn(Optional.of(recipient));
        when(notificationRepository.save(any(Notification.class))).thenReturn(saved);

        Notification result = notificationService.createNotification(1L, "You have a new vote!");

        assertNotNull(result);
        assertEquals(1L, result.getNotificationId());
        assertFalse(result.getIsRead());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 2 — Field mapping
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("createNotification() correctly maps user, message, isRead=false and createdAt")
    void createNotification_fieldsMappedCorrectly() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(recipient));
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        notificationService.createNotification(1L, "New comment on your decision");

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification captured = captor.getValue();
        assertEquals(recipient,                        captured.getUser());
        assertEquals("New comment on your decision",   captured.getMessage());
        assertFalse(captured.getIsRead());
        assertNotNull(captured.getCreatedAt());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 3 — createNotification() user not found
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("createNotification() throws RuntimeException when userId does not exist")
    void createNotification_userNotFound_throwsRuntimeException() {

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> notificationService.createNotification(99L, "Hello"));

        assertEquals("User not found", ex.getMessage());
        verify(notificationRepository, never()).save(any());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 4 — getNotifications() returns descending list
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getNotifications() returns notifications ordered by createdAt descending")
    void getNotifications_returnsDescendingList() {

        Notification n1 = new Notification();
        n1.setNotificationId(2L);
        n1.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));

        Notification n2 = new Notification();
        n2.setNotificationId(1L);
        n2.setCreatedAt(LocalDateTime.of(2025, 1, 1, 9, 0));

        when(notificationRepository.findByUser_UserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(n1, n2));

        List<Notification> result = notificationService.getNotifications(1L);

        assertEquals(2, result.size());
        assertEquals(2L, result.get(0).getNotificationId()); // newest first
        assertEquals(1L, result.get(1).getNotificationId());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 5 — getNotifications() empty
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getNotifications() returns empty list when user has no notifications")
    void getNotifications_noNotifications_returnsEmptyList() {

        when(notificationRepository.findByUser_UserIdOrderByCreatedAtDesc(1L))
                .thenReturn(Collections.emptyList());

        List<Notification> result = notificationService.getNotifications(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 6 — markAsRead() owner success  [D2]
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("markAsRead() sets isRead=true when the owner calls it")
    void markAsRead_owner_setsIsReadTrue() {

        Notification notification = new Notification();
        notification.setNotificationId(1L);
        notification.setUser(recipient);  // owner = alice
        notification.setIsRead(false);

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(notification)).thenReturn(notification);

        Notification result = notificationService.markAsRead(1L, ALICE_EMAIL);

        assertTrue(result.getIsRead());
        verify(notificationRepository).save(notification);
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 7 — markAsRead() notification not found  [D2]
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("markAsRead() throws RuntimeException when notification does not exist")
    void markAsRead_notFound_throwsRuntimeException() {

        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> notificationService.markAsRead(999L, ALICE_EMAIL));

        assertEquals("Notification not found", ex.getMessage());
        verify(notificationRepository, never()).save(any());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 8 — markAsRead() already read (idempotent)  [D2]
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("markAsRead() is idempotent: already-read notification stays isRead=true")
    void markAsRead_alreadyRead_remainsTrue() {

        Notification notification = new Notification();
        notification.setNotificationId(1L);
        notification.setUser(recipient);  // owner = alice
        notification.setIsRead(true);

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(notification)).thenReturn(notification);

        Notification result = notificationService.markAsRead(1L, ALICE_EMAIL);

        assertTrue(result.getIsRead());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 9 — markAsRead() non-owner blocked  [D2 new]
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("markAsRead() throws FORBIDDEN when a different user tries to mark Alice's notification")
    void markAsRead_nonOwner_throwsForbidden() {

        Notification notification = new Notification();
        notification.setNotificationId(1L);
        notification.setUser(recipient);  // owner = alice

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> notificationService.markAsRead(1L, BOB_EMAIL));  // Bob is NOT the owner

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(notificationRepository, never()).save(any());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 10 — markAsRead() notification has null user  [D2 new]
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("markAsRead() throws FORBIDDEN when notification.user is null")
    void markAsRead_nullUser_throwsForbidden() {

        Notification notification = new Notification();
        notification.setNotificationId(2L);
        notification.setUser(null);  // defensive: no owner set

        when(notificationRepository.findById(2L)).thenReturn(Optional.of(notification));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> notificationService.markAsRead(2L, ALICE_EMAIL));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(notificationRepository, never()).save(any());
    }
}
