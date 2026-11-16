package com.decisionhub.repository;

import com.decisionhub.entity.Comment;
import com.decisionhub.entity.Decision;
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
 * Slice test for CommentRepository using @DataJpaTest (H2 in-memory).
 *
 * Tests:
 *   findByDecision_DecisionIdOrderByCreatedAtAsc()
 *     1. Returns comments for the correct decision in ascending createdAt order
 *     2. Returns empty list when decision has no comments
 *     3. Does NOT return comments belonging to a different decision
 *   General persistence
 *     4. Saves top-level comment and assigns auto-generated commentId
 *     5. Saves reply comment with parentComment linked
 */
@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@DisplayName("CommentRepository slice tests")
class CommentRepositoryTest {

    @Autowired private CommentRepository  commentRepository;
    @Autowired private DecisionRepository decisionRepository;
    @Autowired private UserRepository     userRepository;
    @Autowired private RoleRepository     roleRepository;

    private User     user;
    private Decision decisionA;
    private Decision decisionB;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        decisionRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role role = new Role();
        role.setRoleName("USER");
        role = roleRepository.save(role);

        user = new User();
        user.setFullName("Alice Smith");
        user.setEmail("alice@decisionhub.com");
        user.setPasswordHash("hash");
        user.setRole(role);
        user.setCreatedAt(LocalDateTime.now());
        user = userRepository.save(user);

        decisionA = new Decision();
        decisionA.setTitle("Decision A");
        decisionA.setPollType("SINGLE_CHOICE");
        decisionA.setCreatedBy(user);
        decisionA.setCreatedAt(LocalDateTime.now());
        decisionA = decisionRepository.save(decisionA);

        decisionB = new Decision();
        decisionB.setTitle("Decision B");
        decisionB.setPollType("SINGLE_CHOICE");
        decisionB.setCreatedBy(user);
        decisionB.setCreatedAt(LocalDateTime.now());
        decisionB = decisionRepository.save(decisionB);
    }

    private Comment buildComment(Decision decision, String text, LocalDateTime createdAt) {
        Comment c = new Comment();
        c.setDecision(decision);
        c.setUser(user);
        c.setCommentText(text);
        c.setCreatedAt(createdAt);
        return c;
    }

    // ══════════════════════════════════════════════════════════════════════
    // Test 1 — Ascending order by createdAt
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("findByDecision_DecisionIdOrderByCreatedAtAsc returns comments in ascending createdAt order")
    void findByDecisionId_returnsAscendingOrder() {

        LocalDateTime t1 = LocalDateTime.of(2025, 1, 1, 9, 0);
        LocalDateTime t2 = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime t3 = LocalDateTime.of(2025, 1, 1, 11, 0);

        commentRepository.save(buildComment(decisionA, "Third",  t3));
        commentRepository.save(buildComment(decisionA, "First",  t1));
        commentRepository.save(buildComment(decisionA, "Second", t2));

        List<Comment> result = commentRepository
                .findByDecision_DecisionIdOrderByCreatedAtAsc(decisionA.getDecisionId());

        assertEquals(3, result.size());
        assertEquals("First",  result.get(0).getCommentText());
        assertEquals("Second", result.get(1).getCommentText());
        assertEquals("Third",  result.get(2).getCommentText());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Test 2 — Empty result for decision with no comments
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("findByDecision_DecisionIdOrderByCreatedAtAsc returns empty list when no comments exist")
    void findByDecisionId_noComments_returnsEmpty() {

        List<Comment> result = commentRepository
                .findByDecision_DecisionIdOrderByCreatedAtAsc(decisionA.getDecisionId());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Test 3 — Isolation: comments from other decisions not included
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("findByDecision_DecisionIdOrderByCreatedAtAsc does not return comments for other decisions")
    void findByDecisionId_isolatesDecision() {

        commentRepository.save(buildComment(decisionA, "For A", LocalDateTime.now()));
        commentRepository.save(buildComment(decisionB, "For B", LocalDateTime.now()));

        List<Comment> resultA = commentRepository
                .findByDecision_DecisionIdOrderByCreatedAtAsc(decisionA.getDecisionId());
        List<Comment> resultB = commentRepository
                .findByDecision_DecisionIdOrderByCreatedAtAsc(decisionB.getDecisionId());

        assertEquals(1, resultA.size());
        assertEquals("For A", resultA.get(0).getCommentText());

        assertEquals(1, resultB.size());
        assertEquals("For B", resultB.get(0).getCommentText());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Test 4 — Save top-level comment
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("save() persists a top-level comment and assigns a generated commentId")
    void save_topLevelComment_assignsId() {

        Comment comment = buildComment(decisionA, "Great decision!", LocalDateTime.now());
        Comment saved = commentRepository.save(comment);

        assertNotNull(saved.getCommentId());
        assertEquals("Great decision!", saved.getCommentText());
        assertNull(saved.getParentComment());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Test 5 — Save reply with parent linked
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("save() persists a reply comment with parentComment correctly linked")
    void save_replyComment_linksParent() {

        Comment parent = commentRepository.save(
                buildComment(decisionA, "Original comment", LocalDateTime.now()));

        Comment reply = buildComment(decisionA, "Reply to original", LocalDateTime.now());
        reply.setParentComment(parent);
        Comment savedReply = commentRepository.save(reply);

        commentRepository.flush();

        Comment loaded = commentRepository.findById(savedReply.getCommentId()).orElseThrow();
        assertNotNull(loaded.getParentComment());
        assertEquals(parent.getCommentId(), loaded.getParentComment().getCommentId());
    }
}
