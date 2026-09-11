
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

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class MeetingTokenService {

    private final LiveKitConfig config;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MeetingTokenService(LiveKitConfig config) {
        this.config = config;
    }

    public String generateMeetingToken(String roomName, String identity, String displayName,
            boolean isHost, String avatarSeed, String sessionId) {

        AccessToken token = new AccessToken(config.getApiKey(), config.getApiSecret());
        token.setIdentity(identity);
        token.setName(displayName != null ? displayName : identity);
        token.addGrants(new RoomJoin(true), new RoomName(roomName));
        token.setMetadata(buildMetadata(isHost, avatarSeed, identity, sessionId));
        token.setTtl(Duration.ofHours(24).toMillis());   // <-- FIXED: 6h → 24h, gives refresh cycle proper safety margin
        return token.toJwt();
    }

    public String generateMeetingToken(String roomName, String identity, String displayName, boolean isHost) {
        return generateMeetingToken(roomName, identity, displayName, isHost, identity, UUID.randomUUID().toString());
    }

    public String generateMeetingToken(String roomName, String identity, String displayName,
            boolean isHost, String avatarSeed) {
        return generateMeetingToken(roomName, identity, displayName, isHost, avatarSeed, UUID.randomUUID().toString());
    }

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

                        // FIX: a participant can linger in LiveKit's room
                        // list for a few seconds AFTER they've genuinely
                        // disconnected (tracks already gone, but LiveKit
                        // hasn't removed them from the roster yet). Treat
                        // "present but zero active tracks" as a GHOST, not
                        // a real conflicting session — allow the rejoin.
                        int trackCount = p.getTracksCount();
                        if (trackCount == 0) {
                            System.out.println("ℹ️ Ghost participant detected for "
                                + normalizedEmail + " in room " + roomName
                                + " (0 tracks) — allowing rejoin.");
                            continue;
                        }

                        return true;
                    }
                } catch (Exception ignore) {
                }
            }
            return false;
        } catch (Exception e) {
            System.err.println("⚠️ Failed to check active participants for room " + roomName + ": " + e.getMessage());
            return false;
        }
    }

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