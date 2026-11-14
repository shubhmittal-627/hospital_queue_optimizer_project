package com.hospitalqueue.queue_optimizer.repository;

import com.hospitalqueue.queue_optimizer.entity.Doctor;
import com.hospitalqueue.queue_optimizer.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    List<Token> findByDoctor(Doctor doctor);

    Optional<Token> findByDoctorAndTokenNumber(Doctor doctor, int tokenNumber);

    List<Token> findByDoctorId(Long doctorId);

    List<Token> findByPatientNameOrderByCreatedAtDesc(String patientName);

    // Task 2: Priority-based Queue ordering (EMERGENCY > URGENT > NORMAL, then by Token Number)
    @Query("SELECT t FROM Token t WHERE t.doctor.id = :doctorId AND t.status IN ('WAITING', 'IN_PROGRESS') " +
            "ORDER BY CASE t.priorityLevel WHEN 'EMERGENCY' THEN 1 WHEN 'URGENT' THEN 2 ELSE 3 END, t.tokenNumber ASC")
    List<Token> findPriorityQueueForDoctor(Long doctorId);
}