//package com.lms.batch.controller;
//
//import com.lms.batch.dto.*;
//import com.lms.batch.security.JwtUtil;
//import com.lms.batch.service.BatchService;
//import jakarta.servlet.http.HttpServletRequest;
//
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import java.util.*;
//import com.lms.batch.dto.TrainerDTO;
//
//@RestController
//@RequestMapping("/api/batch")
//public class BatchController {
//
//    private final BatchService service;
//    private final JwtUtil jwtUtil;
//
//    public BatchController(BatchService service, JwtUtil jwtUtil) {
//        this.service = service;
//        this.jwtUtil = jwtUtil;
//    }
//
//    /* ADMIN */
//
//    @PostMapping("/admin/batches")
//    public BatchResponseDTO create(@RequestBody CreateBatchRequest req) {
//        return service.createBatch(req);
//    }
//    
//    @DeleteMapping("/admin/batches/{batchId}")
//    public void deleteBatch(@PathVariable Long batchId) {
//        service.deleteBatch(batchId);
//    }
//
//    
////    @GetMapping("/admin/batches")
////    public List<BatchResponseDTO> getAllBatches() {
////        return service.getAllBatches();
////    }
//    @GetMapping("/admin/batches")
//    public List<BatchResponseDTO> getAllBatches(HttpServletRequest request) {
//        String orgId = jwtUtil.extractOrganizationId(
//                request.getHeader("Authorization").substring(7));
//        return service.getAllBatches(orgId);
//    }
//
//
//    @GetMapping("/admin/batches/{batchId}/trainer-students")
//    public Map<String,List<String>> getTrainerStudents(@PathVariable Long batchId){
//        return service.getTrainerStudents(batchId);
//    }
//
//    @PostMapping("/admin/batches/{batchId}/trainers/{trainerEmail}/students")
//    public void assignStudents(
//            @PathVariable Long batchId,
//            @PathVariable String trainerEmail,
//            @RequestBody AssignStudentsRequest req){
//        service.assignStudentsToTrainer(batchId, trainerEmail, req.getStudentEmails());
//    }
//    @PutMapping("/admin/batches/{batchId}/trainers/{trainerEmail}")
//    public void assignTrainer(
//            @PathVariable Long batchId,
//            @PathVariable String trainerEmail) {
//        service.assignTrainer(batchId, trainerEmail);
//    }
//
//
//
//    @DeleteMapping("/admin/batches/{batchId}/trainer")
//    public void removeTrainer(
//            @PathVariable Long batchId,
//            @RequestParam String trainerEmail) {
//
//        service.removeTrainer(batchId, trainerEmail);
//    }
//
//    
//    
//    @DeleteMapping("/admin/batches/{batchId}/trainers/{trainerEmail}/students/{studentEmail:.+}")
//    public void removeStudent(
//            @PathVariable Long batchId,
//            @PathVariable String trainerEmail,
//            @PathVariable String studentEmail) {
//
//        service.removeStudentFromTrainer(batchId, trainerEmail, studentEmail);
//    }
//
//
//    /* TRAINER */
//
//    @GetMapping("/trainer")
//    public List<BatchResponseDTO> trainerBatches(HttpServletRequest req){
//        return service.getBatchesForTrainer(extract(req));
//    }
//
//    /* STUDENT */
//
//    
//
//    private String extract(HttpServletRequest request){
//        String token=request.getHeader("Authorization").substring(7);
//        return jwtUtil.extractEmail(token);
//    }
//
//    @GetMapping("/admin/batches/{batchId}/trainers/{trainerEmail}/available-students")
//    public List<StudentDTO> availableStudents(
//            @PathVariable Long batchId,
//            @PathVariable String trainerEmail,
//            HttpServletRequest request) {
//
//        String orgId = jwtUtil.extractOrganizationId(
//                request.getHeader("Authorization").substring(7));
//
//        return service.getAvailableStudents(batchId, trainerEmail, orgId);
//    }
//    
//    @GetMapping("/admin/batches/{batchId}/available-trainers")
//    public List<TrainerDTO> availableTrainers(
//            @PathVariable Long batchId,
//            HttpServletRequest request) {          // ← add this
//
//        String orgId = jwtUtil.extractOrganizationId(request  // ← extract orgId from token
//                .getHeader("Authorization").substring(7));
//
//        return service.getAvailableTrainers(batchId, orgId);   // ← pass it down
//    }
//    @GetMapping("/trainer/students")
//    public List<String> trainerStudents(HttpServletRequest req) {
//        String email = extract(req);
//        return service.getStudentsForTrainer(email);
//    }
//    @GetMapping("/trainer/batches/{batchId}/students")
//    public List<String> trainerBatchStudents(
//            @PathVariable Long batchId,
//            HttpServletRequest request) {
//
//        String trainerEmail = extract(request);
//        return service.getStudentsForTrainerBatch(batchId, trainerEmail);
//    }
//    @GetMapping("/student/classroom")
//    public StudentClassroomDTO studentClassroom(HttpServletRequest req){
//        return service.getStudentClassroom(extract(req));
//    }
//
//    /* ===== SUPERADMIN ONLY — global batches (organizationId = null) ===== */
//    @GetMapping("/admin/batches/global")
//    public List<BatchResponseDTO> getGlobalBatches() {
//        return service.getGlobalBatches();
//    }
//    //SUPERADMIN get teh trainers form userfeign clinet 
//    @GetMapping("/admin/batches/{batchId}/available-trainers-global")
//    public List<TrainerDTO> availableTrainersGlobal(@PathVariable Long batchId) {
//        return service.getAvailableTrainersGlobal(batchId);
//    }
////same userfiegnclint get sthe studenst from user servcie for super amdin 
//    @GetMapping("/admin/batches/{batchId}/trainers/{trainerEmail}/available-students-global")
//    public List<StudentDTO> availableStudentsGlobal(
//            @PathVariable Long batchId,
//            @PathVariable String trainerEmail) {
//        return service.getAvailableStudentsGlobal(batchId, trainerEmail);
//    }
//    
//    //for count of remiang deprt.barcnhs,batches to show in org details profile tab 
// // BatchController.java — ADD:
//    @GetMapping("/admin/org-summary")
//    public ResponseEntity<Map<String, Object>> getOrgSummary(
//            HttpServletRequest request) {
//        String orgId = jwtUtil.extractOrganizationId(
//                request.getHeader("Authorization").substring(7));
//        return ResponseEntity.ok(service.getOrgSummary(orgId));
//    }
//    
// // ===== SUPERADMIN — get batches by orgId path variable =====
// // Used for SuperAdmin viewing another org's batch list
// @GetMapping("/admin/batches/by-org/{organizationId}")
// public List<BatchResponseDTO> getBatchesByOrg(
//         @PathVariable String organizationId) {
//     return service.getBatchesByOrg(organizationId);  // reuses existing logic
// }
//}
package com.lms.batch.controller;

