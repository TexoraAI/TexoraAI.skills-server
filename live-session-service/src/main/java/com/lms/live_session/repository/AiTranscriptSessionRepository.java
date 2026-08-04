//package com.lms.live_session.repository;
//
//import com.lms.live_session.entity.AiTranscriptSession;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//
//@Repository
//public interface AiTranscriptSessionRepository extends JpaRepository<AiTranscriptSession, Long> {
//    List<AiTranscriptSession> findByTrainerEmailOrderByCreatedAtDesc(String trainerEmail);
//}
package com.lms.live_session.repository;
import com.lms.live_session.entity.AiTranscriptSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface AiTranscriptSessionRepository extends JpaRepository<AiTranscriptSession, Long> {
    List<AiTranscriptSession> findByTrainerEmailOrderByCreatedAtDesc(String trainerEmail);

    // NEW — used by AiWorkflowExecutionService's ac1 (save to notes) to find
    // an existing transcript session tied to a live session, most recent
    // first, in case more than one exists for the same liveSessionId.
    Optional<AiTranscriptSession> findFirstByLiveSessionIdOrderByStartedAtDesc(Long liveSessionId);
}