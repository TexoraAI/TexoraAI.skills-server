package com.lms.live_session.controller;

import com.lms.live_session.service.MeetingTokenService;
import io.livekit.server.WebhookReceiver;
import livekit.LivekitWebhook.WebhookEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LiveKitWebhookController {

    private final WebhookReceiver webhookReceiver;
    private final MeetingTokenService meetingTokenService;

    public LiveKitWebhookController(
            @Value("${livekit.api-key}") String apiKey,
            @Value("${livekit.api-secret}") String apiSecret,
            MeetingTokenService meetingTokenService) {
        this.webhookReceiver = new WebhookReceiver(apiKey, apiSecret);
        this.meetingTokenService = meetingTokenService;
    }

    @PostMapping("/api/livekit/webhook")
    public void handleWebhook(
    		@RequestHeader("Authorization") String authHeader,
            @RequestBody String body) {
        try {
            WebhookEvent event = webhookReceiver.receive(body, authHeader);
            String eventType = event.getEvent();

            switch (eventType) {
                case "room_finished" -> {
                    String roomName = event.getRoom().getName();
                    System.out.println("✅ Room finished, closing: " + roomName);
                    meetingTokenService.closeRoom(roomName);
                }
                case "track_unpublished" -> {
                    System.out.println("⚠️ [AUDIO-DEBUG] track_unpublished — room="
                        + event.getRoom().getName()
                        + " participant=" + event.getParticipant().getIdentity()
                        + " identity=" + event.getParticipant().getIdentity()
                        + " track=" + event.getTrack().getSid()
                        + " kind=" + event.getTrack().getType());
                }
                case "track_published" -> {
                    System.out.println("✅ [AUDIO-DEBUG] track_published — room="
                        + event.getRoom().getName()
                        + " participant=" + event.getParticipant().getIdentity()
                        + " track=" + event.getTrack().getSid()
                        + " kind=" + event.getTrack().getType());
                }
                case "participant_left" -> {
                    System.out.println("👋 [AUDIO-DEBUG] participant_left — room="
                        + event.getRoom().getName()
                        + " participant=" + event.getParticipant().getIdentity());
                }
                case "participant_joined" -> {
                    System.out.println("👋 [AUDIO-DEBUG] participant_joined — room="
                        + event.getRoom().getName()
                        + " participant=" + event.getParticipant().getIdentity());
                }
                default -> {
                    // other events ignored for now, but no longer silent-swallowed
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Webhook verification/processing failed: " + e.getMessage());
        }
    }
}