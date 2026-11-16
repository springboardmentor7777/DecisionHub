package com.decisionhub.repository;

import com.decisionhub.entity.Community;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Slice test for CommunityRepository using @DataJpaTest (H2 in-memory).
 *
 * Tests:
 *   1. save() persists a Community and assigns a generated communityId
 *   2. findById() returns the Community when it exists
 *   3. findById() returns empty Optional for an unknown id
 *   4. findAll() returns all saved communities
 *   5. save() with null communityName throws DataIntegrityViolationException
 */
@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@DisplayName("CommunityRepository slice tests")
class CommunityRepositoryTest {

    @Autowired private CommunityRepository communityRepository;
    @Autowired private UserRepository      userRepository;
    @Autowired private RoleRepository      roleRepository;

    private User creator;

    @BeforeEach
    void setUp() {
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

    private Community buildCommunity(String name) {
        Community c = new Community();
        c.setCommunityName(name);
        c.setDescription("A test community");
        c.setCreatedBy(creator);
        c.setCreatedAt(LocalDateTime.now());
        return c;
    }

    // ══════════════════════════════════════════════════════════════════════
    // Test 1 — save() assigns communityId
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("save() persists a Community and assigns a generated communityId")
    void save_validCommunity_assignsId() {

        Community saved = communityRepository.save(buildCommunity("Tech Enthusiasts"));

        assertNotNull(saved.getCommunityId());
        assertEquals("Tech Enthusiasts",  saved.getCommunityName());
        assertEquals("A test community",  saved.getDescription());
        assertEquals(creator.getUserId(), saved.getCreatedBy().getUserId());
        assertNotNull(saved.getCreatedAt());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Test 2 — findById() found
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("findById() returns the Community when it exists")
    void findById_existingId_returnsCommunity() {

        Community saved = communityRepository.save(buildCommunity("Science Club"));

        Optional<Community> result = communityRepository.findById(saved.getCommunityId());

        assertTrue(result.isPresent());
        assertEquals("Science Club", result.get().getCommunityName());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Test 3 — findById() not found
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("findById() returns empty Optional for a non-existent communityId")
    void findById_unknownId_returnsEmpty() {

        Optional<Community> result = communityRepository.findById(999L);

        assertFalse(result.isPresent());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Test 4 — findAll() returns all communities
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("findAll() returns all persisted communities")
    void findAll_returnsAllCommunities() {

        communityRepository.save(buildCommunity("Community A"));
        communityRepository.save(buildCommunity("Community B"));
        communityRepository.save(buildCommunity("Community C"));

        List<Community> all = communityRepository.findAll();

        assertEquals(3, all.size());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Test 5 — Null name violates NOT NULL constraint
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("save() with null communityName throws DataIntegrityViolationException")
    void save_nullName_throwsDataIntegrityViolationException() {

        assertThrows(DataIntegrityViolationException.class, () -> {
            communityRepository.save(buildCommunity(null));
            communityRepository.flush();
        });
    }
}
