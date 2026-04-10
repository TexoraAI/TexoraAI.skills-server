//package com.lms.assessment.controller;
//
//import com.lms.assessment.dto.SubmitAttemptRequest;
//import com.lms.assessment.dto.QuizResultResponse;
//import com.lms.assessment.model.Attempt;
//import com.lms.assessment.service.AttemptService;
//import com.lms.assessment.service.QuizService;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/attempts")
//public class AttemptController {
//
//    private final AttemptService attemptService;
//    private final QuizService quizService;
//
//    public AttemptController(AttemptService attemptService, QuizService quizService) {
//        this.attemptService = attemptService;
//        this.quizService = quizService;
//    }
//
//    @PostMapping("/submit")
//    public QuizResultResponse submit(@RequestBody SubmitAttemptRequest req) {
//        return quizService.submitAnswers(req);
//    }
//
//    @GetMapping("/{id}")
//    public Attempt get(@PathVariable Long id) {
//        return attemptService.getAttempt(id);
//    }
//    @GetMapping("/has-attempted/{quizId}")
//    public boolean hasAttempted(@PathVariable Long quizId) {
//        return attemptService.hasUserAttempted(quizId);
//    }
//
//}

package com.lms.assessment.controller;
import java.util.Map;

import com.lms.assessment.dto.AttemptHistoryResponse;
import com.lms.assessment.dto.QuizResultResponse;
import com.lms.assessment.dto.SubmitAttemptRequest;
import com.lms.assessment.model.Attempt;
import com.lms.assessment.service.AttemptService;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
@RestController
@RequestMapping("/api/attempts")
public class AttemptController {

    private final AttemptService attemptService;

    public AttemptController(AttemptService attemptService) {
        this.attemptService = attemptService;
    }

    // ============================
   


//    @PostMapping("/submit")
//    public QuizResultResponse submit(@RequestBody SubmitAttemptRequest request) {
//
//        Map<Long, Boolean> correctnessMap = new HashMap<>();
//
//        // ✅ pass map to service
//        Attempt attempt = attemptService.submitAttempt(request, correctnessMap);
//
//        int totalQuestions = attempt.getQuiz().getQuestions().size();
//        int correctAnswers = attempt.getScore();
//
//        double percentage = (correctAnswers * 100.0) / totalQuestions;
//
//        // ✅ BUILD FULL RESPONSE
//        QuizResultResponse res = new QuizResultResponse();
//
//        res.setAttemptId(attempt.getId());
//        res.setScore(correctAnswers); // raw score
//        res.setPercentage(percentage);
//        res.setTotalQuestions(totalQuestions);
//        res.setCorrectAnswers(correctAnswers);
//        res.setPerQuestionCorrectness(correctnessMap);
//
//        return res;
//    }
    @PostMapping("/submit")
    public QuizResultResponse submit(@RequestBody SubmitAttemptRequest request) {

        Map<Long, Boolean> correctnessMap = new HashMap<>();

        // ✅ service already builds full response
        return attemptService.submitAttempt(request, correctnessMap);
    }
    // GET ATTEMPT
    // ============================
    @GetMapping("/{id}")
    public Attempt get(@PathVariable Long id) {
        return attemptService.getAttempt(id);
    }

    // ============================
    // HAS USER ATTEMPTED
    // ============================
    @GetMapping("/has-attempted/{quizId}")
    public boolean hasAttempted(@PathVariable Long quizId) {
        return attemptService.hasUserAttempted(quizId);
    }
 // ============================
 // TRAINER: GET ALL ATTEMPTS FOR A QUIZ
 // ============================
 @GetMapping("/quiz/{quizId}")
 public java.util.List<Attempt> getAttemptsForQuiz(@PathVariable Long quizId) {
     return attemptService.getAttemptsForQuiz(quizId);
 }
//============================
//STUDENT: MY ATTEMPT HISTORY
//============================
// @GetMapping("/my")
//public java.util.List<Attempt> myAttempts() {
//  return attemptService.getMyAttempts();
//}

 @GetMapping("/my")
 public List<AttemptHistoryResponse> myAttempts() {
     return attemptService.getMyAttempts();
 }
}

