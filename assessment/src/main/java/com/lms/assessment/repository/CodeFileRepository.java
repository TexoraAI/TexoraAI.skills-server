package com.lms.assessment.repository;

import com.lms.assessment.model.CodeFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CodeFileRepository extends JpaRepository<CodeFile, Long> {

    // All files for a student in a batch (sorted newest first)
    List<CodeFile> findByStudentEmailAndBatchIdOrderByUpdatedAtDesc(
        String studentEmail, String batchId
    );

    // Check if filename already exists for this student+batch+language
    Optional<CodeFile> findByStudentEmailAndBatchIdAndFileName(
        String studentEmail, String batchId, String fileName
    );

    // Filter by language
    List<CodeFile> findByStudentEmailAndBatchIdAndLanguageOrderByUpdatedAtDesc(
        String studentEmail, String batchId, String language
    );

    // Delete all files for a student (admin use)
    void deleteByStudentEmailAndBatchId(String studentEmail, String batchId);
}