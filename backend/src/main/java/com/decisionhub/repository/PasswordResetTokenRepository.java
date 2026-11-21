package com.decisionhub.repository;

import com.decisionhub.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, Long> {
}