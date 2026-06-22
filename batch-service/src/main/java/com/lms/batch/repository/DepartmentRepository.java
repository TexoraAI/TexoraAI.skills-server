package com.lms.batch.repository;

import com.lms.batch.entity.Department;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    List<Department> findByOrganizationId(String organizationId);
    
    List<Department> findByOrganizationIdIsNull();
    
    long countByOrganizationId(String organizationId);
}