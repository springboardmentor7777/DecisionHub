package com.decisionhub.repository;

import com.decisionhub.entity.Decision;
import com.decisionhub.entity.Option;
import com.decisionhub.entity.Role;
import com.decisionhub.entity.User;
import com.decisionhub.entity.Vote;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Slice test for VoteRepository using @DataJpaTest (H2 in-memory).
 *
 * Tests:
 *   General persistence
 *     1. save() persists a vote with an option and returns a generated voteId
 *     2. save() persists a rating-only vote (optionId null)
 *     3. findAll() returns both votes cast on a decision
 *   BUG B6 confirmation
 *     4. VoteRepository has NO unique constraint on (user_id, decision_id):
 *        two votes by the same user on the same decision are both saved.
 *        This test documents current DB-level behaviour; a future migration
 *        adding a unique constraint will make this test fail, signalling the fix.
 */
@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@DisplayName("VoteRepository slice tests")
class VoteRepositoryTest {

    @Autowired private VoteRepository     voteRepository;
    @Autowired private DecisionRepository decisionRepository;
    @Autowired private OptionRepository   optionRepository;
    @Autowired private UserRepository     userRepository;
    @Autowired private RoleRepository     roleRepository;

    private User     voter;
    private Decision decision;
    private Option   option;

    @BeforeEach
    void setUp() {
        voteRepository.deleteAll();
        optionRepository.deleteAll();
        decisionRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role role = new Role();
        role.setRoleName("USER");
        role = roleRepository.save(role);

        voter = new User();
        voter.setFullName("Alice");
        voter.setEmail("alice@decisionhub.com");
        voter.setPasswordHash("hash");
        voter.setRole(role);
        voter.setCreatedAt(LocalDateTime.now());
        voter = userRepository.save(voter);

        decision = new Decision();
        decision.setTitle("Best IDE?");
        decision.setPollType("SINGLE_CHOICE");
        decision.setCreatedBy(voter);
        decision.setCreatedAt(LocalDateTime.now());
        decision = decisionRepository.save(decision);

        option = new Option();
        option.setDecision(decision);
        option.setOptionText("IntelliJ IDEA");
        option.setCreatedAt(LocalDateTime.now());
        option = optionRepository.save(option);
    }

    private Vote buildVote(User user, Decision dec, Option opt, Integer rating) {
        Vote v = new Vote();
        v.setUser(user);
        v.setDecision(dec);
        v.setOption(opt);
        v.setRating(rating);
        v.setVotedAt(LocalDateTime.now());
        return v;
    }

