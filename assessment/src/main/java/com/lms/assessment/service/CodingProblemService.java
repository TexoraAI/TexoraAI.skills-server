package com.lms.assessment.service;

import com.lms.assessment.dto.CodingProblemRequest;
import com.lms.assessment.dto.CodingProblemResponse;
import com.lms.assessment.dto.TestCaseRequest;
import com.lms.assessment.dto.TestCaseResponse;
import com.lms.assessment.model.CodingProblem;
import com.lms.assessment.model.TestCase;
import com.lms.assessment.repository.CodingProblemRepository;
import com.lms.assessment.repository.ProblemAssignmentRepository;
import com.lms.assessment.repository.TestCaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CodingProblemService {

    private static final Logger log = LoggerFactory.getLogger(CodingProblemService.class);

    private final CodingProblemRepository codingProblemRepository;
    private final TestCaseRepository testCaseRepository;
    private final ProblemAssignmentRepository problemAssignmentRepository;

    public CodingProblemService(CodingProblemRepository codingProblemRepository,
                                TestCaseRepository testCaseRepository,
                                ProblemAssignmentRepository problemAssignmentRepository) {
        this.codingProblemRepository = codingProblemRepository;
        this.testCaseRepository      = testCaseRepository;
        this.problemAssignmentRepository=problemAssignmentRepository;
    }

    // ── Trainer: create problem with test cases ───
    @Transactional
    public CodingProblemResponse createProblem(CodingProblemRequest request) {
        String trainerEmail = extractEmailFromJwt();

        CodingProblem problem = new CodingProblem();
        problem.setTitle(request.getTitle());
        problem.setDescription(request.getDescription());
        problem.setInputFormat(request.getInputFormat());
        problem.setOutputFormat(request.getOutputFormat());
        problem.setConstraints(request.getConstraints());
        problem.setSampleInput(request.getSampleInput());
        problem.setSampleOutput(request.getSampleOutput());
        problem.setDifficulty(request.getDifficulty());
        problem.setTotalMarks(request.getTotalMarks());
        problem.setTrainerEmail(trainerEmail);

        CodingProblem saved = codingProblemRepository.save(problem);

        // Save test cases if provided
        if (request.getTestCases() != null && !request.getTestCases().isEmpty()) {
            List<TestCase> testCases = request.getTestCases().stream()
                .map(tc -> buildTestCase(tc, saved))
                .collect(Collectors.toList());
            testCaseRepository.saveAll(testCases);
            saved.setTestCases(testCases);
        }

        log.info("Problem created: id={} by trainer={}", saved.getId(), trainerEmail);
        return toResponse(saved, false);
    }

    // ── Trainer: update problem ───────────────────
    @Transactional
    public CodingProblemResponse updateProblem(Long problemId, CodingProblemRequest request) {
        String trainerEmail = extractEmailFromJwt();

        CodingProblem problem = codingProblemRepository.findById(problemId)
            .orElseThrow(() -> new RuntimeException("Problem not found: " + problemId));

        if (!problem.getTrainerEmail().equals(trainerEmail)) {
            throw new RuntimeException("Access denied: you did not create this problem");
        }

        problem.setTitle(request.getTitle());
        problem.setDescription(request.getDescription());
        problem.setInputFormat(request.getInputFormat());
        problem.setOutputFormat(request.getOutputFormat());
        problem.setConstraints(request.getConstraints());
        problem.setSampleInput(request.getSampleInput());
        problem.setSampleOutput(request.getSampleOutput());
        problem.setDifficulty(request.getDifficulty());
        problem.setTotalMarks(request.getTotalMarks());

        CodingProblem updated = codingProblemRepository.save(problem);
        log.info("Problem updated: id={}", problemId);
        return toResponse(updated, false);
    }

    // ── Trainer: add test case to existing problem ─
    @Transactional
    public TestCaseResponse addTestCase(Long problemId, TestCaseRequest request) {
        CodingProblem problem = codingProblemRepository.findById(problemId)
            .orElseThrow(() -> new RuntimeException("Problem not found: " + problemId));

        TestCase testCase = buildTestCase(request, problem);
        TestCase saved = testCaseRepository.save(testCase);

        log.info("TestCase added: problemId={} testCaseId={}", problemId, saved.getId());
        return toTestCaseResponse(saved, false);
    }

    // ── Trainer: delete problem (soft delete) ─────
//    @Transactional
//    public void deleteProblem(Long problemId) {
//        CodingProblem problem = codingProblemRepository.findById(problemId)
//            .orElseThrow(() -> new RuntimeException("Problem not found: " + problemId));
//        problem.setIsActive(false);
//        codingProblemRepository.save(problem);
//        log.info("Problem soft-deleted: id={}", problemId);
//    }

 // In CodingProblemService.java — replace deleteProblem method:
//    @Transactional
//    public void deleteProblem(Long problemId) {
//        CodingProblem problem = codingProblemRepository.findById(problemId)
//            .orElseThrow(() -> new RuntimeException("Problem not found: " + problemId));
//        codingProblemRepository.delete(problem);  // hard delete
//        log.info("Problem hard-deleted: id={}", problemId);
//    }
    @Transactional
    public void deleteProblem(Long problemId) {
        CodingProblem problem = codingProblemRepository.findById(problemId)
            .orElseThrow(() -> new RuntimeException("Problem not found: " + problemId));

        // 1. Delete all assignments for this problem first
        problemAssignmentRepository.deleteByProblemId(problemId);

        // 2. Delete all test cases
        testCaseRepository.deleteAllByProblemId(problemId);

        // 3. Now safe to delete the problem
        codingProblemRepository.delete(problem);
        log.info("Problem hard-deleted: id={}", problemId);
    }
    
    // ── Trainer: get own problems ─────────────────
    @Transactional(readOnly = true)
    public List<CodingProblemResponse> getMyProblems() {
        String trainerEmail = extractEmailFromJwt();
        return codingProblemRepository.findByTrainerEmailOrderByCreatedAtDesc(trainerEmail)
            .stream()
            .map(p -> toResponse(p, false))
            .collect(Collectors.toList());
    }

    // ── Trainer: get single problem with all test cases ──
    @Transactional(readOnly = true)
    public CodingProblemResponse getProblemById(Long problemId) {
        CodingProblem problem = codingProblemRepository.findById(problemId)
            .orElseThrow(() -> new RuntimeException("Problem not found: " + problemId));
        return toResponse(problem, false); // trainer sees all test cases
    }

    // ── Student: get problem (hide hidden test cases) ─
    @Transactional(readOnly = true)
    public CodingProblemResponse getProblemForStudent(Long problemId) {
        CodingProblem problem = codingProblemRepository.findById(problemId)
            .orElseThrow(() -> new RuntimeException("Problem not found: " + problemId));
        return toResponse(problem, true); // strip hidden test cases
    }

    // ── Trainer: get test cases for a problem ─────
    @Transactional(readOnly = true)
    public List<TestCaseResponse> getTestCases(Long problemId) {
        return testCaseRepository.findByProblemIdOrderById(problemId)
            .stream()
            .map(tc -> toTestCaseResponse(tc, false))
            .collect(Collectors.toList());
    }

    // ── Private helpers ───────────────────────────
    private TestCase buildTestCase(TestCaseRequest request, CodingProblem problem) {
        TestCase tc = new TestCase();
        tc.setProblem(problem);
        tc.setInput(request.getInput());
        tc.setExpectedOutput(request.getExpectedOutput().trim());
        tc.setIsHidden(request.getIsHidden() != null && request.getIsHidden());
        tc.setMarks(request.getWeightage() != null ? request.getWeightage() : 1);
        return tc;
    }

    private CodingProblemResponse toResponse(CodingProblem problem, boolean studentView) {
        List<TestCaseResponse> visibleCases = problem.getTestCases().stream()
            .filter(tc -> !studentView || !tc.getIsHidden())
            .map(tc -> toTestCaseResponse(tc, studentView))
            .collect(Collectors.toList());

        return CodingProblemResponse.builder()
            .id(problem.getId())
            .title(problem.getTitle())
            .description(problem.getDescription())
            .inputFormat(problem.getInputFormat())
            .outputFormat(problem.getOutputFormat())
            .constraints(problem.getConstraints())
            .sampleInput(problem.getSampleInput())
            .sampleOutput(problem.getSampleOutput())
            .difficulty(problem.getDifficulty())
            .trainerEmail(problem.getTrainerEmail())
            .totalMarks(problem.getTotalMarks())
            .isActive(problem.getIsActive())
            .createdAt(problem.getCreatedAt())
            .updatedAt(problem.getUpdatedAt())
            .visibleTestCases(visibleCases)
            .build();
    }

    private TestCaseResponse toTestCaseResponse(TestCase tc, boolean studentView) {
        return TestCaseResponse.builder()
            .id(tc.getId())
            .input(tc.getIsHidden() && studentView ? null : tc.getInput())
            .expectedOutput(tc.getIsHidden() && studentView ? null : tc.getExpectedOutput())
            .isHidden(tc.getIsHidden())
            .weightage(tc.getMarks())
            .build();
    }

    private String extractEmailFromJwt() {
        return SecurityContextHolder.getContext()
            .getAuthentication()
            .getName();
    }
}