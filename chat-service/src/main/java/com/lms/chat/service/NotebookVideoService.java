package com.lms.chat.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Service
public class NotebookVideoService {

    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;
    private static final double MIN_SLIDE_SECONDS = 2.5;
    private static final double WORDS_PER_SECOND_ESTIMATE = 2.5; // ~150 wpm fallback
    private static final int SHORT_FORMAT_MAX_SLIDES = 4;
    // Bounds worst-case parallel image-generation time regardless of source
    // content length, and keeps videos to a reasonable length anyway.
    private static final int MAX_SLIDES_FOR_VIDEO = 6;
    // Overall wait for the whole per-slide image-generation batch. Any slide
    // whose image isn't done by then falls back to the plain layout, same as
    // the existing per-slide failure fallback.
    private static final long IMAGE_BATCH_TIMEOUT_SECONDS = 90;
    // Closest OpenAI image size to the video's 16:9 canvas; cropped to fit via drawCoverFit.
    private static final String ILLUSTRATION_IMAGE_SIZE = "1536x1024";
    // Visual clarity matters less for a few seconds of video background than
    // for a single standalone Infographic the person keeps/downloads, and
    // "low" quality generates noticeably faster.
    private static final String VIDEO_ILLUSTRATION_QUALITY = "low";

    @Value("${app.ffmpeg.path:ffmpeg}")
    private String ffmpegPath;

    private final OpenAiService openAiService;
    private final S3Service s3Service;
    private final ExecutorService imageGenerationExecutor;

    public NotebookVideoService(OpenAiService openAiService, S3Service s3Service,
                                 ExecutorService imageGenerationExecutor) {
        this.openAiService = openAiService;
        this.s3Service = s3Service;
        this.imageGenerationExecutor = imageGenerationExecutor;
    }

