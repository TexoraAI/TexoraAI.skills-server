package com.lms.live_session.service;

import com.lms.live_session.config.LiveKitConfig;
import io.livekit.server.AccessToken;
import io.livekit.server.RoomJoin;
import io.livekit.server.RoomName;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.Map;
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
     * @param identity    unique participant identity (e.g. user email)
     * @param displayName display name shown in the room
     * @param isHost      reserved for future host-only grants (recording, mute-all, etc.)
     */
//    public String generateMeetingToken(String roomName, String identity, String displayName, boolean isHost) {
//        AccessToken token = new AccessToken(config.getApiKey(), config.getApiSecret());
//
//        token.setIdentity(identity);
//        token.setName(displayName != null ? displayName : identity);
//
//        token.addGrants(new RoomJoin(true), new RoomName(roomName));
//
//        return token.toJwt();
//    }
    public String generateMeetingToken(String roomName, String identity, String displayName,
            boolean isHost, String avatarSeed) {
AccessToken token = new AccessToken(config.getApiKey(), config.getApiSecret());
token.setIdentity(identity);
token.setName(displayName != null ? displayName : identity);
token.addGrants(new RoomJoin(true), new RoomName(roomName));
token.setMetadata(buildMetadata(isHost, avatarSeed, identity));
return token.toJwt();
}

public String generateMeetingToken(String roomName, String identity, String displayName, boolean isHost) {
return generateMeetingToken(roomName, identity, displayName, isHost, identity);
}

private String buildMetadata(boolean isHost, String avatarSeed, String fallbackIdentity) {
String seed = (avatarSeed != null && !avatarSeed.isBlank()) ? avatarSeed : fallbackIdentity;
Map<String, Object> metadata = new LinkedHashMap<>();
metadata.put("isHost", isHost);
metadata.put("avatarSeed", seed);
try {
return objectMapper.writeValueAsString(metadata);
} catch (Exception e) {
return "{\"isHost\":" + isHost + ",\"avatarSeed\":\"" + seed.replace("\"", "") + "\"}";
}
}
}