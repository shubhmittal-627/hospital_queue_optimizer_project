package com.hospitalqueue.queue_optimizer.repository;

import com.hospitalqueue.queue_optimizer.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
}