package com.decisionhub.service;

import com.decisionhub.entity.Community;
import com.decisionhub.entity.Role;
import com.decisionhub.entity.User;
import com.decisionhub.repository.CommunityRepository;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CommunityService.
 *
 * All collaborators are mocked — no Spring context or database needed.
 * SecurityContextHolder is set/cleared per-test via a mock Authentication.
 *
 * Tests:
 *   createCommunity() — 4 scenarios
 *     1. Success: authenticated user, user found in DB → community saved and returned
 *     2. Fields set correctly on saved Community (ArgumentCaptor verification)
 *     3. Null authentication → IllegalStateException("User is not authenticated")
 *     4. User email not found in DB → IllegalStateException("User not found")
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CommunityService unit tests")
class CommunityServiceTest {

    @Mock private CommunityRepository communityRepository;
    @Mock private UserRepository       userRepository;

    @InjectMocks
    private CommunityService communityService;

    // ── Fixtures ──────────────────────────────────────────────────────────

    private static final String EMAIL       = "alice@decisionhub.com";
    private static final String COMM_NAME   = "Tech Enthusiasts";
    private static final String DESCRIPTION = "A community for tech lovers";

    private User creator;

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
    }

    /** Set a mock authenticated principal in the SecurityContext. */
    private void authenticateAs(String email) {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                email, null, Collections.emptyList());
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(auth);
        SecurityContextHolder.setContext(ctx);
    }

    @BeforeEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 1 — Happy path
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("createCommunity() saves and returns a Community when user is authenticated and found")
    void createCommunity_authenticatedUser_returnsCreatedCommunity() {

        authenticateAs(EMAIL);

        Community saved = new Community();
        saved.setCommunityId(10L);
        saved.setCommunityName(COMM_NAME);
        saved.setDescription(DESCRIPTION);
        saved.setCreatedBy(creator);
        saved.setCreatedAt(LocalDateTime.now());

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(creator));
        when(communityRepository.save(any(Community.class))).thenReturn(saved);

        Community result = communityService.createCommunity(COMM_NAME, DESCRIPTION);

        assertNotNull(result);
        assertEquals(10L, result.getCommunityId());
        assertEquals(COMM_NAME, result.getCommunityName());
        assertEquals(DESCRIPTION, result.getDescription());
        assertEquals(creator, result.getCreatedBy());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 2 — Field mapping verification via ArgumentCaptor
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("createCommunity() sets name, description, createdBy and createdAt on the persisted entity")
    void createCommunity_fieldsMappedCorrectly() {

        authenticateAs(EMAIL);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(creator));
        when(communityRepository.save(any(Community.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        communityService.createCommunity(COMM_NAME, DESCRIPTION);

        ArgumentCaptor<Community> captor = ArgumentCaptor.forClass(Community.class);
        verify(communityRepository).save(captor.capture());

        Community captured = captor.getValue();
        assertEquals(COMM_NAME,   captured.getCommunityName());
        assertEquals(DESCRIPTION, captured.getDescription());
        assertEquals(creator,     captured.getCreatedBy());
        assertNotNull(captured.getCreatedAt());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 3 — Null authentication
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("createCommunity() throws IllegalStateException when SecurityContext has no authentication")
    void createCommunity_nullAuthentication_throwsIllegalStateException() {

        // SecurityContext cleared in @BeforeEach — authentication is null.

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> communityService.createCommunity(COMM_NAME, DESCRIPTION)
        );

        assertEquals("User is not authenticated", ex.getMessage());
        verify(communityRepository, never()).save(any());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 4 — Authenticated but user absent in DB
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("createCommunity() throws IllegalStateException when authenticated email is absent in DB")
    void createCommunity_userNotFound_throwsIllegalStateException() {

        authenticateAs(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> communityService.createCommunity(COMM_NAME, DESCRIPTION)
        );

        assertEquals("User not found", ex.getMessage());
        verify(communityRepository, never()).save(any());
    }
}
