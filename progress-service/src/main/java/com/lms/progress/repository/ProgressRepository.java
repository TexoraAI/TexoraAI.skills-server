package com.lms.progress.repository;

import com.lms.progress.model.Progress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProgressRepository extends JpaRepository<Progress, Long> {

    Optional<Progress> findByStudentEmailAndCourseId(String email, Long courseId);

    void deleteByStudentEmail(String studentEmail);   // ✅ WORKS NOW
}