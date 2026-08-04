package com.lms.chat.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AudioTranscriptionService {

    @Value("${openai.api.key}")
    private String apiKey;
    @Value("${aws.s3.bucket}")
    private String bucket;
    @Value("${aws.region}")
    private String awsRegion;
    @Value("${aws.access-key}")
    private String awsAccessKey;
    @Value("${aws.secret-key}")
    private String awsSecretKey;
    @Value("${app.ffmpeg.path:ffmpeg}")
    private String ffmpegPath;

    private static final String WHISPER_URL = "https://api.openai.com/v1/audio/transcriptions";
    private static final long WHISPER_MAX_BYTES = 24L * 1024 * 1024; // stay under 25MB cap

    // Backup safety net — EgressService now confirms S3 existence before
    // recordingS3Url is even set, but keep this as defense-in-depth.
    private static final int MAX_S3_DOWNLOAD_RETRIES = 6;
    private static final long S3_DOWNLOAD_RETRY_DELAY_MS = 5000;

    private final RestTemplate restTemplate = buildRestTemplate();

    private RestTemplate buildRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30_000);
        factory.setReadTimeout(300_000); // 5 min — covers a large chunk upload/transcode regardless of total length
        return new RestTemplate(factory);
    }

    public String downloadAndTranscribe(String s3Url) {
        File videoFile = null, audioFile = null;
        List<File> chunkFiles = new ArrayList<>();
        try {
            String key = extractKeyFromUrl(s3Url);
            System.out.println("Recording URL = " + s3Url + " | Key = " + key);

            videoFile = File.createTempFile("meeting-video-", ".mp4");
            downloadFromS3(key, videoFile);
            System.out.println("Downloaded size = " + videoFile.length());

            audioFile = extractAudio(videoFile);
            System.out.println("Extracted audio size = " + audioFile.length());

            StringBuilder transcript = new StringBuilder();

            if (audioFile.length() <= WHISPER_MAX_BYTES) {
                // Short recording — single Whisper call
                transcript.append(callWhisper(audioFile));
            } else {
                // Long recording — chunk automatically, same code path
                chunkFiles = splitAudioIntoChunks(audioFile);
                for (int i = 0; i < chunkFiles.size(); i++) {
                    System.out.println("▶ Transcribing chunk " + (i + 1) + "/" + chunkFiles.size());
                    transcript.append(callWhisper(chunkFiles.get(i))).append(" ");
                }
            }

            System.out.println("✅ Transcript length = " + transcript.length());
            return transcript.toString();

        } catch (Exception e) {
            System.err.println("❌ AudioTranscriptionService FAILED");
            e.printStackTrace();
            throw new RuntimeException("Audio transcription failed", e);
        } finally {
            deleteQuietly(videoFile);
            deleteQuietly(audioFile);
            for (File f : chunkFiles) deleteQuietly(f);
        }
    }

    private void deleteQuietly(File f) {
        if (f != null) { try { f.delete(); } catch (Exception ignored) {} }
    }

    private String extractKeyFromUrl(String s3Url) {
        String marker = ".amazonaws.com/";
        int idx = s3Url.indexOf(marker);
        if (idx == -1) throw new RuntimeException("Unrecognized S3 URL format: " + s3Url);
        return s3Url.substring(idx + marker.length());
    }

    private void downloadFromS3(String key, File destination) {
        S3Client s3 = S3Client.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(awsAccessKey, awsSecretKey)))
                .build();
        try {
            GetObjectRequest request = GetObjectRequest.builder().bucket(bucket).key(key).build();
            for (int attempt = 1; attempt <= MAX_S3_DOWNLOAD_RETRIES; attempt++) {
                try {
                    // FIX: File.createTempFile() already created an empty 0-byte file to
                    // reserve the path. The SDK's ResponseTransformer.toFile(Path) uses
                    // Files.copy(..., CREATE_NEW), which refuses to write over an existing
                    // file and throws FileAlreadyExistsException every time, regardless of
                    // S3 timing. Deleting the placeholder right before download frees the
                    // path so Files.copy can create it fresh. (Using delete() here instead
                    // of the FileTransformerConfiguration overload, since that overload
                    // requires AWS SDK v2 2.20+.)
                    destination.delete();
                    s3.getObject(request, ResponseTransformer.toFile(destination.toPath()));
                    return;
                } catch (software.amazon.awssdk.services.s3.model.NoSuchKeyException e) {
                    if (attempt == MAX_S3_DOWNLOAD_RETRIES) throw e;
                    System.out.println("⏳ Retry " + attempt + "/" + MAX_S3_DOWNLOAD_RETRIES + " for key " + key);
                    try { Thread.sleep(S3_DOWNLOAD_RETRY_DELAY_MS); }
                    catch (InterruptedException ie) { Thread.currentThread().interrupt(); throw e; }
                }
            }
        } finally {
            s3.close();
        }
    }

    // Uses ffmpegPath (configurable via app.ffmpeg.path; falls back to "ffmpeg" on PATH
    // if unset — which is what production/Docker relies on). Works identically for a
    // 1-min or 40-min file — just produces a proportionally sized mp3.
    private File extractAudio(File videoFile) throws Exception {
        File audioFile = File.createTempFile("meeting-audio-", ".mp3");
        ProcessBuilder pb = new ProcessBuilder(
                ffmpegPath, "-y", "-i", videoFile.getAbsolutePath(),
                "-vn", "-ac", "1", "-b:a", "64k",
                audioFile.getAbsolutePath());
        pb.redirectErrorStream(true);
        Process process = pb.start();
        try (var in = process.getInputStream()) { in.readAllBytes(); }
        if (process.waitFor() != 0) throw new RuntimeException("ffmpeg audio extraction failed");
        return audioFile;
    }

    // Only invoked when audio exceeds Whisper's limit — for short meetings
    // this method is never called at all.
    private List<File> splitAudioIntoChunks(File audioFile) throws Exception {
        File tempDir = Files.createTempDirectory("meeting-audio-chunks-").toFile();
        String pattern = new File(tempDir, "chunk-%03d.mp3").getAbsolutePath();
        ProcessBuilder pb = new ProcessBuilder(
                ffmpegPath, "-y", "-i", audioFile.getAbsolutePath(),
                "-f", "segment", "-segment_time", "600", "-c", "copy", pattern);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        try (var in = process.getInputStream()) { in.readAllBytes(); }
        if (process.waitFor() != 0) throw new RuntimeException("ffmpeg splitting failed");

        File[] chunks = tempDir.listFiles((dir, name) -> name.startsWith("chunk-"));
        if (chunks == null || chunks.length == 0) throw new RuntimeException("No audio chunks produced");
        List<File> result = new ArrayList<>(List.of(chunks));
        result.sort((a, b) -> a.getName().compareTo(b.getName()));
        return result;
    }

    private String callWhisper(File audioFile) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new org.springframework.core.io.FileSystemResource(audioFile));
        body.add("model", "whisper-1");

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(WHISPER_URL, requestEntity, Map.class);
        Map<?, ?> responseBody = response.getBody();
        if (responseBody != null && responseBody.containsKey("text")) {
            return (String) responseBody.get("text");
        }
        throw new RuntimeException("Whisper response did not contain transcript text");
    }
}