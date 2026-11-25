package com.decisionhub.service;

import com.decisionhub.dto.ApiDtos.AdminUserResponse;
import com.decisionhub.dto.ApiDtos.UserRoleResponse;
import com.decisionhub.entity.AppUser;
import com.decisionhub.entity.Role;
import com.decisionhub.entity.UserNotification;
import com.decisionhub.repository.UserNotificationRepository;
import com.decisionhub.repository.UserRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserManagementService {
    private final UserRepository users;
    private final AuditService audit;
    private final UserNotificationRepository notifications;

    public UserManagementService(UserRepository users, AuditService audit,
                                 UserNotificationRepository notifications) {
        this.users = users;
        this.audit = audit;
        this.notifications = notifications;
    }

    /** Lists accounts, optionally filtered by role and by a name/email search term. */
    @Transactional(readOnly = true)
    public List<AdminUserResponse> list(Role role, String query) {
        String needle = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        return users.findAll(Sort.by("id")).stream()
                .filter(user -> role == null || user.getRole() == role)
                .filter(user -> needle.isEmpty()
                        || user.getEmail().toLowerCase(Locale.ROOT).contains(needle)
                        || user.getDisplayName().toLowerCase(Locale.ROOT).contains(needle))
                .map(user -> new AdminUserResponse(user.getId(), user.getEmail(), user.getDisplayName(),
                        user.getRole(), user.getCreatedAt()))
                .toList();
    }

    @Transactional
    public UserRoleResponse updateRole(String adminEmail, long userId, Role newRole) {
        AppUser admin = users.findByEmailIgnoreCase(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        AppUser target = users.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (target.getRole() == newRole) {
            return new UserRoleResponse(target.getId(), target.getEmail(), target.getRole());
        }
        if (target.getId().equals(admin.getId())) {
            throw new ConflictException("Administrators cannot change their own role");
        }
        // Safety net: normally unreachable (the caller is a second admin), but it guarantees
        // the platform can never be left without an administrator.
        if (target.getRole() == Role.ADMIN && newRole != Role.ADMIN && users.countByRole(Role.ADMIN) <= 1) {
            throw new ConflictException("At least one administrator must remain");
        }

        Role previous = target.getRole();
        target.setRole(newRole);
        audit.record(adminEmail, "ROLE_CHANGED", "USER", target.getId(),
                target.getEmail() + ": " + previous + " -> " + newRole);
        notifications.save(new UserNotification(target, "Your role was changed to " + newRole, null));
        return new UserRoleResponse(target.getId(), target.getEmail(), target.getRole());
    }
}
