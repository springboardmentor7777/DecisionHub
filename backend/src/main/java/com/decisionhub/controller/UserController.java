
package com.decisionhub.controller;

import com.decisionhub.dto.UpdateUserRequest;
import com.decisionhub.dto.UserResponse;
import com.decisionhub.service.UserService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET /api/users/{id}
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // PUT /api/users/{id} — only the authenticated user may update their own profile
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        // Retrieve the authenticated user's profile to check ownership
        UserResponse current = userService.getUserById(id);
        if (!current.getEmail().equals(userDetails.getUsername())) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(userService.updateUser(id, request.getFullName()));
    }
}
