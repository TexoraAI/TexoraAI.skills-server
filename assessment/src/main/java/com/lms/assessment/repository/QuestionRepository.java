//package com.lms.assessment.repository;
//
//import com.lms.assessment.model.Question;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.List;
//
//public interface QuestionRepository extends JpaRepository<Question, Long> {
//    List<Question> findByQuiz_Id(Long quizId);
//}
package com.lms.assessment.repository;

import com.lms.assessment.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByQuiz_Id(Long quizId);

    void deleteByQuizId(Long quizId);
}