    // ══════════════════════════════════════════════════════════════════════
    // Test 1 — Save vote with option
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("save() persists a vote with an Option and assigns a generated voteId")
    void save_voteWithOption_assignsId() {

        Vote saved = voteRepository.save(buildVote(voter, decision, option, null));

        assertNotNull(saved.getVoteId());
        assertEquals(voter.getUserId(),       saved.getUser().getUserId());
        assertEquals(decision.getDecisionId(), saved.getDecision().getDecisionId());
        assertEquals(option.getOptionId(),     saved.getOption().getOptionId());
        assertNull(saved.getRating());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Test 2 — Save rating-only vote (no option)
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("save() persists a rating-only vote with no Option")
    void save_ratingVote_noOption() {

        Vote saved = voteRepository.save(buildVote(voter, decision, null, 4));

        assertNotNull(saved.getVoteId());
        assertNull(saved.getOption());
        assertEquals(4, saved.getRating());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Test 3 — findAll returns all votes on a decision
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("findAll() returns all votes cast across the repository")
    void findAll_returnsAllVotes() {

        voteRepository.save(buildVote(voter, decision, option, null));
        voteRepository.save(buildVote(voter, decision, null,   3));

        List<Vote> all = voteRepository.findAll();
        assertEquals(2, all.size());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Test 4 — existsByUser_UserIdAndDecision_DecisionId (B6 guard method)
    //
    // Verifies the new derived query method added for the B6 application-layer
    // duplicate-vote guard. Returns true when a vote exists for the same
    // user+decision, false otherwise.
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("existsByUser_UserIdAndDecision_DecisionId() returns true after first vote, false before")
    void existsByUserAndDecision_returnsTrueAfterVote() {

        // Before any vote
        assertFalse(voteRepository.existsByUser_UserIdAndDecision_DecisionId(
                voter.getUserId(), decision.getDecisionId()));

        // Cast first vote
        voteRepository.save(buildVote(voter, decision, option, null));
        voteRepository.flush();

        // Now returns true
        assertTrue(voteRepository.existsByUser_UserIdAndDecision_DecisionId(
                voter.getUserId(), decision.getDecisionId()));
    }

    // ══════════════════════════════════════════════════════════════════════
    // Test 5 — countByOption_OptionId
    //
    // Verifies the new derived query added to support OptionResponse.voteCount.
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("countByOption_OptionId() returns 0 when no votes, and correct count after votes")
    void countByOption_zeroThenCorrectCount() {

        // No votes yet
        assertEquals(0L, voteRepository.countByOption_OptionId(option.getOptionId()));

        // Create a second option to confirm counts are option-specific
        Option optionB = new Option();
        optionB.setDecision(decision);
        optionB.setOptionText("VS Code");
        optionB.setCreatedAt(LocalDateTime.now());
        optionB = optionRepository.save(optionB);

        // Cast two votes for option A
        voteRepository.save(buildVote(voter, decision, option, null));

        Role role2 = new Role();
        role2.setRoleName("ADMIN");
        role2 = roleRepository.save(role2);
        User voter2 = new User();
        voter2.setFullName("Bob Jones");
        voter2.setEmail("bob@decisionhub.com");
        voter2.setPasswordHash("hash2");
        voter2.setRole(role2);
        voter2.setCreatedAt(LocalDateTime.now());
        voter2 = userRepository.save(voter2);
        voteRepository.save(buildVote(voter2, decision, option, null));

        voteRepository.flush();

        assertEquals(2L, voteRepository.countByOption_OptionId(option.getOptionId()));
        assertEquals(0L, voteRepository.countByOption_OptionId(optionB.getOptionId()));
    }

    // ══════════════════════════════════════════════════════════════════════
    // Test 6 — countVotesGroupedByOptionIds (bulk GROUP BY query)
    //
    // Verifies the N+1-eliminating bulk query added to support
    // OptionService.getOptionsWithVoteCounts().
    //
    // Scenarios:
    //   a) No votes at all → empty result, all options default to 0
    //   b) Votes for one option only → correct count returned, absent option = 0
    //   c) Votes for multiple options → each count is accurate
    //   d) Option ID not in the queried set → not included in results
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("countVotesGroupedByOptionIds() returns correct counts: zero, one option, multiple options")
    void countVotesGroupedByOptionIds_allScenarios() {

        // Create a second option to test multi-option grouping
        Option optionB = new Option();
        optionB.setDecision(decision);
        optionB.setOptionText("VS Code");
        optionB.setCreatedAt(LocalDateTime.now());
        optionB = optionRepository.save(optionB);

        List<Long> bothOptionIds = List.of(option.getOptionId(), optionB.getOptionId());

        // ── Scenario a: no votes → empty result ───────────────────────────
        List<Object[]> empty = voteRepository.countVotesGroupedByOptionIds(bothOptionIds);
        assertTrue(empty.isEmpty(), "Expected no rows when there are no votes");

        // ── Scenario b: one vote for option A only ─────────────────────────
        voteRepository.save(buildVote(voter, decision, option, null));
        voteRepository.flush();

        Map<Long, Long> countsB = toMap(voteRepository.countVotesGroupedByOptionIds(bothOptionIds));
        assertEquals(1L,  countsB.getOrDefault(option.getOptionId(),  0L));
        assertEquals(0L,  countsB.getOrDefault(optionB.getOptionId(), 0L),
                "Option with no votes must be absent from result (caller defaults to 0)");

        // ── Scenario c: multiple options with different vote counts ─────────
        Role role2 = new Role();
        role2.setRoleName("ADMIN");
        role2 = roleRepository.save(role2);
        User voter2 = new User();
        voter2.setFullName("Bob Jones");
        voter2.setEmail("bob@decisionhub.com");
        voter2.setPasswordHash("hash2");
        voter2.setRole(role2);
        voter2.setCreatedAt(LocalDateTime.now());
        voter2 = userRepository.save(voter2);

        // voter2 votes for option B
        voteRepository.save(buildVote(voter2, decision, optionB, null));
        voteRepository.flush();

        Map<Long, Long> countsC = toMap(voteRepository.countVotesGroupedByOptionIds(bothOptionIds));
        assertEquals(1L, countsC.get(option.getOptionId()),  "Option A should have 1 vote");
        assertEquals(1L, countsC.get(optionB.getOptionId()), "Option B should have 1 vote");

        // ── Scenario d: querying only option A → option B not in set ───────
        Map<Long, Long> countsD = toMap(
                voteRepository.countVotesGroupedByOptionIds(List.of(option.getOptionId())));
        assertEquals(1L,  countsD.get(option.getOptionId()));
        assertFalse(countsD.containsKey(optionB.getOptionId()),
                "Option B must not appear when not in the queried ID set");
    }

    /** Helper: collapses Object[]{optionId, count} rows into a Map<Long, Long>. */
    private Map<Long, Long> toMap(List<Object[]> rows) {
        return rows.stream().collect(Collectors.toMap(
                r -> (Long) r[0],
                r -> (Long) r[1]));
    }

    // ══════════════════════════════════════════════════════════════════════
    // Test 7 — existsByUser_UserIdAndDecision_DecisionIdAndOption_OptionId
    //          (D3 MULTIPLE_CHOICE per-option duplicate guard)
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("[D3] existsByUser_UserIdAndDecision_DecisionIdAndOption_OptionId() returns false before vote and true after")
    void existsByUserAndDecisionAndOption_returnsTrueOnlyForMatchingOption() {

        // Create a second option on the same decision
        Option optionB = new Option();
        optionB.setDecision(decision);
        optionB.setOptionText("VS Code");
        optionB.setCreatedAt(LocalDateTime.now());
        optionB = optionRepository.save(optionB);

        // Before any vote — both options return false
        assertFalse(voteRepository.existsByUser_UserIdAndDecision_DecisionIdAndOption_OptionId(
                voter.getUserId(), decision.getDecisionId(), option.getOptionId()));
        assertFalse(voteRepository.existsByUser_UserIdAndDecision_DecisionIdAndOption_OptionId(
                voter.getUserId(), decision.getDecisionId(), optionB.getOptionId()));

        // Vote for option A
        voteRepository.save(buildVote(voter, decision, option, null));
        voteRepository.flush();

        // After voting for option A: option A returns true, option B returns false
        assertTrue(voteRepository.existsByUser_UserIdAndDecision_DecisionIdAndOption_OptionId(
                voter.getUserId(), decision.getDecisionId(), option.getOptionId()),
                "Option A should return true after vote");
        assertFalse(voteRepository.existsByUser_UserIdAndDecision_DecisionIdAndOption_OptionId(
                voter.getUserId(), decision.getDecisionId(), optionB.getOptionId()),
                "Option B should still return false — different option");
    }
}
