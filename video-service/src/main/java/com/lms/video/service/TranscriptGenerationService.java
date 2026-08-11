//package com.lms.video.service;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.lms.video.model.FeaturedTranscriptSegment;
//import com.lms.video.model.FeaturedVideoTranscript;
//import com.lms.video.model.TranscriptStatus;
//import com.lms.video.repository.FeaturedTranscriptSegmentRepository;
//import com.lms.video.repository.FeaturedVideoTranscriptRepository;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.core.io.FileSystemResource;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//import org.springframework.util.LinkedMultiValueMap;
//import org.springframework.util.MultiValueMap;
//import org.springframework.web.client.RestTemplate;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.time.Duration;
//import java.time.Instant;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//import java.util.concurrent.TimeUnit;
//
///**
// * Fully decoupled from the upload/VIDEO_READY flow. generateAsync() is fired
// * once (from FeaturedSessionVideoService, right after the video file write
// * succeeds) and does its own thing on a background thread. Nothing in here
// * ever touches Kafka, course-service, or the upload response.
// *
// * ENGINE HISTORY (read before changing the transcription path again):
// * 1. openai-whisper CLI spawned fresh per job -- worked, but every single
// *    job paid a large fixed cost (Python boot + model load from disk)
// *    BEFORE any actual transcription happened. For short videos this
// *    fixed cost dominated total time (e.g. ~4-15 min for a 1:32 video).
// * 2. whisper-ctranslate2 (CTranslate2/faster-whisper) -- reliably crashed
// *    on this host with exit code -1073741819 (0xC0000005 / access
// *    violation) the instant real inference started. Abandoned.
// * 3. THIS VERSION -- calls a persistent Python microservice
// *    (transcribe_server.py, FastAPI + openai-whisper) over HTTP. The
// *    model loads ONCE when that service starts and stays warm in memory;
// *    every job after that skips model loading entirely and only pays for
// *    the actual audio-duration-proportional transcription work.
// *
// * REQUIRES: transcribe_server.py running separately (see that file's own
// * docstring for how to start it) and reachable at
// * app.transcript.microservice-url (default http://localhost:5001).
// *
// * PERFORMANCE NOTE: generateAsync runs on the "transcriptionExecutor" bean
// * (see AsyncConfig), a SINGLE-THREADED pool on purpose -- the microservice
// * itself is also effectively single-threaded (one warm model instance), so
// * sending concurrent requests would just queue behind each other CPU-side
// * anyway; keeping the queueing on the Java side gives cleaner status/DB
// * semantics per job.
// */
//@Service
//public class TranscriptGenerationService {
//
//    private final FeaturedVideoTranscriptRepository transcriptRepo;
//    private final FeaturedTranscriptSegmentRepository segmentRepo;
//    private final ObjectMapper objectMapper = new ObjectMapper();
//    private final RestTemplate restTemplate = new RestTemplate();
//
//    // Same property path as chat-service (app.ffmpeg.path) -- both services
//    // read from the one shared FFMPEG_PATH env var, no hardcoding here.
//    @Value("${app.ffmpeg.path}")
//    private String ffmpegPath;
//
//    // Base URL of the persistent transcribe_server.py microservice.
//    @Value("${app.transcript.microservice-url:http://localhost:5001}")
//    private String microserviceUrl;
//
//    // Bounds ffmpeg extraction + the HTTP call to the microservice.
//    @Value("${app.transcript.process-timeout-minutes:90}")
//    private long processTimeoutMinutes;
//
//    // A job for the same session is considered "already running" (and a
//    // fresh trigger is skipped) if its PROCESSING row was touched within
//    // this window. Prevents duplicate/overlapping runs for one session
//    // (e.g. double upload click, retry, delete+reupload race).
//    private static final Duration DUPLICATE_JOB_GUARD_WINDOW = Duration.ofMinutes(2);
//
//    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
//
//    public TranscriptGenerationService(FeaturedVideoTranscriptRepository transcriptRepo,
//                                        FeaturedTranscriptSegmentRepository segmentRepo) {
//        this.transcriptRepo = transcriptRepo;
//        this.segmentRepo = segmentRepo;
//    }
//
//    @Async("transcriptionExecutor")
//    public void generateAsync(Long sessionId, String videoFilePath) {
//        // ── Duplicate-job guard ──────────────────────────────────────────
//        // If a job for this session is already PROCESSING and was updated
//        // recently, don't start a second one -- let it finish. Without this,
//        // a double-click upload or a delete+reupload race spawns two
//        // concurrent transcription requests, and whichever finishes last
//        // silently overwrites the other's result.
//        Optional<FeaturedVideoTranscript> existingOpt = transcriptRepo.findBySessionId(sessionId);
//        if (existingOpt.isPresent()) {
//            FeaturedVideoTranscript existing = existingOpt.get();
//            boolean recentlyStarted = existing.getStatus() == TranscriptStatus.PROCESSING
//                    && existing.getUpdatedAt() != null
//                    && existing.getUpdatedAt().isAfter(Instant.now().minus(DUPLICATE_JOB_GUARD_WINDOW));
//            if (recentlyStarted) {
//                return;
//            }
//        }
//
//        FeaturedVideoTranscript transcript = existingOpt.orElseGet(FeaturedVideoTranscript::new);
//        transcript.setSessionId(sessionId);
//        transcript.setStatus(TranscriptStatus.PROCESSING);
//        transcript.setErrorMessage(null);
//        if (transcript.getCreatedAt() == null) {
//            transcript.setCreatedAt(Instant.now());
//        }
//        transcript.setUpdatedAt(Instant.now());
//        transcript = transcriptRepo.save(transcript);
//
//        String jobId = sessionId + "_" + System.currentTimeMillis();
//        Path wavPath = Path.of(TEMP_DIR, jobId + ".wav");
//
//        try {
//            // Step 2 -- extract 16kHz mono PCM audio (Whisper's expected input,
//            // avoids Whisper doing its own resampling).
//            runProcess(List.of(
//                    ffmpegPath,
//                    "-y",
//                    "-i", videoFilePath,
//                    "-vn",
//                    "-acodec", "pcm_s16le",
//                    "-ar", "16000",
//                    "-ac", "1",
//                    wavPath.toString()
//            ), "ffmpeg audio extraction");
//
//            // Step 3 -- send the wav to the already-warm microservice instead
//            // of spawning a fresh whisper process. No model-load cost here.
//            JsonNode root = callTranscribeMicroservice(wavPath);
//
//            // Step 4 -- map segments directly, no timestamp re-derivation.
//            List<FeaturedTranscriptSegment> existingSegments =
//                    segmentRepo.findByTranscriptIdOrderByOrderIndexAsc(transcript.getId());
//            if (!existingSegments.isEmpty()) {
//                segmentRepo.deleteAll(existingSegments);
//            }
//
//            List<FeaturedTranscriptSegment> segments = new ArrayList<>();
//            JsonNode segmentsNode = root.get("segments");
//            int order = 0;
//            if (segmentsNode != null && segmentsNode.isArray()) {
//                for (JsonNode seg : segmentsNode) {
//                    FeaturedTranscriptSegment segment = new FeaturedTranscriptSegment();
//                    segment.setTranscript(transcript);
//                    segment.setStartSeconds(seg.path("start").asDouble());
//                    segment.setEndSeconds(seg.path("end").asDouble());
//                    segment.setText(seg.path("text").asText("").trim());
//                    segment.setOrderIndex(order++);
//                    segments.add(segment);
//                }
//            }
//            segmentRepo.saveAll(segments);
//
//            String detectedLanguage = root.hasNonNull("language") ? root.get("language").asText() : null;
//
//            transcript.setLanguage(detectedLanguage);
//            transcript.setStatus(TranscriptStatus.READY);
//            transcript.setErrorMessage(null);
//            transcript.setUpdatedAt(Instant.now());
//            transcriptRepo.save(transcript);
//
//        } catch (Exception e) {
//            // Step 5 -- any failure in extraction/HTTP call/parsing lands here.
//            transcript.setStatus(TranscriptStatus.FAILED);
//            transcript.setErrorMessage(truncate(e.getMessage() != null ? e.getMessage() : e.toString()));
//            transcript.setUpdatedAt(Instant.now());
//            transcriptRepo.save(transcript);
//        } finally {
//            cleanupFile(wavPath);
//        }
//    }
//
//    /**
//     * POSTs the wav file to transcribe_server.py's /transcribe endpoint and
//     * returns the parsed JSON body. Throws if the service is unreachable
//     * (e.g. not started) or returns a non-2xx status, with a message that
//     * makes the "did you start transcribe_server.py?" possibility obvious.
//     */
//    private JsonNode callTranscribeMicroservice(Path wavPath) throws IOException {
//        String url = microserviceUrl + "/transcribe";
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
//
//        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
//        body.add("file", new FileSystemResource(wavPath.toFile()));
//
//        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
//
//        ResponseEntity<String> response;
//        try {
//            response = restTemplate.postForEntity(url, requestEntity, String.class);
//        } catch (Exception e) {
//            throw new IOException("Could not reach transcription microservice at " + url
//                    + " -- is transcribe_server.py running? (" + e.getMessage() + ")", e);
//        }
//
//        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
//            throw new IOException("Transcription microservice returned status "
//                    + response.getStatusCode() + ": " + response.getBody());
//        }
//
//        return objectMapper.readTree(response.getBody());
//    }
//
//    private void runProcess(List<String> command, String label) throws IOException, InterruptedException {
//        ProcessBuilder pb = new ProcessBuilder(command);
//        pb.redirectErrorStream(true);
//
//        Process process;
//        try {
//            process = pb.start();
//        } catch (IOException e) {
//            throw new IOException(label + " could not be started (is it installed and on the configured path?): "
//                    + e.getMessage(), e);
//        }
//
//        try (BufferedReader reader = new BufferedReader(
//                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
//            while (reader.readLine() != null) {
//                // discarded -- swap for a logger if you want the raw ffmpeg output
//            }
//        }
//
//        boolean finished = process.waitFor(processTimeoutMinutes, TimeUnit.MINUTES);
//        if (!finished) {
//            process.destroyForcibly();
//            throw new IOException(label + " timed out after " + processTimeoutMinutes + " minute(s)");
//        }
//        if (process.exitValue() != 0) {
//            throw new IOException(label + " failed with exit code " + process.exitValue());
//        }
//    }
//
//    private void cleanupFile(Path path) {
//        if (path == null) return;
//        try {
//            Files.deleteIfExists(path);
//        } catch (IOException ignored) {
//            // best-effort cleanup only -- never let temp-file cleanup fail the job
//        }
//    }
//
//    private String truncate(String message) {
//        if (message == null) return null;
//        return message.length() > 500 ? message.substring(0, 500) : message;
//    }
//}
package com.lms.video.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.video.model.FeaturedTranscriptSegment;
import com.lms.video.model.FeaturedVideoTranscript;
import com.lms.video.model.TranscriptSourceType;
import com.lms.video.model.TranscriptStatus;
import com.lms.video.repository.FeaturedTranscriptSegmentRepository;
import com.lms.video.repository.FeaturedVideoTranscriptRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Fully decoupled from the upload/VIDEO_READY flow. generateAsync() is fired
 * once (from FeaturedSessionVideoService for featured-content videos, and
 * from VideoService for regular library videos), right after the video file
 * write succeeds, and does its own thing on a background thread. Nothing in
 * here ever touches Kafka, course-service, or the upload response.
 *
 * SOURCE TYPE:
 * Featured-content sessions and library Video rows come from two separate
 * ID sequences, so the same numeric id (e.g. 78) can legitimately belong to
 * both a featured session AND a library video at the same time. Every
 * transcript row is tagged with a TranscriptSourceType (FEATURED /
 * LIBRARY_VIDEO) and all lookups filter on (id, sourceType) together --
 * never on id alone -- to guarantee the two flows can never collide or
 * overwrite each other's transcript.
 *
 * ENGINE HISTORY (read before changing the transcription path again):
 * 1. openai-whisper CLI spawned fresh per job -- worked, but every single
 *    job paid a large fixed cost (Python boot + model load from disk)
 *    BEFORE any actual transcription happened. For short videos this
 *    fixed cost dominated total time.
 * 2. whisper-ctranslate2 (CTranslate2/faster-whisper) -- reliably crashed
 *    on the dev host with exit code -1073741819 (0xC0000005 / access
 *    violation) the instant real inference started. Abandoned.
 * 3. THIS VERSION -- calls a persistent Python microservice
 *    (transcribe_server.py, FastAPI + openai-whisper) over HTTP. The
 *    model loads ONCE when that service starts and stays warm; every job
 *    after that skips model loading entirely.
 *
 * REQUIRES: transcribe_server.py running separately and reachable at
 * app.transcript.microservice-url (default http://localhost:5001).
 *
 * PERFORMANCE NOTE: generateAsync runs on the "transcriptionExecutor" bean
 * (see AsyncConfig), a SINGLE-THREADED pool on purpose -- the microservice
 * itself is also effectively single-threaded (one warm model instance), so
 * concurrent requests would just queue behind each other CPU-side anyway;
 * keeping the queueing on the Java side gives cleaner status/DB semantics
 * per job, for BOTH featured and library-video jobs sharing the one queue.
 */
@Service
public class TranscriptGenerationService {

    private final FeaturedVideoTranscriptRepository transcriptRepo;
    private final FeaturedTranscriptSegmentRepository segmentRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    // Same property path as chat-service (app.ffmpeg.path) -- both services
    // read from the one shared FFMPEG_PATH env var, no hardcoding here.
    @Value("${app.ffmpeg.path}")
    private String ffmpegPath;

    // Base URL of the persistent transcribe_server.py microservice.
    @Value("${app.transcript.microservice-url:http://localhost:5001}")
    private String microserviceUrl;

    // Bounds ffmpeg extraction + the HTTP call to the microservice.
    @Value("${app.transcript.process-timeout-minutes:90}")
    private long processTimeoutMinutes;

    // A job for the same (id, sourceType) pair is considered "already
    // running" (and a fresh trigger is skipped) if its PROCESSING row was
    // touched within this window. Prevents duplicate/overlapping runs for
    // one video (e.g. double upload click, retry, delete+reupload race).
    private static final Duration DUPLICATE_JOB_GUARD_WINDOW = Duration.ofMinutes(2);

    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");

    public TranscriptGenerationService(FeaturedVideoTranscriptRepository transcriptRepo,
                                        FeaturedTranscriptSegmentRepository segmentRepo) {
        this.transcriptRepo = transcriptRepo;
        this.segmentRepo = segmentRepo;
    }

    // ── Existing callers (FeaturedSessionVideoService) keep working
    // completely unchanged -- this just delegates to the 3-arg version
    // with the same default source type transcripts have always used. ──
    public void generateAsync(Long sessionId, String videoFilePath) {
        generateAsync(sessionId, videoFilePath, TranscriptSourceType.FEATURED);
    }

    @Async("transcriptionExecutor")
    public void generateAsync(Long id, String videoFilePath, TranscriptSourceType sourceType) {
        // ── Duplicate-job guard ──────────────────────────────────────────
        Optional<FeaturedVideoTranscript> existingOpt =
                transcriptRepo.findBySessionIdAndSourceType(id, sourceType);
        if (existingOpt.isPresent()) {
            FeaturedVideoTranscript existing = existingOpt.get();
            boolean recentlyStarted = existing.getStatus() == TranscriptStatus.PROCESSING
                    && existing.getUpdatedAt() != null
                    && existing.getUpdatedAt().isAfter(Instant.now().minus(DUPLICATE_JOB_GUARD_WINDOW));
            if (recentlyStarted) {
                return;
            }
        }

        FeaturedVideoTranscript transcript = existingOpt.orElseGet(FeaturedVideoTranscript::new);
        transcript.setSessionId(id);
        transcript.setSourceType(sourceType);
        transcript.setStatus(TranscriptStatus.PROCESSING);
        transcript.setErrorMessage(null);
        if (transcript.getCreatedAt() == null) {
            transcript.setCreatedAt(Instant.now());
        }
        transcript.setUpdatedAt(Instant.now());
        transcript = transcriptRepo.save(transcript);

        String jobId = sourceType.name() + "_" + id + "_" + System.currentTimeMillis();
        Path wavPath = Path.of(TEMP_DIR, jobId + ".wav");

        try {
            // Step 2 -- extract 16kHz mono PCM audio (Whisper's expected input,
            // avoids Whisper doing its own resampling).
            runProcess(List.of(
                    ffmpegPath,
                    "-y",
                    "-i", videoFilePath,
                    "-vn",
                    "-acodec", "pcm_s16le",
                    "-ar", "16000",
                    "-ac", "1",
                    wavPath.toString()
            ), "ffmpeg audio extraction");

            // Step 3 -- send the wav to the already-warm microservice instead
            // of spawning a fresh whisper process. No model-load cost here.
            JsonNode root = callTranscribeMicroservice(wavPath);

            // Step 4 -- map segments directly, no timestamp re-derivation.
            List<FeaturedTranscriptSegment> existingSegments =
                    segmentRepo.findByTranscriptIdOrderByOrderIndexAsc(transcript.getId());
            if (!existingSegments.isEmpty()) {
                segmentRepo.deleteAll(existingSegments);
            }

            List<FeaturedTranscriptSegment> segments = new ArrayList<>();
            JsonNode segmentsNode = root.get("segments");
            int order = 0;
            if (segmentsNode != null && segmentsNode.isArray()) {
                for (JsonNode seg : segmentsNode) {
                    FeaturedTranscriptSegment segment = new FeaturedTranscriptSegment();
                    segment.setTranscript(transcript);
                    segment.setStartSeconds(seg.path("start").asDouble());
                    segment.setEndSeconds(seg.path("end").asDouble());
                    segment.setText(seg.path("text").asText("").trim());
                    segment.setOrderIndex(order++);
                    segments.add(segment);
                }
            }
            segmentRepo.saveAll(segments);

            String detectedLanguage = root.hasNonNull("language") ? root.get("language").asText() : null;

            transcript.setLanguage(detectedLanguage);
            transcript.setStatus(TranscriptStatus.READY);
            transcript.setErrorMessage(null);
            transcript.setUpdatedAt(Instant.now());
            transcriptRepo.save(transcript);

        } catch (Exception e) {
            // Step 5 -- any failure in extraction/HTTP call/parsing lands here.
            transcript.setStatus(TranscriptStatus.FAILED);
            transcript.setErrorMessage(truncate(e.getMessage() != null ? e.getMessage() : e.toString()));
            transcript.setUpdatedAt(Instant.now());
            transcriptRepo.save(transcript);
        } finally {
            cleanupFile(wavPath);
        }
    }

    /**
     * POSTs the wav file to transcribe_server.py's /transcribe endpoint and
     * returns the parsed JSON body. Throws if the service is unreachable
     * (e.g. not started) or returns a non-2xx status, with a message that
     * makes the "did you start transcribe_server.py?" possibility obvious.
     */
    private JsonNode callTranscribeMicroservice(Path wavPath) throws IOException {
        String url = microserviceUrl + "/transcribe";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(wavPath.toFile()));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response;
        try {
            response = restTemplate.postForEntity(url, requestEntity, String.class);
        } catch (Exception e) {
            throw new IOException("Could not reach transcription microservice at " + url
                    + " -- is transcribe_server.py running? (" + e.getMessage() + ")", e);
        }

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IOException("Transcription microservice returned status "
                    + response.getStatusCode() + ": " + response.getBody());
        }

        return objectMapper.readTree(response.getBody());
    }

    private void runProcess(List<String> command, String label) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);

        Process process;
        try {
            process = pb.start();
        } catch (IOException e) {
            throw new IOException(label + " could not be started (is it installed and on the configured path?): "
                    + e.getMessage(), e);
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            while (reader.readLine() != null) {
                // discarded -- swap for a logger if you want the raw ffmpeg output
            }
        }

        boolean finished = process.waitFor(processTimeoutMinutes, TimeUnit.MINUTES);
        if (!finished) {
            process.destroyForcibly();
            throw new IOException(label + " timed out after " + processTimeoutMinutes + " minute(s)");
        }
        if (process.exitValue() != 0) {
            throw new IOException(label + " failed with exit code " + process.exitValue());
        }
    }

    private void cleanupFile(Path path) {
        if (path == null) return;
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // best-effort cleanup only -- never let temp-file cleanup fail the job
        }
    }

    private String truncate(String message) {
        if (message == null) return null;
        return message.length() > 500 ? message.substring(0, 500) : message;
    }
}