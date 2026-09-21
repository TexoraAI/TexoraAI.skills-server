

package com.lms.assessment.service;

import com.lms.assessment.constants.AssessmentUsageLimits;
import com.lms.assessment.dto.JudgeResult;
import com.lms.assessment.dto.SubmissionJudgeResponse;
import com.lms.assessment.model.CodeSubmission;
import com.lms.assessment.model.CodeSubmission.ExecutionStatus;
import com.lms.assessment.model.CodingProblem;
import com.lms.assessment.model.TestCase;
import com.lms.assessment.repository.CodeSubmissionRepository;
import com.lms.assessment.repository.CodingProblemRepository;
import com.lms.assessment.repository.TestCaseRepository;
import com.lms.assessment.service.CodeExecutionService.ExecutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class JudgeService {

    private static final Logger log = LoggerFactory.getLogger(JudgeService.class);

    private final CodeExecutionService codeExecutionService;
    private final CodeSubmissionRepository codeSubmissionRepository;
    private final CodingProblemRepository codingProblemRepository;
    private final TestCaseRepository testCaseRepository;
    private final AssessmentUsageService assessmentUsageService;

    public JudgeService(CodeExecutionService codeExecutionService,
                        CodeSubmissionRepository codeSubmissionRepository,
                        CodingProblemRepository codingProblemRepository,
                        TestCaseRepository testCaseRepository,
                        AssessmentUsageService assessmentUsageService) {
        this.codeExecutionService     = codeExecutionService;
        this.codeSubmissionRepository = codeSubmissionRepository;
        this.codingProblemRepository  = codingProblemRepository;
        this.testCaseRepository       = testCaseRepository;
        this.assessmentUsageService   = assessmentUsageService;
    }

    // ── Student: submit code against a problem ────
    @Transactional
    public SubmissionJudgeResponse judge(Long problemId, String batchId,
                                         String language, String code, String organizationId) {
        String studentEmail = extractEmailFromJwt();

        CodingProblem problem = codingProblemRepository.findById(problemId)
            .orElseThrow(() -> new RuntimeException("Problem not found: " + problemId));

        List<TestCase> testCases = testCaseRepository.findByProblemIdOrderById(problemId);

        if (testCases.isEmpty()) {
            throw new RuntimeException("No test cases found for problem: " + problemId);
        }

        // Quota check happens before any sandboxed code execution runs, so an
        // over-quota request doesn't waste compute on the test-case loop below.
        assessmentUsageService.checkAndIncrement(
            AssessmentUsageLimits.Action.CODING_SOLVE, studentEmail, organizationId);

        List<JudgeResult> judgeResults = new ArrayList<>();
        int marksObtained   = 0;
        int testCasesPassed = 0;
        long totalElapsedMs = 0;
        ExecutionStatus overallExecutionStatus = ExecutionStatus.SUCCESS;

        // Run code against each test case
        for (TestCase tc : testCases) {
            // ← CHANGED: pass tc.getInput() as stdin directly, removed injectInput()
            ExecutionResult result = codeExecutionService.execute(language, code, tc.getInput());
            totalElapsedMs += result.getElapsedMs();

            boolean passed = false;
            String verdict;

            if (result.getStatus() == ExecutionStatus.SUCCESS) {
                passed  = normalizeOutput(result.getOutput())
                              .equals(normalizeOutput(tc.getExpectedOutput()));
                verdict = passed ? "PASSED" : "WRONG_ANSWER";
            } else if (result.getStatus() == ExecutionStatus.TIMEOUT) {
                verdict = "TIMEOUT";
                overallExecutionStatus = ExecutionStatus.TIMEOUT;
            } else {
                verdict = "RUNTIME_ERROR";
                overallExecutionStatus = ExecutionStatus.RUNTIME_ERROR;
            }

            if (passed) {
                marksObtained += tc.getMarks();
                testCasesPassed++;
            }

            judgeResults.add(JudgeResult.builder()
                .testCaseId(tc.getId())
                .input(tc.getIsHidden() ? null : tc.getInput())
                .expectedOutput(tc.getIsHidden() ? null : tc.getExpectedOutput())
                .actualOutput(tc.getIsHidden() ? null : result.getOutput())
                .passed(passed)
                .weightage(tc.getMarks())
                .verdict(verdict)
                .build());
        }

        // Determine overall verdict
        String overallVerdict;
        if (testCasesPassed == testCases.size()) {
            overallVerdict = "ACCEPTED";
        } else if (testCasesPassed > 0) {
            overallVerdict = "PARTIAL";
        } else {
            overallVerdict = "REJECTED";
        }

        // Persist submission
        CodeSubmission submission = new CodeSubmission();
        submission.setStudentEmail(studentEmail);
        submission.setBatchId(batchId);
        submission.setLanguage(language.toUpperCase());
        submission.setCode(code);
        submission.setOutput(overallVerdict);
        submission.setStatus(overallExecutionStatus);
        submission.setExecutionTimeMs(totalElapsedMs);
        CodeSubmission saved = codeSubmissionRepository.save(submission);

        log.info("Judge complete: student={} problem={} verdict={} marks={}/{}",
            studentEmail, problemId, overallVerdict, marksObtained, problem.getTotalMarks());

        return SubmissionJudgeResponse.builder()
            .submissionId(saved.getId())
            .problemId(problemId)
            .studentEmail(studentEmail)
            .batchId(batchId)
            .language(language.toUpperCase())
            .executionStatus(overallExecutionStatus)
            .totalMarks(problem.getTotalMarks())
            .marksObtained(marksObtained)
            .testCasesPassed(testCasesPassed)
            .totalTestCases(testCases.size())
            .overallVerdict(overallVerdict)
            .judgeResults(judgeResults)
            .executionTimeMs(totalElapsedMs)
            .submittedAt(saved.getCreatedAt())
            .build();
    }

    // ── Normalize output for comparison ──────────
    private String normalizeOutput(String output) {
        if (output == null) return "";
        return output.trim()
                     .replaceAll("\r\n", "\n")
                     .replaceAll("\r", "\n")
                     .stripTrailing();
    }

    // ← DELETED: injectInput() method fully removed, no longer needed

    private String extractEmailFromJwt() {
        return SecurityContextHolder.getContext()
            .getAuthentication()
            .getName();
    }
}