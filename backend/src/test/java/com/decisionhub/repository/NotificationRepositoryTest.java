package com.decisionhub.repository;

import com.decisionhub.entity.Notification;
import com.decisionhub.entity.Role;
import com.decisionhub.entity.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Slice test for NotificationRepository using @DataJpaTest (H2 in-memory).
 *
 * Tests:
 *   findByUser_UserIdOrderByCreatedAtDesc()
 *     1. Returns notifications for the correct user in descending createdAt order
 *     2. Returns empty list when user has no notifications
 *     3. Does NOT return notifications belonging to a different user
 *   General persistence
 *     4. save() assigns auto-generated notificationId and defaults isRead=false
 */
@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@DisplayName("NotificationRepository slice tests")
class NotificationRepositoryTest {

    @Autowired private NotificationRepository notificationRepository;
    @Autowired private UserRepository         userRepository;
    @Autowired private RoleRepository         roleRepository;

    private User userA;
    private User userB;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role role = new Role();
        role.setRoleName("USER");
        role = roleRepository.save(role);

        userA = new User();
        userA.setFullName("Alice");
        userA.setEmail("alice@decisionhub.com");
        userA.setPasswordHash("hash");
        userA.setRole(role);
        userA.setCreatedAt(LocalDateTime.now());
        userA = userRepository.save(userA);

        userB = new User();
        userB.setFullName("Bob");
        userB.setEmail("bob@decisionhub.com");
        userB.setPasswordHash("hash");
        userB.setRole(role);
        userB.setCreatedAt(LocalDateTime.now());
        userB = userRepository.save(userB);
    }

    private Notification buildNotification(User user, String message, LocalDateTime createdAt) {
        Notification n = new Notification();
        n.setUser(user);
        n.setMessage(message);
        n.setIsRead(false);
        n.setCreatedAt(createdAt);
        return n;
    }

    // ══════════════════════════════════════════════════════════════════════
    // Test 1 — Descending order by createdAt
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("findByUser_UserIdOrderByCreatedAtDesc returns notifications newest-first")
    void findByUserId_returnsDescendingOrder() {

        LocalDateTime t1 = LocalDateTime.of(2025, 1, 1, 9, 0);
        LocalDateTime t2 = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime t3 = LocalDateTime.of(2025, 1, 1, 11, 0);

        notificationRepository.save(buildNotification(userA, "Oldest", t1));
        notificationRepository.save(buildNotification(userA, "Middle", t2));
        notificationRepository.save(buildNotification(userA, "Newest", t3));

        List<Notification> result = notificationRepository
                .findByUser_UserIdOrderByCreatedAtDesc(userA.getUserId());

        assertEquals(3, result.size());
        assertEquals("Newest", result.get(0).getMessage());
        assertEquals("Middle", result.get(1).getMessage());
        assertEquals("Oldest", result.get(2).getMessage());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Test 2 — Empty result
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("findByUser_UserIdOrderByCreatedAtDesc returns empty list when user has no notifications")
    void findByUserId_noNotifications_returnsEmpty() {

        List<Notification> result = notificationRepository
                .findByUser_UserIdOrderByCreatedAtDesc(userA.getUserId());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Test 3 — Cross-user isolation
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("findByUser_UserIdOrderByCreatedAtDesc does not return notifications for other users")
    void findByUserId_isolatesUser() {

        notificationRepository.save(
                buildNotification(userA, "For Alice", LocalDateTime.now()));
        notificationRepository.save(
                buildNotification(userB, "For Bob",   LocalDateTime.now()));

        List<Notification> aliceResult = notificationRepository
                .findByUser_UserIdOrderByCreatedAtDesc(userA.getUserId());
        List<Notification> bobResult = notificationRepository
                .findByUser_UserIdOrderByCreatedAtDesc(userB.getUserId());

        assertEquals(1, aliceResult.size());
        assertEquals("For Alice", aliceResult.get(0).getMessage());

        assertEquals(1, bobResult.size());
        assertEquals("For Bob", bobResult.get(0).getMessage());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Test 4 — save() assigns id; isRead defaults to false
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("save() assigns auto-generated notificationId and persists isRead=false by default")
    void save_assignsIdAndDefaultsIsReadFalse() {

        Notification saved = notificationRepository.save(
                buildNotification(userA, "Welcome!", LocalDateTime.now()));

        assertNotNull(saved.getNotificationId());
        assertFalse(saved.getIsRead());
        assertEquals("Welcome!", saved.getMessage());
        assertEquals(userA.getUserId(), saved.getUser().getUserId());
    }
}
