package com.lms.chat.repository;
import org.springframework.data.jpa.repository.Modifying;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lms.chat.entity.ChatClassroomAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
public interface ChatClassroomAccessRepository
        extends JpaRepository<ChatClassroomAccess, Long> {

    boolean existsByBatchIdAndTrainerEmailAndStudentEmail(
            Long batchId, String trainer, String student);

    void deleteByStudentEmailAndBatchId(String student, Long batchId);

    void deleteByTrainerEmailAndBatchId(String trainer, Long batchId);

    void deleteByBatchId(Long batchId);
    Optional<ChatClassroomAccess> findByStudentEmail(String studentEmail);

    @Query("""
    	    SELECT c.studentEmail
    	    FROM ChatClassroomAccess c
    	    WHERE c.batchId = :batchId
    	    AND c.trainerEmail = :trainer
    	""")
    	List<String> findStudentsOfTrainer(Long batchId, String trainer);
   
    
    @Modifying
    @Query("""
        UPDATE ChatClassroomAccess c
        SET c.trainerEmail = :trainer
        WHERE c.batchId = :batchId
    """)
    void attachTrainerToBatch(Long batchId, String trainer);

    

    
    @Modifying
    @Query("""
        DELETE FROM ChatClassroomAccess c
        WHERE c.batchId = :batchId
        AND c.studentEmail = :student
    """)
    void removeStudent(Long batchId, String student);

    
    @Query("""
    	    SELECT c.trainerEmail
    	    FROM ChatClassroomAccess c
    	    WHERE c.batchId = :batchId
    	    AND c.studentEmail = :studentEmail
    	""")
    	Optional<String> findTrainerForStudent(Long batchId, String studentEmail);

    @Query("""
    		SELECT a FROM ChatClassroomAccess a
    		WHERE LOWER(TRIM(a.studentEmail)) = LOWER(TRIM(:email))
    		""")
    		Optional<ChatClassroomAccess> findStudentIgnoreCase(@Param("email") String email);

}
