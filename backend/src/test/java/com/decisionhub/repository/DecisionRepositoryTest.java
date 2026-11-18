package com.decisionhub.repository;

import com.decisionhub.entity.Community;
import com.decisionhub.entity.Decision;
import com.decisionhub.entity.Role;
import com.decisionhub.entity.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Slice test for DecisionRepository using @DataJpaTest (H2 in-memory).
 *
 * Tests:
 *   1. save() persists a Decision without community and assigns a generated decisionId
 *   2. save() persists a Decision linked to a Community
 *   3. findById() returns the Decision when it exists
 *   4. findById() returns empty Optional when id does not exist
 *   5. save() with null title throws DataIntegrityViolationException
 */
@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@DisplayName("DecisionRepository slice tests")
class DecisionRepositoryTest {

    @Autowired private DecisionRepository  decisionRepository;
    @Autowired private CommunityRepository communityRepository;
    @Autowired private UserRepository      userRepository;
    @Autowired private RoleRepository      roleRepository;

    private User creator;

    @BeforeEach
    void setUp() {
        decisionRepository.deleteAll();
        communityRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role role = new Role();
        role.setRoleName("USER");
        role = roleRepository.save(role);

        creator = new User();
        creator.setFullName("Alice Smith");
        creator.setEmail("alice@decisionhub.com");
        creator.setPasswordHash("hash");
        creator.setRole(role);
        creator.setCreatedAt(LocalDateTime.now());
        creator = userRepository.save(creator);
    }

    private Decision buildDecision(String title, Community community) {
        Decision d = new Decision();
        d.setTitle(title);
        d.setPollType("SINGLE_CHOICE");
        d.setCreatedBy(creator);
        d.setCreatedAt(LocalDateTime.now());
        d.setCommunity(community);
        return d;
    }

    // ══════════════════════════════════════════════════════════════════════
    // Test 1 — Save without community
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("save() persists a Decision with no community and assigns a generated decisionId")
    void save_noCommunity_assignsId() {

        Decision saved = decisionRepository.save(buildDecision("Best IDE?", null));

        assertNotNull(saved.getDecisionId());
        assertEquals("Best IDE?",     saved.getTitle());
        assertEquals("SINGLE_CHOICE", saved.getPollType());
        assertNull(saved.getCommunity());
        assertEquals(creator.getUserId(), saved.getCreatedBy().getUserId());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Test 2 — Save with community
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("save() persists a Decision linked to a Community")
    void save_withCommunity_linksCommunity() {

        Community community = new Community();
        community.setCommunityName("Tech Hub");
        community.setCreatedBy(creator);
        community.setCreatedAt(LocalDateTime.now());
        community = communityRepository.save(community);

        Decision saved = decisionRepository.save(buildDecision("Best IDE?", community));

        decisionRepository.flush();
        Decision loaded = decisionRepository.findById(saved.getDecisionId()).orElseThrow();

        assertNotNull(loaded.getCommunity());
        assertEquals(community.getCommunityId(), loaded.getCommunity().getCommunityId());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Test 3 — findById() found
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("findById() returns the Decision when it exists")
    void findById_existingId_returnsDecision() {

        Decision saved = decisionRepository.save(buildDecision("Dark or Light mode?", null));

        Optional<Decision> result = decisionRepository.findById(saved.getDecisionId());

        assertTrue(result.isPresent());
        assertEquals("Dark or Light mode?", result.get().getTitle());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Test 4 — findById() not found
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("findById() returns empty Optional for a non-existent decisionId")
    void findById_unknownId_returnsEmpty() {

        Optional<Decision> result = decisionRepository.findById(999L);

        assertFalse(result.isPresent());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Test 5 — Null title violates NOT NULL constraint
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("save() with null title throws DataIntegrityViolationException")
    void save_nullTitle_throwsDataIntegrityViolationException() {

        assertThrows(DataIntegrityViolationException.class, () -> {
            decisionRepository.save(buildDecision(null, null));
            decisionRepository.flush();
        });
    }
}
