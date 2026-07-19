//package com.lms.assessment.repository;
//
//import com.lms.assessment.model.Answer;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.List;
//
//public interface AnswerRepository extends JpaRepository<Answer, Long> {
//    List<Answer> findByAttemptId(Long attemptId);
//    
//   
//
//}
package com.lms.assessment.repository;

import com.lms.assessment.model.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
    List<Answer> findByAttemptId(Long attemptId);

    // Answer has a `attempt` relation (not a flat attemptId field), so the path
    // is a.attempt.id — deletes all answers belonging to attempts on this quiz.
    @Modifying
    @Query("DELETE FROM Answer a WHERE a.attempt.id IN " +
           "(SELECT att.id FROM Attempt att WHERE att.quiz.id = :quizId)")
    void deleteByQuizId(@Param("quizId") Long quizId);
}