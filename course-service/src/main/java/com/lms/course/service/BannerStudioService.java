package com.lms.course.service;

import com.lms.course.dto.BannerStudioAiGenerateRequestDTO;
import com.lms.course.dto.BannerStudioAiGenerateResponseDTO;
import com.lms.course.dto.BannerStudioRequestDTO;
import com.lms.course.dto.BannerStudioResponseDTO;
import com.lms.course.dto.BannerStudioStatusUpdateDTO;
import com.lms.course.exception.BannerNotFoundException;
import com.lms.course.model.BannerStudio;
import com.lms.course.repository.BannerStudioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Concrete service class for Banner Studio — follows the same style as the
 * rest of com.lms.course.service (no service.impl / interface split).
 * Handles CRUD, duplicate, publish/schedule, and delegates AI copy
 * generation to OpenAIService.
 *
 * Image fields (desktop/tablet/mobile) now store S3 keys under
 * images/banner-images/ instead of raw base64 — same pattern as
 * MentorFeedbackService. Presigned URLs are generated only at
 * response time via toResponseDto().
 */
@Service
public class BannerStudioService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    private final BannerStudioRepository bannerStudioRepository;
    private final OpenAIService openAIService;
    private final S3Service s3Service;

    @Value("${image.banner.s3-prefix}")
    private String bannerImagePrefix;

    private String aiGeneratedImagePrefix() {
        return bannerImagePrefix.endsWith("/")
                ? bannerImagePrefix + "ai-generated/"
                : bannerImagePrefix + "/ai-generated/";
    }
    
    @Value("${image.banner.presign-expiry-minutes:15}")
    private long presignExpiryMinutes;

    public BannerStudioService(BannerStudioRepository bannerStudioRepository,
                                OpenAIService openAIService,
                                S3Service s3Service) {
        this.bannerStudioRepository = bannerStudioRepository;
        this.openAIService = openAIService;
        this.s3Service = s3Service;
    }

    // ===================== Image upload =====================

    /**
     * Uploads a single banner creative (desktop/tablet/mobile) to S3 and
     * returns the S3 key. Frontend calls this once per device slot, gets
     * a key back, then sends that key as desktopImageUrl/tabletImageUrl/
     * mobileImageUrl in the create/update payload.
     */
    public String uploadBannerImage(MultipartFile file) throws IOException {
        String safeName = (file.getOriginalFilename() == null ? "image" : file.getOriginalFilename())
                .replaceAll("[^a-zA-Z0-9._-]", "_");
        String key = bannerImagePrefix + System.currentTimeMillis() + "_" + UUID.randomUUID() + "_" + safeName;
        s3Service.uploadFile(key, file);
        return key;
    }

    // ===================== Read =====================

    public List<BannerStudioResponseDTO> getAllBanners(String status, String search) {
        List<BannerStudio> banners;

        BannerStudio.BannerStatus statusEnum = parseStatusOrNull(status);
        String searchTerm = (search == null || search.isBlank()) ? null : search.trim();

        if (statusEnum == null && searchTerm == null) {
            banners = bannerStudioRepository.findAllByOrderByUpdatedAtDesc();
        } else {
            banners = bannerStudioRepository.search(statusEnum, searchTerm);
        }

        return banners.stream().map(this::toResponseDto).toList();
    }

    public BannerStudioResponseDTO getBannerById(Long id) {
        BannerStudio banner = findByIdOrThrow(id);
        return toResponseDto(banner);
    }

    // ===================== Create / Update =====================

    public BannerStudioResponseDTO createBanner(BannerStudioRequestDTO request) {
        BannerStudio banner = new BannerStudio();
        applyRequestToEntity(request, banner);
        BannerStudio saved = bannerStudioRepository.save(banner);
        return toResponseDto(saved);
    }

    public BannerStudioResponseDTO updateBanner(Long id, BannerStudioRequestDTO request) {
        BannerStudio banner = findByIdOrThrow(id);

        // capture old keys before overwrite so we can clean up S3 afterwards
        String oldDesktop = banner.getDesktopImageUrl();
        String oldTablet = banner.getTabletImageUrl();
        String oldMobile = banner.getMobileImageUrl();

        applyRequestToEntity(request, banner);
        BannerStudio saved = bannerStudioRepository.save(banner);

        deleteIfReplaced(oldDesktop, saved.getDesktopImageUrl());
        deleteIfReplaced(oldTablet, saved.getTabletImageUrl());
        deleteIfReplaced(oldMobile, saved.getMobileImageUrl());

        return toResponseDto(saved);
    }

    public void deleteBanner(Long id) {
        BannerStudio banner = findByIdOrThrow(id);
        deleteKeyIfPresent(banner.getDesktopImageUrl());
        deleteKeyIfPresent(banner.getTabletImageUrl());
        deleteKeyIfPresent(banner.getMobileImageUrl());
        bannerStudioRepository.delete(banner);
    }

    public BannerStudioResponseDTO duplicateBanner(Long id) {
        BannerStudio original = findByIdOrThrow(id);

        BannerStudio copy = new BannerStudio();
        copy.setName(original.getName() + " (Copy)");
        copy.setEmoji(original.getEmoji());
        copy.setGradient(original.getGradient());
        copy.setEyebrow(original.getEyebrow());
        copy.setTitle(original.getTitle());
        copy.setSubtitle(original.getSubtitle());
        copy.setCtaText(original.getCtaText());
        copy.setCtaLink(original.getCtaLink());
        copy.setStatus(BannerStudio.BannerStatus.DRAFT);
        copy.setActive(false);
        // NOTE: duplicate shares the same S3 keys as the original (not
        // re-uploaded/copied in S3). Deleting either banner independently
        // would break the other's image — if that matters, copy the S3
        // object under a new key here instead of reusing it directly.
        copy.setDesktopImageUrl(original.getDesktopImageUrl());
        copy.setTabletImageUrl(original.getTabletImageUrl());
        copy.setMobileImageUrl(original.getMobileImageUrl());
        copy.setTitleSize(original.getTitleSize());
        copy.setTitleWeight(original.getTitleWeight());
        copy.setTitleColor(original.getTitleColor());
        copy.setCanvasPadding(original.getCanvasPadding());
        copy.setAlign(original.getAlign());
        copy.setCanvasRadius(original.getCanvasRadius());
        copy.setCtaRadius(original.getCtaRadius());
        copy.setAnimation(original.getAnimation());
        copy.setViews(0L);
        copy.setClicks(0L);

        BannerStudio saved = bannerStudioRepository.save(copy);
        return toResponseDto(saved);
    }

    // ===================== Publish / Schedule =====================

    public BannerStudioResponseDTO publishNow(Long id) {
        BannerStudio banner = findByIdOrThrow(id);
        banner.setStatus(BannerStudio.BannerStatus.ACTIVE);
        banner.setActive(true);
        banner.setStartDate(LocalDate.now());
        BannerStudio saved = bannerStudioRepository.save(banner);
        return toResponseDto(saved);
    }

    public BannerStudioResponseDTO schedule(Long id, BannerStudioStatusUpdateDTO request) {
        BannerStudio banner = findByIdOrThrow(id);
        if (request.getStartDate() == null || request.getStartDate().isBlank()) {
            throw new IllegalArgumentException("startDate is required to schedule a banner");
        }
        banner.setStatus(BannerStudio.BannerStatus.SCHEDULED);
        banner.setActive(false);
        banner.setStartDate(LocalDate.parse(request.getStartDate(), DATE_FMT));
        banner.setStartTime(request.getStartTime() != null ? request.getStartTime() : "09:00");
        BannerStudio saved = bannerStudioRepository.save(banner);
        return toResponseDto(saved);
    }

    public BannerStudioResponseDTO updateStatus(Long id, BannerStudioStatusUpdateDTO request) {
        BannerStudio banner = findByIdOrThrow(id);
        BannerStudio.BannerStatus statusEnum = parseStatusOrNull(request.getStatus());
        if (statusEnum == null) {
            throw new IllegalArgumentException("Invalid status: " + request.getStatus());
        }
        banner.setStatus(statusEnum);
        banner.setActive(statusEnum == BannerStudio.BannerStatus.ACTIVE);
        if (request.getStartDate() != null && !request.getStartDate().isBlank()) {
            banner.setStartDate(LocalDate.parse(request.getStartDate(), DATE_FMT));
        }
        if (request.getStartTime() != null && !request.getStartTime().isBlank()) {
            banner.setStartTime(request.getStartTime());
        }
        BannerStudio saved = bannerStudioRepository.save(banner);
        return toResponseDto(saved);
    }

    // ===================== Analytics =====================

    public BannerStudioResponseDTO registerView(Long id) {
        BannerStudio banner = findByIdOrThrow(id);
        banner.setViews(banner.getViews() + 1);
        return toResponseDto(bannerStudioRepository.save(banner));
    }

    public BannerStudioResponseDTO registerClick(Long id) {
        BannerStudio banner = findByIdOrThrow(id);
        banner.setClicks(banner.getClicks() + 1);
        return toResponseDto(bannerStudioRepository.save(banner));
    }

    // ===================== AI generation =====================

    public BannerStudioAiGenerateResponseDTO generateWithAi(BannerStudioAiGenerateRequestDTO request) {
        BannerStudioAiGenerateResponseDTO copy = openAIService.generateBannerCopy(request);

        try {
            byte[] imageBytes = openAIService.generateBannerImage(request);
            String key = aiGeneratedImagePrefix() + System.currentTimeMillis() + "_" + UUID.randomUUID() + "_ai.png";
            s3Service.uploadBytes(key, imageBytes, "image/png");
            copy.setDesktopImageKey(key);
            copy.setDesktopImageUrl(s3Service.generatePresignedUrl(key, Duration.ofMinutes(presignExpiryMinutes)));
        } catch (Exception e) {
            System.err.println("AI image generation failed, falling back to text-only banner: " + e.getMessage());
        }

        return copy;
    }

    /**
     * Persists an AI-generated preview as a draft banner ("Add to Banners"
     * button). Note: AI generation currently only produces text copy
     * (eyebrow/title/sub/cta/gradient/emoji) — no image is generated or
     * attached here, so desktop/tablet/mobile image fields stay null. If
     * AI image generation is added later, upload the generated image via
     * the same s3Service.uploadFile() path used by uploadBannerImage()
     * and set the resulting key on this banner before saving.
     */
    public BannerStudioResponseDTO saveAiGeneratedBanner(BannerStudioAiGenerateResponseDTO aiResult) {
        BannerStudio banner = new BannerStudio();
        banner.setName(aiResult.getTitle());
        banner.setEmoji(aiResult.getEmoji());
        banner.setGradient(aiResult.getGradient());
        banner.setEyebrow(aiResult.getEyebrow());
        banner.setTitle(aiResult.getTitle());
        banner.setSubtitle(aiResult.getSub());
        banner.setCtaText(aiResult.getCta());
        banner.setStatus(BannerStudio.BannerStatus.DRAFT);
        banner.setActive(false);
        banner.setAiGenerated(true);
        banner.setAiAudience(aiResult.getAudience());
        banner.setAiTheme(aiResult.getTheme());
        banner.setAiBannerType(aiResult.getBannerType());
        banner.setAiStyle(aiResult.getStyle());

        BannerStudio saved = bannerStudioRepository.save(banner);
        return toResponseDto(saved);
    }

    // ===================== Helpers =====================

    private BannerStudio findByIdOrThrow(Long id) {
        return bannerStudioRepository.findById(id)
                .orElseThrow(() -> new BannerNotFoundException(id));
    }

    private BannerStudio.BannerStatus parseStatusOrNull(String status) {
        if (status == null || status.isBlank() || status.equalsIgnoreCase("all")) {
            return null;
        }
        try {
            return BannerStudio.BannerStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** Deletes the old S3 object only if it existed and the key actually changed. */
    private void deleteIfReplaced(String oldKey, String newKey) {
        if (oldKey != null && !oldKey.isBlank() && !oldKey.equals(newKey)) {
            s3Service.deleteFile(oldKey);
        }
    }

//    private void deleteKeyIfPresent(String key) {
//        if (key != null && !key.isBlank()) {
//            s3Service.deleteFile(key);
//        }
//    }
    private void deleteKeyIfPresent(String key) {
        if (key != null && !key.isBlank()) {
            try {
                s3Service.deleteFile(key);
            } catch (Exception e) {
                System.err.println("Failed to delete S3 object for key '" + key + "': " + e.getMessage());
            }
        }
    }

    private void applyRequestToEntity(BannerStudioRequestDTO request, BannerStudio banner) {
        banner.setName(request.getName());
        banner.setEmoji(request.getEmoji());
        banner.setGradient(request.getGradient());
        banner.setEyebrow(request.getEyebrow());
        banner.setTitle(request.getTitle());
        banner.setSubtitle(request.getSubtitle());
        banner.setCtaText(request.getCtaText());
        banner.setCtaLink(request.getCtaLink());

        BannerStudio.BannerStatus statusEnum = parseStatusOrNull(request.getStatus());
        banner.setStatus(statusEnum != null ? statusEnum : BannerStudio.BannerStatus.DRAFT);
        banner.setActive(request.isActive() || banner.getStatus() == BannerStudio.BannerStatus.ACTIVE);

        if (request.getStartDate() != null && !request.getStartDate().isBlank()) {
            banner.setStartDate(LocalDate.parse(request.getStartDate(), DATE_FMT));
        }
        banner.setStartTime(request.getStartTime());
        if (request.getEndDate() != null && !request.getEndDate().isBlank()) {
            banner.setEndDate(LocalDate.parse(request.getEndDate(), DATE_FMT));
        }

        // These hold S3 KEYS (e.g. "images/banner-images/..."), not base64
        // and not presigned URLs. null/absent = leave unchanged (existing
        // key kept). Explicit "" (blank string) = user removed the image,
        // clear it in the DB and let deleteIfReplaced() clean up S3.
        if (request.getDesktopImageUrl() != null) {
            banner.setDesktopImageUrl(request.getDesktopImageUrl().isBlank() ? null : request.getDesktopImageUrl());
        }
        if (request.getTabletImageUrl() != null) {
            banner.setTabletImageUrl(request.getTabletImageUrl().isBlank() ? null : request.getTabletImageUrl());
        }
        if (request.getMobileImageUrl() != null) {
            banner.setMobileImageUrl(request.getMobileImageUrl().isBlank() ? null : request.getMobileImageUrl());
        }

        if (request.getTitleSize() != null) banner.setTitleSize(request.getTitleSize());
        if (request.getTitleWeight() != null) banner.setTitleWeight(request.getTitleWeight());
        if (request.getTitleColor() != null) banner.setTitleColor(request.getTitleColor());
        if (request.getCanvasPadding() != null) banner.setCanvasPadding(request.getCanvasPadding());
        if (request.getAlign() != null) banner.setAlign(request.getAlign());
        if (request.getCanvasRadius() != null) banner.setCanvasRadius(request.getCanvasRadius());
        if (request.getCtaRadius() != null) banner.setCtaRadius(request.getCtaRadius());
        if (request.getAnimation() != null) banner.setAnimation(request.getAnimation());
    }

    /**
     * Converts S3 keys to live presigned URLs at response time, and also
     * exposes the raw keys so the frontend can round-trip them on the next
     * update without resubmitting an expiring presigned URL.
     */
    private BannerStudioResponseDTO toResponseDto(BannerStudio banner) {
        BannerStudioResponseDTO dto = new BannerStudioResponseDTO();
        dto.setId(banner.getId());
        dto.setName(banner.getName());
        dto.setEmoji(banner.getEmoji());
        dto.setGradient(banner.getGradient());
        dto.setEyebrow(banner.getEyebrow());
        dto.setTitle(banner.getTitle());
        dto.setSubtitle(banner.getSubtitle());
        dto.setCtaText(banner.getCtaText());
        dto.setCtaLink(banner.getCtaLink());
        dto.setStatus(banner.getStatus().name().toLowerCase());
        dto.setActive(banner.isActive());
        dto.setStartDate(banner.getStartDate() != null ? banner.getStartDate().format(DATE_FMT) : null);
        dto.setStartTime(banner.getStartTime());
        dto.setEndDate(banner.getEndDate() != null ? banner.getEndDate().format(DATE_FMT) : null);
        dto.setDesktopImageUrl(toPresignedUrl(banner.getDesktopImageUrl()));
        dto.setTabletImageUrl(toPresignedUrl(banner.getTabletImageUrl()));
        dto.setMobileImageUrl(toPresignedUrl(banner.getMobileImageUrl()));
        dto.setDesktopImageKey(banner.getDesktopImageUrl());
        dto.setTabletImageKey(banner.getTabletImageUrl());
        dto.setMobileImageKey(banner.getMobileImageUrl());
        dto.setTitleSize(banner.getTitleSize());
        dto.setTitleWeight(banner.getTitleWeight());
        dto.setTitleColor(banner.getTitleColor());
        dto.setCanvasPadding(banner.getCanvasPadding());
        dto.setAlign(banner.getAlign());
        dto.setCanvasRadius(banner.getCanvasRadius());
        dto.setCtaRadius(banner.getCtaRadius());
        dto.setAnimation(banner.getAnimation());
        dto.setAiGenerated(banner.isAiGenerated());
        dto.setViews(banner.getViews());
        dto.setClicks(banner.getClicks());
        dto.setCtr(banner.getViews() > 0
                ? Math.round((banner.getClicks() * 1000.0 / banner.getViews())) / 10.0
                : 0.0);
        dto.setCreatedAt(banner.getCreatedAt() != null ? banner.getCreatedAt().toString() : null);
        dto.setUpdatedAt(banner.getUpdatedAt() != null ? banner.getUpdatedAt().toString() : null);
        return dto;
    }

    private String toPresignedUrl(String key) {
        if (key == null || key.isBlank()) return null;
        return s3Service.generatePresignedUrl(key, Duration.ofMinutes(presignExpiryMinutes));
    }
}