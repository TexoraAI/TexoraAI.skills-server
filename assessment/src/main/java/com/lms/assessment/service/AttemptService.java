package com.lms.assessment.service;
import java.util.List;
import java.util.Map;
import com.lms.assessment.dto.AnswerRequest;
import com.lms.assessment.dto.AttemptHistoryResponse;
import com.lms.assessment.dto.QuizResultResponse;
import com.lms.assessment.dto.SubmitAttemptRequest;
import com.lms.assessment.model.Attempt;
import com.lms.assessment.model.Option;
import com.lms.assessment.model.Quiz;
import com.lms.assessment.repository.AttemptRepository;
import com.lms.assessment.repository.OptionRepository;
import com.lms.assessment.repository.QuizRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import org.springframework.transaction.annotation.Transactional;
@Service
public class AttemptService {

    private final AttemptRepository attemptRepo;
    private final QuizRepository quizRepo;
    private final OptionRepository optionRepo;

    public AttemptService(
            AttemptRepository attemptRepo,
            QuizRepository quizRepo,
            OptionRepository optionRepo
    ) {
        this.attemptRepo = attemptRepo;
        this.quizRepo = quizRepo;
        this.optionRepo = optionRepo;
    }

 
//
//    @Transactional
//    public Attempt submitAttempt(SubmitAttemptRequest req, Map<Long, Boolean> correctnessMap) {
//
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//
//        if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
//            throw new RuntimeException("JWT not found — user not authenticated");
//        }
//
//        String userEmail = auth.getName();
//
//        Quiz quiz = quizRepo.findById(req.getQuizId())
//                .orElseThrow(() -> new RuntimeException("Quiz not found"));
//
//        int correct = 0;
//
//        if (req.getAnswers() != null) {
//            for (AnswerRequest ans : req.getAnswers()) {
//
//                if (ans.getSelectedOptionId() == null) continue;
//
//                Option selected = optionRepo.findById(ans.getSelectedOptionId())
//                        .orElseThrow(() -> new RuntimeException("Option not found"));
//
//                boolean isCorrect = selected.isCorrect();
//
//                // ✅ store per-question correctness
//                correctnessMap.put(selected.getQuestion().getId(), isCorrect);
//
//                if (isCorrect) correct++;
//            }
//        }
//
//        Attempt attempt = new Attempt();
//        attempt.setQuiz(quiz);
//        attempt.setUserEmail(userEmail);
//        attempt.setScore(correct); // storing correct answers count
//
//        attempt.setStartedAt(Instant.now());
//        attempt.setCompletedAt(Instant.now());
//        attempt.setSubmittedAt(Instant.now());
//
//        return attemptRepo.save(attempt);
//    }
//    
    @Transactional
    public QuizResultResponse submitAttempt(SubmitAttemptRequest req, Map<Long, Boolean> correctnessMap) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
            throw new RuntimeException("JWT not found — user not authenticated");
        }

        String userEmail = auth.getName();

        Quiz quiz = quizRepo.findById(req.getQuizId())
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        int correct = 0;

        if (req.getAnswers() != null) {
            for (AnswerRequest ans : req.getAnswers()) {

                if (ans.getSelectedOptionId() == null) continue;

                Option selected = optionRepo.findById(ans.getSelectedOptionId())
                        .orElseThrow(() -> new RuntimeException("Option not found"));

                boolean isCorrect = selected.isCorrect();

                // ✅ store per-question correctness
                correctnessMap.put(selected.getQuestion().getId(), isCorrect);

                if (isCorrect) correct++;
            }
        }

        // ✅ SAVE ATTEMPT
        Attempt attempt = new Attempt();
        attempt.setQuiz(quiz);
        attempt.setUserEmail(userEmail);
        attempt.setScore(correct);
        attempt.setStartedAt(java.time.Instant.now());
        attempt.setCompletedAt(java.time.Instant.now());
        attempt.setSubmittedAt(java.time.Instant.now());

        Attempt saved = attemptRepo.save(attempt);

        // ✅ CALCULATE RESULT
        int totalQuestions = quiz.getQuestions().size();

        double percentage = totalQuestions > 0
                ? (correct * 100.0) / totalQuestions
                : 0;

        // ✅ BUILD RESPONSE
        QuizResultResponse res = new QuizResultResponse();

        res.setAttemptId(saved.getId());
        res.setQuizId(quiz.getId());        // 🔥 IMPORTANT
        res.setBatchId(quiz.getBatchId());  // 🔥 IMPORTANT

        res.setScore(correct);
        res.setTotalQuestions(totalQuestions);
        res.setCorrectAnswers(correct);
        res.setPercentage(percentage);
        res.setPerQuestionCorrectness(correctnessMap);

        return res;
    }
    
    // =========================
    // CHECK IF ATTEMPTED
    // =========================
    public boolean hasUserAttempted(Long quizId) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth.getName() == null) {
            return false;
        }

        String userEmail = auth.getName();

        return attemptRepo.existsByQuiz_IdAndUserEmail(quizId, userEmail);
    }

    public Attempt getAttempt(Long id) {
        return attemptRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));
    }
 // =========================
 // TRAINER: GET ALL ATTEMPTS FOR A QUIZ
 // =========================
 public java.util.List<Attempt> getAttemptsForQuiz(Long quizId) {
     return attemptRepo.findByQuiz_Id(quizId);
 }
//=========================
//STUDENT: GET MY ATTEMPTS
//=========================
//public java.util.List<Attempt> getMyAttempts() {
//
//  Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//
//  if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
//      throw new RuntimeException("JWT not found — user not authenticated");
//  }
//
//  String userEmail = auth.getName();
//
//  return attemptRepo.findByUserEmailOrderBySubmittedAtDesc(userEmail);
//}
 public List<AttemptHistoryResponse> getMyAttempts() {

	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

	    if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
	        throw new RuntimeException("JWT not found — user not authenticated");
	    }

	    String userEmail = auth.getName();

	    List<Attempt> attempts =
	            attemptRepo.findByUserEmailOrderBySubmittedAtDesc(userEmail);

	    return attempts.stream().map(a -> {

	        // ✅ IMPORTANT: force load questions (lazy fix)
	        int totalQuestions = a.getQuiz().getQuestions().size();

	        double percentage = totalQuestions > 0
	                ? (a.getScore() * 100.0) / totalQuestions
	                : 0;

	        AttemptHistoryResponse res = new AttemptHistoryResponse();

	        res.setAttemptId(a.getId());
	        res.setQuizTitle(a.getQuiz().getTitle());
	        res.setScore(a.getScore());
	        res.setPercentage(percentage);
	        res.setSubmittedAt(a.getSubmittedAt());

	        return res;

	    }).toList();
	}
 
 
}
