package com.lms.assessment.controller;

import com.lms.assessment.dto.CreateQuizWithQuestionsRequest;
import com.lms.assessment.model.Quiz;
import com.lms.assessment.service.QuizService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) { this.quizService = quizService; }

    
//    public Quiz create(@RequestBody Quiz quiz) {
//        return quizService.createQuiz(quiz);
//    }
    @PostMapping
    public Quiz createQuiz(@RequestBody Quiz quiz, Authentication auth) {
        return quizService.createQuiz(quiz, auth.getName());
    }
         @GetMapping("/{id}")     
    public Quiz get(@PathVariable Long id) {
        return quizService.getQuiz(id);
    }

   
         @DeleteMapping("/{id}")
         public void delete(@PathVariable Long id, Authentication auth) {
             quizService.deleteQuizByTrainer(id, auth.getName());
         }
//    @PostMapping("/bulk")
//    public Quiz createQuizWithQuestions(
//            @RequestBody CreateQuizWithQuestionsRequest req) {
//        return quizService.createQuizWithQuestions(req);
//    }
    @PostMapping("/bulk")
    public Quiz createQuizWithQuestions(
            @RequestBody CreateQuizWithQuestionsRequest req,
            Authentication auth
    ) {
        String trainerEmail = auth.getName(); // logged-in trainer email
        return quizService.createQuizWithQuestions(req, trainerEmail);
    }
    
    @GetMapping("/trainer")
    public List<Quiz> trainerQuizzes(Authentication auth) {
        return quizService.getTrainerQuizzes(auth.getName());
    }

    @GetMapping("/student")
    public List<Quiz> studentQuizzes(Authentication auth) {
        return quizService.getStudentQuizzes(auth.getName());
    }
    
}
