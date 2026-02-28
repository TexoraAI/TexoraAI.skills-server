//package com.lms.assessment.service;
//
//import com.lms.assessment.dto.SubmissionResponse;
//import com.lms.assessment.model.Assignment;
//import com.lms.assessment.model.AssignmentSubmission;
//import com.lms.assessment.repository.AssignmentRepository;
//import com.lms.assessment.repository.AssignmentSubmissionRepository;
//import org.springframework.security.core.Authentication;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//public class AssignmentSubmissionService {
//
//    private final AssignmentSubmissionRepository submissionRepository;
//    private final AssignmentRepository assignmentRepository;
//    private final FileStorageService fileStorageService;
//
//    public AssignmentSubmissionService(
//            AssignmentSubmissionRepository submissionRepository,
//            AssignmentRepository assignmentRepository,
//            FileStorageService fileStorageService) {
//
//        this.submissionRepository = submissionRepository;
//        this.assignmentRepository = assignmentRepository;
//        this.fileStorageService = fileStorageService;
//    }
// // 🔵 Student submit
//    public SubmissionResponse submit(Long assignmentId,
//                                     MultipartFile file,
//                                     Authentication authentication) throws IOException {
//
//        String studentEmail = authentication.getName();  // ✅ FIXED
//
//        // Check if already submitted
//        if (submissionRepository
//                .findByAssignmentIdAndStudentEmail(assignmentId, studentEmail)
//                .isPresent()) {
//
//            throw new RuntimeException("You have already submitted this assignment.");
//        }
//
//        Assignment assignment = assignmentRepository.findById(assignmentId)
//                .orElseThrow(() -> new RuntimeException("Assignment not found"));
//
//        String filePath = fileStorageService.saveFile(file, "submissions");
//
//        AssignmentSubmission submission = new AssignmentSubmission();
//        submission.setAssignmentId(assignmentId);
//        submission.setStudentEmail(studentEmail);  // ✅ FIXED
//        submission.setFileName(file.getOriginalFilename());
//        submission.setFilePath(filePath);
//        submission.setSubmittedAt(LocalDateTime.now());
//
//        if (LocalDateTime.now().isAfter(assignment.getDeadline())) {
//            submission.setStatus("LATE");
//        } else {
//            submission.setStatus("SUBMITTED");
//        }
//
//        AssignmentSubmission saved = submissionRepository.save(submission);
//
//        return mapToResponse(saved);
//    }
//
//
//    // 🔵 Trainer view submissions
//    public List<SubmissionResponse> getByAssignment(Long assignmentId) {
//        return submissionRepository.findByAssignmentId(assignmentId)
//                .stream()
//                .map(this::mapToResponse)
//                .collect(Collectors.toList());
//    }
//
//    private SubmissionResponse mapToResponse(AssignmentSubmission sub) {
//        return new SubmissionResponse(
//                sub.getId(),
//                sub.getFileName(),
//                sub.getStatus(),
//                sub.getObtainedMarks(),
//                sub.getSubmittedAt(),
//                "/api/submissions/download?path=" + sub.getFilePath()
//        );
//    }
//}



package com.lms.assessment.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.lms.assessment.dto.SubmissionResponse;
import com.lms.assessment.model.Assignment;
import com.lms.assessment.model.AssignmentSubmission;
import com.lms.assessment.repository.AssignmentRepository;
import com.lms.assessment.repository.AssignmentSubmissionRepository;

@Service
public class AssignmentSubmissionService {

    private final AssignmentSubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final FileStorageService fileStorageService;

    public AssignmentSubmissionService(
            AssignmentSubmissionRepository submissionRepository,
            AssignmentRepository assignmentRepository,
            FileStorageService fileStorageService) {

        this.submissionRepository = submissionRepository;
        this.assignmentRepository = assignmentRepository;
        this.fileStorageService = fileStorageService;
    }

    // 🔵 ================= STUDENT SUBMIT =================
    public SubmissionResponse submit(Long assignmentId,
                                     MultipartFile file,
                                     Authentication authentication) throws IOException {

        // ✅ Get logged-in student email from JWT
        String studentEmail = authentication.getName();

        // ✅ Check if already submitted
        if (submissionRepository
                .findByAssignmentIdAndStudentEmail(assignmentId, studentEmail)
                .isPresent()) {

            throw new RuntimeException("You have already submitted this assignment.");
        }

        // ✅ Validate assignment exists
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        // ✅ Store file
        String filePath = fileStorageService.saveFile(file, "submissions");

        // ✅ Create submission
        AssignmentSubmission submission = new AssignmentSubmission();
        submission.setAssignmentId(assignmentId);
        submission.setStudentEmail(studentEmail);
        submission.setFileName(file.getOriginalFilename());
        submission.setFilePath(filePath);
        submission.setSubmittedAt(LocalDateTime.now());

        // ✅ Deadline check
        if (LocalDateTime.now().isAfter(assignment.getDeadline())) {
            submission.setStatus("LATE");
        } else {
            submission.setStatus("SUBMITTED");
        }

        AssignmentSubmission saved = submissionRepository.save(submission);

        return mapToResponse(saved);
    }

    // 🔵 ================= TRAINER VIEW =================
    public List<SubmissionResponse> getByAssignment(Long assignmentId) {
        return submissionRepository.findByAssignmentId(assignmentId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

 // 🔵 Trainer evaluate submission (give marks)
    public SubmissionResponse evaluateSubmission(Long submissionId, Integer marks) {

        AssignmentSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));

        submission.setObtainedMarks(marks);
        submission.setStatus("EVALUATED");

        AssignmentSubmission saved = submissionRepository.save(submission);

        return mapToResponse(saved);
    }

    
 // 🔵 STUDENT GET HIS SUBMISSIONS (WITH MARKS)
    public List<SubmissionResponse> getMySubmissions(Authentication authentication) {

        String studentEmail = authentication.getName();

        return submissionRepository.findByStudentEmail(studentEmail)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    
    
    // 🔵 ================= RESPONSE MAPPER =================
    private SubmissionResponse mapToResponse(AssignmentSubmission sub) {
        return new SubmissionResponse(
                sub.getId(),
                sub.getAssignmentId(),
                sub.getStudentEmail(),   // ✅ 2nd param
                sub.getFileName(),       // ✅ 3rd param
                sub.getStatus(),         // ✅ 4th param
                sub.getObtainedMarks(),
                sub.getSubmittedAt(),
                "/api/submissions/download?path=" + sub.getFilePath()
        );
    }

}
