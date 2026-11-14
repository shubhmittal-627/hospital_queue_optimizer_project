package com.hospitalqueue.queue_optimizer.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "tokens")
@Data
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int tokenNumber;
    private String patientName;
    private String status = "WAITING";
    private LocalDateTime createdAt = LocalDateTime.now();

    private String visitDate;
    private String timeSlot;
    private String consultationNotes;

    // Task 2: Priority Triage (NORMAL, URGENT, EMERGENCY)
    private String priorityLevel = "NORMAL";

    // Admin Approval Status for Urgent/Emergency requests
    private String approvalStatus = "APPROVED"; // APPROVED, PENDING_APPROVAL, REJECTED

    // Task 3: Rating & Feedback (1-5 Stars)
    private Integer rating;
    private String feedback;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;
}