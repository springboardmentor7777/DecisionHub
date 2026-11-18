package com.decisionhub.repository;

import com.decisionhub.entity.Decision;
import com.decisionhub.entity.Option;
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
 * Slice test for OptionRepository using @DataJpaTest (H2 in-memory).
 *
 * Tests:
 *   1. save() persists an Option linked to a Decision and assigns a generated optionId
 *   2. findById() returns the Option when it exists
 *   3. findAll() returns all options across all decisions
 *   4. save() with null optionText throws DataIntegrityViolationException
 */
@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@DisplayName("OptionRepository slice tests")
class OptionRepositoryTest {

    @Autowired private OptionRepository   optionRepository;
    @Autowired private DecisionRepository decisionRepository;
    @Autowired private UserRepository     userRepository;
    @Autowired private RoleRepository     roleRepository;

    private Decision decision;

    @BeforeEach
    void setUp() {
        optionRepository.deleteAll();
        decisionRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role role = new Role();
        role.setRoleName("USER");
        role = roleRepository.save(role);

        User creator = new User();
        creator.setFullName("Alice Smith");
        creator.setEmail("alice@decisionhub.com");
        creator.setPasswordHash("hash");
        creator.setRole(role);
        creator.setCreatedAt(LocalDateTime.now());
        creator = userRepository.save(creator);

        decision = new Decision();
        decision.setTitle("Best IDE?");
        decision.setPollType("SINGLE_CHOICE");
        decision.setCreatedBy(creator);
        decision.setCreatedAt(LocalDateTime.now());
        decision = decisionRepository.save(decision);
    }

    private Option buildOption(String text) {
        Option o = new Option();
        o.setDecision(decision);
        o.setOptionText(text);
        o.setCreatedAt(LocalDateTime.now());
        return o;
    }

    // ══════════════════════════════════════════════════════════════════════
    // Test 1 — save() assigns optionId and links Decision
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("save() persists an Option linked to a Decision and assigns a generated optionId")
    void save_validOption_assignsIdAndLinksDecision() {

        Option saved = optionRepository.save(buildOption("IntelliJ IDEA"));

        assertNotNull(saved.getOptionId());
        assertEquals("IntelliJ IDEA",          saved.getOptionText());
        assertEquals(decision.getDecisionId(),  saved.getDecision().getDecisionId());
        assertNotNull(saved.getCreatedAt());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Test 2 — findById() found
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("findById() returns the Option when it exists")
    void findById_existingId_returnsOption() {

        Option saved = optionRepository.save(buildOption("VS Code"));

        Optional<Option> result = optionRepository.findById(saved.getOptionId());

        assertTrue(result.isPresent());
        assertEquals("VS Code", result.get().getOptionText());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Test 3 — findAll() returns all options
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("findAll() returns all persisted options across all decisions")
    void findAll_returnsAllOptions() {

        optionRepository.save(buildOption("IntelliJ IDEA"));
        optionRepository.save(buildOption("VS Code"));
        optionRepository.save(buildOption("Eclipse"));

        List<Option> all = optionRepository.findAll();

        assertEquals(3, all.size());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Test 4 — Null text violates NOT NULL constraint
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("save() with null optionText throws DataIntegrityViolationException")
    void save_nullOptionText_throwsDataIntegrityViolationException() {

        assertThrows(DataIntegrityViolationException.class, () -> {
            optionRepository.save(buildOption(null));
            optionRepository.flush();
        });
    }
}
