





package com.lms.live_session.service;

import io.livekit.server.EgressServiceClient;
import livekit.LivekitEgress.*;
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

    // ✅ CORRECT: SDK 0.12.0 uses createClient() static factory method
    private EgressServiceClient buildClient() {
        return EgressServiceClient.createClient(
            livekitUrl,
            apiKey,
            apiSecret
        );
    }

    public String startRecording(Long sessionId) {
        try {
            EgressServiceClient client = buildClient();

            S3Upload s3 = S3Upload.newBuilder()
                .setAccessKey(awsAccessKey)
                .setSecret(awsSecretKey)
                .setBucket(bucket)
                .setRegion(awsRegion)
                .build();

            EncodedFileOutput fileOutput = EncodedFileOutput.newBuilder()
                .setFileType(EncodedFileType.MP4)
                .setFilepath("recordings/session-" + sessionId
                    + "-" + System.currentTimeMillis())
                .setS3(s3)
                .build();

            // ✅ CORRECT order for SDK 0.12.0: roomName, fileOutput
            retrofit2.Response<EgressInfo> response = client
                .startRoomCompositeEgress(
                    "session-" + sessionId,
                    fileOutput
                )
                .execute();

            if (!response.isSuccessful() || response.body() == null) {
                System.err.println("❌ Egress start failed: "
                    + (response.errorBody() != null
                        ? response.errorBody().string()
                        : "null body"));
                return null;
            }

            String egressId = response.body().getEgressId();
            System.out.println("✅ Egress started: " + egressId);
            return egressId;

        } catch (Exception e) {
            System.err.println("❌ Failed to start egress: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public void stopRecording(String egressId) {
        try {
            EgressServiceClient client = buildClient();

            retrofit2.Response<EgressInfo> response = client
                .stopEgress(egressId)
                .execute();

            if (response.isSuccessful()) {
                System.out.println("✅ Egress stopped: " + egressId);
            } else {
                System.err.println("❌ Stop egress failed: "
                    + (response.errorBody() != null
                        ? response.errorBody().string()
                        : "null body"));
            }

        } catch (Exception e) {
            System.err.println("❌ Failed to stop egress: " + e.getMessage());
            e.printStackTrace();
        }
    }
}