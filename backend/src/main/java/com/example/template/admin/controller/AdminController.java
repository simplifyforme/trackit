package com.example.template.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Sample admin endpoints demonstrating ROLE_ADMIN-only access via @PreAuthorize.
 * Extend with real admin logic (user management, audit logs, etc.).
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")   // applied to every method in this controller
public class AdminController {

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, String>> dashboard() {
        return ResponseEntity.ok(Map.of(
                "message", "Admin dashboard",
                "hint", "Extend this with real admin data"
        ));
    }

    @GetMapping("/users")
    public ResponseEntity<Map<String, String>> users() {
        return ResponseEntity.ok(Map.of(
                "message", "Admin user list endpoint — wire up UserRepository + pagination here"
        ));
    }
}
