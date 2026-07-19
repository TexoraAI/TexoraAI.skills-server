//
//package com.lms.assessment.controller;
//
//import com.lms.assessment.dto.BulkUploadResponse;
//import com.lms.assessment.dto.CreateQuizWithQuestionsRequest;
//import com.lms.assessment.model.Quiz;
//import com.lms.assessment.service.BulkQuizParserService;
//import com.lms.assessment.service.QuizService;
//import jakarta.servlet.http.HttpServletRequest;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Map;
//
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.Authentication;
//import org.springframework.web.multipart.MultipartFile;
//import com.lms.assessment.dto.QuizAdminReportResponse;
//import org.springframework.security.access.prepost.PreAuthorize;
//@RestController
//@RequestMapping("/api/quizzes")
//public class QuizController {
//
//    private final QuizService           quizService;
//    private final BulkQuizParserService bulkQuizParserService;
//
//    public QuizController(QuizService quizService,
//                          BulkQuizParserService bulkQuizParserService) {
//        this.quizService           = quizService;
//        this.bulkQuizParserService = bulkQuizParserService;
//    }
//
//    @PostMapping
//    public Quiz createQuiz(@RequestBody Quiz quiz, Authentication auth, HttpServletRequest request) {
//        return quizService.createQuiz(quiz, auth.getName(), orgId(request));
//    }
//
//    @GetMapping("/{id}")
//    public Quiz get(@PathVariable Long id, HttpServletRequest request) {
//        return quizService.getQuiz(id, orgId(request));
//    }
//
//    @DeleteMapping("/{id}")
//    public void delete(@PathVariable Long id, Authentication auth, HttpServletRequest request) {
//        quizService.deleteQuizByTrainer(id, auth.getName(), orgId(request));
//    }
//
//    @PostMapping("/bulk")
//    public Quiz createQuizWithQuestions(
//            @RequestBody CreateQuizWithQuestionsRequest req,
//            Authentication auth,
//            HttpServletRequest request
//    ) {
//        String trainerEmail = auth.getName();
//        return quizService.createQuizWithQuestions(req, trainerEmail, orgId(request));
//    }
//
//    @GetMapping("/trainer")
//    public List<Quiz> trainerQuizzes(Authentication auth, HttpServletRequest request) {
//        return quizService.getTrainerQuizzes(auth.getName(), orgId(request));
//    }
//
//    @GetMapping("/student")
//    public List<Quiz> studentQuizzes(Authentication auth, HttpServletRequest request) {
//        return quizService.getStudentQuizzes(auth.getName(), orgId(request));
//    }
//
//    @PostMapping("/upload-bulk")
//    public ResponseEntity<?> uploadBulkQuiz(
//            @RequestParam("file") MultipartFile file,
//            Authentication auth) {
//        if (file == null || file.isEmpty()) {
//            return ResponseEntity
//                    .badRequest()
//                    .body(Map.of("error", "No file provided"));
//        }
//        try {
//            BulkUploadResponse parsed = bulkQuizParserService.parseFile(file);
//            return ResponseEntity.ok(parsed);
//        } catch (RuntimeException e) {
//            return ResponseEntity
//                    .badRequest()
//                    .body(Map.of("error", e.getMessage()));
//        } catch (Exception e) {
//            return ResponseEntity
//                    .internalServerError()
//                    .body(Map.of("error", "Upload failed: " + e.getMessage()));
//        }
//    }
//
//    // 🏢 Pulled from the JwtFilter's request attribute — never from the request body.
//    private String orgId(HttpServletRequest request) {
//        return (String) request.getAttribute("organizationId");
//    }
//    @GetMapping("/admin")
//    @PreAuthorize("hasRole('TENANT_ADMIN')")
//    public List<QuizAdminReportResponse> getAdminReport(HttpServletRequest httpRequest) {
//        return quizService.getAdminReport(orgId(httpRequest));
//    }
//
//    @GetMapping("/superadmin")
//    @PreAuthorize("hasRole('SUPER_ADMIN')")
//    public List<QuizAdminReportResponse> getSuperAdminReport() {
//        return quizService.getSuperAdminReport();
//    }
//}

package com.lms.assessment.controller;

