package com.decisionhub.controller;

import com.decisionhub.dto.AuthDtos.MessageResponse;
import com.decisionhub.dto.UserDtos.ChangeRoleRequest;
import com.decisionhub.dto.UserDtos.UserResponse;
import com.decisionhub.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** ADMIN only: enforced in SecurityConfig for /api/admin/**. */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final UserService userService;

    @GetMapping("/ping")
    public MessageResponse ping() { return new MessageResponse("admin ok"); }

    @GetMapping("/users")
    public List<UserResponse> users() { return userService.listUsers(); }

    @PutMapping("/users/{id}/role")
    public UserResponse changeRole(Authentication auth, @PathVariable Long id,
                                   @Valid @RequestBody ChangeRoleRequest req) {
        return userService.changeRole(auth.getName(), id, req.role());
    }
}