import com.lms.batch.dto.*;
import com.lms.batch.security.JwtUtil;
import com.lms.batch.service.BatchService;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import com.lms.batch.dto.TrainerDTO;

@RestController
@RequestMapping("/api/batch")
public class BatchController {

    private final BatchService service;
    private final JwtUtil jwtUtil;

    public BatchController(BatchService service, JwtUtil jwtUtil) {
        this.service = service;
        this.jwtUtil = jwtUtil;
    }

    // ─── HELPERS ──────────────────────────────────────────────────────────────

    private String extractEmail(HttpServletRequest request) {
        return jwtUtil.extractEmail(
                request.getHeader("Authorization").substring(7));
    }

    private String extractOrgId(HttpServletRequest request) {
        return jwtUtil.extractOrganizationId(
                request.getHeader("Authorization").substring(7));
    }

    // ─── ADMIN ────────────────────────────────────────────────────────────────

    /**
     * POST /api/batch/admin/batches
     * Feature: create_batch — org resolved inside service from the branch entity.
     * No extra params needed here; enforcement happens in BatchService.createBatch.
     */
    @PostMapping("/admin/batches")
    public BatchResponseDTO create(@RequestBody CreateBatchRequest req) {
        return service.createBatch(req);
    }

    /**
     * DELETE /api/batch/admin/batches/{batchId}
     * Feature: delete_batch — org resolved inside service from the batch entity.
     */
    @DeleteMapping("/admin/batches/{batchId}")
    public void deleteBatch(@PathVariable Long batchId) {
        service.deleteBatch(batchId);
    }

