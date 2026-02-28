
package com.lms.chat.repository;
import com.lms.chat.entity.ChatBatchTrainer;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
public interface ChatBatchTrainerRepository
        extends JpaRepository<ChatBatchTrainer, Long> {

    Optional<ChatBatchTrainer> findByBatchId(Long batchId);
}
