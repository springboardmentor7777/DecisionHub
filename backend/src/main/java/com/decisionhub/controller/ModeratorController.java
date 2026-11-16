package com.decisionhub.controller;

import com.decisionhub.dto.AuthDtos.MessageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** MODERATOR and ADMIN: enforced in SecurityConfig for /api/moderator/**. */
@RestController
@RequestMapping("/api/moderator")
public class ModeratorController {
    @GetMapping("/ping")
    public MessageResponse ping() { return new MessageResponse("moderator ok"); }
}