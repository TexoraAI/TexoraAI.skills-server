package com.lms.live_session.service;

import com.lms.live_session.config.LiveKitConfig;
import io.livekit.server.AccessToken;
import io.livekit.server.RoomJoin;
import io.livekit.server.RoomName;
import io.livekit.server.RoomServiceClient;
import livekit.LivekitModels.ParticipantInfo;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Meeting module's own LiveKit token issuance. Reuses the existing
 * LiveKitConfig bean (api key / secret) only — does NOT call into
 * LiveKitTokenService or any Live Session business logic, so the
 * Meeting flows stay fully independent of Live Session.
 */
@Service
public class MeetingTokenService {

    private final LiveKitConfig config;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MeetingTokenService(LiveKitConfig config) {
        this.config = config;
    }

    /**
     * @param roomName    LiveKit room name for the meeting (meeting.getRoomName())
     * @param identity    STABLE participant identity (e.g. user email / guestIdentity).
     *                    Must stay the same across reconnects for the same person in
     *                    the same meeting — this is what lets LiveKit cleanly replace
     *                    a stale prior connection instead of leaving a ghost behind.
     * @param displayName display name shown in the room
     * @param isHost      reserved for future host-only grants (recording, mute-all, etc.)
     * @param avatarSeed  seed for avatar generation
     * @param sessionId   per-token-request id, kept in metadata only (for debugging/logs),
     *                    never appended to identity anymore
     */
    public String generateMeetingToken(String roomName, String identity, String displayName,
            boolean isHost, String avatarSeed, String sessionId) {

        AccessToken token = new AccessToken(config.getApiKey(), config.getApiSecret());
        token.setIdentity(identity);   // stable — no more "+ sessionId"
        token.setName(displayName != null ? displayName : identity);
        token.addGrants(new RoomJoin(true), new RoomName(roomName));
        token.setMetadata(buildMetadata(isHost, avatarSeed, identity, sessionId)); // sessionId kept here only, for logs
        return token.toJwt();
    }

    /**
     * ✅ OVERLOADED: Backward compatibility - generates sessionId automatically
     */
    public String generateMeetingToken(String roomName, String identity, String displayName, boolean isHost) {
        return generateMeetingToken(roomName, identity, displayName, isHost, identity, UUID.randomUUID().toString());
    }

    /**
     * ✅ OVERLOADED: Another backward compatibility version
     */
    public String generateMeetingToken(String roomName, String identity, String displayName,
            boolean isHost, String avatarSeed) {
        return generateMeetingToken(roomName, identity, displayName, isHost, avatarSeed, UUID.randomUUID().toString());
    }

    /**
     * Live presence check — asks LiveKit who is currently connected
     * to this room right now, and checks each connected participant's
     * metadata (where the email is stored as avatarSeed) for a match.
     * This catches a duplicate email join even if the earlier session
     * crashed/closed without a clean disconnect, since it's checked
     * against LiveKit's live room state, not a DB flag we'd have to keep
     * in sync ourselves.
     *
     * Fails open (returns false) if the LiveKit call itself errors out —
     * we never want a LiveKit API hiccup to block every single join.
     */
    public boolean isEmailAlreadyInRoom(String roomName, String email) {
        if (email == null || email.isBlank()) return false;
        String normalizedEmail = email.trim().toLowerCase();
        try {
            RoomServiceClient client = RoomServiceClient.createClient(
                    config.getUrl(), config.getApiKey(), config.getApiSecret());
            retrofit2.Response<List<ParticipantInfo>> response =
                    client.listParticipants(roomName).execute();

            if (!response.isSuccessful() || response.body() == null) {
                return false;
            }

            for (ParticipantInfo p : response.body()) {
                String metadata = p.getMetadata();
                if (metadata == null || metadata.isBlank()) continue;
                try {
                    JsonNode node = objectMapper.readTree(metadata);
                    String avatarSeed = node.has("avatarSeed") ? node.get("avatarSeed").asText() : null;
                    if (avatarSeed != null && avatarSeed.trim().equalsIgnoreCase(normalizedEmail)) {
                        return true;
                    }
                } catch (Exception ignore) {
                    // malformed metadata on some participant — skip, don't fail the whole check
                }
            }
            return false;
        } catch (Exception e) {
            System.err.println("⚠️ Failed to check active participants for room " + roomName + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Actually closes a LiveKit room. Call this from endMeeting()/deleteMeeting()
     * so an ended or deleted meeting doesn't leave an orphaned room still running
     * on the LiveKit server. This only touches live LiveKit connections/room
     * state — it never touches recordings, summaries, or any other DB data.
     */
    public void closeRoom(String roomName) {
        if (roomName == null || roomName.isBlank()) return;
        try {
            RoomServiceClient client = RoomServiceClient.createClient(
                    config.getUrl(), config.getApiKey(), config.getApiSecret());
            client.deleteRoom(roomName).execute();
        } catch (Exception e) {
            System.err.println("⚠️ Failed to close LiveKit room " + roomName + ": " + e.getMessage());
        }
    }

    private String buildMetadata(boolean isHost, String avatarSeed, String fallbackIdentity, String sessionId) {
        String seed = (avatarSeed != null && !avatarSeed.isBlank()) ? avatarSeed : fallbackIdentity;
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("isHost", isHost);
        metadata.put("avatarSeed", seed);
        metadata.put("sessionId", sessionId);
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (Exception e) {
            return "{\"isHost\":" + isHost + ",\"avatarSeed\":\"" + seed.replace("\"", "") + "\"}";
        }
    }
}