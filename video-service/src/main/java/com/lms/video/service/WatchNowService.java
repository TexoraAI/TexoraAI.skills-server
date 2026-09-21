//
//
//package com.lms.video.service;
//import org.springframework.transaction.annotation.Transactional;
//import java.util.ArrayList;
//import java.util.Map;
//import java.util.stream.Collectors;
//import com.lms.video.dto.WatchNowDTO;
//import com.lms.video.dto.WatchNowStatsDTO;
//import com.lms.video.model.WatchNow;
//import com.lms.video.repository.WatchNowRepository;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.List;
//import java.util.Optional;
//
//@Service
//public class WatchNowService {
//
//    private final WatchNowRepository repo;
//
//    // ─── Storage directory ───────────────────────────────────────────────────
//    // All video files and thumbnails for the WatchNow feature land here.
//    // The stream endpoint in the controller must point to the same path.
//    private static final String VIDEO_DIR =
//            System.getProperty("user.dir") + "/videos/watch-now/";
//
//    public WatchNowService(WatchNowRepository repo) {
//        this.repo = repo;
//    }
//
//    // ── Upload (create) ──────────────────────────────────────────────────────
//    public WatchNow upload(
//            MultipartFile video,
//            MultipartFile thumbnail,
//            WatchNowDTO dto
//    ) throws IOException {
//
//        boolean hasExternalUrl = dto.getExternalVideoUrl() != null && !dto.getExternalVideoUrl().isBlank();
//
//        if (!hasExternalUrl && (video == null || video.isEmpty())) {
//            throw new IllegalArgumentException("Either a video file or an externalVideoUrl is required");
//        }
//        if (thumbnail == null || thumbnail.isEmpty()) {
//            throw new IllegalArgumentException("Thumbnail is required");
//        }
//
//        File dir = new File(VIDEO_DIR);
//        if (!dir.exists()) dir.mkdirs();
//
//        // ── Save video file only if no external URL was supplied ──
//        String fileName = null;
//        String filePath = null;
//        if (!hasExternalUrl) {
//            fileName = System.currentTimeMillis() + "_" + video.getOriginalFilename();
//            filePath = VIDEO_DIR + fileName;
//            video.transferTo(new File(filePath));
//        }
//
//        // ── Save thumbnail ──
//        String thumbName = System.currentTimeMillis() + "_thumb_" + thumbnail.getOriginalFilename();
//        thumbnail.transferTo(new File(VIDEO_DIR + thumbName));
//
//        WatchNow entity = new WatchNow();
//        entity.setQuote(dto.getQuote());
//        entity.setVideoFileName(fileName);
//        entity.setVideoFilePath(filePath);
//        entity.setExternalVideoUrl(hasExternalUrl ? dto.getExternalVideoUrl() : null);
//        entity.setThumbnail(thumbName);
//        entity.setPersonName(dto.getPersonName());
//        entity.setPersonRole(dto.getPersonRole());
//        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : "draft");
//        entity.setSortOrder(dto.getSortOrder());
//
//        return repo.save(entity);
//    }
//
//    // ── Update (edit existing) ───────────────────────────────────────────────
//    public WatchNow update(
//            Long id,
//            MultipartFile video,
//            MultipartFile thumbnail,
//            WatchNowDTO dto
//    ) throws IOException {
//
//        WatchNow existing = repo.findById(id)
//                .orElseThrow(() -> new RuntimeException("WatchNow not found: " + id));
//
//        File dir = new File(VIDEO_DIR);
//        if (!dir.exists()) dir.mkdirs();
//
//        boolean hasExternalUrl = dto.getExternalVideoUrl() != null && !dto.getExternalVideoUrl().isBlank();
//
//        String fileName = existing.getVideoFileName();
//        String filePath = existing.getVideoFilePath();
//        String externalUrl = existing.getExternalVideoUrl();
//
//        if (video != null && !video.isEmpty()) {
//            // new file uploaded -> delete old file, clear external url
//            if (filePath != null) { File old = new File(filePath); if (old.exists()) old.delete(); }
//            fileName = System.currentTimeMillis() + "_" + video.getOriginalFilename();
//            filePath = VIDEO_DIR + fileName;
//            video.transferTo(new File(filePath));
//            externalUrl = null;
//        } else if (hasExternalUrl) {
//            // external url supplied -> delete old file, clear file fields
//            if (filePath != null) { File old = new File(filePath); if (old.exists()) old.delete(); }
//            fileName = null;
//            filePath = null;
//            externalUrl = dto.getExternalVideoUrl();
//        }
//
//        // Replace thumbnail only if a new one is supplied
//        String thumbName = existing.getThumbnail();
//        if (thumbnail != null && !thumbnail.isEmpty()) {
//            if (thumbName != null) { File old = new File(VIDEO_DIR + thumbName); if (old.exists()) old.delete(); }
//            thumbName = System.currentTimeMillis() + "_thumb_" + thumbnail.getOriginalFilename();
//            thumbnail.transferTo(new File(VIDEO_DIR + thumbName));
//        }
//
//        existing.setQuote(dto.getQuote());
//        existing.setVideoFileName(fileName);
//        existing.setVideoFilePath(filePath);
//        existing.setExternalVideoUrl(externalUrl);
//        existing.setThumbnail(thumbName);
//        existing.setPersonName(dto.getPersonName());
//        existing.setPersonRole(dto.getPersonRole());
//        if (dto.getStatus() != null) existing.setStatus(dto.getStatus());
//        existing.setSortOrder(dto.getSortOrder());
//
//        return repo.save(existing);
//    }
//
//    // ── Get all (admin) ──────────────────────────────────────────────────────
//    public List<WatchNow> getAll() {
//        return repo.findAllByOrderBySortOrderAsc();
//    }
//
//    // ── Get published (public) ───────────────────────────────────────────────
//    public List<WatchNow> getPublished() {
//        return repo.findByStatusOrderBySortOrderAsc("published");
//    }
//
//    // ── Get by id ────────────────────────────────────────────────────────────
//    public Optional<WatchNow> getById(Long id) {
//        return repo.findById(id);
//    }
//
//    // ── Delete ────────────────────────────────────────────────────────────────
//    public void deleteById(Long id) {
//        WatchNow entity = repo.findById(id)
//                .orElseThrow(() -> new RuntimeException("WatchNow not found: " + id));
//        deleteFiles(entity);
//        repo.delete(entity);
//    }
//    //reorder
//    @Transactional
//    public void reorder(List<Long> orderedIds) {
//        if (orderedIds == null || orderedIds.isEmpty()) {
//            return;
//        }
//        List<WatchNow> entities = repo.findAllById(orderedIds);
//        Map<Long, WatchNow> byId = entities.stream()
//                .collect(Collectors.toMap(WatchNow::getId, e -> e));
//
//        List<WatchNow> toSave = new ArrayList<>();
//        for (int i = 0; i < orderedIds.size(); i++) {
//            WatchNow entity = byId.get(orderedIds.get(i));
//            if (entity == null) {
//                // Stale id (e.g. deleted in another tab) — skip it instead of
//                // throwing and leaving the rest of the reorder half-applied.
//                continue;
//            }
//            entity.setSortOrder(i);
//            toSave.add(entity);
//        }
//        repo.saveAll(toSave);
//    }
//    // ── Status transitions ──────────────────────────────────────────────────
//    public WatchNow publish(Long id) {
//        WatchNow entity = repo.findById(id)
//                .orElseThrow(() -> new RuntimeException("WatchNow not found: " + id));
//        entity.setStatus("published");
//        return repo.save(entity);
//    }
//
//    public WatchNow saveDraft(Long id) {
//        WatchNow entity = repo.findById(id)
//                .orElseThrow(() -> new RuntimeException("WatchNow not found: " + id));
//        entity.setStatus("draft");
//        return repo.save(entity);
//    }
//
//    // ── Storage directory accessor (used by controller for streaming) ─────────
//    public static String getVideoDir() {
//        return VIDEO_DIR;
//    }
//    public WatchNowStatsDTO getStats() {
//        long total = repo.count();
//        long published = repo.countByStatus("published");
//        long draft = repo.countByStatus("draft");
//        long uploaded = repo.countByVideoFileNameIsNotNull();
//        long external = repo.countByExternalVideoUrlIsNotNull();
//        return new WatchNowStatsDTO(total, published, draft, uploaded, external);
//    }
//    // ─────────────────────────────────────────────────────────────────────────
//    // Helpers
//    // ─────────────────────────────────────────────────────────────────────────
//
//    private void deleteFiles(WatchNow v) {
//        if (v.getVideoFilePath() != null) {
//            File f = new File(v.getVideoFilePath());
//            if (f.exists()) f.delete();
//        }
//        if (v.getThumbnail() != null) {
//            File t = new File(VIDEO_DIR + v.getThumbnail());
//            if (t.exists()) t.delete();
//        }
//    }
//}

