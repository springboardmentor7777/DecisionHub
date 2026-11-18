package com.decisionhub.repository;

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
 * Slice test for UserRepository using @DataJpaTest.
 *
 * @DataJpaTest replaces the production PostgreSQL datasource with an in-memory
 * H2 database and sets ddl-auto=create-drop so Hibernate builds the schema
 * from entity mappings automatically. No Neon or local PostgreSQL is used.
 *
 * Tests (6):
 *   findByEmail()    — found, not found, case-sensitive
 *   existsByEmail()  — true, false
 *   save()           — duplicate email throws DataIntegrityViolationException
 */
@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Role savedRole;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role role = new Role();
        role.setRoleName("USER");
        savedRole = roleRepository.save(role);
    }

    private User buildUser(String email) {
        User user = new User();
        user.setFullName("Test User");
        user.setEmail(email);
        user.setPasswordHash("hashedPassword123");
        user.setRole(savedRole);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    @Test
    @DisplayName("findByEmail() returns the user when the email exists")
    void findByEmail_existingEmail_returnsUser() {
        userRepository.save(buildUser("alice@decisionhub.com"));
        Optional<User> result = userRepository.findByEmail("alice@decisionhub.com");
        assertTrue(result.isPresent());
        assertEquals("alice@decisionhub.com", result.get().getEmail());
        assertEquals("Test User",             result.get().getFullName());
        assertEquals("USER",                  result.get().getRole().getRoleName());
    }

    @Test
    @DisplayName("findByEmail() returns empty Optional when email is not found")
    void findByEmail_unknownEmail_returnsEmpty() {
        Optional<User> result = userRepository.findByEmail("nobody@decisionhub.com");
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("findByEmail() is case-sensitive — uppercase query does not match stored lowercase")
    void findByEmail_uppercaseQuery_doesNotMatch() {
        userRepository.save(buildUser("alice@decisionhub.com"));
        Optional<User> result = userRepository.findByEmail("ALICE@DECISIONHUB.COM");
        assertFalse(result.isPresent(),
                "Database query must be case-sensitive; normalisation is the application's responsibility");
    }

    @Test
    @DisplayName("existsByEmail() returns true when the email is registered")
    void existsByEmail_existingEmail_returnsTrue() {
        userRepository.save(buildUser("alice@decisionhub.com"));
        assertTrue(userRepository.existsByEmail("alice@decisionhub.com"));
    }

    @Test
    @DisplayName("existsByEmail() returns false when the email is not registered")
    void existsByEmail_unknownEmail_returnsFalse() {
        assertFalse(userRepository.existsByEmail("nobody@decisionhub.com"));
    }

    @Test
    @DisplayName("Saving a user with a duplicate email throws DataIntegrityViolationException")
    void save_duplicateEmail_throwsDataIntegrityViolationException() {
        userRepository.save(buildUser("alice@decisionhub.com"));
        userRepository.flush();

        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.save(buildUser("alice@decisionhub.com"));
            userRepository.flush();
        });
    }
}
