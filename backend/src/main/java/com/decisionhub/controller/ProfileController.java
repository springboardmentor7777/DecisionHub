package com.decisionhub.controller;

import com.decisionhub.dto.ApiDtos.ProfileResponse;
import com.decisionhub.dto.ApiDtos.ProfileUpdateRequest;
import com.decisionhub.dto.ApiDtos.PasswordChangeRequest;
import com.decisionhub.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/me")
public class ProfileController {
    private final ProfileService profiles;

    public ProfileController(ProfileService profiles) {
        this.profiles = profiles;
    }

    @GetMapping
    public ProfileResponse get(@AuthenticationPrincipal UserDetails user) {
        return profiles.get(user.getUsername());
    }

    @PutMapping
    public ProfileResponse update(@AuthenticationPrincipal UserDetails user,
                                  @Valid @RequestBody ProfileUpdateRequest request) {
        return profiles.update(user.getUsername(), request);
    }

    @PutMapping("/password")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void changePassword(@AuthenticationPrincipal UserDetails user,
                               @Valid @RequestBody PasswordChangeRequest request) {
        profiles.changePassword(user.getUsername(), request);
    }
}
