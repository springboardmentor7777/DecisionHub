package com.decisionhub.service;

import com.decisionhub.entity.Community;
import com.decisionhub.entity.Decision;
import com.decisionhub.entity.Role;
import com.decisionhub.entity.User;
import com.decisionhub.repository.CommunityRepository;
import com.decisionhub.repository.DecisionRepository;
import com.decisionhub.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DecisionService.
 *
 * Tests:
 *   createDecision() — 5 scenarios
 *     1. Success without community (communityId = null)
 *     2. Success with a valid communityId → community linked
 *     3. Field mapping verified via ArgumentCaptor
 *     4. User not found → IllegalStateException
 *     5. communityId provided but community not found → IllegalArgumentException
 *   updateDecision() — 3 scenarios (D1 ownership fix)
 *     6. Owner updates own decision → 200, decision saved
 *     7. Non-owner attempts update → ResponseStatusException FORBIDDEN
 *     8. Decision not found → IllegalArgumentException
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DecisionService unit tests")
class DecisionServiceTest {

    @Mock private DecisionRepository  decisionRepository;
    @Mock private UserRepository      userRepository;
    @Mock private CommunityRepository communityRepository;

    @InjectMocks
    private DecisionService decisionService;

    // ── Fixtures ──────────────────────────────────────────────────────────

    private static final String EMAIL = "alice@decisionhub.com";

    private User    creator;
    private Community community;

    @BeforeEach
    void setUp() {
        Role role = new Role();
        role.setRoleId(1L);
        role.setRoleName("USER");

        creator = new User();
        creator.setUserId(1L);
        creator.setFullName("Alice Smith");
        creator.setEmail(EMAIL);
        creator.setRole(role);
        creator.setCreatedAt(LocalDateTime.of(2025, 1, 1, 12, 0));

        community = new Community();
        community.setCommunityId(5L);
        community.setCommunityName("Tech Enthusiasts");
        community.setCreatedBy(creator);

        // Authenticate as creator
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(new UsernamePasswordAuthenticationToken(
                EMAIL, null, Collections.emptyList()));
        SecurityContextHolder.setContext(ctx);
    }

    @BeforeEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(String email) {
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(new UsernamePasswordAuthenticationToken(
                email, null, Collections.emptyList()));
        SecurityContextHolder.setContext(ctx);
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 1 — Success without community
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("createDecision() persists decision with no community when communityId is null")
    void createDecision_noCommunity_savesCommunityAsNull() {

        authenticateAs(EMAIL);

        Decision saved = new Decision();
        saved.setDecisionId(1L);
        saved.setTitle("Best IDE?");
        saved.setCreatedBy(creator);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(creator));
        when(decisionRepository.save(any(Decision.class))).thenReturn(saved);

        Decision result = decisionService.createDecision(
                "Best IDE?", "Vote on IDEs", null, "SINGLE_CHOICE", "PUBLIC",
                LocalDateTime.now().plusDays(7));

        assertNotNull(result);
        assertEquals(1L, result.getDecisionId());
        verify(communityRepository, never()).findById(any());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 2 — Success with community
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("createDecision() links Community when a valid communityId is supplied")
    void createDecision_withValidCommunityId_linksCommunity() {

        authenticateAs(EMAIL);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(creator));
        when(communityRepository.findById(5L)).thenReturn(Optional.of(community));
        when(decisionRepository.save(any(Decision.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Decision result = decisionService.createDecision(
                "Best IDE?", "Vote on IDEs", 5L, "SINGLE_CHOICE", "PUBLIC",
                LocalDateTime.now().plusDays(7));

        assertEquals(community, result.getCommunity());
        verify(communityRepository).findById(5L);
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 3 — Field mapping
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("createDecision() correctly maps all supplied fields to the Decision entity")
    void createDecision_fieldsMappedCorrectly() {

        authenticateAs(EMAIL);

        LocalDateTime endsAt = LocalDateTime.of(2025, 12, 31, 23, 59);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(creator));
        when(decisionRepository.save(any(Decision.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        decisionService.createDecision(
                "Best IDE?", "Detailed description", null, "RATING", "PRIVATE", endsAt);

        ArgumentCaptor<Decision> captor = ArgumentCaptor.forClass(Decision.class);
        verify(decisionRepository).save(captor.capture());

        Decision captured = captor.getValue();
        assertEquals("Best IDE?",            captured.getTitle());
        assertEquals("Detailed description", captured.getDescription());
        assertEquals(creator,                captured.getCreatedBy());
        assertEquals("RATING",               captured.getPollType());
        assertEquals("PRIVATE",              captured.getVisibility());
        assertEquals(endsAt,                 captured.getEndsAt());
        assertNotNull(captured.getCreatedAt());
        assertNull(captured.getCommunity());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 4 — User not found
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("createDecision() throws IllegalStateException when authenticated user absent in DB")
    void createDecision_userNotFound_throwsIllegalStateException() {

        authenticateAs(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> decisionService.createDecision(
                        "Title", "Desc", null, "SINGLE_CHOICE", "PUBLIC", null));

        assertEquals("User not found", ex.getMessage());
        verify(decisionRepository, never()).save(any());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 5 — Community not found
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("createDecision() throws IllegalArgumentException when communityId does not exist")
    void createDecision_communityNotFound_throwsIllegalArgumentException() {

        authenticateAs(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(creator));
        when(communityRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> decisionService.createDecision(
                        "Title", "Desc", 99L, "SINGLE_CHOICE", "PUBLIC", null));

        assertEquals("Community not found", ex.getMessage());
        verify(decisionRepository, never()).save(any());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 6 — updateDecision() owner success
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("updateDecision() allows the owner to update their own decision")
    void updateDecision_owner_succeedsAndSaves() {

        authenticateAs(EMAIL); // authenticated as the creator (EMAIL = alice@...)

        Decision existing = new Decision();
        existing.setDecisionId(1L);
        existing.setTitle("Old title");
        existing.setStatus("OPEN");
        existing.setCreatedBy(creator); // creator.email == EMAIL

        when(decisionRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(decisionRepository.save(any(Decision.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Decision result = decisionService.updateDecision(
                1L, "New title", null, "CLOSED", null, null);

        assertEquals("New title", result.getTitle());
        assertEquals("CLOSED",    result.getStatus());
        verify(decisionRepository).save(existing);
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 7 — updateDecision() non-owner blocked (D1 fix)
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("updateDecision() throws ResponseStatusException FORBIDDEN when non-owner attempts update")
    void updateDecision_nonOwner_throwsForbidden() {

        // Authenticate as Bob — different user
        authenticateAs("bob@decisionhub.com");

        Decision existing = new Decision();
        existing.setDecisionId(1L);
        existing.setTitle("Alice's decision");
        existing.setCreatedBy(creator); // creator.email == "alice@decisionhub.com"

        when(decisionRepository.findById(1L)).thenReturn(Optional.of(existing));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> decisionService.updateDecision(
                        1L, "Hijacked title", null, null, null, null));

        assertEquals(org.springframework.http.HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(decisionRepository, never()).save(any());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 8 — updateDecision() decision not found
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("updateDecision() throws IllegalArgumentException when decision does not exist")
    void updateDecision_decisionNotFound_throwsIllegalArgumentException() {

        authenticateAs(EMAIL);
        when(decisionRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> decisionService.updateDecision(
                        999L, "Title", null, null, null, null));

        assertEquals("Decision not found", ex.getMessage());
        verify(decisionRepository, never()).save(any());
    }
}
