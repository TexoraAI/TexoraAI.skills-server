
package com.lms.batch.service;

import com.lms.batch.client.UserClient;
import com.lms.batch.constants.BatchFeatureKeys;
import com.lms.batch.dto.*;
import com.lms.batch.entity.*;
import com.lms.batch.kafka.BatchAssignmentProducer;
import com.lms.batch.kafka.BatchLifecycleProducer;
import com.lms.batch.repository.*;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BatchService {

    private final BatchRepository              batchRepository;
    private final BatchTrainerStudentRepository mappingRepo;
    private final BranchRepository             branchRepository;
    private final UserClient                   userClient;
    private final BatchAssignmentProducer      eventProducer;
    private final BatchLifecycleProducer       lifecycleProducer;
    private final OrgLimitsRepository          orgLimitsRepository;
    private final DepartmentRepository         departmentRepository;
    private final BatchFeatureFlagsService     flagsService;
    private final CacheManager cacheManager;

    public BatchService(
            BatchRepository batchRepository,
            BatchTrainerStudentRepository mappingRepo,
            BranchRepository branchRepository,
            UserClient userClient,
            BatchAssignmentProducer eventProducer,
            BatchLifecycleProducer lifecycleProducer,
            OrgLimitsRepository orgLimitsRepository,
            DepartmentRepository departmentRepository,
            BatchFeatureFlagsService flagsService,
            CacheManager cacheManager
    ) {
        this.batchRepository    = batchRepository;
        this.mappingRepo        = mappingRepo;
        this.branchRepository   = branchRepository;
        this.userClient         = userClient;
        this.eventProducer      = eventProducer;
        this.lifecycleProducer  = lifecycleProducer;
        this.orgLimitsRepository = orgLimitsRepository;
        this.departmentRepository = departmentRepository;
        this.flagsService       = flagsService;
        this.cacheManager       = cacheManager;
    }

    /* ================= ADMIN: CREATE BATCH ================= */
    @Caching(evict = {
        @CacheEvict(value = "batches:org",  key = "#result.organizationId", condition = "#result.organizationId != null"),
        @CacheEvict(value = "org:summary",  key = "#result.organizationId", condition = "#result.organizationId != null")
    })
    public BatchResponseDTO createBatch(CreateBatchRequest request) {
        Branch branch = branchRepository.findById(request.getBranchId())
            .orElseThrow(() -> new RuntimeException("Branch not found"));

        String orgId = branch.getOrganizationId();

        flagsService.enforce(orgId, null, BatchFeatureKeys.CREATE_BATCH);

        if (orgId != null) {
            OrgLimits limits = orgLimitsRepository.findById(orgId).orElse(null);
            if (limits != null && limits.getMaxBatchesPerBranch() != null) {
                long count = batchRepository.countByBranchId(request.getBranchId());
                if (count >= limits.getMaxBatchesPerBranch()) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Batch limit reached for this branch. Max: "
                        + limits.getMaxBatchesPerBranch());
                }
            }
        }

        Batch batch = new Batch();
        batch.setBatchName(request.getBatchName());
        batch.setBranchId(request.getBranchId());
        batch.setDepartmentId(branch.getDepartmentId());
        batch.setOrganizationId(orgId);
        batchRepository.save(batch);
        return map(batch);
    }

    /* ================= ADMIN: DELETE BATCH ================= */
    @Transactional
    public void deleteBatch(Long batchId) {

        String organizationId = batchRepository.findById(batchId)
                .map(Batch::getOrganizationId)
                .orElse(null);

        flagsService.enforce(organizationId, null, BatchFeatureKeys.DELETE_BATCH);

        List<BatchTrainerStudent> mappings = mappingRepo.findByBatchId(batchId);

        for (BatchTrainerStudent m : mappings) {
            if (m.getStudentEmail() != null && !m.getStudentEmail().equals("__EMPTY__")) {
                eventProducer.studentRemoved(m.getStudentEmail(), batchId, organizationId);
                evictStudentCaches(m.getStudentEmail());
            }
        }

        Set<String> trainers = mappings.stream()
                .map(BatchTrainerStudent::getTrainerEmail)
                .collect(Collectors.toSet());

        for (String trainer : trainers) {
            eventProducer.trainerRemoved(trainer, batchId, organizationId);
            evictTrainerBatchCache(trainer);
        }

        mappingRepo.deleteAll(mappings);
        batchRepository.deleteById(batchId);
        lifecycleProducer.batchDeleted(batchId);

        if (organizationId != null) {
            evictOrgBatchCache(organizationId);
            evictOrgSummaryCache(organizationId);
        }
        evictTrainerStudentsCache(batchId);

        System.out.println("🔥 FULL BATCH CLEANUP DONE -> " + batchId);
    }

    /* ================= TRAINER: GET MY BATCHES ================= */
    @Cacheable(value = "batches:trainer", key = "#trainerEmail.toLowerCase()")
    public List<BatchResponseDTO> getBatchesForTrainer(String trainerEmail, String organizationId) {

        flagsService.enforceForUser(organizationId, trainerEmail, BatchFeatureKeys.GET_TRAINER_BATCHES);

        List<Long> batchIds = mappingRepo.findDistinctBatchIdsByTrainer(trainerEmail);

        return batchRepository.findAllById(batchIds)
                .stream()
                .map(this::map)
                .toList();
    }

    /* ================= ADMIN: ASSIGN TRAINER ================= */
    @Transactional
    public void assignTrainer(Long batchId, String trainerEmail) {

        if (trainerEmail == null || trainerEmail.isBlank()
                || trainerEmail.equals("undefined")) {
            throw new RuntimeException("Trainer email missing");
        }

        String organizationId = batchRepository.findById(batchId)
                .map(Batch::getOrganizationId)
                .orElse(null);

        flagsService.enforce(organizationId, null, BatchFeatureKeys.ASSIGN_TRAINER);

        userClient.getUserByEmail(trainerEmail);

        boolean exists = mappingRepo.findByBatchId(batchId)
                .stream()
                .anyMatch(m -> m.getTrainerEmail().equals(trainerEmail));

        if (exists) return;

        BatchTrainerStudent mapping =
                new BatchTrainerStudent(batchId, trainerEmail, "__EMPTY__");
        mappingRepo.save(mapping);

        eventProducer.trainerAssigned(trainerEmail, batchId, organizationId);

        evictTrainerBatchCache(trainerEmail);
        if (organizationId != null) evictOrgBatchCache(organizationId);
        evictTrainerStudentsCache(batchId);
    }

    /* ================= ADMIN: ASSIGN STUDENTS TO TRAINER ================= */
    @Transactional
    public void assignStudentsToTrainer(Long batchId, String trainerEmail,
                                        List<String> students) {

        String organizationId = batchRepository.findById(batchId)
                .map(Batch::getOrganizationId)
                .orElse(null);

        flagsService.enforce(organizationId, null, BatchFeatureKeys.ASSIGN_STUDENTS);

        userClient.getUserByEmail(trainerEmail);

        List<BatchTrainerStudent> existingMappings = mappingRepo.findByBatchId(batchId);

        for (String email : students) {

            for (BatchTrainerStudent existing : existingMappings) {

                if (existing.getStudentEmail().equals(email)) {

                    if (!existing.getTrainerEmail().equals(trainerEmail)) {
                        eventProducer.studentRemoved(email, batchId, organizationId);
                    }

                    mappingRepo.deleteByBatchIdAndStudentEmail(batchId, email);
                    break;
                }
            }

            BatchTrainerStudent map =
                    new BatchTrainerStudent(batchId, trainerEmail, email);
            mappingRepo.save(map);

            eventProducer.studentAssigned(email, batchId, organizationId);
            evictStudentCaches(email);
        }

        evictTrainerStudentsCache(batchId);
        evictTrainerBatchCache(trainerEmail);
    }

    /* ================= ADMIN: REMOVE STUDENT FROM TRAINER ================= */
    @Transactional
    public void removeStudentFromTrainer(Long batchId, String trainerEmail,
                                         String studentEmail) {

        String organizationId = batchRepository.findById(batchId)
                .map(Batch::getOrganizationId)
                .orElse(null);

        flagsService.enforce(organizationId, null, BatchFeatureKeys.REMOVE_STUDENT);

        eventProducer.studentRemoved(studentEmail, batchId, organizationId);

        mappingRepo.deleteByBatchIdAndTrainerEmailAndStudentEmail(
                batchId, trainerEmail, studentEmail);

        evictStudentCaches(studentEmail);
        evictTrainerStudentsCache(batchId);
    }

    /* ================= ADMIN: REMOVE TRAINER ================= */
    @Transactional
    public void removeTrainer(Long batchId, String trainerEmail) {

        String organizationId = batchRepository.findById(batchId)
                .map(Batch::getOrganizationId)
                .orElse(null);

        flagsService.enforce(organizationId, null, BatchFeatureKeys.REMOVE_TRAINER);

        List<BatchTrainerStudent> mappings =
                mappingRepo.findByBatchIdAndTrainerEmail(batchId, trainerEmail);

        for (BatchTrainerStudent map : mappings) {
            if (map.getStudentEmail() != null
                    && !map.getStudentEmail().equals("__EMPTY__")) {
                eventProducer.studentRemoved(map.getStudentEmail(), batchId, organizationId);
                evictStudentCaches(map.getStudentEmail());
            }
        }

        eventProducer.trainerRemoved(trainerEmail, batchId, organizationId);

        mappingRepo.deleteByBatchIdAndTrainerEmail(batchId, trainerEmail);

        evictTrainerBatchCache(trainerEmail);
        evictTrainerStudentsCache(batchId);
        if (organizationId != null) evictOrgBatchCache(organizationId);
    }

    /* ================= STUDENT: GET MY BATCH INFO ================= */
    @Cacheable(value = "student:batch", key = "#studentEmail.toLowerCase()")
    public StudentBatchInfoDTO getStudentBatchInfo(String studentEmail, String organizationId) {

        flagsService.enforceForUser(organizationId, studentEmail, BatchFeatureKeys.GET_STUDENT_BATCH);

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

    /* ================= ADMIN: HELPERS (no direct feature key) ================= */

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

    /* ================= ADMIN: GET ALL BATCHES (org-scoped) ================= */
    @Cacheable(value = "batches:org", key = "#organizationId")
    public List<BatchResponseDTO> getAllBatches(String organizationId) {

        flagsService.enforce(organizationId, null, BatchFeatureKeys.GET_ALL_BATCHES);

        List<Batch> batches = batchRepository.findByOrganizationId(organizationId);

        List<Long> batchIds = batches.stream().map(Batch::getId).toList();
        Map<Long, String> trainerByBatchId = loadTrainerByBatchId(batchIds);

        return batches.stream().map(batch -> {
            BatchResponseDTO dto = new BatchResponseDTO();
            dto.setId(batch.getId());
            dto.setBatchName(batch.getBatchName());
            dto.setBatchCode(batch.getBatchCode());
            dto.setBranchId(batch.getBranchId());
            dto.setDepartmentId(batch.getDepartmentId());
            dto.setOrganizationId(batch.getOrganizationId());
            dto.setActive(batch.isActive());
            dto.setTrainerEmail(trainerByBatchId.get(batch.getId()));
            return dto;
        }).toList();
    }

    /* ================= ADMIN: TRAINER-STUDENT MAPPING ================= */
    @Cacheable(value = "batch:trainer-students", key = "#batchId")
    public Map<String, List<String>> getTrainerStudents(Long batchId) {
        System.out.println("SERVICE VERSION 2 RUNNING");

        String organizationId = batchRepository.findById(batchId)
                .map(Batch::getOrganizationId)
                .orElse(null);

        flagsService.enforce(organizationId, null, BatchFeatureKeys.GET_TRAINER_STUDENTS);

        List<BatchTrainerStudent> rows = mappingRepo.findByBatchId(batchId);

        Map<String, List<String>> map = new LinkedHashMap<>();

        for (BatchTrainerStudent r : rows) {
            String trainer = r.getTrainerEmail();
            map.putIfAbsent(trainer, new ArrayList<>());
            if (r.getStudentEmail() != null) {
                map.get(trainer).add(r.getStudentEmail());
            }
        }

        return map;
    }

    /* ================= ADMIN: AVAILABLE STUDENTS ================= */
    public List<StudentDTO> getAvailableStudents(Long batchId, String trainerEmail, String orgId) {

        flagsService.enforce(orgId, null, BatchFeatureKeys.GET_AVAILABLE_STUDENTS);

        List<String> assignedAnywhere = mappingRepo.findAllAssignedStudentEmails();

        return userClient.getStudentsByOrg(orgId, "STUDENT")
                         .getContent()
                         .stream()
                         .map(u -> new StudentDTO(u.getEmail(), u.getDisplayName()))
                         .filter(s -> !assignedAnywhere.contains(s.getEmail()))
                         .toList();
    }

    /* ================= ADMIN: AVAILABLE TRAINERS ================= */
    public List<TrainerDTO> getAvailableTrainers(Long batchId, String orgId) {

        flagsService.enforce(orgId, null, BatchFeatureKeys.GET_AVAILABLE_TRAINERS);

        List<TrainerDTO> all = userClient.getTrainersByOrg(orgId, "TRAINER").getContent();

        List<String> assigned = mappingRepo.findByBatchId(batchId)
                .stream()
                .map(BatchTrainerStudent::getTrainerEmail)
                .distinct()
                .toList();

        return all.stream()
                .filter(t -> !assigned.contains(t.getEmail()))
                .toList();
    }

    /* ================= LEGACY / INTERNAL HELPERS ================= */

    public List<String> getStudentsForTrainer(String trainerEmail) {
        return mappingRepo.findByTrainerEmail(trainerEmail)
                .stream()
                .map(BatchTrainerStudent::getStudentEmail)
                .filter(email -> email != null && !email.equals("__EMPTY__"))
                .distinct()
                .toList();
    }

    public List<String> getStudentsForTrainerBatch(Long batchId, String trainerEmail) {
        return mappingRepo.findByBatchIdAndTrainerEmail(batchId, trainerEmail)
                .stream()
                .map(BatchTrainerStudent::getStudentEmail)
                .filter(email -> !email.equals("__EMPTY__"))
                .distinct()
                .toList();
    }

    /* ================= STUDENT: CLASSROOM ================= */
    @Cacheable(value = "student:classroom", key = "#email.toLowerCase()")
    public StudentClassroomDTO getStudentClassroom(String email, String organizationId) {

        flagsService.enforceForUser(organizationId, email, BatchFeatureKeys.GET_STUDENT_CLASSROOM);

        Optional<BatchTrainerStudent> optional =
                mappingRepo.findTopByStudentEmailOrderByIdDesc(email);

        if (optional.isEmpty()) return null;

        BatchTrainerStudent mapping = optional.get();

        Batch batch = batchRepository.findById(mapping.getBatchId()).orElse(null);
        if (batch == null) return null;

        UserDTO trainer = userClient.getUserByEmail(mapping.getTrainerEmail());

        return new StudentClassroomDTO(
                batch.getId(),
                batch.getBatchName(),
                trainer.getEmail(),
                trainer.getDisplayName()
        );
    }

    /* ================= INTERNAL CASCADE ================= */

    @Transactional
    public void deleteAllBatchesUnderBranch(Long branchId) {

        List<Batch> batches = batchRepository.findByBranchId(branchId);

        for (Batch batch : batches) {
            deleteBatch(batch.getId());
        }

        System.out.println("🧹 ALL BATCHES DELETED UNDER BRANCH -> " + branchId);
    }

    /* ================= ORG SUMMARY ================= */
    @Cacheable(value = "org:summary", key = "#orgId")
    public Map<String, Object> getOrgSummary(String orgId) {
        long totalDepts    = departmentRepository.countByOrganizationId(orgId);
        long totalBranches = branchRepository.countByOrganizationId(orgId);
        long totalBatches  = batchRepository.countByOrganizationId(orgId);

        Map<String, Object> result = new HashMap<>();
        result.put("currentDepartments", totalDepts);
        result.put("currentBranches",    totalBranches);
        result.put("currentBatches",     totalBatches);
        return result;
    }

    /* ================= SUPERADMIN — NO enforcement ================= */

    public List<BatchResponseDTO> getGlobalBatches() {
        List<Batch> batches = batchRepository.findByOrganizationIdIsNull();
        List<Long> batchIds = batches.stream().map(Batch::getId).toList();
        Map<Long, String> trainerByBatchId = loadTrainerByBatchId(batchIds);

        return batches.stream().map(batch -> mapGlobalDtoWithTrainer(batch, trainerByBatchId)).toList();
    }

    public List<TrainerDTO> getAvailableTrainersGlobal(Long batchId) {
        List<TrainerDTO> all = userClient.getTrainersWithoutOrg("TRAINER").getContent();

        List<String> assigned = mappingRepo.findByBatchId(batchId)
                .stream()
                .map(BatchTrainerStudent::getTrainerEmail)
                .distinct()
                .toList();

        return all.stream().filter(t -> !assigned.contains(t.getEmail())).toList();
    }

    public List<StudentDTO> getAvailableStudentsGlobal(Long batchId, String trainerEmail) {
        List<String> assignedAnywhere = mappingRepo.findAllAssignedStudentEmails();
        List<StudentDTO> all = userClient.getStudentsWithoutOrg("STUDENT").getContent();

        return all.stream()
                .filter(s -> !assignedAnywhere.contains(s.getEmail()))
                .toList();
    }

    public List<BatchResponseDTO> getBatchesByOrg(String organizationId) {
        List<Batch> batches = batchRepository.findByOrganizationId(organizationId);
        List<Long> batchIds = batches.stream().map(Batch::getId).toList();
        Map<Long, String> trainerByBatchId = loadTrainerByBatchId(batchIds);

        return batches.stream().map(batch -> mapGlobalDtoWithTrainer(batch, trainerByBatchId)).toList();
    }

    /* ================= UTIL ================= */

    private BatchResponseDTO map(Batch batch) {
        BatchResponseDTO dto = new BatchResponseDTO();
        dto.setId(batch.getId());
        dto.setBatchName(batch.getBatchName());
        dto.setBatchCode(batch.getBatchCode());
        dto.setBranchId(batch.getBranchId());
        dto.setDepartmentId(batch.getDepartmentId());
        dto.setOrganizationId(batch.getOrganizationId());
        dto.setTrainerEmail(batch.getTrainerEmail());
        dto.setActive(batch.isActive());
        return dto;
    }

    private Map<Long, String> loadTrainerByBatchId(List<Long> batchIds) {
        if (batchIds.isEmpty()) return Collections.emptyMap();
        return mappingRepo.findByBatchIdIn(batchIds)
                .stream()
                .filter(m -> m.getTrainerEmail() != null
                          && !m.getTrainerEmail().equals("__EMPTY__"))
                .collect(Collectors.toMap(
                    BatchTrainerStudent::getBatchId,
                    BatchTrainerStudent::getTrainerEmail,
                    (existing, replacement) -> existing
                ));
    }

    private BatchResponseDTO mapGlobalDtoWithTrainer(Batch batch,
                                                      Map<Long, String> trainerByBatchId) {
        BatchResponseDTO dto = new BatchResponseDTO();
        dto.setId(batch.getId());
        dto.setBatchName(batch.getBatchName());
        dto.setBatchCode(batch.getBatchCode());
        dto.setBranchId(batch.getBranchId());
        dto.setDepartmentId(batch.getDepartmentId());
        dto.setOrganizationId(batch.getOrganizationId());
        dto.setActive(batch.isActive());
        dto.setTrainerEmail(trainerByBatchId.get(batch.getId()));
        return dto;
    }

    private void enforceFeature(String organizationId, String userEmail, String featureKey) {
        if ((organizationId == null || organizationId.isBlank())
                && (userEmail == null || userEmail.isBlank())) {
            return;
        }
        flagsService.enforce(organizationId, userEmail, featureKey);
    }

    // ── CACHE EVICTION HELPERS ────────────────────────────────────────────────

    private void evictStudentCaches(String studentEmail) {
        String key = studentEmail.toLowerCase();
        evict("student:batch", key);
        evict("student:classroom", key);
    }

    private void evictTrainerBatchCache(String trainerEmail) {
        evict("batches:trainer", trainerEmail.toLowerCase());
    }

    private void evictOrgBatchCache(String organizationId) {
        evict("batches:org", organizationId);
    }

    private void evictOrgSummaryCache(String organizationId) {
        evict("org:summary", organizationId);
    }

    private void evictTrainerStudentsCache(Long batchId) {
        evict("batch:trainer-students", batchId);
    }

    private void evict(String cacheName, Object key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
        } else {
            System.err.println("⚠️ No cache named '" + cacheName + "' — eviction skipped for key=" + key);
        }
    }
}