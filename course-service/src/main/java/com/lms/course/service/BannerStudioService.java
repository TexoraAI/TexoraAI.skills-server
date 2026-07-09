package com.lms.course.service;

import com.lms.course.dto.BannerStudioAiGenerateRequestDTO;
import com.lms.course.dto.BannerStudioAiGenerateResponseDTO;
import com.lms.course.dto.BannerStudioRequestDTO;
import com.lms.course.dto.BannerStudioResponseDTO;
import com.lms.course.dto.BannerStudioStatusUpdateDTO;
import com.lms.course.exception.BannerNotFoundException;
import com.lms.course.model.BannerStudio;
import com.lms.course.repository.BannerStudioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Concrete service class for Banner Studio — follows the same style as the
 * rest of com.lms.course.service (no service.impl / interface split).
 * Handles CRUD, duplicate, publish/schedule, and delegates AI copy
 * generation to OpenAIService.
 */
@Service
public class BannerStudioService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    private final BannerStudioRepository bannerStudioRepository;
    private final OpenAIService openAIService;

    public BannerStudioService(BannerStudioRepository bannerStudioRepository, OpenAIService openAIService) {
        this.bannerStudioRepository = bannerStudioRepository;
        this.openAIService = openAIService;
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
        applyRequestToEntity(request, banner);
        BannerStudio saved = bannerStudioRepository.save(banner);
        return toResponseDto(saved);
    }

    public void deleteBanner(Long id) {
        BannerStudio banner = findByIdOrThrow(id);
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
        return openAIService.generateBannerCopy(request);
    }

    /** Persists an AI-generated preview as a draft banner ("Add to Banners" button). */
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

        if (request.getDesktopImageUrl() != null) banner.setDesktopImageUrl(request.getDesktopImageUrl());
        if (request.getTabletImageUrl() != null) banner.setTabletImageUrl(request.getTabletImageUrl());
        if (request.getMobileImageUrl() != null) banner.setMobileImageUrl(request.getMobileImageUrl());

        if (request.getTitleSize() != null) banner.setTitleSize(request.getTitleSize());
        if (request.getTitleWeight() != null) banner.setTitleWeight(request.getTitleWeight());
        if (request.getTitleColor() != null) banner.setTitleColor(request.getTitleColor());
        if (request.getCanvasPadding() != null) banner.setCanvasPadding(request.getCanvasPadding());
        if (request.getAlign() != null) banner.setAlign(request.getAlign());
        if (request.getCanvasRadius() != null) banner.setCanvasRadius(request.getCanvasRadius());
        if (request.getCtaRadius() != null) banner.setCtaRadius(request.getCtaRadius());
        if (request.getAnimation() != null) banner.setAnimation(request.getAnimation());
    }

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
        dto.setDesktopImageUrl(banner.getDesktopImageUrl());
        dto.setTabletImageUrl(banner.getTabletImageUrl());
        dto.setMobileImageUrl(banner.getMobileImageUrl());
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
}