    /**
     * GET /api/batch/admin/batches
     * Feature: get_all_batches — org comes from JWT.
     */
    @GetMapping("/admin/batches")
    public List<BatchResponseDTO> getAllBatches(HttpServletRequest request) {
        String orgId = extractOrgId(request);
        // NEW — orgId passed; enforcement happens in BatchService.getAllBatches
        return service.getAllBatches(orgId);
    }

    /**
     * GET /api/batch/admin/batches/{batchId}/trainer-students
     * Feature: get_trainer_students — org resolved inside service from the batch entity.
     */
    @GetMapping("/admin/batches/{batchId}/trainer-students")
    public Map<String, List<String>> getTrainerStudents(@PathVariable Long batchId) {
        return service.getTrainerStudents(batchId);
    }

    /**
     * POST /api/batch/admin/batches/{batchId}/trainers/{trainerEmail}/students
     * Feature: assign_students — org resolved inside service from the batch entity.
     */
    @PostMapping("/admin/batches/{batchId}/trainers/{trainerEmail}/students")
    public void assignStudents(
            @PathVariable Long batchId,
            @PathVariable String trainerEmail,
            @RequestBody AssignStudentsRequest req) {
        service.assignStudentsToTrainer(batchId, trainerEmail, req.getStudentEmails());
    }

    /**
     * PUT /api/batch/admin/batches/{batchId}/trainers/{trainerEmail}
     * Feature: assign_trainer — org resolved inside service from the batch entity.
     */
    @PutMapping("/admin/batches/{batchId}/trainers/{trainerEmail}")
    public void assignTrainer(
            @PathVariable Long batchId,
            @PathVariable String trainerEmail) {
        service.assignTrainer(batchId, trainerEmail);
    }

    /**
     * DELETE /api/batch/admin/batches/{batchId}/trainer
     * Feature: remove_trainer — org resolved inside service from the batch entity.
     */
    @DeleteMapping("/admin/batches/{batchId}/trainer")
    public void removeTrainer(
            @PathVariable Long batchId,
            @RequestParam String trainerEmail) {
        service.removeTrainer(batchId, trainerEmail);
    }

    /**
     * DELETE /api/batch/admin/batches/{batchId}/trainers/{trainerEmail}/students/{studentEmail}
     * Feature: remove_student — org resolved inside service from the batch entity.
     */
    @DeleteMapping("/admin/batches/{batchId}/trainers/{trainerEmail}/students/{studentEmail:.+}")
    public void removeStudent(
            @PathVariable Long batchId,
            @PathVariable String trainerEmail,
            @PathVariable String studentEmail) {
        service.removeStudentFromTrainer(batchId, trainerEmail, studentEmail);
    }

    /**
     * GET /api/batch/admin/batches/{batchId}/trainers/{trainerEmail}/available-students
     * Feature: get_available_students — org comes from JWT.
     */
    @GetMapping("/admin/batches/{batchId}/trainers/{trainerEmail}/available-students")
    public List<StudentDTO> availableStudents(
            @PathVariable Long batchId,
            @PathVariable String trainerEmail,
            HttpServletRequest request) {
        String orgId = extractOrgId(request);
        return service.getAvailableStudents(batchId, trainerEmail, orgId);
    }

