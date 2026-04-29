//package com.lms.assessment.service;
//
//import com.lms.assessment.dto.CodeExecutionRequest;
//import com.lms.assessment.dto.CodeExecutionResponse;
//import com.lms.assessment.model.CodeSubmission;
//import com.lms.assessment.repository.CodeSubmissionRepository;
//import com.lms.assessment.service.CodeExecutionService.ExecutionResult;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//public class CodeSubmissionService {
//
//    private static final Logger log = LoggerFactory.getLogger(CodeSubmissionService.class);
//
//    private final CodeExecutionService codeExecutionService;
//    private final CodeSubmissionRepository codeSubmissionRepository;
//
//    public CodeSubmissionService(CodeExecutionService codeExecutionService,
//                                 CodeSubmissionRepository codeSubmissionRepository) {
//        this.codeExecutionService       = codeExecutionService;
//        this.codeSubmissionRepository   = codeSubmissionRepository;
//    }
//
//    @Transactional
//    public CodeExecutionResponse executeAndSubmit(CodeExecutionRequest request) {
//        String studentEmail = extractEmailFromJwt();
//        log.info("Code submission: student={} batch={} lang={}",
//            studentEmail, request.getBatchId(), request.getLanguage());
//
//        ExecutionResult result = codeExecutionService.execute(
//            request.getLanguage(), request.getCode()
//        );
//
//        CodeSubmission submission = CodeSubmission.builder()
//            .studentEmail(studentEmail)
//            .batchId(request.getBatchId())
//            .language(request.getLanguage().toUpperCase())
//            .code(request.getCode())
//            .output(result.getOutput())
//            .status(result.getStatus())
//            .executionTimeMs(result.getElapsedMs())
//            .build();
//
//        CodeSubmission saved = codeSubmissionRepository.save(submission);
//        log.info("Submission saved: id={} status={}", saved.getId(), saved.getStatus());
//
//        return toResponse(saved);
//    }
//
//    @Transactional(readOnly = true)
//    public List<CodeExecutionResponse> getMySubmissions(String batchId) {
//        String studentEmail = extractEmailFromJwt();
//        return codeSubmissionRepository
//            .findByBatchIdAndStudentEmailOrderByCreatedAtDesc(batchId, studentEmail)
//            .stream()
//            .map(this::toResponse)
//            .collect(Collectors.toList());
//    }
//
//    @Transactional(readOnly = true)
//    public List<CodeExecutionResponse> getBatchSubmissions(String batchId) {
//        return codeSubmissionRepository
//            .findByBatchIdOrderByCreatedAtDesc(batchId)
//            .stream()
//            .map(this::toResponse)
//            .collect(Collectors.toList());
//    }
//
//    private String extractEmailFromJwt() {
//        return SecurityContextHolder.getContext()
//            .getAuthentication()
//            .getName();
//    }
//
//    private CodeExecutionResponse toResponse(CodeSubmission s) {
//        return CodeExecutionResponse.builder()
//            .submissionId(s.getId())
//            .language(s.getLanguage())
//            .output(s.getOutput())
//            .status(s.getStatus())
//            .executionTimeMs(s.getExecutionTimeMs())
//            .timestamp(s.getCreatedAt())
//            .studentEmail(s.getStudentEmail())
//            .batchId(s.getBatchId())
//            .build();
//    }
//}





package com.lms.assessment.service;

import com.lms.assessment.dto.CodeExecutionRequest;
import com.lms.assessment.dto.CodeExecutionResponse;
import com.lms.assessment.kafka.AssessmentEventProducer;
import com.lms.assessment.model.CodeSubmission;
import com.lms.assessment.repository.CodeSubmissionRepository;
import com.lms.assessment.service.CodeExecutionService.ExecutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CodeSubmissionService {

    private static final Logger log = LoggerFactory.getLogger(CodeSubmissionService.class);

    private final CodeExecutionService codeExecutionService;
    private final CodeSubmissionRepository codeSubmissionRepository;
    private final AssessmentEventProducer   eventProducer;

    public CodeSubmissionService(CodeExecutionService codeExecutionService,
                                 CodeSubmissionRepository codeSubmissionRepository,
                                 AssessmentEventProducer  eventProducer) {
        this.codeExecutionService     = codeExecutionService;
        this.codeSubmissionRepository = codeSubmissionRepository;
        this.eventProducer            = eventProducer; 
    }

    @Transactional
    public CodeExecutionResponse executeAndSubmit(CodeExecutionRequest request) {
        String studentEmail = extractEmailFromJwt();
        log.info("Code submission: student={} batch={} lang={}",
            studentEmail, request.getBatchId(), request.getLanguage());

        // ← CHANGED: added request.getSampleInput() as third argument
        ExecutionResult result = codeExecutionService.execute(
            request.getLanguage(), request.getCode(), request.getSampleInput()
        );

        CodeSubmission submission = CodeSubmission.builder()
            .studentEmail(studentEmail)
            .batchId(request.getBatchId())
            .language(request.getLanguage().toUpperCase())
            .code(request.getCode())
            .output(result.getOutput())
            .status(result.getStatus())
            .executionTimeMs(result.getElapsedMs())
            .build();

        CodeSubmission saved = codeSubmissionRepository.save(submission);
        log.info("Submission saved: id={} status={}", saved.getId(), saved.getStatus());
        eventProducer.publishCodeSubmitted(
                saved.getId(),
                studentEmail,
                request.getBatchId(),
                result.getStatus()
            );
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<CodeExecutionResponse> getMySubmissions(String batchId) {
        String studentEmail = extractEmailFromJwt();
        return codeSubmissionRepository
            .findByBatchIdAndStudentEmailOrderByCreatedAtDesc(batchId, studentEmail)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CodeExecutionResponse> getBatchSubmissions(String batchId) {
        return codeSubmissionRepository
            .findByBatchIdOrderByCreatedAtDesc(batchId)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    private String extractEmailFromJwt() {
        return SecurityContextHolder.getContext()
            .getAuthentication()
            .getName();
    }

    private CodeExecutionResponse toResponse(CodeSubmission s) {
        return CodeExecutionResponse.builder()
            .submissionId(s.getId())
            .language(s.getLanguage())
            .output(s.getOutput())
            .status(s.getStatus())
            .executionTimeMs(s.getExecutionTimeMs())
            .timestamp(s.getCreatedAt())
            .studentEmail(s.getStudentEmail())
            .batchId(s.getBatchId())
            .build();
    }
}