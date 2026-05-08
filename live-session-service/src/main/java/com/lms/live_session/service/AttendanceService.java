package com.lms.live_session.service;

import com.lms.live_session.entity.SessionParticipant;
import com.lms.live_session.repository.ParticipantRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AttendanceService {

    private final ParticipantRepository participantRepository;

    public AttendanceService(ParticipantRepository participantRepository) {
        this.participantRepository = participantRepository;
    }

    // ✅ JOIN SESSION (EMAIL-BASED)
    public SessionParticipant joinSession(Long sessionId, String studentEmail) {

        // 🔒 Prevent duplicate join
        boolean alreadyJoined =
                participantRepository.existsBySessionIdAndStudentEmailAndLeaveTimeIsNull(
                        sessionId, studentEmail
                );

        if (alreadyJoined) {
            throw new RuntimeException("Student already joined this session");
        }

        SessionParticipant participant = new SessionParticipant();
        participant.setSessionId(sessionId);
        participant.setStudentEmail(studentEmail);
        participant.setJoinTime(LocalDateTime.now());
        participant.setStatus("JOINED");

        return participantRepository.save(participant);
    }

    // ✅ LEAVE SESSION (EMAIL-BASED)
    public SessionParticipant leaveSession(Long sessionId, String studentEmail) {

        SessionParticipant participant = participantRepository
                .findBySessionIdAndStudentEmailAndLeaveTimeIsNull(sessionId, studentEmail)
                .orElseThrow(() ->
                        new RuntimeException("Active participant not found")
                );

        participant.setLeaveTime(LocalDateTime.now());
        participant.setStatus("LEFT");

        return participantRepository.save(participant);
    }

    // ✅ GET ALL PARTICIPANTS
    public List<SessionParticipant> getSessionParticipants(Long sessionId) {
        return participantRepository.findBySessionId(sessionId);
    }
}