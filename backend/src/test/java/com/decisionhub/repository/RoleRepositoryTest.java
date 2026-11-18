package com.decisionhub.repository;

import com.decisionhub.entity.Role;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Slice test for RoleRepository using @DataJpaTest.
 *
 * @DataJpaTest uses an in-memory H2 database with ddl-auto=create-drop.
 * No Neon or local PostgreSQL is involved.
 *
 * Tests (4):
 *   findByRoleName() — found, not found, case-sensitive
 *   save()           — duplicate role name throws DataIntegrityViolationException
 */
@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        roleRepository.deleteAll();
    }

    @Test
    @DisplayName("findByRoleName() returns the role when it exists")
    void findByRoleName_existingRole_returnsRole() {
        Role role = new Role();
        role.setRoleName("USER");
        roleRepository.save(role);

        Optional<Role> result = roleRepository.findByRoleName("USER");

        assertTrue(result.isPresent());
        assertEquals("USER", result.get().getRoleName());
        assertNotNull(result.get().getRoleId(), "Auto-generated roleId must not be null after save");
    }

    @Test
    @DisplayName("findByRoleName() returns empty Optional when the role does not exist")
    void findByRoleName_unknownRole_returnsEmpty() {
        Optional<Role> result = roleRepository.findByRoleName("ADMIN");
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("findByRoleName() is case-sensitive — wrong case returns empty")
    void findByRoleName_wrongCase_returnsEmpty() {
        Role role = new Role();
        role.setRoleName("USER");
        roleRepository.save(role);

        Optional<Role> result = roleRepository.findByRoleName("user");

        assertFalse(result.isPresent(), "Role name lookup must be case-sensitive");
    }

    @Test
    @DisplayName("Saving two roles with the same name throws DataIntegrityViolationException")
    void save_duplicateRoleName_throwsDataIntegrityViolationException() {
        Role r1 = new Role();
        r1.setRoleName("USER");
        roleRepository.save(r1);
        roleRepository.flush();

        assertThrows(DataIntegrityViolationException.class, () -> {
            Role r2 = new Role();
            r2.setRoleName("USER");
            roleRepository.save(r2);
            roleRepository.flush();
        });
    }
}
