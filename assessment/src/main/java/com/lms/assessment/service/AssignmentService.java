//
//package com.lms.assessment.service;
//
//import com.lms.assessment.dto.AssignmentResponse;
//import com.lms.assessment.dto.CreateAssignmentRequest;
//import com.lms.assessment.kafka.AssessmentEventProducer;
//import com.lms.assessment.model.Assignment;
//import com.lms.assessment.model.StudentAssignmentMap;
//import com.lms.assessment.model.StudentBatchMap;
//import com.lms.assessment.repository.AssignmentRepository;
//import com.lms.assessment.repository.StudentAssignmentMapRepository;
//import com.lms.assessment.repository.StudentBatchMapRepository;
//import com.lms.assessment.repository.StudentTrainerMapRepository;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//public class AssignmentService {
//
//    private final AssignmentRepository            repository;
//    private final StudentTrainerMapRepository     studentTrainerMapRepository;
//    private final StudentAssignmentMapRepository  studentAssignmentMapRepository;
//    private final StudentBatchMapRepository       studentBatchMapRepo;
//    private final AssessmentEventProducer         producer;               // ← added
//
//    public AssignmentService(
//            AssignmentRepository           repository,
//            StudentTrainerMapRepository    studentTrainerMapRepository,
//            StudentAssignmentMapRepository studentAssignmentMapRepository,
//            StudentBatchMapRepository      studentBatchMapRepo,
//            AssessmentEventProducer        producer) {                    // ← added
//
//        this.repository                   = repository;
//        this.studentTrainerMapRepository  = studentTrainerMapRepository;
//        this.studentAssignmentMapRepository = studentAssignmentMapRepository;
//        this.studentBatchMapRepo          = studentBatchMapRepo;
//        this.producer                     = producer;                     // ← added
//    }
//
//    // ================= CREATE =================
//
//    public AssignmentResponse createAssignment(
//            CreateAssignmentRequest request,
//            String trainerEmail) {
//
//        Assignment assignment = new Assignment();
//        assignment.setTitle(request.getTitle());
//        assignment.setDescription(request.getDescription());
//        assignment.setBatchId(request.getBatchId());
//        assignment.setDeadline(request.getDeadline());
//        assignment.setMaxMarks(request.getMaxMarks());
//        assignment.setDuration(request.getDuration());
//        assignment.setTrainerEmail(trainerEmail);
//        assignment.setCreatedAt(LocalDateTime.now());
//
//        Assignment saved = repository.save(assignment);
//
//        // assign to students in this batch
//        assignAssignmentToStudents(
//                saved.getId(),
//                trainerEmail,
//                saved.getBatchId()
//        );
//
//        // publish Kafka event so notification-service can notify students
//        try {
//            producer.publishAssignmentCreated(
//                    saved.getId(),
//                    saved.getTitle(),
//                    saved.getBatchId(),
//                    saved.getTrainerEmail()
//            );
//        } catch (Exception e) {
//            System.out.println("Kafka unavailable, skipping ASSIGNMENT_CREATED event");
//        }
//
//        return mapToResponse(saved);
//    }
//
//    // ================= ASSIGN TO STUDENTS (private) =================
//
//    private void assignAssignmentToStudents(Long assignmentId,
//                                             String trainerEmail,
//                                             Long batchId) {
//
//        List<String> students = studentTrainerMapRepository
//                .findActiveStudentsByTrainerAndBatch(trainerEmail, batchId);
//
//        for (String student : students) {
//            studentAssignmentMapRepository.save(
//                    new StudentAssignmentMap(assignmentId, student)
//            );
//        }
//    }
//
//    // ================= GET BY STUDENT =================
//
//
//
//    public List<AssignmentResponse> getStudentAssignments(String studentEmail) {
//
//        List<StudentBatchMap> list =
//                studentBatchMapRepo.findAllByStudentEmail(studentEmail);
//
//        if (list.isEmpty()) {
//            System.out.println("❌ No batch found for email: " + studentEmail);
//            return List.of();
//        }
//
//        Long batchId = list.get(0).getBatchId();
//
//        return repository.findByBatchId(batchId)
//                .stream()
//                .map(this::mapToResponse)
//                .toList();
//    }
//    
//    // ================= GET BY TRAINER =================
//
//    public List<AssignmentResponse> getAssignmentsByTrainer(String trainerEmail) {
//        return repository.findByTrainerEmail(trainerEmail)
//                .stream()
//                .map(this::mapToResponse)
//                .collect(Collectors.toList());
//    }
//
//    // ================= UPDATE =================
//
//    public AssignmentResponse updateAssignment(Long id,
//                                                CreateAssignmentRequest request,
//                                                String trainerEmail) {
//
//        Assignment assignment = repository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Assignment not found"));
//
//        if (!assignment.getTrainerEmail().equals(trainerEmail)) {
//            throw new RuntimeException("Unauthorized: You cannot edit this assignment");
//        }
//
//        assignment.setTitle(request.getTitle());
//        assignment.setDescription(request.getDescription());
//        assignment.setBatchId(request.getBatchId());
//        assignment.setDeadline(request.getDeadline());
//        assignment.setMaxMarks(request.getMaxMarks());
//        assignment.setDuration(request.getDuration());
//
//        return mapToResponse(repository.save(assignment));
//    }
//
//    // ================= DELETE =================
//
//    public void deleteAssignment(Long id, String trainerEmail) {
//
//        Assignment assignment = repository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Assignment not found"));
//
//        if (!assignment.getTrainerEmail().equals(trainerEmail)) {
//            throw new RuntimeException("Unauthorized: You cannot delete this assignment");
//        }
//
//        repository.delete(assignment);
//    }
//
//    // ================= MAPPER =================
//
//    private AssignmentResponse mapToResponse(Assignment assignment) {
//        return new AssignmentResponse(
//                assignment.getId(),
//                assignment.getTitle(),
//                assignment.getDescription(),
//                assignment.getBatchId(),
//                assignment.getDeadline(),
//                assignment.getMaxMarks(),
//                assignment.getDuration(),
//                assignment.getCreatedAt()
//        );
//    }
//}

