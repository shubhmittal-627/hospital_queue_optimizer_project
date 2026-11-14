package com.hospitalqueue.queue_optimizer.config;

import com.hospitalqueue.queue_optimizer.entity.User;
import com.hospitalqueue.queue_optimizer.repository.UserRepository;
import com.hospitalqueue.queue_optimizer.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        List<User> oldRows = userRepository.findAll().stream()
                .filter(u -> adminUsername.equals(u.getUsername()))
                .toList();
        userRepository.deleteAll(oldRows);

        userService.registerAdminOrDoctor(adminUsername, adminPassword, "ADMIN");
        System.out.println(">>> ADMIN ACCOUNT READY: " + adminUsername);
    }
}