package com.lms.assessment.repository;

import com.lms.assessment.model.Attempt;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AttemptRepository extends JpaRepository<Attempt, Long> {
    List<Attempt> findByQuiz_Id(Long quizId);
    Optional<Attempt> findByQuiz_IdAndUserEmail(Long quizId, String userEmail);
    boolean existsByQuiz_IdAndUserEmail(Long quizId, String userEmail);
    List<Attempt> findByUserEmailOrderBySubmittedAtDesc(String userEmail);

    void deleteByQuizId(Long quizId);
    


}
