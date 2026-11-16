package com.decisionhub.service;

import com.decisionhub.entity.Role;
import com.decisionhub.entity.User;
import com.decisionhub.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for CustomUserDetailsService.
 *
 * UserRepository is mocked — no Spring context or database connection is needed.
 * Tests verify the exact behaviour of loadUserByUsername():
 *   1. Known user is loaded and mapped to UserDetails correctly.
 *   2. Unknown email causes UsernameNotFoundException with the exact message.
 *   3. Email is trimmed and lowercased before the repository is called.
 */
@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    // Test data constants
    private static final String TEST_EMAIL         = "test@decisionhub.com";
    private static final String TEST_PASSWORD_HASH = "encodedPasswordHash123";
    private static final String TEST_ROLE_NAME     = "USER";

    private User testUser;

    @BeforeEach
    void setUp() {
        // Build a Role entity using the actual setters from Role.java
        Role role = new Role();
        role.setRoleId(1L);
        role.setRoleName(TEST_ROLE_NAME);

        // Build a User entity using the actual setters from User.java
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setFullName("Test User");
        testUser.setEmail(TEST_EMAIL);
        testUser.setPasswordHash(TEST_PASSWORD_HASH);
        testUser.setRole(role);
        testUser.setCreatedAt(LocalDateTime.now());
    }

    // -----------------------------------------------------------------------
    // Scenario 1: Known user is loaded successfully
    //
    // When userRepository.findByEmail() returns a User, the service must
    // build a Spring Security UserDetails object with:
    //   - username  = user.getEmail()
    //   - password  = user.getPasswordHash()
    //   - authority = "ROLE_USER"
    //       (Spring Security's .roles("USER") automatically prepends "ROLE_")
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("loadUserByUsername() returns correctly populated UserDetails for a known email")
    void loadUserByUsername_knownEmail_returnsUserDetails() {
        when(userRepository.findByEmail(TEST_EMAIL))
                .thenReturn(Optional.of(testUser));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(TEST_EMAIL);

        assertNotNull(userDetails,
                "UserDetails must not be null for a known user");
        assertEquals(TEST_EMAIL, userDetails.getUsername(),
                "Username in UserDetails must equal the user's stored email");
        assertEquals(TEST_PASSWORD_HASH, userDetails.getPassword(),
                "Password in UserDetails must equal the user's stored password hash");
        assertTrue(
                userDetails.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_" + TEST_ROLE_NAME)),
                "UserDetails must contain the ROLE_USER authority");
    }

    // -----------------------------------------------------------------------
    // Scenario 2: Unknown email throws UsernameNotFoundException
    //
    // When userRepository.findByEmail() returns Optional.empty(), the service
    // must throw UsernameNotFoundException with the message "User not found".
    // This is the exact message hardcoded in CustomUserDetailsService.
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("loadUserByUsername() throws UsernameNotFoundException for an unknown email")
    void loadUserByUsername_unknownEmail_throwsUsernameNotFoundException() {
        String unknownEmail = "unknown@decisionhub.com";
        when(userRepository.findByEmail(unknownEmail))
                .thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(unknownEmail),
                "An unknown email must cause UsernameNotFoundException"
        );

        assertEquals("User not found", exception.getMessage(),
                "Exception message must match the hardcoded value in the service");
    }

    // -----------------------------------------------------------------------
    // Scenario 3: Email is normalised (trimmed + lowercased) before lookup
    //
    // The service calls email.trim().toLowerCase() before passing the value
    // to findByEmail(). This test verifies that:
    //   - The repository receives the normalised form, not the raw input.
    //   - The returned UserDetails still has the correct username.
    //
    // Raw input:        "  TEST@DECISIONHUB.COM  "
    // Expected lookup:  "test@decisionhub.com"
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("loadUserByUsername() normalises email (trim + lowercase) before repository lookup")
    void loadUserByUsername_unnormalisedEmail_normalisesBeforeLookup() {
        String rawInput        = "  TEST@DECISIONHUB.COM  ";
        String normalisedEmail = TEST_EMAIL; // "test@decisionhub.com"

        when(userRepository.findByEmail(normalisedEmail))
                .thenReturn(Optional.of(testUser));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(rawInput);

        // Confirm the repository was called with the normalised form, not the raw input
        verify(userRepository).findByEmail(normalisedEmail);

        // Confirm the returned UserDetails is correctly populated
        assertEquals(TEST_EMAIL, userDetails.getUsername(),
                "Username in UserDetails must equal the stored email");
    }
}