package com.lms.assessment.service;

import com.lms.assessment.dto.AssignmentResponse;
import com.lms.assessment.dto.CreateAssignmentRequest;
import com.lms.assessment.kafka.AssessmentEventProducer;
import com.lms.assessment.model.Assignment;
import com.lms.assessment.model.StudentAssignmentMap;
import com.lms.assessment.model.StudentBatchMap;
import com.lms.assessment.repository.AssignmentRepository;
import com.lms.assessment.repository.AssignmentSubmissionRepository;
import com.lms.assessment.repository.StudentAssignmentMapRepository;
import com.lms.assessment.repository.StudentBatchMapRepository;
import com.lms.assessment.repository.StudentTrainerMapRepository;
import org.springframework.stereotype.Service;
import com.lms.assessment.dto.AssignmentAdminReportResponse;
import com.lms.assessment.repository.AssignmentSubmissionRepository;
import java.util.Comparator;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AssignmentService {

    private final AssignmentRepository            repository;
    private final StudentTrainerMapRepository     studentTrainerMapRepository;
    private final StudentAssignmentMapRepository  studentAssignmentMapRepository;
    private final StudentBatchMapRepository       studentBatchMapRepo;
    private final AssessmentEventProducer         producer;
    private final AssignmentSubmissionRepository assignmentSubmissionRepository;
    public AssignmentService(
            AssignmentRepository           repository,
            StudentTrainerMapRepository    studentTrainerMapRepository,
            StudentAssignmentMapRepository studentAssignmentMapRepository,
            StudentBatchMapRepository      studentBatchMapRepo,
            AssessmentEventProducer        producer,
            AssignmentSubmissionRepository assignmentSubmissionRepository) {

        this.repository                   = repository;
        this.studentTrainerMapRepository  = studentTrainerMapRepository;
        this.studentAssignmentMapRepository = studentAssignmentMapRepository;
        this.studentBatchMapRepo          = studentBatchMapRepo;
        this.producer                     = producer;
        this.assignmentSubmissionRepository = assignmentSubmissionRepository;
    }

    // ================= CREATE =================

    public AssignmentResponse createAssignment(
            CreateAssignmentRequest request,
            String trainerEmail,
            String organizationId) {

        Assignment assignment = new Assignment();
        assignment.setTitle(request.getTitle());
        assignment.setDescription(request.getDescription());
        assignment.setBatchId(request.getBatchId());
        assignment.setDeadline(request.getDeadline());
        assignment.setMaxMarks(request.getMaxMarks());
        assignment.setDuration(request.getDuration());
        assignment.setTrainerEmail(trainerEmail);
        assignment.setCreatedAt(LocalDateTime.now());
        assignment.setOrganizationId(organizationId); // null for standalone trainers — expected

        Assignment saved = repository.save(assignment);

        assignAssignmentToStudents(
                saved.getId(),
                trainerEmail,
                saved.getBatchId()
        );

        try {
            producer.publishAssignmentCreated(
                    saved.getId(),
                    saved.getTitle(),
                    saved.getBatchId(),
                    saved.getTrainerEmail()
            );
        } catch (Exception e) {
            System.out.println("Kafka unavailable, skipping ASSIGNMENT_CREATED event");
        }

        return mapToResponse(saved);
    }

    // ================= ASSIGN TO STUDENTS (private) =================

    private void assignAssignmentToStudents(Long assignmentId,
                                             String trainerEmail,
                                             Long batchId) {

        List<String> students = studentTrainerMapRepository
                .findActiveStudentsByTrainerAndBatch(trainerEmail, batchId);

        for (String student : students) {
            studentAssignmentMapRepository.save(
                    new StudentAssignmentMap(assignmentId, student)
            );
        }
    }

    // ================= GET BY STUDENT =================

    public List<AssignmentResponse> getStudentAssignments(String studentEmail, String organizationId) {

        List<StudentBatchMap> list =
                studentBatchMapRepo.findAllByStudentEmail(studentEmail);

        if (list.isEmpty()) {
            System.out.println("❌ No batch found for email: " + studentEmail);
            return List.of();
        }

        Long batchId = list.get(0).getBatchId();

        List<Assignment> assignments = repository.findByBatchId(batchId);

        if (organizationId != null) {
            assignments = assignments.stream()
                    .filter(a -> organizationId.equals(a.getOrganizationId()))
                    .collect(Collectors.toList());
        }

        return assignments.stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ================= GET BY TRAINER =================

    public List<AssignmentResponse> getAssignmentsByTrainer(String trainerEmail, String organizationId) {
        List<Assignment> assignments = organizationId == null
                ? repository.findByTrainerEmail(trainerEmail)
                : repository.findByOrganizationIdAndTrainerEmail(organizationId, trainerEmail);

        return assignments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ================= UPDATE =================

    public AssignmentResponse updateAssignment(Long id,
                                                CreateAssignmentRequest request,
                                                String trainerEmail,
                                                String organizationId) {

        Assignment assignment = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        assertSameOrg(assignment.getOrganizationId(), organizationId);

        if (!assignment.getTrainerEmail().equals(trainerEmail)) {
            throw new RuntimeException("Unauthorized: You cannot edit this assignment");
        }

        assignment.setTitle(request.getTitle());
        assignment.setDescription(request.getDescription());
        assignment.setBatchId(request.getBatchId());
        assignment.setDeadline(request.getDeadline());
        assignment.setMaxMarks(request.getMaxMarks());
        assignment.setDuration(request.getDuration());

        return mapToResponse(repository.save(assignment));
    }

    // ================= DELETE =================

    public void deleteAssignment(Long id, String trainerEmail, String organizationId) {

        Assignment assignment = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        assertSameOrg(assignment.getOrganizationId(), organizationId);

        if (!assignment.getTrainerEmail().equals(trainerEmail)) {
            throw new RuntimeException("Unauthorized: You cannot delete this assignment");
        }

        repository.delete(assignment);
    }

    // ================= MAPPER =================

    private AssignmentResponse mapToResponse(Assignment assignment) {
        return new AssignmentResponse(
                assignment.getId(),
                assignment.getTitle(),
                assignment.getDescription(),
                assignment.getBatchId(),
                assignment.getDeadline(),
                assignment.getMaxMarks(),
                assignment.getDuration(),
                assignment.getCreatedAt()
        );
    }

    // ================= ORG GUARD (private) =================

    private void assertSameOrg(String resourceOrgId, String callerOrgId) {
        if (callerOrgId == null) return;
        if (!callerOrgId.equals(resourceOrgId)) {
            throw new RuntimeException("Access denied: assignment belongs to a different organization");
        }
    }
    public List<AssignmentAdminReportResponse> getAdminReport(String organizationId) {
        List<Assignment> assignments = repository.findByOrganizationId(organizationId);
        return assignments.stream()
                .sorted(Comparator.comparing(Assignment::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toAdminReport)
                .collect(Collectors.toList());
    }

    public List<AssignmentAdminReportResponse> getSuperAdminReport() {
        List<Assignment> assignments = repository.findByOrganizationIdIsNull();
        return assignments.stream()
                .sorted(Comparator.comparing(Assignment::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toAdminReport)
                .collect(Collectors.toList());
    }

    private AssignmentAdminReportResponse toAdminReport(Assignment assignment) {
        long submissionCount = assignmentSubmissionRepository.countByAssignmentId(assignment.getId());

        return new AssignmentAdminReportResponse(
                assignment.getId(),
                assignment.getTitle(),
                assignment.getTrainerEmail(),
                assignment.getBatchId(),
                assignment.getDeadline(),
                assignment.getMaxMarks(),
                assignment.getCreatedAt(),
                submissionCount,
                assignment.getOrganizationId() != null ? assignment.getOrganizationId() : "Standalone"
        );
    }
}