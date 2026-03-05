//package com.lms.batch.service;
//
//import com.lms.batch.client.UserClient;
//
//import com.lms.batch.dto.*;
//import com.lms.batch.entity.Batch;
//import com.lms.batch.entity.BatchStudent;
//import com.lms.batch.entity.BatchTrainerStudent;
//import com.lms.batch.kafka.BatchEventProducer;
//import com.lms.batch.repository.BatchRepository;
//
//import com.lms.batch.repository.BatchTrainerStudentRepository;
//import com.lms.batch.repository.BranchRepository;
//import com.lms.batch.util.BatchCodeGenerator;
//import org.springframework.stereotype.Service;
//import java.util.Map;
//import java.util.stream.Collectors;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import com.lms.batch.kafka.BatchLifecycleProducer;
//@Service
//public class BatchService {
//
//    private final BatchRepository batchRepository;
//    
//    private final BatchCodeGenerator batchCodeGenerator;
//    private final BatchEventProducer batchEventProducer;
//    private final UserClient userClient;
//    private final BatchLifecycleProducer batchlifecycleproducer;
//    private final BranchRepository branchrepository;
//    private final BatchTrainerStudentRepository batchTrainerStudentRepository;
//
//    public BatchService(
//            BatchRepository batchRepository,
//            
//            
//            BatchCodeGenerator batchCodeGenerator,
//            BatchEventProducer batchEventProducer,
//            UserClient userClient,
//            BatchLifecycleProducer batchlifecycleproducer,
//            BranchRepository branchrepository,
//            BatchTrainerStudentRepository batchTrainerStudentRepository
//    ) {
//        this.batchRepository = batchRepository;
//      
//        this.batchCodeGenerator = batchCodeGenerator;
//        this.batchEventProducer = batchEventProducer;
//        this.userClient = userClient;
//        this.batchlifecycleproducer=batchlifecycleproducer;
//        this.branchrepository=branchrepository;
//        this.batchTrainerStudentRepository = batchTrainerStudentRepository;
//    }
//
//
//    public BatchResponseDTO createBatch(CreateBatchRequest request) {
//
//        if (!branchrepository.existsById(request.getBranchId())) {
//            throw new RuntimeException("Branch not found");
//        }
//
//        Batch batch = new Batch();
//        batch.setBatchName(request.getBatchName());
//        batch.setBranchId(request.getBranchId());
//
//        batchRepository.save(batch);
//
//        return map(batch);
//    }
//    
//    @Transactional
//    public BatchResponseDTO assignTrainer(Long batchId, String trainerEmail) {
//
//        Batch batch = batchRepository.findById(batchId)
//                .orElseThrow(() -> new RuntimeException("Batch not found"));
//
//        // validate trainer from user-service
//        UserDTO trainer = userClient.getUserByEmail(trainerEmail);
//
//        batch.setTrainerEmail(trainer.getEmail());
//        batchRepository.save(batch);
//
//        // 🔥 VERY IMPORTANT
//        batchEventProducer.sendTrainerAssigned(trainer.getEmail(), batch.getId());
//
//        return map(batch);
//    }
//
//    
//    public List<BatchResponseDTO> getAllBatches() {
//    	return batchRepository.findAll()
//    	        .stream()
//    	        .map(this::map)
//    	        .toList();
//
//    }
//
//    public BatchResponseDTO updateBatch(Long id, CreateBatchRequest request) {
//
//        Batch batch = batchRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Batch not found"));
//
//        batch.setBatchName(request.getBatchName());
//        batch.setBranchId(request.getBranchId());
//        batch.setTrainerEmail(request.getTrainerEmail());
//
//        batchRepository.save(batch);
//
//        batchEventProducer.sendTrainerAssigned(request.getTrainerEmail(), batch.getId());
//
//        return map(batch);
//    }
//
//    @Transactional
//    public void deleteBatch(Long batchId) {
//
//        Batch batch = batchRepository.findById(batchId)
//                .orElseThrow(() -> new RuntimeException("Batch not found"));
//
//      
//
//        // 🔥 notify other services
//        batchlifecycleproducer.sendBatchDeleted(batchId);
//
//        // delete batch
//        batchRepository.delete(batch);
//
//        System.out.println("📦 BATCH DELETED: " + batchId);
//    }
//
//
//    /* ================= TRAINER ================= */
//
//    public List<BatchResponseDTO> getBatchesForTrainer(String trainerEmail) {
//
//        List<Long> batchIds =
//                batchTrainerStudentRepository.findDistinctBatchIdsByTrainer(trainerEmail);
//
//        return batchRepository.findAllById(batchIds)
//                .stream()
//                .map(this::map)
//                .toList();
//    }
//
//
//    
//
//    
//    
//    public StudentBatchInfoDTO getStudentBatchInfo(String studentEmail) {
//
//        BatchTrainerStudent mapping =
//                batchTrainerStudentRepository.findFirstByStudentEmail(studentEmail)
//                .orElseThrow(() -> new RuntimeException("Student not assigned"));
//
//        Batch batch = batchRepository.findById(mapping.getBatchId())
//                .orElseThrow(() -> new RuntimeException("Batch not found"));
//
//        StudentBatchInfoDTO dto = new StudentBatchInfoDTO();
//        dto.setBatchId(batch.getId());
//        dto.setBatchName(batch.getBatchName());
//        dto.setBatchCode(batch.getBatchCode());
//        dto.setTrainerEmail(mapping.getTrainerEmail());
//
//        return dto;
//    }
//
//    
//
//    public Long getStudentCount(Long batchId) {
//        return batchTrainerStudentRepository.countDistinctStudents(batchId);
//    }
//
//    public List<String> getStudents(Long batchId) {
//
//        return batchTrainerStudentRepository.findByBatchId(batchId)
//                .stream()
//                .map(BatchTrainerStudent::getStudentEmail)
//                .distinct()
//                .toList();
//    }
//
//    
//    
//    @Transactional
//    public void assignStudentsToTrainer(Long batchId, String trainerEmail, List<String> studentEmails) {
//
//        // validate trainer exists
//        userClient.getUserByEmail(trainerEmail);
//
//        for (String email : studentEmails) {
//
//            BatchTrainerStudent mapping = new BatchTrainerStudent();
//            mapping.setBatchId(batchId);
//            mapping.setTrainerEmail(trainerEmail);
//            mapping.setStudentEmail(email);
//
//            batchTrainerStudentRepository.save(mapping);
//
//            // keep your kafka event
//            batchEventProducer.sendStudentAssigned(email, batchId);
//        }
//    }
//    public Map<String, List<String>> getTrainerStudents(Long batchId) {
//
//        List<BatchTrainerStudent> list =
//                batchTrainerStudentRepository.findByBatchId(batchId);
//
//        return list.stream()
//                .collect(Collectors.groupingBy(
//                        BatchTrainerStudent::getTrainerEmail,
//                        Collectors.mapping(
//                                BatchTrainerStudent::getStudentEmail,
//                                Collectors.toList()
//                        )
//                ));
//    }
//
//    @Transactional
//    public void removeTrainer(Long batchId, String trainerEmail) {
//
//        batchTrainerStudentRepository.deleteByBatchIdAndTrainerEmail(batchId, trainerEmail);
//
//        Batch batch = batchRepository.findById(batchId).orElseThrow();
//        if (trainerEmail.equals(batch.getTrainerEmail())) {
//            batch.setTrainerEmail(null);
//            batchRepository.save(batch);
//        }
//    }
//
//
//    /* ================= MAPPER ================= */
//
//    private BatchResponseDTO map(Batch batch) {
//        BatchResponseDTO dto = new BatchResponseDTO();
//        dto.setId(batch.getId());
//        dto.setBatchCode(batch.getBatchCode());
//        dto.setBatchName(batch.getBatchName());
//        dto.setBranchId(batch.getBranchId());
//        dto.setTrainerEmail(batch.getTrainerEmail());
//        dto.setActive(batch.isActive());
//        return dto;
//    }
//}
//


