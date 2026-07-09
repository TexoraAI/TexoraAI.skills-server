

package com.lms.chat.repository;
import com.lms.chat.entity.ChatBatchTrainer;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
public interface ChatBatchTrainerRepository
        extends JpaRepository<ChatBatchTrainer, Long> {

    Optional<ChatBatchTrainer> findByBatchId(Long batchId);

    // Org-aware variant — used only when the authenticated user has a non-null organizationId
    Optional<ChatBatchTrainer> findByBatchIdAndOrganizationId(Long batchId, String organizationId);
    
    @Query("SELECT c.batchId FROM ChatBatchTrainer c WHERE c.organizationId IS NULL")
    List<Long> findBatchIdsWithNoOrganization();
}