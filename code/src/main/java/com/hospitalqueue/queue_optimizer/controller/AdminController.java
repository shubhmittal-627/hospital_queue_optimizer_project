package com.hospitalqueue.queue_optimizer.controller;

import com.hospitalqueue.queue_optimizer.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @PostMapping("/doctor-accounts")
    public String createDoctorAccount(@RequestParam String username, @RequestParam String password) {
        userService.registerAdminOrDoctor(username, password, "DOCTOR");
        return "Doctor account created for " + username;
    }
}