package com.lms.batch.service;

import com.lms.batch.client.UserClient;

import com.lms.batch.dto.*;
import com.lms.batch.entity.*;
import com.lms.batch.kafka.BatchAssignmentProducer;
import com.lms.batch.kafka.BatchLifecycleProducer;
import com.lms.batch.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BatchService {

    private final BatchRepository batchRepository;
    private final BatchTrainerStudentRepository mappingRepo;
    private final BranchRepository branchRepository;
    private final UserClient userClient;
    private final BatchAssignmentProducer eventProducer;
    private final BatchLifecycleProducer lifecycleProducer;

    public BatchService(
            BatchRepository batchRepository,
            BatchTrainerStudentRepository mappingRepo,
            BranchRepository branchRepository,
            UserClient userClient,
            BatchAssignmentProducer eventProducer,
            BatchLifecycleProducer lifecycleProducer
    ) {
        this.batchRepository = batchRepository;
        this.mappingRepo = mappingRepo;
        this.branchRepository = branchRepository;
        this.userClient = userClient;
        this.eventProducer = eventProducer;
        this.lifecycleProducer = lifecycleProducer;
    }

    /* ================= CREATE ================= */

    public BatchResponseDTO createBatch(CreateBatchRequest request) {
        if (!branchRepository.existsById(request.getBranchId()))
            throw new RuntimeException("Branch not found");

        Batch batch = new Batch();
        batch.setBatchName(request.getBatchName());
        batch.setBranchId(request.getBranchId());

        batchRepository.save(batch);
        return map(batch);
    }

    @Transactional
    public void deleteBatch(Long batchId) {

        // 1. get all mappings before deleting
        List<BatchTrainerStudent> mappings = mappingRepo.findByBatchId(batchId);

        // 2. notify student removed
        for (BatchTrainerStudent m : mappings) {
            if (m.getStudentEmail() != null && !m.getStudentEmail().equals("__EMPTY__")) {
                eventProducer.studentRemoved(m.getStudentEmail(), batchId);
            }
        }

        // 3. notify trainers removed
        Set<String> trainers = mappings.stream()
                .map(BatchTrainerStudent::getTrainerEmail)
                .collect(Collectors.toSet());

        for (String trainer : trainers) {
            eventProducer.trainerRemoved(trainer, batchId);
        }

        // 4. delete mappings from DB (REAL DELETE)
        mappingRepo.deleteAll(mappings);

        // 5. delete batch
        batchRepository.deleteById(batchId);

        // 6. lifecycle event (global cleanup)
        lifecycleProducer.batchDeleted(batchId);

        System.out.println("🔥 FULL BATCH CLEANUP DONE -> " + batchId);
    }

    
    
    /* ================= TRAINER ================= */

    public List<BatchResponseDTO> getBatchesForTrainer(String trainerEmail) {

        List<Long> batchIds = mappingRepo.findDistinctBatchIdsByTrainer(trainerEmail);

        return batchRepository.findAllById(batchIds)
                .stream()
                .map(this::map)
                .toList();
    }

    @Transactional
    public void assignStudentsToTrainer(Long batchId, String trainerEmail, List<String> students) {

        userClient.getUserByEmail(trainerEmail); // validate trainer

        for (String email : students) {

            // 🔍 find existing mapping (student already assigned in this batch?)
            List<BatchTrainerStudent> existingMappings =
                    mappingRepo.findByBatchId(batchId);

            for (BatchTrainerStudent existing : existingMappings) {

                if (existing.getStudentEmail().equals(email)) {

                    // student moved to another trainer -> remove old relation only
                    if (!existing.getTrainerEmail().equals(trainerEmail)) {
                        eventProducer.studentRemoved(email, batchId);
                    }

                    // remove old mapping
                    mappingRepo.deleteByBatchIdAndStudentEmail(batchId, email);
                    break;
                }
            }

            // 🆕 create new mapping
            BatchTrainerStudent map = new BatchTrainerStudent(batchId, trainerEmail, email);
            mappingRepo.save(map);

            // 📨 emit student assignment ONLY
            eventProducer.studentAssigned(email, batchId);
        }
    }

//    public Map<String, List<String>> getTrainerStudents(Long batchId) {
//
//        return mappingRepo.findByBatchId(batchId)
//                .stream()
//                .collect(Collectors.groupingBy(
//                        BatchTrainerStudent::getTrainerEmail,
//                        Collectors.mapping(BatchTrainerStudent::getStudentEmail, Collectors.toList())
//                ))
//                .entrySet()
//                .stream()
//                .collect(Collectors.toMap(
//                        Map.Entry::getKey,
//                        e -> e.getValue().stream()
//                                .filter(s -> !s.equals("__EMPTY__"))
//                                .toList()
//                ));
//    }
    public Map<String, List<String>> getTrainerStudents(Long batchId) {
    	System.out.println("SERVICE VERSION 2 RUNNING");

        List<BatchTrainerStudent> rows = mappingRepo.findByBatchId(batchId);

        Map<String, List<String>> map = new LinkedHashMap<>();

        for (BatchTrainerStudent r : rows) {

            String trainer = r.getTrainerEmail();

            // ALWAYS create trainer entry
            map.putIfAbsent(trainer, new ArrayList<>());

            // add student only if exists
            if (r.getStudentEmail() != null) {
                map.get(trainer).add(r.getStudentEmail());
            }
        }

        return map;
    }

    @Transactional
    public void removeStudentFromTrainer(Long batchId, String trainerEmail, String studentEmail) {

        // 📨 notify other services student removed from trainer
        eventProducer.studentRemoved(studentEmail, batchId);

        // 🗑 remove mapping
        mappingRepo.deleteByBatchIdAndTrainerEmailAndStudentEmail(
                batchId, trainerEmail, studentEmail);
    }



    @Transactional
    public void removeTrainer(Long batchId, String trainerEmail) {

        List<BatchTrainerStudent> mappings =
                mappingRepo.findByBatchIdAndTrainerEmail(batchId, trainerEmail);

        for (BatchTrainerStudent map : mappings) {

            // ❗ ignore placeholder record
            if (map.getStudentEmail() != null && !map.getStudentEmail().equals("__EMPTY__")) {
                eventProducer.studentRemoved(map.getStudentEmail(), batchId);
            }
        }

        eventProducer.trainerRemoved(trainerEmail, batchId);

        mappingRepo.deleteByBatchIdAndTrainerEmail(batchId, trainerEmail);
    }




    /* ================= STUDENT ================= */

    public StudentBatchInfoDTO getStudentBatchInfo(String studentEmail) {

        BatchTrainerStudent map = mappingRepo.findFirstByStudentEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("Student not assigned"));

        Batch batch = batchRepository.findById(map.getBatchId())
                .orElseThrow(() -> new RuntimeException("Batch not found"));

        StudentBatchInfoDTO dto = new StudentBatchInfoDTO();
        dto.setBatchId(batch.getId());
        dto.setBatchName(batch.getBatchName());
        dto.setBatchCode(batch.getBatchCode());
        dto.setTrainerEmail(map.getTrainerEmail());

        return dto;
    }

    /* ================= ADMIN ================= */

    public Long getStudentCount(Long batchId) {
        return mappingRepo.countDistinctStudents(batchId);
    }

    public List<String> getStudents(Long batchId) {
        return mappingRepo.findByBatchId(batchId)
                .stream()
                .map(BatchTrainerStudent::getStudentEmail)
                .distinct()
                .toList();
    }
    
    public List<BatchResponseDTO> getAllBatches() {

        return batchRepository.findAll()
                .stream()
                .map(batch -> {
                    BatchResponseDTO dto = new BatchResponseDTO();

                    dto.setId(batch.getId());
                    dto.setBatchName(batch.getBatchName());
                    dto.setBatchCode(batch.getBatchCode());
                    dto.setBranchId(batch.getBranchId());
                    dto.setActive(batch.isActive());

                    return dto;
                })
                .toList();
    }
    @Transactional
    public void assignTrainer(Long batchId, String trainerEmail) {

        if (trainerEmail == null || trainerEmail.isBlank() || trainerEmail.equals("undefined")) {
            throw new RuntimeException("Trainer email missing");
        }

        userClient.getUserByEmail(trainerEmail);

        boolean exists = mappingRepo
                .findByBatchId(batchId)
                .stream()
                .anyMatch(m -> m.getTrainerEmail().equals(trainerEmail));

        if (exists) return;

        BatchTrainerStudent mapping =
                new BatchTrainerStudent(batchId, trainerEmail, "__EMPTY__");

        mappingRepo.save(mapping);

        // 📨 MISSING EVENT (add this)
        eventProducer.trainerAssigned(trainerEmail, batchId);
    }

