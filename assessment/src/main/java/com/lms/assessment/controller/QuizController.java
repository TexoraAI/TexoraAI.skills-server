package com.lms.assessment.controller;

import com.lms.assessment.dto.BulkUploadResponse;
import com.lms.assessment.dto.CreateQuizWithQuestionsRequest;
import com.lms.assessment.model.Quiz;
import com.lms.assessment.service.BulkQuizParserService;
import com.lms.assessment.service.QuizService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

//    private final QuizService quizService;

//    public QuizController(QuizService quizService) { this.quizService = quizService; }
    private final QuizService           quizService;
    private final BulkQuizParserService bulkQuizParserService;

    public QuizController(QuizService quizService,
                          BulkQuizParserService bulkQuizParserService) {
        this.quizService           = quizService;
        this.bulkQuizParserService = bulkQuizParserService;
    }
    
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
 // ── NEW: Bulk upload endpoint ─────────────────────────────────────────────

    /**
     * Accepts a file (.pdf / .doc / .docx / .txt / .csv),
     * parses it via Claude AI, and returns structured quiz data.
     * The quiz is NOT saved here — the frontend populates the form
     * and the trainer publishes via the normal /api/quizzes flow.
     */
    @PostMapping("/upload-bulk")
    public ResponseEntity<?> uploadBulkQuiz(
            @RequestParam("file") MultipartFile file,
            Authentication auth) {

        if (file == null || file.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "No file provided"));
        }

        try {
            BulkUploadResponse parsed = bulkQuizParserService.parseFile(file);
            return ResponseEntity.ok(parsed);

        } catch (RuntimeException e) {
            // Known validation / parsing errors
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity
                    .internalServerError()
                    .body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }
}
    

