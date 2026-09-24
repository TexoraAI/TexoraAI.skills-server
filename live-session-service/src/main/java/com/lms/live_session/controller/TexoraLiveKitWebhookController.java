package com.lms.live_session.controller;

import com.lms.live_session.service.EgressService;
import com.lms.live_session.entity.TexoraMeeting;
import com.lms.live_session.entity.TexoraParticipant;
import com.lms.live_session.repository.TexoraMeetingRepository;
import com.lms.live_session.repository.TexoraParticipantRepository;
import io.livekit.server.WebhookReceiver;
import livekit.LivekitWebhook.WebhookEvent;
import livekit.LivekitModels.TrackType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/texorameetings/livekit-webhook")
public class TexoraLiveKitWebhookController {

    private final TexoraParticipantRepository participantRepository;
    private final TexoraMeetingRepository meetingRepository;
    private final EgressService egressService;

    @Value("${livekit.api-key}")
    private String apiKey;
    @Value("${livekit.api-secret}")
    private String apiSecret;

    public TexoraLiveKitWebhookController(TexoraParticipantRepository participantRepository,
                                           TexoraMeetingRepository meetingRepository,
                                           EgressService egressService) {
        this.participantRepository = participantRepository;
        this.meetingRepository = meetingRepository;
        this.egressService = egressService;
    }

    // LiveKit sends Content-Type: application/webhook+json — accept any
    // content type here since Spring may not recognize that MIME type,
    // and we need the raw body string regardless (WebhookReceiver needs
    // the raw, un-parsed string to verify the signature itself).
    @PostMapping
    public ResponseEntity<Void> handle(@RequestHeader("Authorization") String authHeader,
                                        @RequestBody String rawBody) {
        try {
            WebhookReceiver receiver = new WebhookReceiver(apiKey, apiSecret);
            WebhookEvent event = receiver.receive(rawBody, authHeader);

            if (!"track_published".equals(event.getEvent())) {
                return ResponseEntity.ok().build();
            }

            var track = event.getTrack();
            if (track.getType() != TrackType.AUDIO) {
                return ResponseEntity.ok().build();
            }

            String roomName = event.getRoom().getName();
            String identity = event.getParticipant().getIdentity();
            String trackSid = track.getSid();

            if (!roomName.startsWith("texora-meeting-")) {
                return ResponseEntity.ok().build();
            }

            TexoraMeeting meeting = meetingRepository.findByRoomName(roomName).orElse(null);
            if (meeting == null) {
                return ResponseEntity.ok().build();
            }

            TexoraParticipant participant = participantRepository
                    .findByTexoraMeetingIdAndIdentityAndLeftAtIsNull(meeting.getTexoraMeetingId(), identity)
                    .orElse(null);
            if (participant == null) {
                // Track published before our join-token flow recorded the
                // participant row — race condition, safe to just skip;
                // that participant's audio won't be individually recorded
                // for this join, but the mixed room recording still has them.
                return ResponseEntity.ok().build();
            }

            var result = egressService.startTrackEgress(roomName, trackSid, identity);
            if (result != null) {
                participant.setAudioEgressId(result.egressId);
                participant.setTrackSid(trackSid);
                participantRepository.save(participant);
            }

        } catch (Exception e) {
            System.err.println("[TexoraLiveKitWebhookController] Failed to process webhook: " + e.getMessage());
        }
        return ResponseEntity.ok().build();
    }
}