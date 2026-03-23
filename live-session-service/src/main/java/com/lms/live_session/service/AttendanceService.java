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

    public SessionParticipant joinSession(Long sessionId, Long studentId) {

        SessionParticipant participant = new SessionParticipant();
        participant.setSessionId(sessionId);
        participant.setStudentId(studentId);
        participant.setJoinTime(LocalDateTime.now());

        return participantRepository.save(participant);
    }

    public SessionParticipant leaveSession(Long participantId) {

        SessionParticipant participant = participantRepository
                .findById(participantId)
                .orElseThrow(() -> new RuntimeException("Participant not found"));

        participant.setLeaveTime(LocalDateTime.now());

        return participantRepository.save(participant);
    }

    public List<SessionParticipant> getSessionParticipants(Long sessionId) {

        return participantRepository.findBySessionId(sessionId);
    }
}