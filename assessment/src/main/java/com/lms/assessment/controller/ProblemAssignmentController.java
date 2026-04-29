package com.lms.assessment.controller;

import com.lms.assessment.dto.CodingProblemResponse;
import com.lms.assessment.dto.ProblemAssignmentRequest;
import com.lms.assessment.dto.ProblemAssignmentResponse;
import com.lms.assessment.dto.SubmissionJudgeResponse;
import com.lms.assessment.service.JudgeService;
import com.lms.assessment.service.ProblemAssignmentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/assignments")
public class ProblemAssignmentController {

    private final ProblemAssignmentService problemAssignmentService;
    private final JudgeService judgeService;

    public ProblemAssignmentController(ProblemAssignmentService problemAssignmentService,
                                       JudgeService judgeService) {
        this.problemAssignmentService = problemAssignmentService;
        this.judgeService             = judgeService;
    }

    // POST /api/v1/assignments
    // Trainer assigns a problem to a batch
    @PostMapping
    @PreAuthorize("hasAnyRole('TRAINER','ADMIN')")
    public ResponseEntity<ProblemAssignmentResponse> assignProblem(
            @Valid @RequestBody ProblemAssignmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(problemAssignmentService.assignProblem(request));
    }

    // DELETE /api/v1/assignments/{assignmentId}
    // Trainer removes a problem from a batch
    @DeleteMapping("/{assignmentId}")
    @PreAuthorize("hasAnyRole('TRAINER','ADMIN')")
    public ResponseEntity<Void> unassignProblem(@PathVariable Long assignmentId) {
        problemAssignmentService.unassignProblem(assignmentId);
        return ResponseEntity.noContent().build();
    }

    // GET /api/v1/assignments/batch/{batchId}
    // Trainer sees all assigned problems for a batch
    @GetMapping("/batch/{batchId}")
    @PreAuthorize("hasAnyRole('TRAINER','ADMIN')")
    public ResponseEntity<List<ProblemAssignmentResponse>> getAssignmentsByBatch(
            @PathVariable String batchId) {
        return ResponseEntity.ok(problemAssignmentService.getAssignmentsByBatch(batchId));
    }

    // GET /api/v1/assignments/student/problems?batchId=...
    // Student sees all active problems assigned to their batch
    @GetMapping("/student/problems")
    public ResponseEntity<List<CodingProblemResponse>> getProblemsForStudent(
            @RequestParam @NotBlank String batchId) {
        return ResponseEntity.ok(problemAssignmentService.getProblemsForStudent(batchId));
    }

    // GET /api/v1/assignments/student/problems/{problemId}?batchId=...
    // Student opens a specific problem (hidden test cases masked)
    @GetMapping("/student/problems/{problemId}")
    public ResponseEntity<CodingProblemResponse> getProblemForStudent(
            @PathVariable Long problemId) {
        return ResponseEntity.ok(
            problemAssignmentService.getProblemForStudentById(problemId)
        );
    }
    
    // POST /api/v1/assignments/student/problems/{problemId}/submit
    // Student submits code → judge runs all test cases → score returned
    @PostMapping("/student/problems/{problemId}/submit")
    public ResponseEntity<SubmissionJudgeResponse> submitCode(
            @PathVariable Long problemId,
            @RequestParam @NotBlank String batchId,
            @RequestParam @NotBlank String language,
            @RequestBody @NotBlank String code) {
        return ResponseEntity.ok(judgeService.judge(problemId, batchId, language, code));
    }
}