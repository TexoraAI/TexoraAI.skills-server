//package com.lms.assessment.repository;
//
//import com.lms.assessment.model.Quiz;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//public interface QuizRepository extends JpaRepository<Quiz, Long> {
//	
//}
package com.lms.assessment.repository;

import com.lms.assessment.model.Quiz;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    // ONLY ACTIVE quizzes
    List<Quiz> findByActiveTrue();

    // MUST return Optional
    Optional<Quiz> findByIdAndActiveTrue(Long id);
    
    void deleteByBatchId(Long batchId);

    
    @Query("""
    	    SELECT q
    	    FROM Quiz q
    	    WHERE q.id IN (
    	        SELECT sqm.quizId
    	        FROM StudentQuizMap sqm
    	        WHERE sqm.studentEmail = :studentEmail
    	    )
    	    AND q.active = true
    	""")
    	List<Quiz> findAssignedQuizzes(@Param("studentEmail") String studentEmail);
    List<Quiz> findByBatchId(Long batchId);

    @Query("""
    		SELECT q FROM Quiz q
    		WHERE q.id IN (
    		    SELECT sqm.quizId FROM StudentQuizMap sqm
    		    WHERE sqm.studentEmail = :email
    		)
    		AND q.active = true
    		""")
    		List<Quiz> findQuizzesAssignedToStudent(@Param("email") String email);
    List<Quiz> findByTrainerEmailAndActiveTrue(String trainerEmail);
    
    List<Quiz> findByBatchIdAndActiveTrue(Long batchId);
    
    
    @Query("SELECT q FROM Quiz q LEFT JOIN FETCH q.questions WHERE q.id = :id")
    Optional<Quiz> findQuizWithQuestions(@Param("id") Long id);
}
