package com.lms.progress.repository;
 
import com.lms.progress.model.FileProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
 
public interface FileProgressRepository extends JpaRepository<FileProgress, Long> {
 
    // find progress for a specific student in a specific batch
    Optional<FileProgress> findByStudentEmailAndBatchId(String email, Long batchId);
}