package com.lms.video.service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import com.lms.video.dto.WatchNowDTO;
import com.lms.video.dto.WatchNowStatsDTO;
import com.lms.video.model.WatchNow;
import com.lms.video.repository.WatchNowRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
public class WatchNowService {

    private final WatchNowRepository repo;
    private final S3Service s3Service; // ✅ NEW

    // ✅ NEW — separate S3 prefixes for watch-now videos and watch-now thumbnails
    private static final String VIDEO_S3_PREFIX = "videos/watchnow-videos/";
    private static final String IMAGE_S3_PREFIX = "images/watchnow-images/";

    // ✅ NEW — matches youtube.com/watch?v=ID, youtu.be/ID, youtube.com/embed/ID,
    // youtube.com/shorts/ID (with or without extra query params after the id)
    private static final Pattern YOUTUBE_ID_PATTERN = Pattern.compile(
            "(?:youtube\\.com/(?:watch\\?v=|embed/|shorts/)|youtu\\.be/)([a-zA-Z0-9_-]{11})"
    );

    public WatchNowService(WatchNowRepository repo, S3Service s3Service) { // ✅ NEW
        this.repo = repo;
        this.s3Service = s3Service; // ✅ NEW
    }

    // ✅ NEW — extracts the 11-character YouTube video id from any common
    // YouTube URL shape, or null if the url isn't a recognizable YouTube link.
    private String extractYouTubeVideoId(String url) {
        if (url == null) return null;
        Matcher matcher = YOUTUBE_ID_PATTERN.matcher(url);
        return matcher.find() ? matcher.group(1) : null;
    }

