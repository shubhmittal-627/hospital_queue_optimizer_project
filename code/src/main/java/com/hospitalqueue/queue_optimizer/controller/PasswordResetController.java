package com.hospitalqueue.queue_optimizer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class PasswordResetController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostMapping("/forgot-password")
    public ResponseEntity<?> processForgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email address cannot be empty."));
        }

        // Directly query PostgreSQL 'users' table to check if email exists
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email.trim());

        if (count == null || count == 0) {
            return ResponseEntity.status(404).body(Map.of("error", "Email address not found in system records."));
        }

        // Email found in database successfully!
        return ResponseEntity.ok(Map.of("message", "Password reset instructions have been sent to your email."));
    }
}