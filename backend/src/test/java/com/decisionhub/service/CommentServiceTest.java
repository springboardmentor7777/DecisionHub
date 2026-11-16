package com.decisionhub.service;

import com.decisionhub.entity.Comment;
import com.decisionhub.entity.Decision;
import com.decisionhub.entity.Role;
import com.decisionhub.entity.User;
import com.decisionhub.repository.CommentRepository;
import com.decisionhub.repository.DecisionRepository;
import com.decisionhub.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CommentService.
 *
 * Tests:
 *   addComment() — 7 scenarios
 *     1. Top-level comment (parentId = null) — success
 *     2. Reply to a parent comment — success, parent linked
 *     3. Field mapping verified (user, decision, text, createdAt)
 *     4. User not found → RuntimeException
 *     5. Decision not found → RuntimeException
 *     6. Parent comment not found → RuntimeException
 *     7. Parent comment belongs to a different decision → IllegalArgumentException
 *   getComments() — 2 scenarios
 *     8. Returns list ordered by createdAt ascending
 *     9. Returns empty list when no comments exist
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CommentService unit tests")
class CommentServiceTest {

    @Mock private CommentRepository  commentRepository;
    @Mock private DecisionRepository decisionRepository;
    @Mock private UserRepository     userRepository;

    @InjectMocks
    private CommentService commentService;

    // ── Fixtures ──────────────────────────────────────────────────────────

    private static final String EMAIL = "alice@decisionhub.com";

    private User     commenter;
    private Decision decision;

    @BeforeEach
    void setUp() {
        Role role = new Role();
        role.setRoleId(1L);
        role.setRoleName("USER");

        commenter = new User();
        commenter.setUserId(1L);
        commenter.setEmail(EMAIL);
        commenter.setRole(role);

        decision = new Decision();
        decision.setDecisionId(10L);
        decision.setTitle("Best IDE?");
        decision.setCreatedBy(commenter);
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
    // Scenario 1 — Top-level comment
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("addComment() saves top-level comment when parentId is null")
    void addComment_noParent_savesTopLevelComment() {

        authenticateAs(EMAIL);

        Comment saved = new Comment();
        saved.setCommentId(1L);
        saved.setUser(commenter);
        saved.setDecision(decision);
        saved.setCommentText("Great poll!");

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(commenter));
        when(decisionRepository.findById(10L)).thenReturn(Optional.of(decision));
        when(commentRepository.save(any(Comment.class))).thenReturn(saved);

        Comment result = commentService.addComment(10L, null, "Great poll!");

        assertNotNull(result);
        assertEquals(1L, result.getCommentId());
        assertNull(result.getParentComment());
        verify(commentRepository, never()).findById(any());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 2 — Reply to parent comment
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("addComment() links parent comment when parentId is provided")
    void addComment_withParent_linksParentComment() {

        authenticateAs(EMAIL);

        Comment parent = new Comment();
        parent.setCommentId(5L);
        parent.setDecision(decision);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(commenter));
        when(decisionRepository.findById(10L)).thenReturn(Optional.of(decision));
        when(commentRepository.findById(5L)).thenReturn(Optional.of(parent));
        when(commentRepository.save(any(Comment.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Comment result = commentService.addComment(10L, 5L, "Agreed!");

        assertEquals(parent, result.getParentComment());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 3 — Field mapping
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("addComment() correctly maps user, decision, text and createdAt")
    void addComment_fieldsMappedCorrectly() {

        authenticateAs(EMAIL);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(commenter));
        when(decisionRepository.findById(10L)).thenReturn(Optional.of(decision));
        when(commentRepository.save(any(Comment.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Comment result = commentService.addComment(10L, null, "Very insightful!");

        assertEquals(commenter,       result.getUser());
        assertEquals(decision,        result.getDecision());
        assertEquals("Very insightful!", result.getCommentText());
        assertNotNull(result.getCreatedAt());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 4 — User not found
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("addComment() throws RuntimeException when authenticated user absent in DB")
    void addComment_userNotFound_throwsRuntimeException() {

        authenticateAs(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> commentService.addComment(10L, null, "Hello"));

        assertEquals("User not found", ex.getMessage());
        verify(commentRepository, never()).save(any());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 5 — Decision not found
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("addComment() throws RuntimeException when decision is not found")
    void addComment_decisionNotFound_throwsRuntimeException() {

        authenticateAs(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(commenter));
        when(decisionRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> commentService.addComment(99L, null, "Hello"));

        assertEquals("Decision not found", ex.getMessage());
        verify(commentRepository, never()).save(any());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 6 — Parent comment not found
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("addComment() throws RuntimeException when parent comment is not found")
    void addComment_parentNotFound_throwsRuntimeException() {

        authenticateAs(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(commenter));
        when(decisionRepository.findById(10L)).thenReturn(Optional.of(decision));
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> commentService.addComment(10L, 999L, "Reply"));

        assertEquals("Parent comment not found", ex.getMessage());
        verify(commentRepository, never()).save(any());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 7 — Parent belongs to different decision
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("addComment() throws IllegalArgumentException when parent belongs to a different decision")
    void addComment_parentBelongsToDifferentDecision_throwsIllegalArgumentException() {

        authenticateAs(EMAIL);

        Decision otherDecision = new Decision();
        otherDecision.setDecisionId(99L);

        Comment parent = new Comment();
        parent.setCommentId(5L);
        parent.setDecision(otherDecision); // belongs to decision 99, not 10

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(commenter));
        when(decisionRepository.findById(10L)).thenReturn(Optional.of(decision));
        when(commentRepository.findById(5L)).thenReturn(Optional.of(parent));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> commentService.addComment(10L, 5L, "Reply"));

        assertEquals("Parent comment belongs to another decision", ex.getMessage());
        verify(commentRepository, never()).save(any());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 8 — getComments() returns ordered list
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getComments() returns comments ordered by createdAt ascending")
    void getComments_returnsOrderedList() {

        Comment c1 = new Comment();
        c1.setCommentId(1L);
        c1.setCreatedAt(LocalDateTime.of(2025, 1, 1, 9, 0));

        Comment c2 = new Comment();
        c2.setCommentId(2L);
        c2.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));

        when(commentRepository.findByDecision_DecisionIdOrderByCreatedAtAsc(10L))
                .thenReturn(List.of(c1, c2));

        List<Comment> result = commentService.getComments(10L);

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getCommentId());
        assertEquals(2L, result.get(1).getCommentId());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scenario 9 — getComments() returns empty list
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getComments() returns empty list when no comments exist for the decision")
    void getComments_noComments_returnsEmptyList() {

        when(commentRepository.findByDecision_DecisionIdOrderByCreatedAtAsc(10L))
                .thenReturn(Collections.emptyList());

        List<Comment> result = commentService.getComments(10L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
