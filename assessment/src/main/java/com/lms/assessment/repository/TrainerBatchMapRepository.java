//package com.lms.assessment.repository;
//
//import com.lms.assessment.model.TrainerBatchMap;
//import org.springframework.data.jpa.repository.JpaRepository;
//import java.util.List;
//public interface TrainerBatchMapRepository extends JpaRepository<TrainerBatchMap, Long> {
//
//    boolean existsByTrainerEmailAndBatchId(String email, Long batchId);
//
//    void deleteByTrainerEmailAndBatchId(String email, Long batchId);
//
//    void deleteByBatchId(Long batchId);
//
//    void deleteByTrainerEmail(String email);
//    
//    List<TrainerBatchMap> findByTrainerEmail(String email);
//
//}
package com.lms.assessment.repository;

import com.lms.assessment.model.TrainerBatchMap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrainerBatchMapRepository extends JpaRepository<TrainerBatchMap, Long> {
    boolean existsByTrainerEmailAndBatchId(String email, Long batchId);
    void deleteByTrainerEmailAndBatchId(String email, Long batchId);
    void deleteByBatchId(Long batchId);
    void deleteByTrainerEmail(String email);

    List<TrainerBatchMap> findByTrainerEmail(String email);

    // 🏢 Multi-tenancy — org-scoped lookups/deletes
    List<TrainerBatchMap> findByOrganizationIdAndTrainerEmail(String organizationId, String email);

    // Used to resolve a batch's organizationId as the source of truth when a
    // student is assigned to that same batch (see BatchAssignmentConsumer TODO).
    Optional<TrainerBatchMap> findFirstByBatchId(Long batchId);

    void deleteByOrganizationIdAndBatchId(String organizationId, Long batchId);
}