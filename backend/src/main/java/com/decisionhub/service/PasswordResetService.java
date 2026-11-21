
package com.decisionhub.service;

import com.decisionhub.entity.PasswordResetToken;
import com.decisionhub.entity.User;
import com.decisionhub.exception.BadRequestException;
import com.decisionhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    static final long TOKEN_TTL_MINUTES = 30;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private final ConcurrentMap<String, PasswordResetToken> tokens =
            new ConcurrentHashMap<>();

    @Transactional
    public void requestReset(String email) {
        userRepository.findByEmail(email.trim().toLowerCase())
                .filter(u -> u.getPassword() != null && u.isEnabled())
                .ifPresent(this::issueToken);
    }

    @Transactional
    public synchronized void resetPassword(String rawToken, String newPassword) {
        PasswordResetToken token = tokens.get(hash(rawToken));

        if (token == null || token.isUsed()
                || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Invalid or expired token");
        }

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        token.setUsed(true);
    }

    private synchronized void issueToken(User user) {
        tokens.entrySet().removeIf(entry ->
                Objects.equals(entry.getValue().getUser().getEmail(), user.getEmail()));

        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);

        String raw = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);

        String tokenHash = hash(raw);

        tokens.put(tokenHash, PasswordResetToken.builder()
                .tokenHash(tokenHash)
                .user(user)
                .expiresAt(LocalDateTime.now().plusMinutes(TOKEN_TTL_MINUTES))
                .createdAt(LocalDateTime.now())
                .used(false)
                .build());

        emailService.sendPasswordReset(user, raw);
    }

    static String hash(String raw) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
