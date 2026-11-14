package com.hospitalqueue.queue_optimizer.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "doctors")
@Data
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    private String qualification;      // e.g. "MBBS, MD (Cardiology)"
    private int experienceYears;        // e.g. 12
    private String visitingDays;        // e.g. "Monday - Saturday"
    private String timeSlots;           // e.g. "Mon-Fri: 9 AM - 1 PM, 3 PM - 8 PM | Sat: 9 AM - 1 PM"

    private int avgConsultMinutes;
    private int lastTokenIssued = 0;
    private int currentTokenServing = 0;
}