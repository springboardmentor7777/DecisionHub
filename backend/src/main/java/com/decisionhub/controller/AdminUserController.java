package com.decisionhub.controller;

import com.decisionhub.dto.ApiDtos.AdminUserResponse;
import com.decisionhub.dto.ApiDtos.AuditLogResponse;
import com.decisionhub.dto.ApiDtos.UserRoleResponse;
import com.decisionhub.dto.ApiDtos.UserRoleUpdateRequest;
import com.decisionhub.entity.Role;
import com.decisionhub.service.AuditService;
import com.decisionhub.service.UserManagementService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Locale;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminUserController {
    private final UserManagementService userManagement;
    private final AuditService audit;

    public AdminUserController(UserManagementService userManagement, AuditService audit) {
        this.userManagement = userManagement;
        this.audit = audit;
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminUserResponse> listUsers(@RequestParam(required = false) String role,
                                             @RequestParam(required = false) String q) {
        Role parsedRole = null;
        if (role != null && !role.isBlank()) {
            try {
                parsedRole = Role.valueOf(role.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException("role must be one of USER, MODERATOR, ADMIN");
            }
        }
        return userManagement.list(parsedRole, q);
    }

    @PatchMapping("/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public UserRoleResponse updateRole(@PathVariable long id,
                                       @Valid @RequestBody UserRoleUpdateRequest request,
                                       @AuthenticationPrincipal UserDetails admin) {
        return userManagement.updateRole(admin.getUsername(), id, request.role());
    }

    @GetMapping("/audit-logs")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AuditLogResponse> auditLogs() {
        return audit.recent();
    }
}
