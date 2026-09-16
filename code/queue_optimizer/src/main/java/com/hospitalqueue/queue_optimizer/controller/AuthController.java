package com.hospitalqueue.queue_optimizer.controller;

import com.hospitalqueue.queue_optimizer.config.JwtUtil;
import com.hospitalqueue.queue_optimizer.entity.User;
import com.hospitalqueue.queue_optimizer.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public User register(@RequestParam String username, @RequestParam String password, @RequestParam String role) {
        return userService.registerUser(username, password, role);
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password) {
        User user = userService.login(username, password);
        return jwtUtil.generateToken(user.getUsername(), user.getRole());
    }
}