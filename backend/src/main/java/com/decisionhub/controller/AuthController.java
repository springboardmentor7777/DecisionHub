package com.decisionhub.controller;

import com.decisionhub.dto.AuthDtos.*;
import com.decisionhub.dto.UserDtos.UserResponse;
import com.decisionhub.service.AuthService;
import com.decisionhub.service.PasswordResetService;
import com.decisionhub.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final UserService userService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    /** The frontend calls this after a page refresh to restore the user from the stored token. */
    @GetMapping("/me")
    public UserResponse me(Authentication auth) {
        return userService.getProfile(auth.getName());
    }

    @PostMapping("/forgot-password")
    public MessageResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        passwordResetService.requestReset(req.email());
        return new MessageResponse("If that email is registered, a reset link has been sent");
    }

    @PostMapping("/reset-password")
    public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        passwordResetService.resetPassword(req.token(), req.newPassword());
        return new MessageResponse("Password updated successfully");
    }
}