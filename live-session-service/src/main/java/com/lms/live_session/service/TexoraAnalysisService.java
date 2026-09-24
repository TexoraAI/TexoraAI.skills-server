package com.lms.live_session.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.live_session.entity.TexoraAnalysis;
import com.lms.live_session.entity.TexoraAnalysisStatus;
import com.lms.live_session.entity.TexoraMeeting;
import com.lms.live_session.entity.TexoraParticipant;
import com.lms.live_session.repository.TexoraAnalysisRepository;
import com.lms.live_session.repository.TexoraParticipantRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

@Service
public class TexoraAnalysisService {

    private final TexoraAnalysisRepository analysisRepository;
    private final TexoraWebhookService webhookService;
    private final TexoraParticipantRepository participantRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ffmpeg.path}")
    private String ffmpegPath;

    // CHANGED — was whisper.command / whisper.model, used with ProcessBuilder
    // to run Whisper directly in this service. Now points at the shared,
    // already-warm transcript-microservice (same one video-service/
    // chat-service already call) — called over HTTP instead.
    @Value("${transcript.microservice-url}")
    private String transcriptMicroserviceUrl;

    @Value("${openai.api-key}")
    private String openAiApiKey;

    @Value("${openai.model:gpt-4o-mini}")
    private String openAiModel;

    @Value("${aws.access-key}")
    private String awsAccessKey;

    @Value("${aws.secret-key}")
    private String awsSecretKey;

    @Value("${aws.region}")
    private String awsRegion;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${texora.transcript-signing-secret}")
    private String transcriptSigningSecret;

    @Value("${app.base-url}")
    private String baseUrl;

    public TexoraAnalysisService(TexoraAnalysisRepository analysisRepository, TexoraWebhookService webhookService,
                                  TexoraParticipantRepository participantRepository) {
        this.analysisRepository = analysisRepository;
        this.webhookService = webhookService;
        this.participantRepository = participantRepository;
    }

    @Async
    public void processAsync(TexoraMeeting meeting) {
        String meetingId = meeting.getTexoraMeetingId();

        TexoraAnalysis analysis = analysisRepository.findByTexoraMeetingId(meetingId)
                .orElseGet(() -> {
                    TexoraAnalysis a = new TexoraAnalysis();
                    a.setTexoraMeetingId(meetingId);
                    return a;
                });
        analysis.setStatus(TexoraAnalysisStatus.PROCESSING);
        analysisRepository.save(analysis);

        Path workDir = null;
        try {
            workDir = Files.createTempDirectory("texora-analysis-" + meetingId);

            List<TexoraParticipant> participants = participantRepository.findByTexoraMeetingId(meetingId);
            List<String> perTrackAudioUrls = participants.stream()
                    .map(TexoraParticipant::getAudioS3Url)
                    .filter(java.util.Objects::nonNull)
                    .toList();

            String transcript;
            if (!perTrackAudioUrls.isEmpty()) {
                transcript = transcribePerParticipant(participants, workDir);
            } else if (meeting.getRecordingS3Url() != null) {
                Path videoFile = workDir.resolve("recording.mp4");
                downloadFromS3(meeting.getRecordingS3Url(), videoFile);
                Path audioFile = workDir.resolve("audio.wav");
                runFfmpegExtractAudio(videoFile, audioFile);
                transcript = runWhisperTranscribeFlat(audioFile, workDir);
            } else {
                fail(meeting, analysis, "NO_AUDIO", "No recording was produced for this meeting.");
                return;
            }

            if (transcript == null || transcript.isBlank()) {
                fail(meeting, analysis, "NO_AUDIO", "Transcription produced no text — audio may be empty or silent.");
                return;
            }

            analysis.setTranscriptText(transcript);

            String transcriptKey = uploadTranscriptToS3(meetingId, transcript);
            analysis.setTranscriptS3Key(transcriptKey);

            String summaryJson = generateSummary(transcript, meeting);
            analysis.setSummaryJson(summaryJson);

            analysis.setStatus(TexoraAnalysisStatus.COMPLETED);
            analysisRepository.save(analysis);

            String[] transcriptUrlData = buildTranscriptUrl(meetingId);
            webhookService.emitAnalysisCompleted(meeting, analysis.getAnalysisVersion(),
                    objectMapper.readTree(summaryJson), transcriptUrlData[0], transcriptUrlData[1]);

        } catch (Exception e) {
            System.err.println("[TexoraAnalysisService] Failed for " + meetingId + ": " + e.getMessage());
            e.printStackTrace();
            fail(meeting, analysis, "INTERNAL_ERROR", e.getMessage());
        } finally {
            if (workDir != null) {
                deleteRecursively(workDir);
            }
        }
    }

    private record TranscriptSegment(String speakerName, double absoluteStartSeconds, String text) {}

    private String transcribePerParticipant(List<TexoraParticipant> participants, Path workDir) throws Exception {
        List<TranscriptSegment> allSegments = new ArrayList<>();
        LocalDateTime earliestJoin = participants.stream()
                .map(TexoraParticipant::getJoinedAt)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now(ZoneId.of("UTC")));

        for (TexoraParticipant p : participants) {
            if (p.getAudioS3Url() == null) continue;
            try {
                Path rawAudio = workDir.resolve(p.getIdentity() + "-raw.ogg");
                downloadFromS3(p.getAudioS3Url(), rawAudio);

                Path wavAudio = workDir.resolve(p.getIdentity() + ".wav");
                runFfmpegExtractAudio(rawAudio, wavAudio);

                double offsetSeconds = java.time.Duration.between(earliestJoin, p.getJoinedAt()).getSeconds();

                List<TranscriptSegment> segments = runWhisperTranscribeJson(wavAudio, p.getDisplayName(), offsetSeconds);
                allSegments.addAll(segments);
            } catch (Exception e) {
                System.err.println("[TexoraAnalysisService] Failed to transcribe " + p.getIdentity() + ": " + e.getMessage());
            }
        }

        allSegments.sort(java.util.Comparator.comparingDouble(TranscriptSegment::absoluteStartSeconds));

        StringBuilder sb = new StringBuilder();
        for (TranscriptSegment seg : allSegments) {
            sb.append(seg.speakerName()).append(": ").append(seg.text().trim()).append("\n");
        }
        return sb.toString();
    }

    // CHANGED — was a ProcessBuilder call to whisper CLI with
    // --output_format json. Now calls the shared microservice over HTTP
    // and reads its {language, segments:[{start,end,text}]} response.
    private List<TranscriptSegment> runWhisperTranscribeJson(Path audioFile, String displayName, double offsetSeconds)
            throws IOException {
        JsonNode root = callTranscribeMicroservice(audioFile);

        List<TranscriptSegment> segments = new ArrayList<>();
        JsonNode segmentsNode = root.get("segments");
        if (segmentsNode != null && segmentsNode.isArray()) {
            for (JsonNode seg : segmentsNode) {
                double start = seg.path("start").asDouble();
                String text = seg.path("text").asText();
                segments.add(new TranscriptSegment(displayName, offsetSeconds + start, text));
            }
        }
        return segments;
    }

    // CHANGED — was a ProcessBuilder call to whisper CLI with
    // --output_format txt. Now calls the same microservice and joins its
    // segments' text into one flat string (used only for the mixed-
    // recording fallback path, which has no per-speaker labels anyway).
    private String runWhisperTranscribeFlat(Path audioFile, Path workDir) throws IOException {
        JsonNode root = callTranscribeMicroservice(audioFile);
        JsonNode segmentsNode = root.get("segments");
        if (segmentsNode == null || !segmentsNode.isArray()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (JsonNode seg : segmentsNode) {
            sb.append(seg.path("text").asText("").trim()).append(" ");
        }
        return sb.toString().trim();
    }

    // NEW — the actual HTTP call. Matches transcribe_server.py's contract:
    // multipart POST with a "file" field, JSON response with language +
    // segments[{start,end,text}].
    private JsonNode callTranscribeMicroservice(Path audioFile) throws IOException {
        String url = transcriptMicroserviceUrl + "/transcribe";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(audioFile.toFile()));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response;
        try {
            response = restTemplate.postForEntity(url, requestEntity, String.class);
        } catch (Exception e) {
            throw new IOException("Could not reach transcription microservice at " + url
                    + " — is it running? (" + e.getMessage() + ")", e);
        }

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IOException("Transcription microservice returned status "
                    + response.getStatusCode() + ": " + response.getBody());
        }

        return objectMapper.readTree(response.getBody());
    }

    private void fail(TexoraMeeting meeting, TexoraAnalysis analysis, String reason, String message) {
        analysis.setStatus(TexoraAnalysisStatus.FAILED);
        analysis.setFailureReason(reason);
        analysis.setFailureMessage(message);
        analysisRepository.save(analysis);
        webhookService.emitAnalysisFailed(meeting, reason, message, true);
    }

    private void downloadFromS3(String s3Url, Path destination) throws IOException {
        String prefix = "https://" + bucket + ".s3." + awsRegion + ".amazonaws.com/";
        String key = s3Url.startsWith(prefix) ? s3Url.substring(prefix.length()) : s3Url;

        S3Client s3 = S3Client.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(awsAccessKey, awsSecretKey)))
                .build();
        try {
            GetObjectRequest request = GetObjectRequest.builder().bucket(bucket).key(key).build();
            s3.getObject(request, destination);
        } finally {
            s3.close();
        }
    }

    // UNCHANGED — audio extraction still runs locally via ffmpeg.
    private void runFfmpegExtractAudio(Path videoFile, Path audioFile) throws IOException, InterruptedException {
        List<String> command = List.of(
                ffmpegPath, "-y", "-i", videoFile.toString(),
                "-ar", "16000", "-ac", "1", "-vn", audioFile.toString()
        );
        runProcess(command, 5 * 60_000);
    }

    // UNCHANGED — still used by runFfmpegExtractAudio.
    private void runProcess(List<String> command, long timeoutMs) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        Thread drain = new Thread(() -> {
            try (var reader = process.inputReader()) {
                reader.lines().forEach(line -> System.out.println("[proc] " + line));
            } catch (IOException ignored) {}
        });
        drain.setDaemon(true);
        drain.start();

        boolean finished = process.waitFor(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new IOException("Process timed out: " + String.join(" ", command));
        }
        if (process.exitValue() != 0) {
            throw new IOException("Process failed with exit code " + process.exitValue() + ": " + String.join(" ", command));
        }
    }

    private void deleteRecursively(Path dir) {
        try (var stream = Files.walk(dir)) {
            stream.sorted((a, b) -> b.compareTo(a)).forEach(p -> {
                try { Files.deleteIfExists(p); } catch (IOException ignored) {}
            });
        } catch (IOException ignored) {}
    }

    private String generateSummary(String transcript, TexoraMeeting meeting) throws Exception {
        String systemPrompt = """
                You are analyzing an interview transcript. Produce ONLY a JSON object with this exact shape,
                no markdown, no commentary:
                {
                  "summary": "plain text, a few sentences",
                  "strengths": ["short bullet", "..."],
                  "concerns": ["short bullet", "..."],
                  "topics": [{"name": "...", "notes": "...", "coverage": "strong|moderate|weak|not_covered"}],
                  "communication": "one or two sentences"
                }
                Do NOT include a hire/no-hire recommendation or a numeric score under any field name.
                """;

        Map<String, Object> body = Map.of(
                "model", openAiModel,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", "Interview topic: " + meeting.getTopic() + "\n\nTranscript:\n" + transcript)
                ),
                "temperature", 0.3
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://api.openai.com/v1/chat/completions", request, String.class);

        var root = objectMapper.readTree(response.getBody());
        String content = root.path("choices").get(0).path("message").path("content").asText();

        String cleaned = content.trim().replaceAll("^```json", "").replaceAll("^```", "").replaceAll("```$", "").trim();
        objectMapper.readTree(cleaned);
        return cleaned;
    }

    private String uploadTranscriptToS3(String meetingId, String transcriptText) {
        String key = "transcripts/" + meetingId + ".txt";
        S3Client s3 = S3Client.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(awsAccessKey, awsSecretKey)))
                .build();
        try {
            s3.putObject(
                    PutObjectRequest.builder().bucket(bucket).key(key).contentType("text/plain; charset=utf-8").build(),
                    RequestBody.fromString(transcriptText, StandardCharsets.UTF_8)
            );
            return key;
        } finally {
            s3.close();
        }
    }

    private String[] buildTranscriptUrl(String meetingId) {
        long expiresAtEpoch = Instant.now().plus(30, java.time.temporal.ChronoUnit.DAYS).getEpochSecond();
        String signedString = meetingId + "." + expiresAtEpoch;
        String signature;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(transcriptSigningSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] sig = mac.doFinal(signedString.getBytes(StandardCharsets.UTF_8));
            signature = HexFormat.of().formatHex(sig);
        } catch (Exception e) {
            throw new RuntimeException("Failed to sign transcript URL", e);
        }
        String url = baseUrl + "/api/v1/texorameetings/transcripts/" + meetingId
                + "?exp=" + expiresAtEpoch + "&sig=" + signature;
        String expiresAtIso = Instant.ofEpochSecond(expiresAtEpoch).toString();
        return new String[]{url, expiresAtIso};
    }
}