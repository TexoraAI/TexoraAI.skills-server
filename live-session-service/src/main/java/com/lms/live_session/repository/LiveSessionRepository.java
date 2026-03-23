package com.lms.live_session.repository;

import com.lms.live_session.entity.LiveSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LiveSessionRepository extends JpaRepository<LiveSession, Long> {

    List<LiveSession> findByBatchId(Long batchId);
   

    List<LiveSession> findByBatchIdIn(List<Long> batchIds);

    void deleteByBatchId(Long batchId);
    List<LiveSession> findByBatchIdAndStatus(Long batchId, String status);
}
