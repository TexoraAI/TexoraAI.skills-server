//
//package com.lms.live_session.service;
//
//import io.livekit.server.EgressServiceClient;
//import io.livekit.server.RoomServiceClient;
//import livekit.LivekitEgress.*;
//import livekit.LivekitModels.Room;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//@Service
//public class EgressService {
//
//    @Value("${livekit.url}")
//    private String livekitUrl;
//    @Value("${livekit.api-key}")
//    private String apiKey;
//    @Value("${livekit.api-secret}")
//    private String apiSecret;
//    @Value("${aws.s3.bucket}")
//    private String bucket;
//    @Value("${aws.access-key}")
//    private String awsAccessKey;
//    @Value("${aws.secret-key}")
//    private String awsSecretKey;
//    @Value("${aws.region}")
//    private String awsRegion;
//
//    private EgressServiceClient buildEgressClient() {
//        return EgressServiceClient.createClient(livekitUrl, apiKey, apiSecret);
//    }
//
//    private RoomServiceClient buildRoomClient() {
//        return RoomServiceClient.createClient(livekitUrl, apiKey, apiSecret);
//    }
//
//    // ✅ NEW — fixes "requested room does not exist"
//    private boolean ensureRoomExists(String roomName) {
//        try {
//            RoomServiceClient client = buildRoomClient();
//            retrofit2.Response<Room> response = client.createRoom(roomName).execute();
//            if (!response.isSuccessful()) {
//                System.err.println("❌ createRoom failed: "
//                    + (response.errorBody() != null ? response.errorBody().string() : "null body"));
//                return false;
//            }
//            System.out.println("✅ Room ensured: " + roomName);
//            return true;
//        } catch (Exception e) {
//            System.err.println("❌ Failed to ensure room exists: " + e.getMessage());
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    // ✅ CHANGED SIGNATURE — added fileSuffix param so each start/stop cycle gets its own file
//    public String startRecording(Long sessionId, String fileSuffix) {
//        String roomName = "session-" + sessionId;
//        try {
//            if (!ensureRoomExists(roomName)) {
//                System.err.println("❌ Cannot start egress — room could not be created/verified: " + roomName);
//                return null;
//            }
//
//            EgressServiceClient client = buildEgressClient();
//            S3Upload s3 = S3Upload.newBuilder()
//                .setAccessKey(awsAccessKey)
//                .setSecret(awsSecretKey)
//                .setBucket(bucket)
//                .setRegion(awsRegion)
//                .build();
//
//            EncodedFileOutput fileOutput = EncodedFileOutput.newBuilder()
//                .setFileType(EncodedFileType.MP4)
//                .setFilepath("recordings/session-" + sessionId + "-" + fileSuffix)
//                .setS3(s3)
//                .build();
//
//            retrofit2.Response<EgressInfo> response = client
//                .startRoomCompositeEgress(roomName, fileOutput)
//                .execute();
//
////            if (!response.isSuccessful() || response.body() == null) {
////                System.err.println("❌ Egress start failed: "
////                    + (response.errorBody() != null ? response.errorBody().string() : "null body"));
////                return null;
////            }
//            System.out.println("HTTP Status: " + response.code());
//
//            if (!response.isSuccessful()) {
//                System.out.println("===== EGRESS ERROR =====");
//
//                if (response.errorBody() != null) {
//                    System.out.println(response.errorBody().string());
//                }
//
//                return null;
//            }
//
//            if (response.body() == null) {
//                System.out.println("Response body is NULL");
//                return null;
//            }
//
//            System.out.println("Egress State: " + response.body().getStatus());
//            System.out.println("Egress ID: " + response.body().getEgressId());
//
//            String egressId = response.body().getEgressId();
//            System.out.println("✅ Egress started: " + egressId + " for room " + roomName);
//            return egressId;
//
//        } catch (Exception e) {
//            System.err.println("❌ Failed to start egress: " + e.getMessage());
//            e.printStackTrace();
//            return null;
//        }
//    }
//
//    // ✅ CHANGED — now returns boolean so caller knows if stop actually succeeded
//
//    public boolean stopRecording(String egressId) {
//        if (egressId == null) return false;
//        try {
//            EgressServiceClient client = buildEgressClient();
//            retrofit2.Response<EgressInfo> response = client.stopEgress(egressId).execute();
//
//            if (response.isSuccessful()) {
//                System.out.println("✅ Egress stopped: " + egressId);
//                return true;
//            }
//
//            String errorBody = "";
//            try {
//                errorBody = response.errorBody() != null ? response.errorBody().string() : "";
//            } catch (Exception readEx) {
//                System.err.println("⚠️ Could not read stopEgress error body: " + readEx.getMessage());
//            }
//
//            boolean alreadyGone = errorBody.contains("\"code\":\"not_found\"")
//                || errorBody.contains("\"code\":\"failed_precondition\"")
//                || errorBody.contains("EGRESS_FAILED")
//                || errorBody.toLowerCase().contains("egress not found")
//                || errorBody.toLowerCase().contains("cannot be stopped");
//
//            if (alreadyGone) {
//                System.out.println("⚠️ stopEgress reported egress already gone/failed for " + egressId
//                    + " — treating as stopped. LiveKit response: " + errorBody);
//                return true;
//            }
//
//            System.err.println("❌ Stop egress failed: " + errorBody);
//            return false;
//
//        } catch (Exception e) {
//            System.err.println("❌ Failed to stop egress: " + e.getMessage());
//            e.printStackTrace();
//            return false;
//        }
//    }
//}


