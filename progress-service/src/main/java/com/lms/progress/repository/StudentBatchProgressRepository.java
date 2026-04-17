package com.lms.progress.repository;

import com.lms.progress.model.StudentBatchProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentBatchProgressRepository extends JpaRepository<StudentBatchProgress, Long> {

    List<StudentBatchProgress> findByBatchId(String batchId);

    Optional<StudentBatchProgress> findByBatchIdAndStudentId(String batchId, String studentId);

    List<StudentBatchProgress> findByTrainerEmail(String trainerEmail);

    List<StudentBatchProgress> findByStudentId(String studentId);

    boolean existsByBatchIdAndStudentId(String batchId, String studentId);

    @Query("SELECT DISTINCT s.batchId FROM StudentBatchProgress s WHERE s.trainerEmail = :trainerEmail")
    List<String> findDistinctBatchIdsByTrainerEmail(@Param("trainerEmail") String trainerEmail);

    @Query("SELECT COUNT(DISTINCT s.studentId) FROM StudentBatchProgress s WHERE s.trainerEmail = :trainerEmail")
    long countDistinctStudentsByTrainerEmail(@Param("trainerEmail") String trainerEmail);

    @Query("SELECT COUNT(DISTINCT s.studentId) FROM StudentBatchProgress s WHERE s.batchId = :batchId")
    long countStudentsByBatchId(@Param("batchId") String batchId);
}