    // ── Upload (create) ──────────────────────────────────────────────────────
    public WatchNow upload(
            MultipartFile video,
            MultipartFile thumbnail,
            WatchNowDTO dto
    ) throws IOException {

        boolean hasExternalUrl = dto.getExternalVideoUrl() != null && !dto.getExternalVideoUrl().isBlank();

        if (!hasExternalUrl && (video == null || video.isEmpty())) {
            throw new IllegalArgumentException("Either a video file or an externalVideoUrl is required");
        }

        // ✅ NEW — if it's a YouTube link, auto-generate the real YouTube
        // thumbnail and skip the manual thumbnail requirement entirely.
        String youTubeId = hasExternalUrl ? extractYouTubeVideoId(dto.getExternalVideoUrl()) : null;
        boolean isYouTube = youTubeId != null;

        if (!isYouTube && (thumbnail == null || thumbnail.isEmpty())) {
            throw new IllegalArgumentException("Thumbnail is required");
        }

        WatchNow entity = new WatchNow();
        entity.setQuote(dto.getQuote());

        if (isYouTube) {
            // ✅ No file storage needed — this is a real, permanent YouTube URL
            entity.setVideoFileName(null);
            entity.setVideoFilePath(null);
            entity.setExternalVideoUrl(dto.getExternalVideoUrl());
            entity.setThumbnail("https://img.youtube.com/vi/" + youTubeId + "/hqdefault.jpg");
        } else if (hasExternalUrl) {
            // Non-YouTube external URL (Vimeo/direct link) — still needs a manual thumbnail
            entity.setVideoFileName(null);
            entity.setVideoFilePath(null);
            entity.setExternalVideoUrl(dto.getExternalVideoUrl());

            String thumbName = System.currentTimeMillis() + "_thumb_" + thumbnail.getOriginalFilename();
            s3Service.uploadFile(IMAGE_S3_PREFIX + thumbName, thumbnail);
            entity.setThumbnail(thumbName);
        } else {
            // Manual video upload — video to S3, thumbnail to S3
            String fileName = System.currentTimeMillis() + "_" + video.getOriginalFilename();
            s3Service.uploadFile(VIDEO_S3_PREFIX + fileName, video);
            entity.setVideoFileName(fileName);
            entity.setVideoFilePath(null); // no longer meaningful — file lives in S3
            entity.setExternalVideoUrl(null);

            String thumbName = System.currentTimeMillis() + "_thumb_" + thumbnail.getOriginalFilename();
            s3Service.uploadFile(IMAGE_S3_PREFIX + thumbName, thumbnail);
            entity.setThumbnail(thumbName);
        }

        entity.setPersonName(dto.getPersonName());
        entity.setPersonRole(dto.getPersonRole());
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : "draft");
        entity.setSortOrder(dto.getSortOrder());

