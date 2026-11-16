package com.decisionhub.service;

import com.decisionhub.entity.Decision;
import com.decisionhub.entity.Option;
import com.decisionhub.entity.Role;
import com.decisionhub.entity.User;
import com.decisionhub.entity.Vote;
import com.decisionhub.repository.DecisionRepository;
import com.decisionhub.repository.OptionRepository;
import com.decisionhub.repository.UserRepository;
import com.decisionhub.repository.VoteRepository;

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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for VoteService.
 *
 * Tests:
 *   castVote() — 13 scenarios
 *     1.  Success with optionId (SINGLE_CHOICE) → vote saved with option linked
 *     2.  Success with rating only (RATING, optionId = null) → no option lookup
 *     3.  Field mapping verified via ArgumentCaptor
 *     4.  User not found → IllegalStateException
 *     5.  Decision not found → IllegalArgumentException
 *     6.  Option not found → IllegalArgumentException
 *     7.  SINGLE_CHOICE duplicate vote → IllegalStateException, save() never called
 *     8.  First SINGLE_CHOICE vote allowed when no prior vote exists
 *     9.  RATING duplicate vote → IllegalStateException [D3]
 *     10. MULTIPLE_CHOICE: second vote for DIFFERENT option → allowed [D3]
 *     11. MULTIPLE_CHOICE: vote for SAME option twice → IllegalStateException [D3]
 *     12. MULTIPLE_CHOICE: first vote always succeeds [D3]
 *     13. MULTIPLE_CHOICE with null optionId → allowed (no option-level guard) [D3]
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VoteService unit tests")
class VoteServiceTest {

    @Mock private VoteRepository     voteRepository;
    @Mock private UserRepository     userRepository;
    @Mock private DecisionRepository decisionRepository;
    @Mock private OptionRepository   optionRepository;

    @InjectMocks
    private VoteService voteService;

    // ── Fixtures ──────────────────────────────────────────────────────────

    private static final String EMAIL = "alice@decisionhub.com";

    private User     voter;
    private Decision singleChoiceDecision;
    private Decision multipleChoiceDecision;
    private Decision ratingDecision;
    private Option   optionA;
    private Option   optionB;

