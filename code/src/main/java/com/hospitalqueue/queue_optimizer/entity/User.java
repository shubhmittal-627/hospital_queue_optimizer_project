package com.hospitalqueue.queue_optimizer.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    private String password;
    private String role; // PATIENT, DOCTOR, ADMIN

    // --- Extended Patient Details ---
    private String fullName;
    private String dateOfBirth;
    private String gender;
    private String bloodGroup;
    private String phoneNumber;
    private String email;
    private String address;
    private String emergencyContactName;
    private String emergencyPhone;
    private String medicalConditions;
    private String allergies;
}