package com.lms.live_session.service;

import io.livekit.server.EgressServiceClient;
import io.livekit.server.RoomServiceClient;
import livekit.LivekitEgress.*;
import livekit.LivekitModels.Room;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EgressService {

    @Value("${livekit.url}")
    private String livekitUrl;
    @Value("${livekit.api-key}")
    private String apiKey;
    @Value("${livekit.api-secret}")
    private String apiSecret;
    @Value("${aws.s3.bucket}")
    private String bucket;
    @Value("${aws.access-key}")
    private String awsAccessKey;
    @Value("${aws.secret-key}")
    private String awsSecretKey;
    @Value("${aws.region}")
    private String awsRegion;
    
    private static final int MAX_START_RETRIES = 2;
    private static final long RETRY_DELAY_MS = 6000;

    private EgressServiceClient buildEgressClient() {
        return EgressServiceClient.createClient(livekitUrl, apiKey, apiSecret);
    }

    private RoomServiceClient buildRoomClient() {
        return RoomServiceClient.createClient(livekitUrl, apiKey, apiSecret);
    }

    private boolean ensureRoomExists(String roomName) {
        try {
            RoomServiceClient client = buildRoomClient();
            retrofit2.Response<Room> response = client.createRoom(roomName).execute();
            if (!response.isSuccessful()) {
                System.err.println("❌ createRoom failed: "
                    + (response.errorBody() != null ? response.errorBody().string() : "null body"));
                return false;
            }
            System.out.println("✅ Room ensured: " + roomName);
            return true;
        } catch (Exception e) {
            System.err.println("❌ Failed to ensure room exists: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ✅ NEW — small result holder so callers get BOTH the egressId and the
    // exact fileSuffix used to build the S3 filepath. This is the single
    // source of truth for what the file is actually named in S3.
    public static class EgressStartResult {
        public final String egressId;
        public final String fileSuffix;
        public EgressStartResult(String egressId, String fileSuffix) {
            this.egressId = egressId;
            this.fileSuffix = fileSuffix;
        }
    }

    // ✅ CHANGED — no longer takes fileSuffix as a param; generates it
    // internally and returns it back to the caller so nobody has to
    // reconstruct the filename later.
//    public EgressStartResult startRecording(Long sessionId) {
//        String roomName = "session-" + sessionId;
//        String fileSuffix = String.valueOf(System.currentTimeMillis());
//        try {
//            if (!ensureRoomExists(roomName)) {
//                System.err.println("❌ Cannot start egress — room could not be created/verified: " + roomName);
//                return null;
//            }
//
//            EgressServiceClient client = buildEgressClient();
//            S3Upload s3 = S3Upload.newBuilder()
//                .setAccessKey(awsAccessKey)
//                .setSecret(awsSecretKey)
//                .setBucket(bucket)
//                .setRegion(awsRegion)
//                .build();
//
//            EncodedFileOutput fileOutput = EncodedFileOutput.newBuilder()
//                .setFileType(EncodedFileType.MP4)
//                .setFilepath("recordings/session-" + sessionId + "-" + fileSuffix)
//                .setS3(s3)
//                .build();
//
//            retrofit2.Response<EgressInfo> response = client
//                .startRoomCompositeEgress(roomName, fileOutput)
//                .execute();
//
//            System.out.println("HTTP Status: " + response.code());
//
//            if (!response.isSuccessful()) {
//                System.out.println("===== EGRESS ERROR =====");
//                if (response.errorBody() != null) {
//                    System.out.println(response.errorBody().string());
//                }
//                return null;
//            }
//
//            if (response.body() == null) {
//                System.out.println("Response body is NULL");
//                return null;
//            }
//
//            System.out.println("Egress State: " + response.body().getStatus());
//            System.out.println("Egress ID: " + response.body().getEgressId());
//
//            String egressId = response.body().getEgressId();
//            System.out.println("✅ Egress started: " + egressId + " for room " + roomName
//                + " fileSuffix=" + fileSuffix);
//            return new EgressStartResult(egressId, fileSuffix);
//
//        } catch (Exception e) {
//            System.err.println("❌ Failed to start egress: " + e.getMessage());
//            e.printStackTrace();
//            return null;
//        }
//    }
    // 6s between retries

    // ✅ CHANGED — public method now retries on failure instead of giving up immediately
    public EgressStartResult startRecording(Long sessionId) {
        for (int attempt = 1; attempt <= MAX_START_RETRIES; attempt++) {
            EgressStartResult result = attemptStartRecording(sessionId, attempt);
            if (result != null) {
                return result;
            }
            if (attempt < MAX_START_RETRIES) {
                System.out.println("⏳ Retrying egress start for session " + sessionId
                    + " in " + (RETRY_DELAY_MS / 1000) + "s (attempt " + (attempt + 1) + "/" + MAX_START_RETRIES + ")");
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }
        System.err.println("❌ All egress start attempts failed for session " + sessionId);
        return null;
    }

    // ✅ NEW — this is your original startRecording() body, unchanged logic,
    // just renamed and made private so the retry wrapper above can call it.
    private EgressStartResult attemptStartRecording(Long sessionId, int attemptNumber) {
        String roomName = "session-" + sessionId;
        String fileSuffix = String.valueOf(System.currentTimeMillis());
        try {
            if (!ensureRoomExists(roomName)) {
                System.err.println("❌ Cannot start egress — room could not be created/verified: " + roomName
                    + " (attempt " + attemptNumber + ")");
                return null;
            }

            EgressServiceClient client = buildEgressClient();
            S3Upload s3 = S3Upload.newBuilder()
                .setAccessKey(awsAccessKey)
                .setSecret(awsSecretKey)
                .setBucket(bucket)
                .setRegion(awsRegion)
                .build();

            EncodedFileOutput fileOutput = EncodedFileOutput.newBuilder()
                .setFileType(EncodedFileType.MP4)
                .setFilepath("recordings/session-" + sessionId + "-" + fileSuffix)
                .setS3(s3)
                .build();

            retrofit2.Response<EgressInfo> response = client
                .startRoomCompositeEgress(roomName, fileOutput)
                .execute();

            System.out.println("HTTP Status: " + response.code() + " (attempt " + attemptNumber + ")");

            if (!response.isSuccessful()) {
                System.out.println("===== EGRESS ERROR (attempt " + attemptNumber + ") =====");
                if (response.errorBody() != null) {
                    System.out.println(response.errorBody().string());
                }
                return null;
            }

            if (response.body() == null) {
                System.out.println("Response body is NULL (attempt " + attemptNumber + ")");
                return null;
            }

            System.out.println("Egress State: " + response.body().getStatus());
            System.out.println("Egress ID: " + response.body().getEgressId());

            String egressId = response.body().getEgressId();
            System.out.println("✅ Egress started: " + egressId + " for room " + roomName
                + " fileSuffix=" + fileSuffix + " (attempt " + attemptNumber + ")");
            return new EgressStartResult(egressId, fileSuffix);

        } catch (Exception e) {
            System.err.println("❌ Failed to start egress (attempt " + attemptNumber + "): " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    // ✅ NEW — this is the method you should use going forward. It stops the
    // egress AND returns LiveKit's own EgressInfo, which contains the real,
    // authoritative filename(s) that were actually uploaded to S3. This is
    // what fixes the filename-mismatch bug: we stop guessing the S3 key and
    // instead read exactly what LiveKit says it wrote.
    public EgressInfo stopRecordingAndGetInfo(String egressId) {
        if (egressId == null) return null;
        try {
            EgressServiceClient client = buildEgressClient();
            retrofit2.Response<EgressInfo> response = client.stopEgress(egressId).execute();

            if (response.isSuccessful() && response.body() != null) {
                System.out.println("✅ Egress stopped: " + egressId);
                return response.body();
            }

            String errorBody = "";
            try {
                errorBody = response.errorBody() != null ? response.errorBody().string() : "";
            } catch (Exception readEx) {
                System.err.println("⚠️ Could not read stopEgress error body: " + readEx.getMessage());
            }

            boolean alreadyGone = errorBody.contains("\"code\":\"not_found\"")
                || errorBody.contains("\"code\":\"failed_precondition\"")
                || errorBody.contains("EGRESS_FAILED")
                || errorBody.toLowerCase().contains("egress not found")
                || errorBody.toLowerCase().contains("cannot be stopped");

            if (alreadyGone) {
                System.out.println("⚠️ stopEgress reported egress already gone/failed for " + egressId
                    + " — treating as stopped-but-unknown-result. LiveKit response: " + errorBody);
                // Returning null here on purpose — we genuinely don't know what
                // file (if any) exists, so the caller must NOT create a
                // recordings row with a guessed URL. Caller should mark this
                // as a failed/unknown recording, not silently succeed.
                return null;
            }

            System.err.println("❌ Stop egress failed: " + errorBody);
            return null;

        } catch (Exception e) {
            System.err.println("❌ Failed to stop egress: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Kept for backward compatibility if anything else calls this — but
    // prefer stopRecordingAndGetInfo() everywhere now.
    public boolean stopRecording(String egressId) {
        return stopRecordingAndGetInfo(egressId) != null;
    }
}