package com.hospitalqueue.queue_optimizer.repository;

import com.hospitalqueue.queue_optimizer.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
}
