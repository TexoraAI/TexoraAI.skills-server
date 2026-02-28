package com.lms.assessment.repository;

import com.lms.assessment.model.StudentQuizMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StudentQuizMapRepository extends JpaRepository<StudentQuizMap, Long> {

    List<StudentQuizMap> findByStudentEmail(String studentEmail);
    void deleteByQuizId(Long quizId);
   
    @Query("SELECT sqm.quizId FROM StudentQuizMap sqm WHERE sqm.studentEmail = :email")
    List<Long> findQuizIdsByStudent(String email);

    boolean existsByQuizIdAndStudentEmail(Long quizId, String studentEmail);
}