        return repo.save(entity);
    }

    // ── Update (edit existing) ───────────────────────────────────────────────
    public WatchNow update(
            Long id,
            MultipartFile video,
            MultipartFile thumbnail,
            WatchNowDTO dto
    ) throws IOException {

        WatchNow existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("WatchNow not found: " + id));

        boolean hasExternalUrl = dto.getExternalVideoUrl() != null && !dto.getExternalVideoUrl().isBlank();
        String youTubeId = hasExternalUrl ? extractYouTubeVideoId(dto.getExternalVideoUrl()) : null;
        boolean isYouTube = youTubeId != null;

        String fileName = existing.getVideoFileName();
        String externalUrl = existing.getExternalVideoUrl();
        String thumbName = existing.getThumbnail();

        if (video != null && !video.isEmpty()) {
            // new file uploaded -> delete old S3 video (if any), clear external url
            if (fileName != null && !fileName.isBlank()) {
                try { s3Service.deleteFile(VIDEO_S3_PREFIX + fileName); }
                catch (Exception e) { System.out.println("Could not delete old watch-now video from S3: " + e.getMessage()); }
            }
            fileName = System.currentTimeMillis() + "_" + video.getOriginalFilename();
            s3Service.uploadFile(VIDEO_S3_PREFIX + fileName, video);
            externalUrl = null;

        } else if (isYouTube) {
            // switched to a YouTube url -> delete old S3 video (if any)
            if (fileName != null && !fileName.isBlank()) {
                try { s3Service.deleteFile(VIDEO_S3_PREFIX + fileName); }
                catch (Exception e) { System.out.println("Could not delete old watch-now video from S3: " + e.getMessage()); }
            }
            fileName = null;
            externalUrl = dto.getExternalVideoUrl();

        } else if (hasExternalUrl) {
            // switched to a non-YouTube external url -> delete old S3 video (if any)
            if (fileName != null && !fileName.isBlank()) {
                try { s3Service.deleteFile(VIDEO_S3_PREFIX + fileName); }
                catch (Exception e) { System.out.println("Could not delete old watch-now video from S3: " + e.getMessage()); }
            }
            fileName = null;
            externalUrl = dto.getExternalVideoUrl();
        }

        if (isYouTube) {
            // ✅ Always auto-refresh the thumbnail from YouTube — ignore any
            // manually supplied thumbnail file, delete an old uploaded one if present.
            if (thumbName != null && !thumbName.isBlank() && !thumbName.startsWith("http")) {
                try { s3Service.deleteFile(IMAGE_S3_PREFIX + thumbName); }
                catch (Exception e) { System.out.println("Could not delete old watch-now thumbnail from S3: " + e.getMessage()); }
            }
            thumbName = "https://img.youtube.com/vi/" + youTubeId + "/hqdefault.jpg";
        } else if (thumbnail != null && !thumbnail.isEmpty()) {
            // Manual thumbnail replacement
            if (thumbName != null && !thumbName.isBlank() && !thumbName.startsWith("http")) {
                try { s3Service.deleteFile(IMAGE_S3_PREFIX + thumbName); }
                catch (Exception e) { System.out.println("Could not delete old watch-now thumbnail from S3: " + e.getMessage()); }
            }
            thumbName = System.currentTimeMillis() + "_thumb_" + thumbnail.getOriginalFilename();
            s3Service.uploadFile(IMAGE_S3_PREFIX + thumbName, thumbnail);
        }

        existing.setQuote(dto.getQuote());
        existing.setVideoFileName(fileName);
        existing.setVideoFilePath(null); // no longer meaningful — file lives in S3
        existing.setExternalVideoUrl(externalUrl);
        existing.setThumbnail(thumbName);
        existing.setPersonName(dto.getPersonName());
        existing.setPersonRole(dto.getPersonRole());
        if (dto.getStatus() != null) existing.setStatus(dto.getStatus());
        existing.setSortOrder(dto.getSortOrder());

        return repo.save(existing);
    }

    // ── Get all (admin) ──────────────────────────────────────────────────────
    public List<WatchNow> getAll() {
        return repo.findAllByOrderBySortOrderAsc();
    }

    // ── Get published (public) ───────────────────────────────────────────────
    public List<WatchNow> getPublished() {
        return repo.findByStatusOrderBySortOrderAsc("published");
    }

    // ── Get by id ────────────────────────────────────────────────────────────
    public Optional<WatchNow> getById(Long id) {
        return repo.findById(id);
    }

    // ── Delete ────────────────────────────────────────────────────────────────
    public void deleteById(Long id) {
        WatchNow entity = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("WatchNow not found: " + id));
        deleteFiles(entity);
        repo.delete(entity);
    }
    //reorder
    @Transactional
    public void reorder(List<Long> orderedIds) {
        if (orderedIds == null || orderedIds.isEmpty()) {
            return;
        }
        List<WatchNow> entities = repo.findAllById(orderedIds);
        Map<Long, WatchNow> byId = entities.stream()
                .collect(Collectors.toMap(WatchNow::getId, e -> e));

        List<WatchNow> toSave = new ArrayList<>();
        for (int i = 0; i < orderedIds.size(); i++) {
            WatchNow entity = byId.get(orderedIds.get(i));
            if (entity == null) {
                // Stale id (e.g. deleted in another tab) — skip it instead of
                // throwing and leaving the rest of the reorder half-applied.
                continue;
            }
            entity.setSortOrder(i);
            toSave.add(entity);
        }
        repo.saveAll(toSave);
    }
    // ── Status transitions ──────────────────────────────────────────────────
    public WatchNow publish(Long id) {
        WatchNow entity = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("WatchNow not found: " + id));
        entity.setStatus("published");
        return repo.save(entity);
    }

    public WatchNow saveDraft(Long id) {
        WatchNow entity = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("WatchNow not found: " + id));
        entity.setStatus("draft");
        return repo.save(entity);
    }

    public WatchNowStatsDTO getStats() {
        long total = repo.count();
        long published = repo.countByStatus("published");
        long draft = repo.countByStatus("draft");
        long uploaded = repo.countByVideoFileNameIsNotNull();
        long external = repo.countByExternalVideoUrlIsNotNull();
        return new WatchNowStatsDTO(total, published, draft, uploaded, external);
    }

    // ================= PLAYBACK / THUMBNAIL URL =================
    // ✅ NEW — used by the controller's /stream endpoint to redirect the
    // browser straight to a fresh, temporary S3 link instead of proxying
    // bytes through our server. Works for both videos and manually-uploaded
    // thumbnails — picks the right S3 folder based on file extension.
    public String getPlaybackUrl(String fileName) {
        String lower = fileName.toLowerCase();
        boolean isImage = lower.endsWith(".jpg") || lower.endsWith(".jpeg")
                || lower.endsWith(".png") || lower.endsWith(".webp");
        String prefix = isImage ? IMAGE_S3_PREFIX : VIDEO_S3_PREFIX;
        return s3Service.generatePresignedUrl(prefix + fileName, Duration.ofHours(2));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private void deleteFiles(WatchNow v) {
        // ✅ CHANGED — delete from S3 instead of local disk
        if (v.getVideoFileName() != null && !v.getVideoFileName().isBlank()) {
            try {
                s3Service.deleteFile(VIDEO_S3_PREFIX + v.getVideoFileName());
            } catch (Exception e) {
                System.out.println("Could not delete watch-now video from S3: " + e.getMessage());
            }
        }
        // ✅ NEW — only delete from S3 if it's OUR uploaded thumbnail, not a
        // YouTube thumbnail URL (that one isn't stored in our bucket at all)
        if (v.getThumbnail() != null && !v.getThumbnail().isBlank() && !v.getThumbnail().startsWith("http")) {
            try {
                s3Service.deleteFile(IMAGE_S3_PREFIX + v.getThumbnail());
            } catch (Exception e) {
                System.out.println("Could not delete watch-now thumbnail from S3: " + e.getMessage());
            }
        }
    }
}