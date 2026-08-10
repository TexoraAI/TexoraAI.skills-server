package com.lms.course.repository;

import com.lms.course.model.SyllabusSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SyllabusSessionRepository extends JpaRepository<SyllabusSession, Long> {
}