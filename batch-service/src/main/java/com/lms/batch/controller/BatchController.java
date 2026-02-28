package com.lms.batch.controller;

import com.lms.batch.dto.*;
import com.lms.batch.security.JwtUtil;
import com.lms.batch.service.BatchService;
import jakarta.servlet.http.HttpServletRequest;
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

    /* ADMIN */

    @PostMapping("/admin/batches")
    public BatchResponseDTO create(@RequestBody CreateBatchRequest req) {
        return service.createBatch(req);
    }
    
    @DeleteMapping("/admin/batches/{batchId}")
    public void deleteBatch(@PathVariable Long batchId) {
        service.deleteBatch(batchId);
    }

    
    @GetMapping("/admin/batches")
    public List<BatchResponseDTO> getAllBatches() {
        return service.getAllBatches();
    }


    @GetMapping("/admin/batches/{batchId}/trainer-students")
    public Map<String,List<String>> getTrainerStudents(@PathVariable Long batchId){
        return service.getTrainerStudents(batchId);
    }

    @PostMapping("/admin/batches/{batchId}/trainers/{trainerEmail}/students")
    public void assignStudents(
            @PathVariable Long batchId,
            @PathVariable String trainerEmail,
            @RequestBody AssignStudentsRequest req){
        service.assignStudentsToTrainer(batchId, trainerEmail, req.getStudentEmails());
    }
    @PutMapping("/admin/batches/{batchId}/trainers/{trainerEmail}")
    public void assignTrainer(
            @PathVariable Long batchId,
            @PathVariable String trainerEmail) {
        service.assignTrainer(batchId, trainerEmail);
    }


//    @DeleteMapping("/admin/batches/{batchId}/trainers/{trainerEmail:.+}")
//    public void removeTrainer(
//            @PathVariable Long batchId,
//            @PathVariable String trainerEmail) {
//
//        service.removeTrainer(batchId, trainerEmail);
//    }
    @DeleteMapping("/admin/batches/{batchId}/trainer")
    public void removeTrainer(
            @PathVariable Long batchId,
            @RequestParam String trainerEmail) {

        service.removeTrainer(batchId, trainerEmail);
    }

    
    
    @DeleteMapping("/admin/batches/{batchId}/trainers/{trainerEmail}/students/{studentEmail:.+}")
    public void removeStudent(
            @PathVariable Long batchId,
            @PathVariable String trainerEmail,
            @PathVariable String studentEmail) {

        service.removeStudentFromTrainer(batchId, trainerEmail, studentEmail);
    }


    /* TRAINER */

    @GetMapping("/trainer")
    public List<BatchResponseDTO> trainerBatches(HttpServletRequest req){
        return service.getBatchesForTrainer(extract(req));
    }

    /* STUDENT */

    

    private String extract(HttpServletRequest request){
        String token=request.getHeader("Authorization").substring(7);
        return jwtUtil.extractEmail(token);
    }
    @GetMapping("/admin/batches/{batchId}/trainers/{trainerEmail}/available-students")
    public List<StudentDTO> availableStudents(
            @PathVariable Long batchId,
            @PathVariable String trainerEmail) {

        return service.getAvailableStudents(batchId, trainerEmail);
    }

    /* ADMIN — AVAILABLE TRAINERS FOR A BATCH */
    @GetMapping("/admin/batches/{batchId}/available-trainers")
    public List<TrainerDTO> availableTrainers(@PathVariable Long batchId) {
        return service.getAvailableTrainers(batchId);
    }
    
    @GetMapping("/trainer/students")
    public List<String> trainerStudents(HttpServletRequest req) {
        String email = extract(req);
        return service.getStudentsForTrainer(email);
    }
    @GetMapping("/trainer/batches/{batchId}/students")
    public List<String> trainerBatchStudents(
            @PathVariable Long batchId,
            HttpServletRequest request) {

        String trainerEmail = extract(request);
        return service.getStudentsForTrainerBatch(batchId, trainerEmail);
    }
    @GetMapping("/student/classroom")
    public StudentClassroomDTO studentClassroom(HttpServletRequest req){
        return service.getStudentClassroom(extract(req));
    }


}
