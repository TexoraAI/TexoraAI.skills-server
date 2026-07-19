//package com.lms.assessment.controller;
//
//import com.lms.assessment.dto.CreateQuestionRequest;
//import com.lms.assessment.model.Question;
//import com.lms.assessment.service.QuestionService;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/questions")
//public class QuestionController {
//
//    private final QuestionService questionService;
//
//    public QuestionController(QuestionService questionService) {
//        this.questionService = questionService;
//    }
//
//    @PostMapping
//    public Question addQuestion(@RequestBody CreateQuestionRequest req) {
//        return questionService.addQuestion(req);
//    }
//}
package com.lms.assessment.controller;

import com.lms.assessment.constants.AssessmentFeatureKeys;
import com.lms.assessment.dto.CreateQuestionRequest;
import com.lms.assessment.model.Question;
import com.lms.assessment.service.AssessmentFeatureFlagsService;
import com.lms.assessment.service.QuestionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;
    private final AssessmentFeatureFlagsService featureFlagsService;

    public QuestionController(QuestionService questionService,
                              AssessmentFeatureFlagsService featureFlagsService) {
        this.questionService = questionService;
        this.featureFlagsService = featureFlagsService;
    }

    @PostMapping
    public Question addQuestion(@RequestBody CreateQuestionRequest req, HttpServletRequest httpRequest) {
        // Adding a question is a child action of quiz management — same key, no separate toggle.
        featureFlagsService.enforce(orgId(httpRequest), callerEmail(), AssessmentFeatureKeys.CREATE_QUIZ);
        return questionService.addQuestion(req);
    }

    // 🏢 Pulled from the JwtFilter's request attribute — never from the request body.
    private String orgId(HttpServletRequest request) {
        return (String) request.getAttribute("organizationId");
    }

    // ── Feature-flag helper: no Authentication/Principal param exists here ────
    private String callerEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }
}