package com.decisionhub.controller;

import com.decisionhub.dto.AuthDtos.*;
import com.decisionhub.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }
}

@RestController
@RequestMapping("/api")
public class TestController {
    @GetMapping("/users/me")
    public String me(java.security.Principal p) { return "Hello " + p.getName(); }

    @GetMapping("/admin/ping")
    public String admin() { return "admin ok"; }
}