package com.lms.assessment.controller;

import com.lms.assessment.dto.CodingProblemRequest;
import com.lms.assessment.dto.CodingProblemResponse;
import com.lms.assessment.dto.TestCaseRequest;
import com.lms.assessment.dto.TestCaseResponse;
import com.lms.assessment.service.CodingProblemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/problems")
public class CodingProblemController {

    private final CodingProblemService codingProblemService;

    public CodingProblemController(CodingProblemService codingProblemService) {
        this.codingProblemService = codingProblemService;
    }

    // POST /api/v1/problems
    @PostMapping
    @PreAuthorize("hasAnyRole('TRAINER','ADMIN')")
    public ResponseEntity<CodingProblemResponse> createProblem(
            @Valid @RequestBody CodingProblemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(codingProblemService.createProblem(request));
    }

    // PUT /api/v1/problems/{problemId}
    @PutMapping("/{problemId}")
    @PreAuthorize("hasAnyRole('TRAINER','ADMIN')")
    public ResponseEntity<CodingProblemResponse> updateProblem(
            @PathVariable Long problemId,
            @Valid @RequestBody CodingProblemRequest request) {
        return ResponseEntity.ok(codingProblemService.updateProblem(problemId, request));
    }

    // DELETE /api/v1/problems/{problemId}
    @DeleteMapping("/{problemId}")
    @PreAuthorize("hasAnyRole('TRAINER','ADMIN')")
    public ResponseEntity<Void> deleteProblem(@PathVariable Long problemId) {
        codingProblemService.deleteProblem(problemId);
        return ResponseEntity.noContent().build();
    }

    // GET /api/v1/problems/my  (trainer sees own problems)
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('TRAINER','ADMIN')")
    public ResponseEntity<List<CodingProblemResponse>> getMyProblems() {
        return ResponseEntity.ok(codingProblemService.getMyProblems());
    }

    // GET /api/v1/problems/{problemId}  (trainer full view)
    @GetMapping("/{problemId}")
    @PreAuthorize("hasAnyRole('TRAINER','ADMIN')")
    public ResponseEntity<CodingProblemResponse> getProblemById(
            @PathVariable Long problemId) {
        return ResponseEntity.ok(codingProblemService.getProblemById(problemId));
    }

    // POST /api/v1/problems/{problemId}/testcases
    @PostMapping("/{problemId}/testcases")
    @PreAuthorize("hasAnyRole('TRAINER','ADMIN')")
    public ResponseEntity<TestCaseResponse> addTestCase(
            @PathVariable Long problemId,
            @Valid @RequestBody TestCaseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(codingProblemService.addTestCase(problemId, request));
    }

    // GET /api/v1/problems/{problemId}/testcases
    @GetMapping("/{problemId}/testcases")
    @PreAuthorize("hasAnyRole('TRAINER','ADMIN')")
    public ResponseEntity<List<TestCaseResponse>> getTestCases(
            @PathVariable Long problemId) {
        return ResponseEntity.ok(codingProblemService.getTestCases(problemId));
    }
}