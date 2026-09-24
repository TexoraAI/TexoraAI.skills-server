package com.lms.live_session.service;

import com.lms.live_session.service.MeetingTokenService;
import com.lms.live_session.entity.TexoraMeeting;
import com.lms.live_session.entity.TexoraMeetingStatus;
import com.lms.live_session.repository.TexoraMeetingRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Component
public class TexoraMeetingScheduler {

    private final TexoraMeetingRepository repository;
    private final MeetingTokenService tokenService;
    private final TexoraMeetingService texoraMeetingService;
    private final TexoraAnalysisService texoraAnalysisService;
    private final TexoraWebhookService webhookService;

    private static final int GRACE_MINUTES = 2;
    private static final int AUTO_EXTEND_MINUTES = 30;

    public TexoraMeetingScheduler(TexoraMeetingRepository repository,
                                   MeetingTokenService tokenService,
                                   TexoraMeetingService texoraMeetingService,
                                   TexoraAnalysisService texoraAnalysisService,
                                   TexoraWebhookService webhookService) {
        this.repository = repository;
        this.tokenService = tokenService;
        this.texoraMeetingService = texoraMeetingService;
        this.texoraAnalysisService = texoraAnalysisService;
        this.webhookService = webhookService;
    }

    @Scheduled(fixedRate = 120_000)
    public void closeOverdueMeetings() {
        LocalDateTime nowUtc = LocalDateTime.now(ZoneId.of("UTC"));

        List<TexoraMeeting> active = repository.findByStatus(TexoraMeetingStatus.ACTIVE);
        for (TexoraMeeting meeting : active) {
            LocalDateTime dueBy = meeting.getStartTimeUtc()
                    .plusMinutes(meeting.getDurationMinutes())
                    .plusMinutes(GRACE_MINUTES);

            if (!nowUtc.isAfter(dueBy)) {
                continue;
            }

            if (tokenService.isRoomStillActive(meeting.getRoomName())) {
                meeting.setDurationMinutes(meeting.getDurationMinutes() + AUTO_EXTEND_MINUTES);
                repository.save(meeting);
                System.out.println("[TexoraMeetingScheduler] " + meeting.getTexoraMeetingId()
                        + " still active — auto-extended " + AUTO_EXTEND_MINUTES + " min.");
                continue;
            }

            endMeeting(meeting, nowUtc);
        }

        List<TexoraMeeting> scheduled = repository.findByStatus(TexoraMeetingStatus.SCHEDULED);
        for (TexoraMeeting meeting : scheduled) {
            LocalDateTime dueBy = meeting.getStartTimeUtc()
                    .plusMinutes(meeting.getDurationMinutes())
                    .plusMinutes(GRACE_MINUTES);
            if (nowUtc.isAfter(dueBy)) {
                meeting.setStatus(TexoraMeetingStatus.NO_SHOW);
                repository.save(meeting);

                List<Map<String, Object>> attendance = texoraMeetingService.buildAttendanceData(meeting);
                webhookService.emitMeetingEnded(meeting, attendance);
            }
        }
    }

    private void endMeeting(TexoraMeeting meeting, LocalDateTime nowUtc) {
        texoraMeetingService.stopRecordingIfRunning(meeting);
        meeting.setStatus(TexoraMeetingStatus.ENDED);
        meeting.setEndedAt(nowUtc);
        repository.save(meeting);
        tokenService.closeRoom(meeting.getRoomName());

        List<Map<String, Object>> attendance = texoraMeetingService.buildAttendanceData(meeting);
        webhookService.emitMeetingEnded(meeting, attendance);
        texoraAnalysisService.processAsync(meeting);
    }
}