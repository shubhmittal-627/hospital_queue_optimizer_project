package com.hospitalqueue.queue_optimizer.repository;

import com.hospitalqueue.queue_optimizer.entity.Doctor;
import com.hospitalqueue.queue_optimizer.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    // Derived query to fetch all tokens for a specific doctor
    List<Token> findByDoctor(Doctor doctor);

    // Derived query to find a specific token by doctor and token number
    Optional<Token> findByDoctorAndTokenNumber(Doctor doctor, int tokenNumber);

    // Alternative finder using doctor ID directly (useful for cache/service layer lookups)
    List<Token> findByDoctorId(Long doctorId);
}