import com.lms.assessment.constants.AssessmentFeatureKeys;
import com.lms.assessment.dto.BulkUploadResponse;
import com.lms.assessment.dto.CreateQuizWithQuestionsRequest;
import com.lms.assessment.model.Quiz;
import com.lms.assessment.service.AssessmentFeatureFlagsService;
import com.lms.assessment.service.BulkQuizParserService;
import com.lms.assessment.service.QuizService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import com.lms.assessment.dto.QuizAdminReportResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    private final QuizService                   quizService;
    private final BulkQuizParserService          bulkQuizParserService;
    private final AssessmentFeatureFlagsService  featureFlagsService;

    public QuizController(QuizService quizService,
                          BulkQuizParserService bulkQuizParserService,
                          AssessmentFeatureFlagsService featureFlagsService) {
        this.quizService           = quizService;
        this.bulkQuizParserService = bulkQuizParserService;
        this.featureFlagsService   = featureFlagsService;
    }

    @PostMapping
    public Quiz createQuiz(@RequestBody Quiz quiz, Authentication auth, HttpServletRequest request) {
        featureFlagsService.enforce(orgId(request), auth.getName(), AssessmentFeatureKeys.CREATE_QUIZ);
        return quizService.createQuiz(quiz, auth.getName(), orgId(request));
    }

    @GetMapping("/{id}")
    public Quiz get(@PathVariable Long id, HttpServletRequest request) {
        return quizService.getQuiz(id, orgId(request));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, Authentication auth, HttpServletRequest request) {
        featureFlagsService.enforce(orgId(request), auth.getName(), AssessmentFeatureKeys.CREATE_QUIZ);
        quizService.deleteQuizByTrainer(id, auth.getName(), orgId(request));
    }

    @PostMapping("/bulk")
    public Quiz createQuizWithQuestions(
            @RequestBody CreateQuizWithQuestionsRequest req,
            Authentication auth,
            HttpServletRequest request
    ) {
        featureFlagsService.enforce(orgId(request), auth.getName(), AssessmentFeatureKeys.CREATE_QUIZ);
        String trainerEmail = auth.getName();
        return quizService.createQuizWithQuestions(req, trainerEmail, orgId(request));
    }

    @GetMapping("/trainer")
    public List<Quiz> trainerQuizzes(Authentication auth, HttpServletRequest request) {
        featureFlagsService.enforce(orgId(request), auth.getName(), AssessmentFeatureKeys.CREATE_QUIZ);
        return quizService.getTrainerQuizzes(auth.getName(), orgId(request));
    }

    @GetMapping("/student")
    public List<Quiz> studentQuizzes(Authentication auth, HttpServletRequest request) {
        featureFlagsService.enforce(orgId(request), auth.getName(), AssessmentFeatureKeys.ATTEMPT_QUIZ);
        return quizService.getStudentQuizzes(auth.getName(), orgId(request));
    }

    @PostMapping("/upload-bulk")
    public ResponseEntity<?> uploadBulkQuiz(
            @RequestParam("file") MultipartFile file,
            Authentication auth,
            HttpServletRequest request) {
        featureFlagsService.enforce(orgId(request), auth.getName(), AssessmentFeatureKeys.CREATE_QUIZ);
        if (file == null || file.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "No file provided"));
        }
        try {
            BulkUploadResponse parsed = bulkQuizParserService.parseFile(file);
            return ResponseEntity.ok(parsed);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .internalServerError()
                    .body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }

    // 🏢 Pulled from the JwtFilter's request attribute — never from the request body.
    private String orgId(HttpServletRequest request) {
        return (String) request.getAttribute("organizationId");
    }

    // ── Feature-flag helper: email for endpoints with no Authentication param ──
    private String callerEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public List<QuizAdminReportResponse> getAdminReport(HttpServletRequest httpRequest) {
        featureFlagsService.enforce(orgId(httpRequest), callerEmail(), AssessmentFeatureKeys.VIEW_QUIZ_ADMIN_REPORT);
        return quizService.getAdminReport(orgId(httpRequest));
    }

    @GetMapping("/superadmin")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public List<QuizAdminReportResponse> getSuperAdminReport() {
        return quizService.getSuperAdminReport();
    }
}