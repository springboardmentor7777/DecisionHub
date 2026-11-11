package com.decisionhub.service;

import com.decisionhub.dto.ApiDtos.ProfileResponse;
import com.decisionhub.dto.ApiDtos.ProfileUpdateRequest;
import com.decisionhub.dto.ApiDtos.PasswordChangeRequest;
import com.decisionhub.entity.AppUser;
import com.decisionhub.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public ProfileService(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public ProfileResponse get(String email) {
        return response(findUser(email));
    }

    @Transactional
    public ProfileResponse update(String email, ProfileUpdateRequest request) {
        AppUser user = findUser(email);
        user.setDisplayName(request.displayName().trim());
        user.setBio(request.bio());
        user.setInterests(request.interests());
        return response(user);
    }

    @Transactional
    public void changePassword(String email, PasswordChangeRequest request) {
        AppUser user = findUser(email);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
    }

    private AppUser findUser(String email) {
        return users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private ProfileResponse response(AppUser user) {
        return new ProfileResponse(user.getId(), user.getEmail(), user.getDisplayName(), user.getRole(),
                user.getBio(), user.getInterests(), user.getCreatedAt());
    }
}
