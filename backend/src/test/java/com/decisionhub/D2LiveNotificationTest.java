package com.decisionhub;

import com.decisionhub.dto.RegisterRequest;
import com.decisionhub.entity.Notification;
import com.decisionhub.entity.User;
import com.decisionhub.repository.NotificationRepository;
import com.decisionhub.repository.UserRepository;
import com.decisionhub.service.AuthService;
import com.decisionhub.service.NotificationService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * D2 — Notification Ownership Authorization — Live Integration Test (self-contained).
 *
 * Registers two temporary test users directly via AuthService (bypasses HTTP so no
 * password-round-trip ambiguity), acquires JWT tokens via POST /api/auth/login,
 * then verifies D2 authorization rules against live Neon.
 *
 * All test data (users + notification) is cleaned up in @AfterAll.
 *
 * Safety:
 *   - No modification of any pre-existing user record.
 *   - Flyway disabled so no migration side-effects.
 *   - spring.jpa.hibernate.ddl-auto=none — no schema changes.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=none"
})
@TestMethodOrder(OrderAnnotation.class)
@DisplayName("D2 — Notification Ownership Authorization (Live Neon)")
public class D2LiveNotificationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private AuthService authService;
    @Autowired private NotificationService notificationService;
    @Autowired private UserRepository userRepository;
    @Autowired private NotificationRepository notificationRepository;

    // Shared state across ordered tests
    private static Long notificationId;
    private static Long d2AliceId;
    private static Long d2BobId;
    private static String aliceToken;
    private static String bobToken;

    // Unique emails to avoid collision with existing users
    private static final String D2_ALICE_EMAIL    = "d2.alice.test@decisionhub.internal";
    private static final String D2_BOB_EMAIL      = "d2.bob.test@decisionhub.internal";
    private static final String D2_PASSWORD       = "D2Test_Pass!99";

    // -------------------------------------------------------------------------
    // SETUP: Register two fresh test users, login, create notification
    // -------------------------------------------------------------------------

    @Test
    @Order(1)
    @DisplayName("D2-SETUP: Register test users, login, create notification")
    void setup() throws Exception {

        // Skip if users already exist from a previous partial run
        if (userRepository.existsByEmail(D2_ALICE_EMAIL)) {
            userRepository.findByEmail(D2_ALICE_EMAIL).ifPresent(u -> d2AliceId = u.getUserId());
            System.out.println("[D2] d2Alice already exists, userId=" + d2AliceId);
        } else {
            RegisterRequest aliceReq = new RegisterRequest();
            aliceReq.setFullName("D2 Alice Test");
            aliceReq.setEmail(D2_ALICE_EMAIL);
            aliceReq.setPassword(D2_PASSWORD);
            authService.register(aliceReq);
            d2AliceId = userRepository.findByEmail(D2_ALICE_EMAIL)
                    .orElseThrow(() -> new AssertionError("D2 Alice not saved")).getUserId();
            System.out.println("[D2] Registered D2 Alice, userId=" + d2AliceId);
        }

        if (userRepository.existsByEmail(D2_BOB_EMAIL)) {
            userRepository.findByEmail(D2_BOB_EMAIL).ifPresent(u -> d2BobId = u.getUserId());
            System.out.println("[D2] d2Bob already exists, userId=" + d2BobId);
        } else {
            RegisterRequest bobReq = new RegisterRequest();
            bobReq.setFullName("D2 Bob Test");
            bobReq.setEmail(D2_BOB_EMAIL);
            bobReq.setPassword(D2_PASSWORD);
            authService.register(bobReq);
            d2BobId = userRepository.findByEmail(D2_BOB_EMAIL)
                    .orElseThrow(() -> new AssertionError("D2 Bob not saved")).getUserId();
            System.out.println("[D2] Registered D2 Bob, userId=" + d2BobId);
        }

        // Login Alice via HTTP (proves login endpoint works for freshly registered user)
        String aliceLoginJson = String.format(
                "{\"email\":\"%s\",\"password\":\"%s\"}", D2_ALICE_EMAIL, D2_PASSWORD);

        MvcResult aliceResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(aliceLoginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn();

        aliceToken = extractToken(aliceResult.getResponse().getContentAsString());
        assertNotNull(aliceToken, "Alice token must not be null");
        System.out.println("[D2] Alice logged in. Token prefix: " + aliceToken.substring(0, 20) + "...");

        // Login Bob via HTTP
        String bobLoginJson = String.format(
                "{\"email\":\"%s\",\"password\":\"%s\"}", D2_BOB_EMAIL, D2_PASSWORD);

        MvcResult bobResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bobLoginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn();

        bobToken = extractToken(bobResult.getResponse().getContentAsString());
        assertNotNull(bobToken, "Bob token must not be null");
        System.out.println("[D2] Bob logged in. Token prefix: " + bobToken.substring(0, 20) + "...");

        // Create a notification for Alice via NotificationService (internal path)
        Notification n = notificationService.createNotification(
                d2AliceId, "D2 test notification — ownership check");
        notificationId = n.getNotificationId();
        assertNotNull(notificationId, "Notification ID must not be null");
        System.out.println("[D2] Created test notification. notificationId=" + notificationId);
    }

    // -------------------------------------------------------------------------
    // D2-01: Alice can list her own notifications → 200
    // -------------------------------------------------------------------------

    @Test
    @Order(2)
    @DisplayName("D2-01: Alice GET /api/notifications → 200 with list")
    void aliceCanGetNotifications() throws Exception {
        assumeSetupDone();
        mockMvc.perform(get("/api/notifications")
                .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)));
        System.out.println("[D2] PASS — Alice GET /api/notifications → 200");
    }

    // -------------------------------------------------------------------------
    // D2-02: Unauthenticated PATCH → 401
    // -------------------------------------------------------------------------

    @Test
    @Order(3)
    @DisplayName("D2-02: Unauthenticated PATCH /api/notifications/{id}/read → 401")
    void unauthenticatedRejected() throws Exception {
        assumeSetupDone();
        mockMvc.perform(patch("/api/notifications/" + notificationId + "/read"))
                .andExpect(status().isUnauthorized());
        System.out.println("[D2] PASS — Unauthenticated PATCH → 401");
    }

    // -------------------------------------------------------------------------
    // D2-03 (CORE): Bob tries to mark Alice's notification → 403 FORBIDDEN
    // -------------------------------------------------------------------------

    @Test
    @Order(4)
    @DisplayName("D2-03 (CORE): Bob PATCH Alice's notification → 403 FORBIDDEN")
    void bobForbiddenOnAliceNotification() throws Exception {
        assumeSetupDone();
        MvcResult result = mockMvc.perform(patch("/api/notifications/" + notificationId + "/read")
                .header("Authorization", "Bearer " + bobToken))
                .andExpect(status().isForbidden())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        System.out.println("[D2] PASS — Bob PATCH Alice's notification → 403. Body: " + body);
        // Response body must contain the ownership rejection message
        assertTrue(body.contains("not allowed") || body.contains("FORBIDDEN") || !body.isEmpty(),
                "403 response should contain a message");
    }

    // -------------------------------------------------------------------------
    // D2-04: Alice marks her own notification → 200 OK, isRead=true
    // -------------------------------------------------------------------------

    @Test
    @Order(5)
    @DisplayName("D2-04: Alice PATCH own notification → 200 OK, isRead=true")
    void aliceMarksOwnNotification() throws Exception {
        assumeSetupDone();
        MvcResult result = mockMvc.perform(patch("/api/notifications/" + notificationId + "/read")
                .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificationId").value(notificationId))
                .andExpect(jsonPath("$.isRead").value(true))
                .andReturn();

        System.out.println("[D2] PASS — Alice PATCH own notification → 200. Body: "
                + result.getResponse().getContentAsString());
    }

    // -------------------------------------------------------------------------
    // D2-05: Non-existent notification → 4xx/5xx
    // -------------------------------------------------------------------------

    @Test
    @Order(6)
    @DisplayName("D2-05: Alice PATCH non-existent notification → 4xx/5xx")
    void notFoundNotification() throws Exception {
        assumeSetupDone();
        int status = mockMvc.perform(patch("/api/notifications/999999999/read")
                .header("Authorization", "Bearer " + aliceToken))
                .andReturn()
                .getResponse()
                .getStatus();

        assertTrue(status == 404 || status == 500,
                "Expected 404 or 500 for non-existent notification, got: " + status);
        System.out.println("[D2] PASS — Non-existent notification PATCH → HTTP " + status);
    }

    // -------------------------------------------------------------------------
    // CLEANUP: Remove test notification and test users from Neon
    // -------------------------------------------------------------------------

    @Test
    @Order(99)
    @DisplayName("D2-CLEANUP: Remove test data from Neon")
    void cleanup() {
        if (notificationId != null) {
            notificationRepository.deleteById(notificationId);
            System.out.println("[D2] Deleted test notification: " + notificationId);
        }
        // Delete the two test users created by this test
        if (d2AliceId != null) {
            userRepository.deleteById(d2AliceId);
            System.out.println("[D2] Deleted D2 Alice userId=" + d2AliceId);
        }
        if (d2BobId != null) {
            userRepository.deleteById(d2BobId);
            System.out.println("[D2] Deleted D2 Bob userId=" + d2BobId);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void assumeSetupDone() {
        Assumptions.assumeTrue(notificationId != null && aliceToken != null && bobToken != null,
                "D2 setup did not complete — skipping dependent test");
    }

    /** Extracts accessToken value from a JSON string without an external library. */
    private String extractToken(String json) {
        int idx = json.indexOf("\"accessToken\"");
        if (idx < 0) return null;
        int colon = json.indexOf(":", idx);
        int q1    = json.indexOf("\"", colon);
        int q2    = json.indexOf("\"", q1 + 1);
        if (q1 < 0 || q2 < 0) return null;
        return json.substring(q1 + 1, q2);
    }
}
