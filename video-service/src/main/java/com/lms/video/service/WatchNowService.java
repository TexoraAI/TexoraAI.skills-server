package com.lms.video.service;

import com.lms.video.dto.WatchNowDTO;
import com.lms.video.model.WatchNow;
import com.lms.video.repository.WatchNowRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.lms.video.dto.WatchNowStatsDTO;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class WatchNowService {

    private final WatchNowRepository repo;

    // ─── Storage directory ───────────────────────────────────────────────────
    // All video files and thumbnails for the WatchNow feature land here.
    // The stream endpoint in the controller must point to the same path.
    private static final String VIDEO_DIR =
            System.getProperty("user.dir") + "/videos/watch-now/";

    public WatchNowService(WatchNowRepository repo) {
        this.repo = repo;
    }

    // ── Upload (create) ──────────────────────────────────────────────────────
    public WatchNow upload(
            MultipartFile video,
            MultipartFile thumbnail,
            WatchNowDTO dto
    ) throws IOException {

        // Ensure storage directory exists
        File dir = new File(VIDEO_DIR);
        if (!dir.exists()) dir.mkdirs();

        // ── Save video file ──
        String fileName  = null;
        String filePath  = null;
        if (video != null && !video.isEmpty()) {
            fileName = System.currentTimeMillis() + "_" + video.getOriginalFilename();
            filePath = VIDEO_DIR + fileName;
            video.transferTo(new File(filePath));
        }

        // ── Save thumbnail (optional) ──
        String thumbName = null;
        if (thumbnail != null && !thumbnail.isEmpty()) {
            thumbName = System.currentTimeMillis() + "_thumb_" + thumbnail.getOriginalFilename();
            thumbnail.transferTo(new File(VIDEO_DIR + thumbName));
        }

        // ── Build entity ──
        WatchNow entity = buildEntity(new WatchNow(), dto, fileName, filePath, thumbName);
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

        File dir = new File(VIDEO_DIR);
        if (!dir.exists()) dir.mkdirs();

        // Replace video only if a new one is supplied
        String fileName = existing.getFileName();
        String filePath = existing.getFilePath();
        if (video != null && !video.isEmpty()) {
            // delete old file
            if (filePath != null) { File old = new File(filePath); if (old.exists()) old.delete(); }
            fileName = System.currentTimeMillis() + "_" + video.getOriginalFilename();
            filePath = VIDEO_DIR + fileName;
            video.transferTo(new File(filePath));
        }

        // Replace thumbnail only if a new one is supplied
        String thumbName = existing.getThumbnail();
        if (thumbnail != null && !thumbnail.isEmpty()) {
            if (thumbName != null) { File old = new File(VIDEO_DIR + thumbName); if (old.exists()) old.delete(); }
            thumbName = System.currentTimeMillis() + "_thumb_" + thumbnail.getOriginalFilename();
            thumbnail.transferTo(new File(VIDEO_DIR + thumbName));
        }

        buildEntity(existing, dto, fileName, filePath, thumbName);
        return repo.save(existing);
    }

    // ── Get all ──────────────────────────────────────────────────────────────
    public List<WatchNow> getAll() {
        return repo.findAll();
    }

    // ── Get by id ────────────────────────────────────────────────────────────
    public Optional<WatchNow> getById(Long id) {
        return repo.findById(id);
    }

    // ── Delete by primary key ─────────────────────────────────────────────────
    public void deleteById(Long id) {
        WatchNow entity = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("WatchNow not found: " + id));
        deleteFiles(entity);
        repo.delete(entity);
    }

    // ── Delete by courseId (legacy / bulk) ───────────────────────────────────
    public void deleteByCourseId(Long courseId) {
        List<WatchNow> list = repo.findByCourseId(courseId);
        for (WatchNow v : list) {
            deleteFiles(v);
            repo.delete(v);
        }
    }

    // ── Storage directory accessor (used by controller for streaming) ─────────
    public static String getVideoDir() {
        return VIDEO_DIR;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private WatchNow buildEntity(
            WatchNow e,
            WatchNowDTO dto,
            String fileName,
            String filePath,
            String thumbName
    ) {
        e.setCourseId(dto.getCourseId());
        e.setTitle(dto.getTitle());
        e.setFileName(fileName);
        e.setFilePath(filePath);
        e.setThumbnail(thumbName);
        e.setInstructorName(dto.getInstructorName());
        e.setInstructorRole(dto.getInstructorRole());
        e.setExperience(dto.getExperience());
        e.setStudentCount(dto.getStudentCount());
        e.setDescription(dto.getDescription());
        e.setLearnPoints(dto.getLearnPoints());
        e.setPublishDate(dto.getPublishDate());
        e.setLearnersCount(dto.getLearnersCount());
        e.setShowInstructorLive(dto.isShowInstructorLive());
        e.setPlatformName(dto.getPlatformName());
        e.setFeaturedTag(dto.getFeaturedTag());
        e.setHostedBy(dto.getHostedBy());
        e.setStatus(dto.getStatus() != null ? dto.getStatus() : "published");
        e.setShowMoreEnabled(dto.isShowMoreEnabled());
        // Only overwrite videoUrl if DTO carries one (allows keeping existing)
        if (dto.getVideoUrl() != null && !dto.getVideoUrl().isBlank()) {
            e.setVideoUrl(dto.getVideoUrl());
        }
        return e;
    }

    private void deleteFiles(WatchNow v) {
        if (v.getFilePath() != null) {
            File f = new File(v.getFilePath());
            if (f.exists()) f.delete();
        }
        if (v.getThumbnail() != null) {
            File t = new File(VIDEO_DIR + v.getThumbnail());
            if (t.exists()) t.delete();
        }
    }
    
    public WatchNowStatsDTO getDashboardStats() {

        long totalCourses = repo.count();

        long publishedCourses = repo.countByStatus("published");

        long draftCourses = repo.countByStatus("draft");

        long totalLearners = repo.findAll()
                .stream()
                .map(WatchNow::getLearnersCount)
                .filter(v -> v != null && !v.isBlank())
                .map(v -> v.replace("+", "").trim())
                .filter(v -> v.matches("\\d+"))
                .mapToLong(Long::parseLong)
                .sum();

        return new WatchNowStatsDTO(
                totalCourses,
                publishedCourses,
                draftCourses,
                totalLearners
        );
    }
}