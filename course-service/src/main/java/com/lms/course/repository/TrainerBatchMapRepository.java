package com.lms.course.repository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.lms.course.model.TrainerBatchMap;

public interface TrainerBatchMapRepository extends JpaRepository<TrainerBatchMap, Long> {

    List<TrainerBatchMap> findByTrainerEmail(String email);
    void deleteByTrainerEmailAndBatchId(String email, Long batchId);
    void deleteByBatchId(Long batchId);
    boolean existsByTrainerEmailAndBatchId(String email, Long batchId);
}