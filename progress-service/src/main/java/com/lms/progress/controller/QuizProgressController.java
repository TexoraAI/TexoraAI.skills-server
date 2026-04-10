package com.lms.progress.controller;

import com.lms.progress.dto.QuizProgressResponse;
import com.lms.progress.service.QuizProgressService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quiz-progress")
public class QuizProgressController {

    private final QuizProgressService service;

    public QuizProgressController(QuizProgressService service) {
        this.service = service;
    }

    // ================= GET =================
    @GetMapping("/user")
    public QuizProgressResponse get(
            @RequestParam String email,
            @RequestParam Long batchId) {

        return service.get(email, batchId);
    }

    // ================= MARK ATTEMPT =================
    @PostMapping("/mark-attempted")
    public QuizProgressResponse mark(
            @RequestParam String email,
            @RequestParam Long batchId,
            @RequestParam Long quizId,
            @RequestParam int totalQuizzes) {

        return service.markAttempted(email, batchId, quizId, totalQuizzes);
    }
}