    @BeforeEach
    void setUp() {
        Role role = new Role();
        role.setRoleId(1L);
        role.setRoleName("USER");

        voter = new User();
        voter.setUserId(1L);
        voter.setEmail(EMAIL);
        voter.setRole(role);

        singleChoiceDecision = new Decision();
        singleChoiceDecision.setDecisionId(10L);
        singleChoiceDecision.setTitle("Best IDE?");
        singleChoiceDecision.setPollType("SINGLE_CHOICE");
        singleChoiceDecision.setCreatedBy(voter);

        multipleChoiceDecision = new Decision();
        multipleChoiceDecision.setDecisionId(20L);
        multipleChoiceDecision.setTitle("Pick features");
        multipleChoiceDecision.setPollType("MULTIPLE_CHOICE");
        multipleChoiceDecision.setCreatedBy(voter);

        ratingDecision = new Decision();
        ratingDecision.setDecisionId(30L);
        ratingDecision.setTitle("Rate this");
        ratingDecision.setPollType("RATING");
        ratingDecision.setCreatedBy(voter);

        optionA = new Option();
        optionA.setOptionId(100L);
        optionA.setOptionText("IntelliJ IDEA");
        optionA.setDecision(singleChoiceDecision);

        optionB = new Option();
        optionB.setOptionId(200L);
        optionB.setOptionText("VS Code");
        optionB.setDecision(multipleChoiceDecision);
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
    // Scenario 1 — Success with optionId (SINGLE_CHOICE)
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("castVote() saves vote with linked Option when optionId is provided (SINGLE_CHOICE)")
    void castVote_withOptionId_savesVoteWithOption() {

        authenticateAs(EMAIL);

        Vote saved = new Vote();
        saved.setVoteId(1L);
        saved.setUser(voter);
        saved.setDecision(singleChoiceDecision);
        saved.setOption(optionA);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(voter));
        when(decisionRepository.findById(10L)).thenReturn(Optional.of(singleChoiceDecision));
        when(voteRepository.existsByUser_UserIdAndDecision_DecisionId(1L, 10L)).thenReturn(false);
        when(optionRepository.findById(100L)).thenReturn(Optional.of(optionA));
        when(voteRepository.save(any(Vote.class))).thenReturn(saved);

        Vote result = voteService.castVote(10L, 100L, null);

        assertNotNull(result);
        assertEquals(1L, result.getVoteId());
        assertEquals(optionA, result.getOption());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 2 — Rating-only vote (no option)
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("castVote() saves vote with rating when optionId is null (RATING poll)")
    void castVote_ratingOnly_savesVoteWithoutOption() {

        authenticateAs(EMAIL);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(voter));
        when(decisionRepository.findById(30L)).thenReturn(Optional.of(ratingDecision));
        when(voteRepository.existsByUser_UserIdAndDecision_DecisionId(1L, 30L)).thenReturn(false);
        when(voteRepository.save(any(Vote.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Vote result = voteService.castVote(30L, null, 4);

        assertNull(result.getOption());
        assertEquals(4, result.getRating());
        verify(optionRepository, never()).findById(any());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 3 — Field mapping
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("castVote() correctly maps user, decision, option, rating and votedAt to Vote entity")
    void castVote_fieldsMappedCorrectly() {

        authenticateAs(EMAIL);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(voter));
        when(decisionRepository.findById(10L)).thenReturn(Optional.of(singleChoiceDecision));
        when(voteRepository.existsByUser_UserIdAndDecision_DecisionId(1L, 10L)).thenReturn(false);
        when(optionRepository.findById(100L)).thenReturn(Optional.of(optionA));
        when(voteRepository.save(any(Vote.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        voteService.castVote(10L, 100L, 5);

        ArgumentCaptor<Vote> captor = ArgumentCaptor.forClass(Vote.class);
        verify(voteRepository).save(captor.capture());

        Vote captured = captor.getValue();
        assertEquals(voter,                captured.getUser());
        assertEquals(singleChoiceDecision, captured.getDecision());
        assertEquals(optionA,              captured.getOption());
        assertEquals(5,                    captured.getRating());
        assertNotNull(captured.getVotedAt());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 4 — User not found
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("castVote() throws IllegalStateException when authenticated user absent in DB")
    void castVote_userNotFound_throwsIllegalStateException() {

        authenticateAs(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> voteService.castVote(10L, 100L, null));

        verify(voteRepository, never()).save(any());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 5 — Decision not found
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("castVote() throws IllegalArgumentException when decision is not found")
    void castVote_decisionNotFound_throwsIllegalArgumentException() {

        authenticateAs(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(voter));
        when(decisionRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> voteService.castVote(99L, 100L, null));

        assertEquals("Decision not found", ex.getMessage());
        verify(voteRepository, never()).save(any());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 6 — Option not found
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("castVote() throws IllegalArgumentException when optionId does not exist")
    void castVote_optionNotFound_throwsIllegalArgumentException() {

        authenticateAs(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(voter));
        when(decisionRepository.findById(10L)).thenReturn(Optional.of(singleChoiceDecision));
        when(voteRepository.existsByUser_UserIdAndDecision_DecisionId(1L, 10L)).thenReturn(false);
        when(optionRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> voteService.castVote(10L, 999L, null));

        assertEquals("Option not found", ex.getMessage());
        verify(voteRepository, never()).save(any());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 7 — SINGLE_CHOICE duplicate vote blocked
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("[D3] SINGLE_CHOICE: duplicate vote throws IllegalStateException")
    void castVote_singleChoice_duplicateVote_throwsIllegalStateException() {

        authenticateAs(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(voter));
        when(decisionRepository.findById(10L)).thenReturn(Optional.of(singleChoiceDecision));
        when(voteRepository.existsByUser_UserIdAndDecision_DecisionId(1L, 10L)).thenReturn(true);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> voteService.castVote(10L, 100L, null));

        assertEquals("User has already voted on this decision", ex.getMessage());
        verify(voteRepository, never()).save(any());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 8 — SINGLE_CHOICE first vote succeeds
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("castVote() allows first vote when no prior vote exists for user/decision (SINGLE_CHOICE)")
    void castVote_singleChoice_firstVote_succeeds() {

        authenticateAs(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(voter));
        when(decisionRepository.findById(10L)).thenReturn(Optional.of(singleChoiceDecision));
        when(voteRepository.existsByUser_UserIdAndDecision_DecisionId(1L, 10L)).thenReturn(false);
        when(optionRepository.findById(100L)).thenReturn(Optional.of(optionA));
        when(voteRepository.save(any(Vote.class))).thenAnswer(inv -> inv.getArgument(0));

        Vote result = voteService.castVote(10L, 100L, null);

        assertNotNull(result);
        verify(voteRepository).save(any(Vote.class));
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 9 — RATING duplicate vote blocked  [D3]
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("[D3] RATING: duplicate vote throws IllegalStateException")
    void castVote_rating_duplicateVote_throwsIllegalStateException() {

        authenticateAs(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(voter));
        when(decisionRepository.findById(30L)).thenReturn(Optional.of(ratingDecision));
        when(voteRepository.existsByUser_UserIdAndDecision_DecisionId(1L, 30L)).thenReturn(true);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> voteService.castVote(30L, null, 5));

        assertEquals("User has already voted on this decision", ex.getMessage());
        verify(voteRepository, never()).save(any());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 10 — MULTIPLE_CHOICE: different option → allowed  [D3]
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("[D3] MULTIPLE_CHOICE: voting for a different option is allowed")
    void castVote_multipleChoice_differentOption_allowed() {

        authenticateAs(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(voter));
        when(decisionRepository.findById(20L)).thenReturn(Optional.of(multipleChoiceDecision));
        // User already voted for optionA (100) — but is now voting for optionB (200)
        when(voteRepository.existsByUser_UserIdAndDecision_DecisionIdAndOption_OptionId(
                1L, 20L, 200L)).thenReturn(false);
        when(optionRepository.findById(200L)).thenReturn(Optional.of(optionB));
        when(voteRepository.save(any(Vote.class))).thenAnswer(inv -> inv.getArgument(0));

        Vote result = voteService.castVote(20L, 200L, null);

        assertNotNull(result);
        verify(voteRepository).save(any(Vote.class));
        // The broad existsByUser_UserIdAndDecision_DecisionId must NOT have been called
        verify(voteRepository, never()).existsByUser_UserIdAndDecision_DecisionId(anyLong(), anyLong());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 11 — MULTIPLE_CHOICE: same option twice → blocked  [D3]
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("[D3] MULTIPLE_CHOICE: voting for the same option twice throws IllegalStateException")
    void castVote_multipleChoice_sameOptionTwice_throwsIllegalStateException() {

        authenticateAs(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(voter));
        when(decisionRepository.findById(20L)).thenReturn(Optional.of(multipleChoiceDecision));
        // Simulate: user has already voted for optionB (200)
        when(voteRepository.existsByUser_UserIdAndDecision_DecisionIdAndOption_OptionId(
                1L, 20L, 200L)).thenReturn(true);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> voteService.castVote(20L, 200L, null));

        assertEquals("You have already selected this option", ex.getMessage());
        verify(voteRepository, never()).save(any());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 12 — MULTIPLE_CHOICE: first vote always succeeds  [D3]
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("[D3] MULTIPLE_CHOICE: first option vote succeeds")
    void castVote_multipleChoice_firstVote_succeeds() {

        authenticateAs(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(voter));
        when(decisionRepository.findById(20L)).thenReturn(Optional.of(multipleChoiceDecision));
        when(voteRepository.existsByUser_UserIdAndDecision_DecisionIdAndOption_OptionId(
                1L, 20L, 200L)).thenReturn(false);
        when(optionRepository.findById(200L)).thenReturn(Optional.of(optionB));
        when(voteRepository.save(any(Vote.class))).thenAnswer(inv -> inv.getArgument(0));

        Vote result = voteService.castVote(20L, 200L, null);

        assertNotNull(result);
        verify(voteRepository).save(any(Vote.class));
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 13 — MULTIPLE_CHOICE with null optionId → no option guard  [D3]
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("[D3] MULTIPLE_CHOICE with null optionId skips the option-level guard")
    void castVote_multipleChoice_nullOptionId_noGuardCalled() {

        authenticateAs(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(voter));
        when(decisionRepository.findById(20L)).thenReturn(Optional.of(multipleChoiceDecision));
        // optionId is null → option guard must NOT be called
        when(voteRepository.save(any(Vote.class))).thenAnswer(inv -> inv.getArgument(0));

        voteService.castVote(20L, null, null);

        verify(voteRepository, never()).existsByUser_UserIdAndDecision_DecisionIdAndOption_OptionId(
                anyLong(), anyLong(), anyLong());
        verify(voteRepository, never()).existsByUser_UserIdAndDecision_DecisionId(anyLong(), anyLong());
        verify(voteRepository).save(any(Vote.class));
    }
}
