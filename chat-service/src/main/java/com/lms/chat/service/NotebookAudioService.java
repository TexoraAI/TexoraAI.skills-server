package com.lms.chat.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NotebookAudioService {

    private static final Pattern LINE_PATTERN = Pattern.compile("^(HOST_A|HOST_B):\\s*(.*)$");

    @Value("${app.ffmpeg.path:ffmpeg}")
    private String ffmpegPath;

    private final OpenAiService openAiService;
    private final S3Service s3Service;

    public NotebookAudioService(OpenAiService openAiService, S3Service s3Service) {
        this.openAiService = openAiService;
        this.s3Service = s3Service;
    }

    /**
     * Takes a two-host script (lines prefixed "HOST_A:"/"HOST_B:"), synthesizes
     * each line with the corresponding voice, stitches the clips together with
     * ffmpeg, uploads the final MP3 to S3, and returns its key.
     */
    public String generate(String script, Long notebookId) {
        List<String[]> lines = parseScript(script); // [speaker, text]
        if (lines.isEmpty()) {
            throw new RuntimeException("Audio generation failed: script had no HOST_A/HOST_B lines");
        }

        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("notebook-audio-" + notebookId + "-");

            List<File> clipFiles = new ArrayList<>();
            int index = 0;
            for (String[] line : lines) {
                String speaker = line[0];
                String text = line[1];
                if (text.isBlank()) {
                    continue;
                }
                String voice = "HOST_A".equals(speaker) ? OpenAiService.VOICE_HOST_A : OpenAiService.VOICE_HOST_B;

                byte[] audio = openAiService.textToSpeech(text, voice);
                File clipFile = tempDir.resolve("clip_" + (index++) + ".mp3").toFile();
                Files.write(clipFile.toPath(), audio);
                clipFiles.add(clipFile);
            }

            if (clipFiles.isEmpty()) {
                throw new RuntimeException("Audio generation failed: no non-empty dialogue lines to synthesize");
            }

            File concatListFile = tempDir.resolve("concat_list.txt").toFile();
            try (var writer = Files.newBufferedWriter(concatListFile.toPath())) {
                for (File clip : clipFiles) {
                    writer.write("file '" + clip.getAbsolutePath().replace("'", "'\\''") + "'");
                    writer.newLine();
                }
            }

            File outputFile = tempDir.resolve("final.mp3").toFile();
            runFfmpeg(List.of(
                    ffmpegPath, "-y",
                    "-f", "concat", "-safe", "0",
                    "-i", concatListFile.getAbsolutePath(),
                    "-c:a", "libmp3lame", "-q:a", "2",
                    outputFile.getAbsolutePath()
            ));

            byte[] finalAudio = Files.readAllBytes(outputFile.toPath());
            String key = "files/notebook-service-files/audio/" + notebookId + "/" + UUID.randomUUID() + ".mp3";
            s3Service.uploadBytes(key, finalAudio, "audio/mpeg");
            return key;

        } catch (Exception e) {
            throw new RuntimeException("Audio generation failed: " + e.getMessage(), e);
        } finally {
            deleteRecursively(tempDir);
        }
    }

    private List<String[]> parseScript(String script) {
        List<String[]> lines = new ArrayList<>();
        if (script == null) {
            return lines;
        }
        for (String rawLine : script.split("\\r?\\n")) {
            String trimmed = rawLine.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            Matcher m = LINE_PATTERN.matcher(trimmed);
            if (m.matches()) {
                lines.add(new String[] { m.group(1), m.group(2).trim() });
            }
        }
        return lines;
    }

    private void runFfmpeg(List<String> command) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        // Drain output so the process can't block on a full pipe buffer
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            while (reader.readLine() != null) {
                // discarded; swap for a logger if ffmpeg diagnostics are needed
            }
        }

        boolean finished = process.waitFor(120, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("ffmpeg timed out while stitching audio clips");
        }
        if (process.exitValue() != 0) {
            throw new RuntimeException("ffmpeg exited with code " + process.exitValue());
        }
    }

    private void deleteRecursively(Path dir) {
        if (dir == null) {
            return;
        }
        try (var walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder())
                .forEach(p -> {
                    try {
                        Files.deleteIfExists(p);
                    } catch (IOException ignored) {
                        // best-effort cleanup
                    }
                });
        } catch (IOException ignored) {
            // best-effort cleanup
        }
    }
}