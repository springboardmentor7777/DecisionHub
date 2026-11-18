package com.decisionhub.service;

import com.decisionhub.dto.ApiDtos.UserRoleResponse;
import com.decisionhub.entity.AppUser;
import com.decisionhub.entity.Role;
import com.decisionhub.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserManagementService {
    private final UserRepository users;

    public UserManagementService(UserRepository users) {
        this.users = users;
    }

    @Transactional
    public UserRoleResponse updateRole(long userId, Role role) {
        AppUser user = users.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setRole(role);
        return new UserRoleResponse(user.getId(), user.getEmail(), user.getRole());
    }
}