    /**
     * GET /api/batch/admin/batches/{batchId}/available-trainers
     * Feature: get_available_trainers — org comes from JWT.
     */
    @GetMapping("/admin/batches/{batchId}/available-trainers")
    public List<TrainerDTO> availableTrainers(
            @PathVariable Long batchId,
            HttpServletRequest request) {
        String orgId = extractOrgId(request);
        return service.getAvailableTrainers(batchId, orgId);
    }

    // ─── TRAINER ──────────────────────────────────────────────────────────────

    /**
     * GET /api/batch/trainer
     * Feature: get_trainer_batches
     * NEW — extract both email AND organizationId from JWT.
     * organizationId may be null for org-less trainers → enforcement uses email scope.
     */
    @GetMapping("/trainer")
    public List<BatchResponseDTO> trainerBatches(HttpServletRequest request) {
        String email = extractEmail(request);
        String orgId = extractOrgId(request); // null if trainer has no org
        return service.getBatchesForTrainer(email, orgId);
    }

    /**
     * GET /api/batch/trainer/students
     * Internal helper — no feature flag enforcement (not in SERVICES_CONFIG).
     */
    @GetMapping("/trainer/students")
    public List<String> trainerStudents(HttpServletRequest request) {
        String email = extractEmail(request);
        return service.getStudentsForTrainer(email);
    }

    /**
     * GET /api/batch/trainer/batches/{batchId}/students
     * Internal helper — no feature flag enforcement (not in SERVICES_CONFIG).
     */
    @GetMapping("/trainer/batches/{batchId}/students")
    public List<String> trainerBatchStudents(
            @PathVariable Long batchId,
            HttpServletRequest request) {
        String trainerEmail = extractEmail(request);
        return service.getStudentsForTrainerBatch(batchId, trainerEmail);
    }

    // ─── STUDENT ──────────────────────────────────────────────────────────────

    /**
     * GET /api/batch/student/classroom
     * Feature: get_student_classroom
     * NEW — extract both email AND organizationId from JWT.
     * organizationId may be null for org-less students → enforcement uses email scope.
     */
    @GetMapping("/student/classroom")
    public StudentClassroomDTO studentClassroom(HttpServletRequest request) {
        String email = extractEmail(request);
        String orgId = extractOrgId(request); // null if student has no org
        return service.getStudentClassroom(email, orgId);
    }

    // ─── ORG SUMMARY ──────────────────────────────────────────────────────────

    /**
     * GET /api/batch/admin/org-summary
     * No feature flag — just counts for dashboard display.
     */
    @GetMapping("/admin/org-summary")
    public ResponseEntity<Map<String, Object>> getOrgSummary(HttpServletRequest request) {
        String orgId = extractOrgId(request);
        return ResponseEntity.ok(service.getOrgSummary(orgId));
    }

    // ─── SUPERADMIN ONLY — NO enforcement (SuperAdmin sees everything) ─────────

    @GetMapping("/admin/batches/global")
    public List<BatchResponseDTO> getGlobalBatches() {
        return service.getGlobalBatches();
    }

    @GetMapping("/admin/batches/{batchId}/available-trainers-global")
    public List<TrainerDTO> availableTrainersGlobal(@PathVariable Long batchId) {
        return service.getAvailableTrainersGlobal(batchId);
    }

    @GetMapping("/admin/batches/{batchId}/trainers/{trainerEmail}/available-students-global")
    public List<StudentDTO> availableStudentsGlobal(
            @PathVariable Long batchId,
            @PathVariable String trainerEmail) {
        return service.getAvailableStudentsGlobal(batchId, trainerEmail);
    }

    @GetMapping("/admin/batches/by-org/{organizationId}")
    public List<BatchResponseDTO> getBatchesByOrg(@PathVariable String organizationId) {
        return service.getBatchesByOrg(organizationId);
    }
}