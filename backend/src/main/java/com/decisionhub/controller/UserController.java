package com.decisionhub.controller;

import com.decisionhub.dto.UserDtos.UpdateProfileRequest;
import com.decisionhub.dto.UserDtos.UserResponse;
import com.decisionhub.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public UserResponse getMe(Authentication auth) {
        return userService.getProfile(auth.getName());
    }

    @PutMapping("/me")
    public UserResponse updateMe(Authentication auth, @Valid @RequestBody UpdateProfileRequest req) {
        return userService.updateProfile(auth.getName(), req);
    }
}