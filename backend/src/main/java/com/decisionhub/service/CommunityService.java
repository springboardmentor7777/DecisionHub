
package com.decisionhub.service;

import com.decisionhub.entity.Community;
import com.decisionhub.entity.User;
import com.decisionhub.repository.CommunityRepository;
import com.decisionhub.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;

    public CommunityService(
            CommunityRepository communityRepository,
            UserRepository userRepository) {
        this.communityRepository = communityRepository;
        this.userRepository = userRepository;
    }

    public Community createCommunity(String name, String description) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User is not authenticated");
        }

        User creator = userRepository.findByEmail(
                authentication.getName()
        ).orElseThrow(() -> new IllegalStateException(
                "User not found"));

        Community community = new Community();
        community.setCommunityName(name);
        community.setDescription(description);
        community.setCreatedBy(creator);
        community.setCreatedAt(LocalDateTime.now());

        return communityRepository.save(community);
    }

    // B10 — Pageable overload for scalable list retrieval
    public Page<Community> getAllCommunities(Pageable pageable) {
        return communityRepository.findAll(pageable);
    }

    public List<Community> getAllCommunities() {
        return communityRepository.findAll();
    }

    public Community getCommunityById(Long id) {
        return communityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Community not found"));
    }
}
