package com.lms.assessment.service;

import com.lms.assessment.dto.AssignmentResponse;
import com.lms.assessment.dto.CreateAssignmentRequest;
import com.lms.assessment.model.Assignment;
import com.lms.assessment.model.StudentAssignmentMap;
import com.lms.assessment.repository.AssignmentRepository;
import com.lms.assessment.repository.StudentAssignmentMapRepository;
import com.lms.assessment.repository.StudentBatchMapRepository;
import com.lms.assessment.repository.StudentTrainerMapRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AssignmentService {

	
    private final AssignmentRepository repository;
    private final StudentTrainerMapRepository studentTrainerMapRepository;
	private final StudentAssignmentMapRepository studentAssignmentMapRepository;
	private final StudentBatchMapRepository studentBatchMapRepo;
    public AssignmentService(AssignmentRepository repository,StudentTrainerMapRepository studentTrainerMapRepository,StudentAssignmentMapRepository studentAssignmentMapRepository,StudentBatchMapRepository studentBatchMapRepo) {
    	
    	
        this.repository = repository;
        this.studentTrainerMapRepository=studentTrainerMapRepository;
        this.studentAssignmentMapRepository=studentAssignmentMapRepository;
        this.studentBatchMapRepo=studentBatchMapRepo;
    }

//    // ================= CREATE =================
//
//    public AssignmentResponse createAssignment(
//            CreateAssignmentRequest request,
//            String trainerEmail) {
//
//        Assignment assignment = new Assignment();
//
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
//        return mapToResponse(saved);
//    }

    public AssignmentResponse createAssignment(
            CreateAssignmentRequest request,
            String trainerEmail) {

        Assignment assignment = new Assignment();

        assignment.setTitle(request.getTitle());
        assignment.setDescription(request.getDescription());
        assignment.setBatchId(request.getBatchId());
        assignment.setDeadline(request.getDeadline());
        assignment.setMaxMarks(request.getMaxMarks());
        assignment.setDuration(request.getDuration());
        assignment.setTrainerEmail(trainerEmail);
        assignment.setCreatedAt(LocalDateTime.now());

        Assignment saved = repository.save(assignment);

        // 🔥 ASSIGN TO TRAINER STUDENTS (IMPORTANT)
        assignAssignmentToStudents(
                saved.getId(),
                trainerEmail,
                saved.getBatchId()
        );

        return mapToResponse(saved);
    }
    private void assignAssignmentToStudents(
            Long assignmentId,
            String trainerEmail,
            Long batchId) {

        List<String> students =
                studentTrainerMapRepository
                        .findActiveStudentsByTrainerAndBatch(
                                trainerEmail,
                                batchId
                        );

        for (String student : students) {
            StudentAssignmentMap map =
                    new StudentAssignmentMap(
                            assignmentId,
                            student
                    );

            studentAssignmentMapRepository.save(map);
        }
    }
    // ================= GET BY BATCH =================
//
//    public List<AssignmentResponse> getStudentAssignments(String email) {
//        return repository.findAssignmentsForStudent(email)
//                .stream()
//                .map(this::mapToResponse)
//                .toList();
//    }

    
    public List<AssignmentResponse> getStudentAssignments(String studentEmail) {

        // 1️⃣ Get student's batch
        Long batchId = studentBatchMapRepo
                .findBatchIdByStudentEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("Student batch not found"));

        // 2️⃣ Get assignments for that batch
        return repository.findByBatchId(batchId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    // ================= GET BY TRAINER =================

    public List<AssignmentResponse> getAssignmentsByTrainer(String trainerEmail) {

        return repository.findByTrainerEmail(trainerEmail)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ================= UPDATE =================

    public AssignmentResponse updateAssignment(
            Long id,
            CreateAssignmentRequest request,
            String trainerEmail) {

        Assignment assignment = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Assignment not found"));

        // 🔒 Ownership Check
        if (!assignment.getTrainerEmail().equals(trainerEmail)) {
            throw new RuntimeException(
                    "Unauthorized: You cannot edit this assignment");
        }

        assignment.setTitle(request.getTitle());
        assignment.setDescription(request.getDescription());
        assignment.setBatchId(request.getBatchId());
        assignment.setDeadline(request.getDeadline());
        assignment.setMaxMarks(request.getMaxMarks());
        assignment.setDuration(request.getDuration());

        Assignment updated = repository.save(assignment);

        return mapToResponse(updated);
    }

    // ================= DELETE =================

    public void deleteAssignment(Long id, String trainerEmail) {

        Assignment assignment = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Assignment not found"));

        // 🔒 Ownership Check
        if (!assignment.getTrainerEmail().equals(trainerEmail)) {
            throw new RuntimeException(
                    "Unauthorized: You cannot delete this assignment");
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
}
