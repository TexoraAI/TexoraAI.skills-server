//
//package com.lms.assessment.controller;
//
//import java.util.Map;
//import com.lms.assessment.dto.AttemptHistoryResponse;
//import com.lms.assessment.dto.QuizResultResponse;
//import com.lms.assessment.dto.SubmitAttemptRequest;
//import com.lms.assessment.model.Attempt;
//import com.lms.assessment.service.AttemptService;
//import jakarta.servlet.http.HttpServletRequest;
//import org.springframework.web.bind.annotation.*;
//import java.util.HashMap;
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/attempts")
//public class AttemptController {
//
//    private final AttemptService attemptService;
//
//    public AttemptController(AttemptService attemptService) {
//        this.attemptService = attemptService;
//    }
//
//    @PostMapping("/submit")
//    public QuizResultResponse submit(@RequestBody SubmitAttemptRequest request, HttpServletRequest httpRequest) {
//        Map<Long, Boolean> correctnessMap = new HashMap<>();
//        // ✅ service already builds full response
//        return attemptService.submitAttempt(request, correctnessMap, orgId(httpRequest));
//    }
//
//    // GET ATTEMPT
//    @GetMapping("/{id}")
//    public Attempt get(@PathVariable Long id, HttpServletRequest httpRequest) {
//        return attemptService.getAttempt(id, orgId(httpRequest));
//    }
//
//    // HAS USER ATTEMPTED
//    @GetMapping("/has-attempted/{quizId}")
//    public boolean hasAttempted(@PathVariable Long quizId, HttpServletRequest httpRequest) {
//        return attemptService.hasUserAttempted(quizId, orgId(httpRequest));
//    }
//
//    // TRAINER: GET ALL ATTEMPTS FOR A QUIZ
//    @GetMapping("/quiz/{quizId}")
//    public java.util.List<Attempt> getAttemptsForQuiz(@PathVariable Long quizId, HttpServletRequest httpRequest) {
//        return attemptService.getAttemptsForQuiz(quizId, orgId(httpRequest));
//    }
//
//    // STUDENT: MY ATTEMPT HISTORY
//    @GetMapping("/my")
//    public List<AttemptHistoryResponse> myAttempts(HttpServletRequest httpRequest) {
//        return attemptService.getMyAttempts(orgId(httpRequest));
//    }
//
//    private String orgId(HttpServletRequest request) {
//        return (String) request.getAttribute("organizationId");
//    }
//}


package com.lms.assessment.controller;

import java.util.Map;
import com.lms.assessment.constants.AssessmentFeatureKeys;
import com.lms.assessment.dto.AttemptHistoryResponse;
import com.lms.assessment.dto.QuizResultResponse;
import com.lms.assessment.dto.SubmitAttemptRequest;
import com.lms.assessment.model.Attempt;
import com.lms.assessment.service.AssessmentFeatureFlagsService;
import com.lms.assessment.service.AttemptService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/attempts")
public class AttemptController {

    private final AttemptService attemptService;
    private final AssessmentFeatureFlagsService featureFlagsService;

    public AttemptController(AttemptService attemptService,
                             AssessmentFeatureFlagsService featureFlagsService) {
        this.attemptService = attemptService;
        this.featureFlagsService = featureFlagsService;
    }

    @PostMapping("/submit")
    public QuizResultResponse submit(@RequestBody SubmitAttemptRequest request, HttpServletRequest httpRequest) {
        featureFlagsService.enforce(orgId(httpRequest), callerEmail(), AssessmentFeatureKeys.ATTEMPT_QUIZ);
        Map<Long, Boolean> correctnessMap = new HashMap<>();
        // ✅ service already builds full response
        return attemptService.submitAttempt(request, correctnessMap, orgId(httpRequest));
    }

    // GET ATTEMPT
    @GetMapping("/{id}")
    public Attempt get(@PathVariable Long id, HttpServletRequest httpRequest) {
        return attemptService.getAttempt(id, orgId(httpRequest));
    }

    // HAS USER ATTEMPTED
    @GetMapping("/has-attempted/{quizId}")
    public boolean hasAttempted(@PathVariable Long quizId, HttpServletRequest httpRequest) {
        featureFlagsService.enforce(orgId(httpRequest), callerEmail(), AssessmentFeatureKeys.ATTEMPT_QUIZ);
        return attemptService.hasUserAttempted(quizId, orgId(httpRequest));
    }

    // TRAINER: GET ALL ATTEMPTS FOR A QUIZ
    @GetMapping("/quiz/{quizId}")
    public java.util.List<Attempt> getAttemptsForQuiz(@PathVariable Long quizId, HttpServletRequest httpRequest) {
        featureFlagsService.enforce(orgId(httpRequest), callerEmail(), AssessmentFeatureKeys.CREATE_QUIZ);
        return attemptService.getAttemptsForQuiz(quizId, orgId(httpRequest));
    }

    // STUDENT: MY ATTEMPT HISTORY
    @GetMapping("/my")
    public List<AttemptHistoryResponse> myAttempts(HttpServletRequest httpRequest) {
        featureFlagsService.enforce(orgId(httpRequest), callerEmail(), AssessmentFeatureKeys.ATTEMPT_QUIZ);
        return attemptService.getMyAttempts(orgId(httpRequest));
    }

    private String orgId(HttpServletRequest request) {
        return (String) request.getAttribute("organizationId");
    }

    // ── Feature-flag helper: this controller has no @PreAuthorize and no
    // Authentication/Principal param anywhere — email is pulled from the
    // SecurityContext the same way JwtFilter populates it. ──────────────────
    private String callerEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }
}