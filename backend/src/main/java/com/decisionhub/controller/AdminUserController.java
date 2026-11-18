package com.decisionhub.controller;

import com.decisionhub.dto.ApiDtos.UserRoleResponse;
import com.decisionhub.dto.ApiDtos.UserRoleUpdateRequest;
import com.decisionhub.service.UserManagementService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {
    private final UserManagementService userManagement;

    public AdminUserController(UserManagementService userManagement) {
        this.userManagement = userManagement;
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public UserRoleResponse updateRole(@PathVariable long id,
                                       @Valid @RequestBody UserRoleUpdateRequest request) {
        return userManagement.updateRole(id, request.role());
    }
}
