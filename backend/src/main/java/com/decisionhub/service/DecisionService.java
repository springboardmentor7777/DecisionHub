
package com.decisionhub.service;

import com.decisionhub.entity.*;
import com.decisionhub.repository.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DecisionService {

    private final DecisionRepository decisionRepository;
    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;

    public DecisionService(
            DecisionRepository decisionRepository,
            UserRepository userRepository,
            CommunityRepository communityRepository) {
        this.decisionRepository = decisionRepository;
        this.userRepository = userRepository;
        this.communityRepository = communityRepository;
    }

    public Decision createDecision(
            String title,
            String description,
            Long communityId,
            String pollType,
            String visibility,
            LocalDateTime endsAt) {

        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        User creator = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        Decision decision = new Decision();
        decision.setTitle(title);
        decision.setDescription(description);
        decision.setCreatedBy(creator);
        decision.setPollType(pollType != null ? pollType : "SINGLE_CHOICE");
        decision.setVisibility(visibility != null ? visibility : "PUBLIC");
        decision.setEndsAt(endsAt);
        decision.setCreatedAt(LocalDateTime.now());

        if (communityId != null) {
            Community community = communityRepository.findById(communityId)
                    .orElseThrow(() -> new IllegalArgumentException("Community not found"));
            decision.setCommunity(community);
        }

        return decisionRepository.save(decision);
    }

    // B10 — Pageable overload
    public Page<Decision> getAllDecisions(Pageable pageable) {
        return decisionRepository.findAll(pageable);
    }

    public List<Decision> getAllDecisions() {
        return decisionRepository.findAll();
    }

    public Decision getDecisionById(Long id) {
        return decisionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Decision not found"));
    }

    public Decision updateDecision(Long id, String title, String description,
                                   String status, String visibility,
                                   LocalDateTime endsAt) {
        Decision decision = decisionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Decision not found"));

        // D1 — Ownership check: only the creator may update this decision.
        String currentEmail = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        if (decision.getCreatedBy() == null
                || !decision.getCreatedBy().getEmail().equals(currentEmail)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "You are not the owner of this decision");
        }

        if (title != null && !title.isBlank()) {
            decision.setTitle(title);
        }
        if (description != null) {
            decision.setDescription(description);
        }
        if (status != null && !status.isBlank()) {
            decision.setStatus(status);
        }
        if (visibility != null && !visibility.isBlank()) {
            decision.setVisibility(visibility);
        }
        if (endsAt != null) {
            decision.setEndsAt(endsAt);
        }

        return decisionRepository.save(decision);
    }
}
