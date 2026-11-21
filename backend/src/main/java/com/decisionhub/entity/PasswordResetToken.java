package com.decisionhub.entity;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken {

    private Long id;

    private String tokenHash;

    private User user;

    private LocalDateTime expiresAt;

    private boolean used;

    private LocalDateTime createdAt;
}