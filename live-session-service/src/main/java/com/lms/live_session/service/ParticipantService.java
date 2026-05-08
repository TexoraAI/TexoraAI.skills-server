//// service/ParticipantService.java
//package com.lms.live_session.service;
//
//import com.lms.live_session.entity.SessionParticipant;
//import com.lms.live_session.repository.ParticipantRepository;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.time.temporal.ChronoUnit;
//import java.util.List;
//
//@Service
//public class ParticipantService {
//
//    private final ParticipantRepository repository;
//
//    public ParticipantService(ParticipantRepository repository) {
//        this.repository = repository;
//    }
//
//    // ✅ Student joins session
//    public SessionParticipant joinSession(Long sessionId, Long studentId) {
//        // Check if already joined
//        List<SessionParticipant> existing = repository.findBySessionId(sessionId);
//        boolean alreadyJoined = existing.stream()
//            .anyMatch(p -> p.getStudentId().equals(studentId) && p.getLeaveTime() == null);
//
//        if (alreadyJoined) {
//            throw new RuntimeException("Student already in session");
//        }
//
//        SessionParticipant participant = new SessionParticipant();
//        participant.setSessionId(sessionId);
//        participant.setStudentId(studentId);
//        participant.setJoinTime(LocalDateTime.now());
//
//        return repository.save(participant);
//    }
//
//    // ✅ Student leaves session
//    public SessionParticipant leaveSession(Long sessionId, Long studentId) {
//        List<SessionParticipant> records = repository.findBySessionId(sessionId);
//
//        SessionParticipant participant = records.stream()
//            .filter(p -> p.getStudentId().equals(studentId) && p.getLeaveTime() == null)
//            .findFirst()
//            .orElseThrow(() -> new RuntimeException("Participant record not found"));
//
//        participant.setLeaveTime(LocalDateTime.now());
//
//        // Calculate watch percentage (compared to session duration)
//        long minutesAttended = ChronoUnit.MINUTES.between(
//            participant.getJoinTime(), participant.getLeaveTime()
//        );
//        participant.setWatchPercentage((int) Math.min(minutesAttended, 100));
//
//        return repository.save(participant);
//    }
//
//    // ✅ Get all participants for a session
//    public List<SessionParticipant> getSessionParticipants(Long sessionId) {
//        return repository.findBySessionId(sessionId);
//    }
//}
// service/ParticipantService.java
package com.lms.live_session.service;

import com.lms.live_session.entity.SessionParticipant;
import com.lms.live_session.repository.ParticipantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ParticipantService {

    private final ParticipantRepository repository;

    public ParticipantService(ParticipantRepository repository) {
        this.repository = repository;
    }

    // ─────────────────────────────────────────────────────────
    // Student joins session
    // Called from frontend: participantJoin(sessionId, studentEmail)
    // ─────────────────────────────────────────────────────────
    @Transactional
    public SessionParticipant joinSession(
            Long sessionId,
            Long batchId,
            String studentEmail,
            String trainerEmail) {

        // ✅ Check if already actively joined (same pattern as feedback duplicate check)
        if (repository.existsBySessionIdAndStudentEmailAndLeaveTimeIsNull(
                sessionId, studentEmail)) {
            throw new RuntimeException(
                "Student " + studentEmail + " is already in session " + sessionId
            );
        }

        SessionParticipant participant = new SessionParticipant();
        participant.setSessionId(sessionId);
        participant.setBatchId(batchId);
        participant.setStudentEmail(studentEmail);
        participant.setTrainerEmail(trainerEmail);
        // joinTime and status set by @PrePersist

        SessionParticipant saved = repository.save(participant);
        System.out.println("✅ Participant joined: " + studentEmail + " → session " + sessionId);
        return saved;
    }

    // ─────────────────────────────────────────────────────────
    // Student leaves session — calculates duration + watch %
    // ─────────────────────────────────────────────────────────
    @Transactional
    public SessionParticipant leaveSession(Long sessionId, String studentEmail) {

        SessionParticipant participant = repository
            .findBySessionIdAndStudentEmailAndLeaveTimeIsNull(sessionId, studentEmail)
            .orElseThrow(() -> new RuntimeException(
                "No active participant record for " + studentEmail + " in session " + sessionId
            ));

        participant.setLeaveTime(LocalDateTime.now());
        participant.setStatus("LEFT");

        // ✅ Calculate minutes attended
        long minutesAttended = ChronoUnit.MINUTES.between(
            participant.getJoinTime(),
            participant.getLeaveTime()
        );
        participant.setWatchPercentage((int) Math.min(minutesAttended, 100));

        SessionParticipant saved = repository.save(participant);
        System.out.println("✅ Participant left: " + studentEmail
            + " — attended " + minutesAttended + " min");
        return saved;
    }

    // ─────────────────────────────────────────────────────────
    // Get all participants for a session (attendance report)
    // ─────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<SessionParticipant> getSessionParticipants(Long sessionId) {
        return repository.findBySessionIdOrderByJoinTimeDesc(sessionId);
    }

    // ─────────────────────────────────────────────────────────
    // Get all sessions a student attended (by email)
    // ─────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<SessionParticipant> getStudentHistory(String studentEmail) {
        return repository.findByStudentEmailOrderByJoinTimeDesc(studentEmail);
    }

    // ─────────────────────────────────────────────────────────
    // Get all attendees for a trainer's sessions (by email)
    // ─────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<SessionParticipant> getTrainerSessionHistory(
            String trainerEmail, Long batchId) {
        return repository.findByTrainerEmailAndBatchId(trainerEmail, batchId);
    }

    // ─────────────────────────────────────────────────────────
    // Count currently active (joined but not left) participants
    // ─────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public long getActiveCount(Long sessionId) {
        return repository.countActiveBySession(sessionId);
    }

    // ─────────────────────────────────────────────────────────
    // Check if student already joined a session
    // ─────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public boolean hasJoined(Long sessionId, String studentEmail) {
        return repository.existsBySessionIdAndStudentEmailAndLeaveTimeIsNull(
            sessionId, studentEmail
        );
    }
}