package com.hospitalqueue.queue_optimizer.repository;

import com.hospitalqueue.queue_optimizer.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    List<Token> findByDoctorIdOrderByTokenNumber(Long doctorId);
    Optional<Token> findByDoctorIdAndTokenNumber(Long doctorId, int tokenNumber);
}