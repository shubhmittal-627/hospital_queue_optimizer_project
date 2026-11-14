package com.hospitalqueue.queue_optimizer.config; // (Adjust this package name if yours is different)

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for REST APIs & local testing
                .authorizeHttpRequests(auth -> auth
                        // Allow public access to static resources, login page, and auth/oauth endpoints
                        .requestMatchers("/", "/index.html", "/css/**", "/js/**", "/api/auth/**", "/oauth2/**", "/login/**", "/ws-queue/**").permitAll()
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        // Redirect back to your frontend dashboard with the success flag after Google login
                        .defaultSuccessUrl("/index.html?oauthSuccess=true", true)
                );

        return http.build();
    }
}