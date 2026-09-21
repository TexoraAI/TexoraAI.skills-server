package com.lms.chat.repository;

import com.lms.chat.entity.NotebookUsageTracking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotebookUsageTrackingRepository extends JpaRepository<NotebookUsageTracking, Long> {

    Optional<NotebookUsageTracking> findByStudentEmailAndPeriod(String studentEmail, String period);
}