package com.decisionhub.controller;

import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@RestController
@RequestMapping("/api")
public class TestController {
    @GetMapping("/users/me")
    public String me(Principal p) { return "Hello " + p.getName(); }

    @GetMapping("/admin/ping")
    public String admin() { return "admin ok"; }
}