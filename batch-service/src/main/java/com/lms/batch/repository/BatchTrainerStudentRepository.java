package com.lms.batch.repository;

import com.lms.batch.entity.BatchTrainerStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface BatchTrainerStudentRepository
        extends JpaRepository<BatchTrainerStudent, Long> {

    List<BatchTrainerStudent> findByBatchId(Long batchId);

    List<BatchTrainerStudent> findByBatchIdAndTrainerEmail(Long batchId, String trainerEmail);

    void deleteByBatchIdAndTrainerEmail(Long batchId, String trainerEmail);

    void deleteByBatchIdAndStudentEmail(Long batchId, String studentEmail);
    
    void deleteByBatchIdAndTrainerEmailAndStudentEmail(
            Long batchId,
            String trainerEmail,
            String studentEmail
    );
   
    
    @Query("SELECT COUNT(DISTINCT bts.studentEmail) FROM BatchTrainerStudent bts WHERE bts.batchId = :batchId")
    Long countDistinctStudents(@Param("batchId") Long batchId);

    @Query("""
    	       SELECT DISTINCT b.batchId
    	       FROM BatchTrainerStudent b
    	       WHERE b.trainerEmail = :email
    	       """)
    	List<Long> findDistinctBatchIdsByTrainer(@Param("email") String email);

    Optional<BatchTrainerStudent> findFirstByStudentEmail(String email);
    Optional<BatchTrainerStudent> 
    findTopByStudentEmailOrderByIdDesc(String studentEmail);

    

    void deleteByBatchId(Long batchId);

    
}