    /**
     * Renders a "narrated slideshow": one image per slide (either the
     * existing static Graphics2D title+bullets layout, or — for the
     * illustrated visual styles — an AI-generated background with the same
     * title/bullets composited on top) and one continuous narration track
     * from a single TTS call, stitched together with ffmpeg into an MP4,
     * uploaded to S3.
     *
     * @param slidesArray the slide content generated upstream
     * @param notebookId  owning notebook, used for S3 key/temp dir naming
     * @param format      "short" | "explainer" (default "explainer" if null/unrecognized).
     *                    "short" renders a reduced subset of slides and skips
     *                    speaker notes in the narration, so both the video and
     *                    its narration end up noticeably shorter.
     * @param visualStyle "auto-select" | "custom" | "classic" | "whiteboard" | "kawaii"
     *                    (default "auto-select"). "classic"/"auto-select" keep the
     *                    original plain Graphics2D rendering untouched (no extra API
     *                    calls). The other three generate an illustrated background
     *                    per slide via OpenAiService.generateImage, falling back to
     *                    the plain rendering for any slide whose illustration fails
     *                    or doesn't complete within the batch timeout.
     * @param focus       optional free-text topic to emphasize in the narration
     *                    (and, for "custom" visual style, in the illustration prompt)
     */
    public String generate(JsonNode slidesArray, Long notebookId, String format, String visualStyle, String focus) {
        if (slidesArray == null || !slidesArray.isArray() || slidesArray.isEmpty()) {
            throw new RuntimeException("Video generation failed: no slide content to render");
        }

        String resolvedFormat = normalizeFormat(format);
        String resolvedVisualStyle = normalizeVisualStyle(visualStyle);
        String resolvedFocus = (focus == null) ? "" : focus.trim();

        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("notebook-video-" + notebookId + "-");

            List<JsonNode> selectedSlides = selectSlidesForFormat(slidesArray, resolvedFormat);
            // Bound worst-case parallel image-generation time regardless of
            // how much source content exists.
            if (selectedSlides.size() > MAX_SLIDES_FOR_VIDEO) {
                selectedSlides = selectedSlides.subList(0, MAX_SLIDES_FOR_VIDEO);
            }

            // 1. Build one narration script across the selected slides + per-slide text lengths
            List<String> slideTexts = new ArrayList<>();
            StringBuilder narrationBuilder = new StringBuilder();
            for (JsonNode slideNode : selectedSlides) {
                String title = slideNode.path("title").asText("");
                StringBuilder slideText = new StringBuilder(title);
                JsonNode bullets = slideNode.path("bullets");
                if (bullets.isArray()) {
                    for (JsonNode bullet : bullets) {
                        slideText.append(". ").append(bullet.asText(""));
                    }
                }
                // Speaker notes are the most verbose part of a slide, so "short"
                // format drops them to help keep the narration noticeably shorter.
                if ("explainer".equals(resolvedFormat)) {
                    String notes = slideNode.path("speakerNotes").asText("");
                    if (!notes.isBlank()) {
                        slideText.append(". ").append(notes);
                    }
                }
                slideTexts.add(slideText.toString());
                narrationBuilder.append(slideText).append(". ");
            }
            String rawNarration = narrationBuilder.toString().trim();
            if (rawNarration.isBlank()) {
                throw new RuntimeException("Video generation failed: slides had no narratable text");
            }

            String fullNarration = resolvedFocus.isEmpty()
                    ? rawNarration
                    : buildFocusedNarration(rawNarration, resolvedFocus, resolvedFormat);

            // 2. One TTS call for the whole narration
            byte[] audioBytes = openAiService.textToSpeech(fullNarration, OpenAiService.VOICE_HOST_A);
            File audioFile = tempDir.resolve("narration.mp3").toFile();
            Files.write(audioFile.toPath(), audioBytes);

            double totalDuration = getAudioDurationSeconds(audioFile);
            if (totalDuration <= 0) {
                // Fall back to a words-per-second estimate if ffprobe isn't available
                int wordCount = fullNarration.split("\\s+").length;
                totalDuration = Math.max(wordCount / WORDS_PER_SECOND_ESTIMATE, MIN_SLIDE_SECONDS * slideTexts.size());
            }

            // 3. Allocate each slide a duration proportional to its share of narration text
            int totalChars = slideTexts.stream().mapToInt(String::length).sum();
            List<Double> durations = new ArrayList<>();
            for (String text : slideTexts) {
                double share = totalChars == 0 ? 1.0 / slideTexts.size() : (double) text.length() / totalChars;
                durations.add(Math.max(share * totalDuration, MIN_SLIDE_SECONDS));
            }

            // 4. Render each selected slide as a PNG (parallel image generation
            //    for illustrated styles, sequential plain layout otherwise)
            List<File> imageFiles = renderSlides(selectedSlides, tempDir, resolvedVisualStyle, resolvedFocus);

            // 5. Build the ffmpeg concat list (image + duration per slide; last image
            //    repeated because ffmpeg's concat demuxer ignores the final entry's duration)
            File imageListFile = tempDir.resolve("images_list.txt").toFile();
            try (var writer = Files.newBufferedWriter(imageListFile.toPath())) {
                for (int i = 0; i < imageFiles.size(); i++) {
                    writer.write("file '" + imageFiles.get(i).getAbsolutePath().replace("'", "'\\''") + "'");
                    writer.newLine();
                    writer.write("duration " + String.format("%.3f", durations.get(i)));
                    writer.newLine();
                }
                writer.write("file '" + imageFiles.get(imageFiles.size() - 1).getAbsolutePath().replace("'", "'\\''") + "'");
                writer.newLine();
            }

            // 6. Stitch images + narration into an MP4
            File outputFile = tempDir.resolve("final.mp4").toFile();
            runFfmpeg(List.of(
                    ffmpegPath, "-y",
                    "-f", "concat", "-safe", "0",
                    "-i", imageListFile.getAbsolutePath(),
                    "-i", audioFile.getAbsolutePath(),
                    "-c:v", "libx264", "-pix_fmt", "yuv420p",
                    "-c:a", "aac",
                    "-shortest",
                    outputFile.getAbsolutePath()
            ));

            byte[] videoBytes = Files.readAllBytes(outputFile.toPath());
            String key = "files/notebook-service-files/video/" + notebookId + "/" + UUID.randomUUID() + ".mp4";
            s3Service.uploadBytes(key, videoBytes, "video/mp4");
            return key;

        } catch (Exception e) {
            throw new RuntimeException("Video generation failed: " + e.getMessage(), e);
        } finally {
            deleteRecursively(tempDir);
        }
    }

    /**
     * Picks which slides make it into the video. "explainer" keeps everything
     * (unchanged behavior). "short" evenly samples down to a handful of
     * slides — always keeping the first (title/overview) and last (summary)
     * slide — so the video and its narration both end up noticeably shorter.
     * (Independently, generate(...) additionally caps the result at
     * MAX_SLIDES_FOR_VIDEO regardless of format.)
     */
    private List<JsonNode> selectSlidesForFormat(JsonNode slidesArray, String format) {
        List<JsonNode> all = new ArrayList<>();
        slidesArray.forEach(all::add);

        if (!"short".equals(format) || all.size() <= SHORT_FORMAT_MAX_SLIDES) {
            return all;
        }

        int total = all.size();
        int targetCount = SHORT_FORMAT_MAX_SLIDES;
        TreeSet<Integer> indices = new TreeSet<>();
        for (int i = 0; i < targetCount; i++) {
            indices.add((int) Math.round(i * (total - 1) / (double) (targetCount - 1)));
        }

        List<JsonNode> selected = new ArrayList<>();
        for (int idx : indices) {
            selected.add(all.get(idx));
        }
        return selected;
    }

    /**
     * Rewrites the raw concatenated slide text into a single narration script
     * that emphasizes the given focus topic. Falls back silently to the raw
     * narration if this call fails, since a focus-narration hiccup shouldn't
     * abort an otherwise-working video.
     */
    private String buildFocusedNarration(String rawNarration, String focus, String format) {
        String lengthHint = "short".equals(format)
                ? "Keep it brief and punchy, matching the shorter runtime of this video."
                : "Keep it clear and thorough, matching the longer runtime of this video.";
        String systemPrompt = """
                You are turning raw slide content (slide titles, bullets, and notes,
                concatenated together) into a single continuous narration script for
                a text-to-speech voiceover on an educational video. Rewrite it as
                natural spoken narration — no bullet points, no mentions of "slide",
                just flowing narration a narrator would read aloud. Do not invent
                facts that aren't in the content below.

                Focus specifically on: %s. Give this topic noticeably more emphasis
                and detail than the rest of the material, while still briefly
                covering the other content for context. %s

                Raw slide content:
                %s
                """.formatted(focus, lengthHint, rawNarration);

        try {
            String rewritten = openAiService.chat(systemPrompt, "Write the narration script now.");
            if (rewritten != null && !rewritten.isBlank()) {
                return rewritten.trim();
            }
        } catch (Exception e) {
            System.err.println("⚠ Focused narration generation failed, falling back to the raw slide "
                    + "narration: " + e.getMessage());
        }
        return rawNarration;
    }

    /**
     * Renders every selected slide to a PNG, in original slide order.
     *
     * For "custom"/"whiteboard"/"kawaii" (illustrated) styles, all per-slide
     * background-artwork generations are kicked off concurrently on
     * imageGenerationExecutor and awaited together (bounded by
     * IMAGE_BATCH_TIMEOUT_SECONDS) instead of one-at-a-time, since that
     * sequential OpenAI Image API round-trip was the actual bottleneck.
     * Compositing/plain-layout rendering itself stays on the calling thread,
     * per slide, since it's cheap and doesn't need parallelizing.
     *
     * Any slide whose artwork generation fails, or isn't done by the time
     * the overall batch timeout elapses, silently falls back to the plain
     * Graphics2D layout for that slide only — same behavior as before, just
     * driven by a future's outcome instead of a per-iteration try/catch.
     *
     * "classic"/"auto-select" skip artwork generation entirely (no extra API
     * calls) and render the plain layout sequentially, same as before.
     */
    private List<File> renderSlides(List<JsonNode> selectedSlides, Path tempDir,
                                     String visualStyle, String focus) throws IOException {
        boolean useGeneratedArt = "custom".equals(visualStyle)
                || "whiteboard".equals(visualStyle)
                || "kawaii".equals(visualStyle);

        int n = selectedSlides.size();
        List<String> titles = new ArrayList<>(n);
        List<List<String>> bulletsPerSlide = new ArrayList<>(n);
        for (JsonNode slideNode : selectedSlides) {
            titles.add(slideNode.path("title").asText(""));
            List<String> bullets = new ArrayList<>();
            JsonNode bulletsNode = slideNode.path("bullets");
            if (bulletsNode.isArray()) {
                bulletsNode.forEach(b -> bullets.add(b.asText("")));
            }
            bulletsPerSlide.add(bullets);
        }

        // artwork.get(i) stays null (plain-layout fallback) unless generation
        // for slide i both succeeded and completed within the batch timeout.
        BufferedImage[] artwork = new BufferedImage[n];

        if (useGeneratedArt) {
            List<CompletableFuture<BufferedImage>> futures = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                String title = titles.get(i);
                List<String> bullets = bulletsPerSlide.get(i);
                int slideIndex = i;
                CompletableFuture<BufferedImage> future = CompletableFuture
                        .supplyAsync(() -> {
                            try {
                                return generateSlideArtwork(title, bullets, visualStyle, focus);
                            } catch (Exception e) {
                                System.err.println("⚠ Slide illustration failed for slide " + slideIndex
                                        + ", falling back to the plain slide layout: " + e.getMessage());
                                return null;
                            }
                        }, imageGenerationExecutor)
                        .exceptionally(ex -> {
                            System.err.println("⚠ Slide illustration failed for slide " + slideIndex
                                    + ", falling back to the plain slide layout: " + ex.getMessage());
                            return null;
                        });
                futures.add(future);
            }

            CompletableFuture<Void> all = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            try {
                all.get(IMAGE_BATCH_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                System.err.println("⚠ Slide illustration batch did not finish within "
                        + IMAGE_BATCH_TIMEOUT_SECONDS + "s; any unfinished slides fall back to the plain layout");
            } catch (ExecutionException | InterruptedException e) {
                // Individual failures are already handled per-future above, so this
                // is unexpected — log and continue with whatever completed.
                System.err.println("⚠ Slide illustration batch wait failed: " + e.getMessage());
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
            }

            for (int i = 0; i < n; i++) {
                CompletableFuture<BufferedImage> future = futures.get(i);
                // getNow(null): if it finished (success or handled failure) this
                // returns its result (possibly null); if it's still running past
                // the batch timeout, this also returns null — same fallback.
                artwork[i] = future.getNow(null);
            }
        }

        // Assemble the final ordered list of images by index — order is
        // preserved regardless of which artwork future finished first, since
        // we're indexing into pre-sized arrays throughout.
        List<File> imageFiles = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            String title = titles.get(i);
            List<String> bullets = bulletsPerSlide.get(i);
            if (artwork[i] != null) {
                imageFiles.add(renderSlideImageOverArtwork(title, bullets, artwork[i], tempDir, i));
            } else {
                imageFiles.add(renderSlideImage(title, bullets, tempDir, i));
            }
        }
        return imageFiles;
    }

    /**
     * Calls the shared OpenAI image-generation endpoint for one slide's
     * illustrated background. Requests "low" quality — clarity matters less
     * for a few seconds of video background than for a single standalone
     * Infographic, and low quality generates noticeably faster, which is
     * what makes the parallel batch above finish quickly.
     */
    private BufferedImage generateSlideArtwork(String title, List<String> bullets, String visualStyle, String focus) throws IOException {
        String styleDescriptor = switch (visualStyle) {
            case "whiteboard" -> "hand-drawn whiteboard sketch style, black marker on white background";
            case "kawaii" -> "cute kawaii illustration style, soft pastel colors";
            default -> (focus != null && !focus.isEmpty())
                    ? "clean modern illustration style, subtly themed around: " + focus
                    : "clean modern illustration style";
        };

        String contentSummary = bullets.isEmpty() ? title : title + ": " + String.join("; ", bullets);
        String prompt = "Simple illustrative background image for an educational video slide about: \""
                + contentSummary + "\". Style: " + styleDescriptor + ". No embedded text or words in the "
                + "image itself. Wide landscape composition, uncluttered so text can be overlaid on top later.";

        byte[] imageBytes = openAiService.generateImage(prompt, ILLUSTRATION_IMAGE_SIZE, VIDEO_ILLUSTRATION_QUALITY);
        BufferedImage raw = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (raw == null) {
            throw new IOException("Generated slide illustration could not be decoded");
        }
        return raw;
    }

    /** Composites the slide's title/bullets over an AI-generated background image. */
    private File renderSlideImageOverArtwork(String title, List<String> bullets, BufferedImage artwork,
                                              Path tempDir, int index) throws IOException {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            drawCoverFit(g, artwork, WIDTH, HEIGHT);

            // Semi-transparent dark band behind the text so it stays legible
            // against whatever background the image model generated.
            int bandHeight = Math.min(140 + 36 * Math.min(bullets.size(), 4), HEIGHT - 40);
            g.setColor(new Color(0, 0, 0, 140));
            g.fillRect(0, HEIGHT - bandHeight, WIDTH, bandHeight);

            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 40));
            int y = drawWrapped(g, title, 60, HEIGHT - bandHeight + 50, WIDTH - 120, 46);

            g.setFont(new Font("SansSerif", Font.PLAIN, 26));
            y = Math.max(y, HEIGHT - bandHeight + 100);
            for (String bullet : bullets) {
                y = drawWrapped(g, "•  " + bullet, 80, y, WIDTH - 160, 36);
                if (y > HEIGHT - 30) break; // basic overflow guard; no multi-slide splitting
            }
        } finally {
            g.dispose();
        }

        File file = tempDir.resolve("slide_" + index + ".png").toFile();
        ImageIO.write(image, "png", file);
        return file;
    }

    /** Scales+crops src to exactly fill targetW x targetH (cover-fit), centered. */
    private void drawCoverFit(Graphics2D g, BufferedImage src, int targetW, int targetH) {
        double srcRatio = src.getWidth() / (double) src.getHeight();
        double targetRatio = targetW / (double) targetH;
        int drawW, drawH, x, y;
        if (srcRatio > targetRatio) {
            drawH = targetH;
            drawW = (int) Math.round(targetH * srcRatio);
            x = (targetW - drawW) / 2;
            y = 0;
        } else {
            drawW = targetW;
            drawH = (int) Math.round(targetW / srcRatio);
            x = 0;
            y = (targetH - drawH) / 2;
        }
        g.drawImage(src, x, y, drawW, drawH, null);
    }

    private String normalizeFormat(String format) {
        if (format == null) {
            return "explainer";
        }
        String f = format.trim().toLowerCase();
        return switch (f) {
            case "short", "explainer" -> f;
            default -> "explainer";
        };
    }

    private String normalizeVisualStyle(String visualStyle) {
        if (visualStyle == null) {
            return "auto-select";
        }
        String v = visualStyle.trim().toLowerCase();
        return switch (v) {
            case "auto-select", "custom", "classic", "whiteboard", "kawaii" -> v;
            default -> "auto-select";
        };
    }

    private File renderSlideImage(String title, List<String> bullets, Path tempDir, int index) throws IOException {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, WIDTH, HEIGHT);

            g.setColor(new Color(25, 25, 25));
            g.setFont(new Font("SansSerif", Font.BOLD, 46));
            int y = drawWrapped(g, title, 60, 100, WIDTH - 120, 52);

            g.setFont(new Font("SansSerif", Font.PLAIN, 30));
            y = Math.max(y, 220);
            for (String bullet : bullets) {
                y = drawWrapped(g, "•  " + bullet, 80, y, WIDTH - 160, 42);
                y += 20;
                if (y > HEIGHT - 60) break; // basic overflow guard; no multi-slide splitting
            }
        } finally {
            g.dispose();
        }

        File file = tempDir.resolve("slide_" + index + ".png").toFile();
        ImageIO.write(image, "png", file);
        return file;
    }

    /** Basic word-wrap so long titles/bullets don't run off the image. Returns the y position after the block. */
    private int drawWrapped(Graphics2D g, String text, int x, int y, int maxWidth, int lineHeight) {
        if (text == null || text.isBlank()) {
            return y;
        }
        FontMetrics fm = g.getFontMetrics();
        StringBuilder line = new StringBuilder();
        int curY = y;
        for (String word : text.split(" ")) {
            String candidate = line.isEmpty() ? word : line + " " + word;
            if (fm.stringWidth(candidate) > maxWidth && !line.isEmpty()) {
                g.drawString(line.toString(), x, curY);
                curY += lineHeight;
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(candidate);
            }
        }
        if (!line.isEmpty()) {
            g.drawString(line.toString(), x, curY);
            curY += lineHeight;
        }
        return curY;
    }

    private double getAudioDurationSeconds(File audioFile) {
        String ffprobePath = ffmpegPath.endsWith("ffmpeg")
                ? ffmpegPath.substring(0, ffmpegPath.length() - "ffmpeg".length()) + "ffprobe"
                : "ffprobe";
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    ffprobePath, "-v", "error", "-show_entries", "format=duration",
                    "-of", "default=noprint_wrapper=1:nokey=1", audioFile.getAbsolutePath());
            pb.redirectErrorStream(true);
            Process process = pb.start();
            String output;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                output = reader.lines().collect(Collectors.joining()).trim();
            }
            boolean finished = process.waitFor(15, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return -1;
            }
            return Double.parseDouble(output);
        } catch (Exception e) {
            // ffprobe not available/parseable — caller falls back to a words-per-second estimate
            return -1;
        }
    }

    private void runFfmpeg(List<String> command) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            while (reader.readLine() != null) {
                // discarded; swap for a logger if ffmpeg diagnostics are needed
            }
        }

        boolean finished = process.waitFor(180, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("ffmpeg timed out while rendering the video");
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