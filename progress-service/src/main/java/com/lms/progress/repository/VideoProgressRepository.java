package com.lms.progress.repository;
 
import com.lms.progress.model.VideoProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
 
public interface VideoProgressRepository extends JpaRepository<VideoProgress, Long> {
 
    // find progress for a specific student in a specific batch
    Optional<VideoProgress> findByStudentEmailAndBatchId(String email, Long batchId);
}