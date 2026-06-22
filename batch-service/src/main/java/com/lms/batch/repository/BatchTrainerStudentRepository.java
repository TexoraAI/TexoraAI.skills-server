//package com.lms.batch.repository;
//
//import com.lms.batch.entity.BatchTrainerStudent;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import java.util.List;
//import java.util.Optional;
//
//public interface BatchTrainerStudentRepository
//        extends JpaRepository<BatchTrainerStudent, Long> {
//
//    List<BatchTrainerStudent> findByBatchId(Long batchId);
//
//    List<BatchTrainerStudent> findByBatchIdAndTrainerEmail(Long batchId, String trainerEmail);
//
//    void deleteByBatchIdAndTrainerEmail(Long batchId, String trainerEmail);
//
//    void deleteByBatchIdAndStudentEmail(Long batchId, String studentEmail);
//    
//    void deleteByBatchIdAndTrainerEmailAndStudentEmail(
//            Long batchId,
//            String trainerEmail,
//            String studentEmail
//    );
//   
//    
//    @Query("SELECT COUNT(DISTINCT bts.studentEmail) FROM BatchTrainerStudent bts WHERE bts.batchId = :batchId")
//    Long countDistinctStudents(@Param("batchId") Long batchId);
//
//    @Query("""
//    	       SELECT DISTINCT b.batchId
//    	       FROM BatchTrainerStudent b
//    	       WHERE b.trainerEmail = :email
//    	       """)
//    	List<Long> findDistinctBatchIdsByTrainer(@Param("email") String email);
//
//    Optional<BatchTrainerStudent> findFirstByStudentEmail(String email);
//    Optional<BatchTrainerStudent> 
//    findTopByStudentEmailOrderByIdDesc(String studentEmail);
//
//    
//
//    void deleteByBatchId(Long batchId);
//
// // Add these two to your existing BatchTrainerStudentRepository
//
// // Used by AuthEventConsumer.handleTrainerDeleted()
// List<BatchTrainerStudent> findByTrainerEmail(String trainerEmail);
//
// // Used by AuthEventConsumer.handleStudentDeleted()
// List<BatchTrainerStudent> findByStudentEmail(String studentEmail);
// 
// @Query("SELECT DISTINCT b.studentEmail FROM BatchTrainerStudent b WHERE b.studentEmail IS NOT NULL AND b.studentEmail <> '__EMPTY__'")
// List<String> findAllAssignedStudentEmails();
//}
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
            Long batchId, String trainerEmail, String studentEmail);

    @Query("SELECT COUNT(DISTINCT bts.studentEmail) FROM BatchTrainerStudent bts WHERE bts.batchId = :batchId")
    Long countDistinctStudents(@Param("batchId") Long batchId);

    @Query("SELECT DISTINCT b.batchId FROM BatchTrainerStudent b WHERE b.trainerEmail = :email")
    List<Long> findDistinctBatchIdsByTrainer(@Param("email") String email);

    Optional<BatchTrainerStudent> findFirstByStudentEmail(String email);
    Optional<BatchTrainerStudent> findTopByStudentEmailOrderByIdDesc(String studentEmail);

    void deleteByBatchId(Long batchId);

    List<BatchTrainerStudent> findByTrainerEmail(String trainerEmail);
    List<BatchTrainerStudent> findByStudentEmail(String studentEmail);

    @Query("SELECT DISTINCT b.studentEmail FROM BatchTrainerStudent b WHERE b.studentEmail IS NOT NULL AND b.studentEmail <> '__EMPTY__'")
    List<String> findAllAssignedStudentEmails();

    // OPTIMIZATION: Added batch-scoped query to fetch all mappings for a list of batchIds
    // in ONE query instead of N queries inside getAllBatches/getBatchesByOrg loops.
    // This eliminates the N+1 problem when loading trainer info for batch lists.
    @Query("SELECT b FROM BatchTrainerStudent b WHERE b.batchId IN :batchIds")
    List<BatchTrainerStudent> findByBatchIdIn(@Param("batchIds") List<Long> batchIds);
}