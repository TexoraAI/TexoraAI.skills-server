//package com.lms.assessment.controller;
//import com.lms.assessment.dto.CreateOptionRequest;
//import com.lms.assessment.model.Option;
//import com.lms.assessment.service.OptionService;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/options")
//public class OptionController {
//
//    private final OptionService optionService;
//
//    public OptionController(OptionService optionService) {
//        this.optionService = optionService;
//    }
//
//    @PostMapping
//    public Option addOption(@RequestBody CreateOptionRequest req) {
//        return optionService.addOption(req);
//    }
//}
package com.lms.assessment.controller;
import com.lms.assessment.constants.AssessmentFeatureKeys;
import com.lms.assessment.dto.CreateOptionRequest;
import com.lms.assessment.model.Option;
import com.lms.assessment.service.AssessmentFeatureFlagsService;
import com.lms.assessment.service.OptionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/options")
public class OptionController {

    private final OptionService optionService;
    private final AssessmentFeatureFlagsService featureFlagsService;

    public OptionController(OptionService optionService,
                            AssessmentFeatureFlagsService featureFlagsService) {
        this.optionService = optionService;
        this.featureFlagsService = featureFlagsService;
    }

    @PostMapping
    public Option addOption(@RequestBody CreateOptionRequest req, HttpServletRequest httpRequest) {
        // Adding an option is a child action of quiz management — same key, no separate toggle.
        featureFlagsService.enforce(orgId(httpRequest), callerEmail(), AssessmentFeatureKeys.CREATE_QUIZ);
        return optionService.addOption(req);
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