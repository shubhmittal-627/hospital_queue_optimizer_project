package com.hospitalqueue.queue_optimizer.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class GoogleAuthController {

    @GetMapping("/google/success")
    public ResponseEntity<?> handleGoogleLoginSuccess(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return ResponseEntity.badRequest().body("Authentication failed or principal is null");
        }

        // getAttributes().get(...) use karenge
        String email = (String) principal.getAttributes().get("email");
        String name = (String) principal.getAttributes().get("name");
        String picture = (String) principal.getAttributes().get("picture");

        return ResponseEntity.ok(Map.of(
                "message", "Google Login Successful",
                "email", email != null ? email : "",
                "name", name != null ? name : "",
                "picture", picture != null ? picture : ""
        ));
    }
}