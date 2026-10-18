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
    private int avgConsultMinutes;
    private int lastTokenIssued = 0;
    private int currentTokenServing = 0;
}
