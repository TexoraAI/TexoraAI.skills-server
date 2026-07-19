//package com.lms.assessment.service;
//import java.util.List;
//import java.util.Map;
//import com.lms.assessment.dto.AnswerRequest;
//import com.lms.assessment.dto.AttemptHistoryResponse;
//import com.lms.assessment.dto.QuizResultResponse;
//import com.lms.assessment.dto.SubmitAttemptRequest;
//import com.lms.assessment.model.Attempt;
//import com.lms.assessment.model.Option;
//import com.lms.assessment.model.Quiz;
//import com.lms.assessment.repository.AttemptRepository;
//import com.lms.assessment.repository.OptionRepository;
//import com.lms.assessment.repository.QuizRepository;
//
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Service;
//
//import java.time.Duration;
//import java.time.Instant;
//import org.springframework.transaction.annotation.Transactional;
//@Service
//public class AttemptService {
//
//    private final AttemptRepository attemptRepo;
//    private final QuizRepository quizRepo;
//    private final OptionRepository optionRepo;
//
//    public AttemptService(
//            AttemptRepository attemptRepo,
//            QuizRepository quizRepo,
//            OptionRepository optionRepo
//    ) {
//        this.attemptRepo = attemptRepo;
//        this.quizRepo = quizRepo;
//        this.optionRepo = optionRepo;
//    }
//
// 
//
//    @Transactional
//    public QuizResultResponse submitAttempt(SubmitAttemptRequest req, Map<Long, Boolean> correctnessMap) {
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
//        // ✅ SAVE ATTEMPT
//        Attempt attempt = new Attempt();
//        attempt.setQuiz(quiz);
//        attempt.setUserEmail(userEmail);
//        attempt.setScore(correct);
//        attempt.setStartedAt(java.time.Instant.now());
//        attempt.setCompletedAt(java.time.Instant.now());
//        attempt.setSubmittedAt(java.time.Instant.now());
//
//        Attempt saved = attemptRepo.save(attempt);
//
//        // ✅ CALCULATE RESULT
//        int totalQuestions = quiz.getQuestions().size();
//
//        double percentage = totalQuestions > 0
//                ? (correct * 100.0) / totalQuestions
//                : 0;
//
//        // ✅ BUILD RESPONSE
//        QuizResultResponse res = new QuizResultResponse();
//
//        res.setAttemptId(saved.getId());
//        res.setQuizId(quiz.getId());        // 🔥 IMPORTANT
//        res.setBatchId(quiz.getBatchId());  // 🔥 IMPORTANT
//
//        res.setScore(correct);
//        res.setTotalQuestions(totalQuestions);
//        res.setCorrectAnswers(correct);
//        res.setPercentage(percentage);
//        res.setPerQuestionCorrectness(correctnessMap);
//
//        return res;
//    }
//    
//    // =========================
//    // CHECK IF ATTEMPTED
//    // =========================
//    public boolean hasUserAttempted(Long quizId) {
//
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//
//        if (auth == null || auth.getName() == null) {
//            return false;
//        }
//
//        String userEmail = auth.getName();
//
//        return attemptRepo.existsByQuiz_IdAndUserEmail(quizId, userEmail);
//    }
//
//    public Attempt getAttempt(Long id) {
//        return attemptRepo.findById(id)
//                .orElseThrow(() -> new RuntimeException("Attempt not found"));
//    }
// // =========================
// // TRAINER: GET ALL ATTEMPTS FOR A QUIZ
// // =========================
// public java.util.List<Attempt> getAttemptsForQuiz(Long quizId) {
//     return attemptRepo.findByQuiz_Id(quizId);
// }
//
////STUDENT: GET MY ATTEMPTS
// public List<AttemptHistoryResponse> getMyAttempts() {
//
//	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//
//	    if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
//	        throw new RuntimeException("JWT not found — user not authenticated");
//	    }
//
//	    String userEmail = auth.getName();
//
//	    List<Attempt> attempts =
//	            attemptRepo.findByUserEmailOrderBySubmittedAtDesc(userEmail);
//
//	    return attempts.stream().map(a -> {
//
//	        // ✅ IMPORTANT: force load questions (lazy fix)
//	        int totalQuestions = a.getQuiz().getQuestions().size();
//
//	        double percentage = totalQuestions > 0
//	                ? (a.getScore() * 100.0) / totalQuestions
//	                : 0;
//
//	        AttemptHistoryResponse res = new AttemptHistoryResponse();
//
//	        res.setAttemptId(a.getId());
//	        res.setQuizTitle(a.getQuiz().getTitle());
//	        res.setScore(a.getScore());
//	        res.setPercentage(percentage);
//	        res.setSubmittedAt(a.getSubmittedAt());
//
//	        return res;
//
//	    }).toList();
//	}
//}
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

    @Transactional
    public QuizResultResponse submitAttempt(SubmitAttemptRequest req, Map<Long, Boolean> correctnessMap, String organizationId) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
            throw new RuntimeException("JWT not found — user not authenticated");
        }

        String userEmail = auth.getName();

        Quiz quiz = quizRepo.findById(req.getQuizId())
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        assertSameOrg(quiz.getOrganizationId(), organizationId);

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
    public boolean hasUserAttempted(Long quizId, String organizationId) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth.getName() == null) {
            return false;
        }

        String userEmail = auth.getName();

        Quiz quiz = quizRepo.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        assertSameOrg(quiz.getOrganizationId(), organizationId);

        return attemptRepo.existsByQuiz_IdAndUserEmail(quizId, userEmail);
    }

    public Attempt getAttempt(Long id, String organizationId) {
        Attempt attempt = attemptRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));

        assertSameOrg(attempt.getQuiz().getOrganizationId(), organizationId);

        return attempt;
    }

    // =========================
    // TRAINER: GET ALL ATTEMPTS FOR A QUIZ
    // =========================
    public java.util.List<Attempt> getAttemptsForQuiz(Long quizId, String organizationId) {
        Quiz quiz = quizRepo.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        assertSameOrg(quiz.getOrganizationId(), organizationId);

        return attemptRepo.findByQuiz_Id(quizId);
    }

    // STUDENT: GET MY ATTEMPTS
    public List<AttemptHistoryResponse> getMyAttempts(String organizationId) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
            throw new RuntimeException("JWT not found — user not authenticated");
        }

        String userEmail = auth.getName();

        List<Attempt> attempts =
                attemptRepo.findByUserEmailOrderBySubmittedAtDesc(userEmail);

        return attempts.stream()
                .filter(a -> organizationId == null || organizationId.equals(a.getQuiz().getOrganizationId()))
                .map(a -> {

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

    // ================= ORG GUARD (private) =================

    private void assertSameOrg(String resourceOrgId, String callerOrgId) {
        if (callerOrgId == null) return;
        if (!callerOrgId.equals(resourceOrgId)) {
            throw new RuntimeException("Access denied: quiz belongs to a different organization");
        }
    }
}