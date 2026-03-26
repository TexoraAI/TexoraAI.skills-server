package com.lms.live_session.service;

import com.lms.live_session.config.LiveKitConfig;
import io.livekit.server.AccessToken;
import io.livekit.server.RoomJoin;
import io.livekit.server.RoomName;
import org.springframework.stereotype.Service;

@Service
public class LiveKitTokenService {

    private final LiveKitConfig config;

    public LiveKitTokenService(LiveKitConfig config) {
        this.config = config;
    }

    public String generateTrainerToken(Long sessionId) {

        String roomName = "session-" + sessionId;

        AccessToken token = new AccessToken(
                config.getApiKey(),
                config.getApiSecret()
        );

        token.setIdentity("trainer-" + sessionId);
        token.setName("Trainer");

        // ✅ Fixed: RoomName is a separate grant
        token.addGrants(new RoomJoin(true), new RoomName(roomName));

        return token.toJwt();
    }

    public String generateStudentToken(Long sessionId, Long studentId) {

        String roomName = "session-" + sessionId;

        AccessToken token = new AccessToken(
                config.getApiKey(),
                config.getApiSecret()
        );

        token.setIdentity("student-" + studentId);
        token.setName("Student-" + studentId);

        // ✅ Fixed: RoomName is a separate grant
        token.addGrants(new RoomJoin(true), new RoomName(roomName));

        return token.toJwt();
    }
    public String generateCallToken(String identity, String roomName) {

        AccessToken token = new AccessToken(
                config.getApiKey(),
                config.getApiSecret()
        );

        token.setIdentity(identity);
        token.setName(identity);

        token.addGrants(new RoomJoin(true), new RoomName(roomName));

        return token.toJwt();
    }
}