//    public List<StudentDTO> getAvailableStudents(Long batchId,String trainerEmail) {
//
//        // already assigned students in batch
//        List<String> assigned = mappingRepo.findByBatchId(batchId)
//                .stream()
//                .map(BatchTrainerStudent::getStudentEmail)
//                .toList();
//
//        // get all students from user-service
//        return userClient.getAllStudents()
//                .stream()
//                .filter(u -> !assigned.contains(u.getEmail()))
//                .map(u -> new StudentDTO(u.getEmail(), u.getDisplayName()))
//                .toList();
//    }

    public List<StudentDTO> getAvailableStudents(Long batchId, String trainerEmail) {

        // students assigned ONLY to this trainer
        List<String> assignedToThisTrainer = mappingRepo
                .findByBatchIdAndTrainerEmail(batchId, trainerEmail)
                .stream()
                .map(BatchTrainerStudent::getStudentEmail)
                .toList();

        // all students of the batch (from user service)
        List<StudentDTO> allStudents = userClient.getAllStudents()
                .stream()
                .map(u -> new StudentDTO(u.getEmail(), u.getDisplayName()))
                .toList();

        // remove only this trainer students
        return allStudents.stream()
                .filter(s -> !assignedToThisTrainer.contains(s.getEmail()))
                .toList();
    }

    
    public List<TrainerDTO> getAvailableTrainers(Long batchId) {

        // 1️⃣ fetch all trainers from user-service
        List<TrainerDTO> all = userClient.getAllTrainers();

        // 2️⃣ get trainers already used in this batch
        List<String> assigned = mappingRepo.findByBatchId(batchId)
                .stream()
                .map(BatchTrainerStudent::getTrainerEmail)
                .distinct()
                .toList();

        // filter only not assigned
        return all.stream()
                .filter(t -> !assigned.contains(t.getEmail()))
                .toList();

    }
    public List<String> getStudentsForTrainer(String trainerEmail) {

        return mappingRepo.findAll()
                .stream()
                .filter(m -> trainerEmail.equals(m.getTrainerEmail()))
                .map(BatchTrainerStudent::getStudentEmail)
                .filter(email -> email != null && !email.equals("__EMPTY__"))
                .distinct()
                .toList();
    }
    public List<String> getStudentsForTrainerBatch(Long batchId, String trainerEmail) {

        return mappingRepo.findByBatchId(batchId)
                .stream()
                .filter(m -> m.getTrainerEmail().equals(trainerEmail))
                .map(BatchTrainerStudent::getStudentEmail)
                .filter(email -> !email.equals("__EMPTY__"))
                .distinct()
                .toList();
    }
    //classroom
    public StudentClassroomDTO getStudentClassroom(String email) {

        Optional<BatchTrainerStudent> optional =
                mappingRepo.findTopByStudentEmailOrderByIdDesc(email);

        // student not yet assigned
        if (optional.isEmpty()) {
            return null;
        }
//code for 
        BatchTrainerStudent mapping = optional.get();

        Batch batch = batchRepository.findById(mapping.getBatchId())
                .orElse(null);

        if (batch == null) return null;

        UserDTO trainer = userClient.getUserByEmail(mapping.getTrainerEmail());

        return new StudentClassroomDTO(
                batch.getBatchName(),
                trainer.getEmail(),
                trainer.getDisplayName()
        );
    }

    @Transactional
    public void deleteAllBatchesUnderBranch(Long branchId) {

        List<Batch> batches = batchRepository.findByBranchId(branchId);

        for (Batch batch : batches) {
            deleteBatch(batch.getId());
        }

        System.out.println("🧹 ALL BATCHES DELETED UNDER BRANCH -> " + branchId);
    }

    
    /* ================= UTIL ================= */

    private BatchResponseDTO map(Batch batch) {
        BatchResponseDTO dto = new BatchResponseDTO();
        dto.setId(batch.getId());
        dto.setBatchName(batch.getBatchName());
        dto.setBatchCode(batch.getBatchCode());
        dto.setBranchId(batch.getBranchId());
        dto.setTrainerEmail(batch.getTrainerEmail());
        dto.setActive(batch.isActive());
        return dto;
    }
}
