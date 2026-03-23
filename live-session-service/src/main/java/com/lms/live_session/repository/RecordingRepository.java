package com.lms.live_session.repository;

import com.lms.live_session.entity.Recording;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RecordingRepository extends JpaRepository<Recording, Long> {

    List<Recording> findByBatchId(Long batchId);

}