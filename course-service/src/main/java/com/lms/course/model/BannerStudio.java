//package com.lms.course.model;
//
//import jakarta.persistence.*;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Entity backing the Banner Studio feature (CMS Management > Banner Studio).
// * A single row represents one promotional banner shown across ILM ORA
// * (landing pages, dashboards, etc.) along with its design/builder settings,
// * per-device creative assets, scheduling info and lifetime analytics.
// */
//@Entity
//@Table(name = "banner_studio")
//public class BannerStudio {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    // ===================== Core content =====================
//
//    @Column(nullable = false, length = 255)
//    private String name;
//
//    @Column(length = 10)
//    private String emoji;
//
//    /** CSS gradient / solid color used as the card + canvas background. */
//    @Column(length = 500)
//    private String gradient;
//
//    /** Small label shown above the title, e.g. "AI & Machine Learning". */
//    @Column(length = 150)
//    private String eyebrow;
//
//    @Column(length = 255)
//    private String title;
//
//    @Column(length = 1000)
//    private String subtitle;
//
//    @Column(name = "cta_text", length = 100)
//    private String ctaText;
//
//    @Column(name = "cta_link", length = 1000)
//    private String ctaLink;
//
//    /** draft | scheduled | active | expired */
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false, length = 20)
//    private BannerStatus status = BannerStatus.DRAFT;
//
//    @Column(nullable = false)
//    private boolean active = false;
//
//    // ===================== Scheduling =====================
//
//    @Column(name = "start_date")
//    private LocalDate startDate;
//
//    @Column(name = "start_time", length = 10)
//    private String startTime;
//
//    @Column(name = "end_date")
//    private LocalDate endDate;
//
//    // ===================== Per-device creative assets =====================
//
//    @Column(name = "desktop_image_url", length = 1000)
//    private String desktopImageUrl;
//
//    @Column(name = "tablet_image_url", length = 1000)
//    private String tabletImageUrl;
//
//    @Column(name = "mobile_image_url", length = 1000)
//    private String mobileImageUrl;
//
//    // ===================== Builder / design settings =====================
//
//    @Column(name = "title_size")
//    private Integer titleSize = 30;
//
//    @Column(name = "title_weight", length = 10)
//    private String titleWeight = "700";
//
//    @Column(name = "title_color", length = 20)
//    private String titleColor = "#ffffff";
//
//    @Column(name = "canvas_padding")
//    private Integer canvasPadding = 40;
//
//    /** left | center | right */
//    @Column(length = 10)
//    private String align = "left";
//
//    @Column(name = "canvas_radius")
//    private Integer canvasRadius = 18;
//
//    @Column(name = "cta_radius")
//    private Integer ctaRadius = 30;
//
//    /** none | fade | slide | zoom */
//    @Column(length = 20)
//    private String animation = "none";
//
//    // ===================== AI generation metadata =====================
//
//    @Column(name = "ai_generated", nullable = false)
//    private boolean aiGenerated = false;
//
//    @Column(name = "ai_prompt", length = 2000)
//    private String aiPrompt;
//
//    @Column(name = "ai_audience", length = 150)
//    private String aiAudience;
//
//    @Column(name = "ai_theme", length = 150)
//    private String aiTheme;
//
//    @Column(name = "ai_banner_type", length = 150)
//    private String aiBannerType;
//
//    @Column(name = "ai_style", length = 150)
//    private String aiStyle;
//
//    // ===================== Analytics =====================
//
//    @Column(nullable = false)
//    private long views = 0L;
//
//    @Column(nullable = false)
//    private long clicks = 0L;
//
//    // ===================== Audit =====================
//
//    @Column(name = "created_at", nullable = false, updatable = false)
//    private LocalDateTime createdAt;
//
//    @Column(name = "updated_at", nullable = false)
//    private LocalDateTime updatedAt;
//
//    @PrePersist
//    protected void onCreate() {
//        LocalDateTime now = LocalDateTime.now();
//        this.createdAt = now;
//        this.updatedAt = now;
//    }
//
//    @PreUpdate
//    protected void onUpdate() {
//        this.updatedAt = LocalDateTime.now();
//    }
//
//    public enum BannerStatus {
//        DRAFT, SCHEDULED, ACTIVE, EXPIRED
//    }
//
//    public BannerStudio() {
//    }
//
//    // ===================== Getters & Setters =====================
//
//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public String getEmoji() {
//        return emoji;
//    }
//
//    public void setEmoji(String emoji) {
//        this.emoji = emoji;
//    }
//
//    public String getGradient() {
//        return gradient;
//    }
//
//    public void setGradient(String gradient) {
//        this.gradient = gradient;
//    }
//
//    public String getEyebrow() {
//        return eyebrow;
//    }
//
//    public void setEyebrow(String eyebrow) {
//        this.eyebrow = eyebrow;
//    }
//
//    public String getTitle() {
//        return title;
//    }
//
//    public void setTitle(String title) {
//        this.title = title;
//    }
//
//    public String getSubtitle() {
//        return subtitle;
//    }
//
//    public void setSubtitle(String subtitle) {
//        this.subtitle = subtitle;
//    }
//
//    public String getCtaText() {
//        return ctaText;
//    }
//
//    public void setCtaText(String ctaText) {
//        this.ctaText = ctaText;
//    }
//
//    public String getCtaLink() {
//        return ctaLink;
//    }
//
//    public void setCtaLink(String ctaLink) {
//        this.ctaLink = ctaLink;
//    }
//
//    public BannerStatus getStatus() {
//        return status;
//    }
//
//    public void setStatus(BannerStatus status) {
//        this.status = status;
//    }
//
//    public boolean isActive() {
//        return active;
//    }
//
//    public void setActive(boolean active) {
//        this.active = active;
//    }
//
//    public LocalDate getStartDate() {
//        return startDate;
//    }
//
//    public void setStartDate(LocalDate startDate) {
//        this.startDate = startDate;
//    }
//
//    public String getStartTime() {
//        return startTime;
//    }
//
//    public void setStartTime(String startTime) {
//        this.startTime = startTime;
//    }
//
//    public LocalDate getEndDate() {
//        return endDate;
//    }
//
//    public void setEndDate(LocalDate endDate) {
//        this.endDate = endDate;
//    }
//
//    public String getDesktopImageUrl() {
//        return desktopImageUrl;
//    }
//
//    public void setDesktopImageUrl(String desktopImageUrl) {
//        this.desktopImageUrl = desktopImageUrl;
//    }
//
//    public String getTabletImageUrl() {
//        return tabletImageUrl;
//    }
//
//    public void setTabletImageUrl(String tabletImageUrl) {
//        this.tabletImageUrl = tabletImageUrl;
//    }
//
//    public String getMobileImageUrl() {
//        return mobileImageUrl;
//    }
//
//    public void setMobileImageUrl(String mobileImageUrl) {
//        this.mobileImageUrl = mobileImageUrl;
//    }
//
//    public Integer getTitleSize() {
//        return titleSize;
//    }
//
//    public void setTitleSize(Integer titleSize) {
//        this.titleSize = titleSize;
//    }
//
//    public String getTitleWeight() {
//        return titleWeight;
//    }
//
//    public void setTitleWeight(String titleWeight) {
//        this.titleWeight = titleWeight;
//    }
//
//    public String getTitleColor() {
//        return titleColor;
//    }
//
//    public void setTitleColor(String titleColor) {
//        this.titleColor = titleColor;
//    }
//
//    public Integer getCanvasPadding() {
//        return canvasPadding;
//    }
//
//    public void setCanvasPadding(Integer canvasPadding) {
//        this.canvasPadding = canvasPadding;
//    }
//
//    public String getAlign() {
//        return align;
//    }
//
//    public void setAlign(String align) {
//        this.align = align;
//    }
//
//    public Integer getCanvasRadius() {
//        return canvasRadius;
//    }
//
//    public void setCanvasRadius(Integer canvasRadius) {
//        this.canvasRadius = canvasRadius;
//    }
//
//    public Integer getCtaRadius() {
//        return ctaRadius;
//    }
//
//    public void setCtaRadius(Integer ctaRadius) {
//        this.ctaRadius = ctaRadius;
//    }
//
//    public String getAnimation() {
//        return animation;
//    }
//
//    public void setAnimation(String animation) {
//        this.animation = animation;
//    }
//
//    public boolean isAiGenerated() {
//        return aiGenerated;
//    }
//
//    public void setAiGenerated(boolean aiGenerated) {
//        this.aiGenerated = aiGenerated;
//    }
//
//    public String getAiPrompt() {
//        return aiPrompt;
//    }
//
//    public void setAiPrompt(String aiPrompt) {
//        this.aiPrompt = aiPrompt;
//    }
//
//    public String getAiAudience() {
//        return aiAudience;
//    }
//
//    public void setAiAudience(String aiAudience) {
//        this.aiAudience = aiAudience;
//    }
//
//    public String getAiTheme() {
//        return aiTheme;
//    }
//
//    public void setAiTheme(String aiTheme) {
//        this.aiTheme = aiTheme;
//    }
//
//    public String getAiBannerType() {
//        return aiBannerType;
//    }
//
//    public void setAiBannerType(String aiBannerType) {
//        this.aiBannerType = aiBannerType;
//    }
//
//    public String getAiStyle() {
//        return aiStyle;
//    }
//
//    public void setAiStyle(String aiStyle) {
//        this.aiStyle = aiStyle;
//    }
//
//    public long getViews() {
//        return views;
//    }
//
//    public void setViews(long views) {
//        this.views = views;
//    }
//
//    public long getClicks() {
//        return clicks;
//    }
//
//    public void setClicks(long clicks) {
//        this.clicks = clicks;
//    }
//
//    public LocalDateTime getCreatedAt() {
//        return createdAt;
//    }
//
//    public void setCreatedAt(LocalDateTime createdAt) {
//        this.createdAt = createdAt;
//    }
//
//    public LocalDateTime getUpdatedAt() {
//        return updatedAt;
//    }
//
//    public void setUpdatedAt(LocalDateTime updatedAt) {
//        this.updatedAt = updatedAt;
//    }
//}
package com.lms.course.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity backing the Banner Studio feature (CMS Management > Banner Studio).
 * A single row represents one promotional banner shown across ILM ORA
 * (landing pages, dashboards, etc.) along with its design/builder settings,
 * per-device creative assets, scheduling info and lifetime analytics.
 *
 * NOTE: desktop/tablet/mobile image fields store full base64 data URLs
 * (e.g. "data:image/png;base64,iVBORw0KG...") which can be very long —
 * these MUST use TEXT columns, never varchar(n), or the image gets
 * silently truncated and renders as broken/half images.
 */
@Entity
@Table(name = "banner_studio")
public class BannerStudio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===================== Core content =====================

    @Column(nullable = false, length = 255, columnDefinition = "varchar(255)")
    private String name;

    @Column(length = 10, columnDefinition = "varchar(10)")
    private String emoji;

    /** CSS gradient / solid color used as the card + canvas background. */
    @Column(length = 500, columnDefinition = "varchar(500)")
    private String gradient;

    /** Small label shown above the title, e.g. "AI & Machine Learning". */
    @Column(length = 150, columnDefinition = "varchar(150)")
    private String eyebrow;

    @Column(length = 255, columnDefinition = "varchar(255)")
    private String title;

    @Column(length = 1000, columnDefinition = "varchar(1000)")
    private String subtitle;

    @Column(name = "cta_text", length = 100, columnDefinition = "varchar(100)")
    private String ctaText;

    @Column(name = "cta_link", length = 1000, columnDefinition = "varchar(1000)")
    private String ctaLink;

    /** draft | scheduled | active | expired */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20)")
    private BannerStatus status = BannerStatus.DRAFT;

    @Column(nullable = false)
    private boolean active = false;

    // ===================== Scheduling =====================

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "start_time", length = 10, columnDefinition = "varchar(10)")
    private String startTime;

    @Column(name = "end_date")
    private LocalDate endDate;

    // ===================== Per-device creative assets =====================
    // TEXT (unlimited length) — base64 data URLs can be 50k+ characters.
    // Using varchar(n) here is what causes "half images" — the value gets
    // truncated before it's stored, so only part of the image data is saved.

    
//    @Column(name = "desktop_image_url", columnDefinition = "TEXT")
//    private String desktopImageUrl;
//
//    
//    @Column(name = "tablet_image_url", columnDefinition = "TEXT")
//    private String tabletImageUrl;
//
//    
//    @Column(name = "mobile_image_url", columnDefinition = "TEXT")
//    private String mobileImageUrl;
    @Column(name = "desktop_image_url", columnDefinition = "TEXT")
    private String desktopImageUrl;

    @Column(name = "tablet_image_url", columnDefinition = "TEXT")
    private String tabletImageUrl;

    @Column(name = "mobile_image_url", columnDefinition = "TEXT")
    private String mobileImageUrl;

    // ===================== Builder / design settings =====================

    @Column(name = "title_size")
    private Integer titleSize = 30;

    @Column(name = "title_weight", length = 10, columnDefinition = "varchar(10)")
    private String titleWeight = "700";

    @Column(name = "title_color", length = 20, columnDefinition = "varchar(20)")
    private String titleColor = "#ffffff";

    @Column(name = "canvas_padding")
    private Integer canvasPadding = 40;

    /** left | center | right */
    @Column(length = 10, columnDefinition = "varchar(10)")
    private String align = "left";

    @Column(name = "canvas_radius")
    private Integer canvasRadius = 18;

    @Column(name = "cta_radius")
    private Integer ctaRadius = 30;

    /** none | fade | slide | zoom */
    @Column(length = 20, columnDefinition = "varchar(20)")
    private String animation = "none";

    // ===================== AI generation metadata =====================

    @Column(name = "ai_generated", nullable = false)
    private boolean aiGenerated = false;

    @Column(name = "ai_prompt", length = 2000, columnDefinition = "varchar(2000)")
    private String aiPrompt;

    @Column(name = "ai_audience", length = 150, columnDefinition = "varchar(150)")
    private String aiAudience;

    @Column(name = "ai_theme", length = 150, columnDefinition = "varchar(150)")
    private String aiTheme;

    @Column(name = "ai_banner_type", length = 150, columnDefinition = "varchar(150)")
    private String aiBannerType;

    @Column(name = "ai_style", length = 150, columnDefinition = "varchar(150)")
    private String aiStyle;

    // ===================== Analytics =====================

    @Column(nullable = false)
    private long views = 0L;

    @Column(nullable = false)
    private long clicks = 0L;

    // ===================== Audit =====================

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum BannerStatus {
        DRAFT, SCHEDULED, ACTIVE, EXPIRED
    }

    public BannerStudio() {
    }

    // ===================== Getters & Setters =====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public String getGradient() {
        return gradient;
    }

    public void setGradient(String gradient) {
        this.gradient = gradient;
    }

    public String getEyebrow() {
        return eyebrow;
    }

    public void setEyebrow(String eyebrow) {
        this.eyebrow = eyebrow;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getCtaText() {
        return ctaText;
    }

    public void setCtaText(String ctaText) {
        this.ctaText = ctaText;
    }

    public String getCtaLink() {
        return ctaLink;
    }

    public void setCtaLink(String ctaLink) {
        this.ctaLink = ctaLink;
    }

    public BannerStatus getStatus() {
        return status;
    }

    public void setStatus(BannerStatus status) {
        this.status = status;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getDesktopImageUrl() {
        return desktopImageUrl;
    }

    public void setDesktopImageUrl(String desktopImageUrl) {
        this.desktopImageUrl = desktopImageUrl;
    }

    public String getTabletImageUrl() {
        return tabletImageUrl;
    }

    public void setTabletImageUrl(String tabletImageUrl) {
        this.tabletImageUrl = tabletImageUrl;
    }

    public String getMobileImageUrl() {
        return mobileImageUrl;
    }

    public void setMobileImageUrl(String mobileImageUrl) {
        this.mobileImageUrl = mobileImageUrl;
    }

    public Integer getTitleSize() {
        return titleSize;
    }

    public void setTitleSize(Integer titleSize) {
        this.titleSize = titleSize;
    }

    public String getTitleWeight() {
        return titleWeight;
    }

    public void setTitleWeight(String titleWeight) {
        this.titleWeight = titleWeight;
    }

    public String getTitleColor() {
        return titleColor;
    }

    public void setTitleColor(String titleColor) {
        this.titleColor = titleColor;
    }

    public Integer getCanvasPadding() {
        return canvasPadding;
    }

    public void setCanvasPadding(Integer canvasPadding) {
        this.canvasPadding = canvasPadding;
    }

    public String getAlign() {
        return align;
    }

    public void setAlign(String align) {
        this.align = align;
    }

    public Integer getCanvasRadius() {
        return canvasRadius;
    }

    public void setCanvasRadius(Integer canvasRadius) {
        this.canvasRadius = canvasRadius;
    }

    public Integer getCtaRadius() {
        return ctaRadius;
    }

    public void setCtaRadius(Integer ctaRadius) {
        this.ctaRadius = ctaRadius;
    }

    public String getAnimation() {
        return animation;
    }

    public void setAnimation(String animation) {
        this.animation = animation;
    }

    public boolean isAiGenerated() {
        return aiGenerated;
    }

    public void setAiGenerated(boolean aiGenerated) {
        this.aiGenerated = aiGenerated;
    }

    public String getAiPrompt() {
        return aiPrompt;
    }

    public void setAiPrompt(String aiPrompt) {
        this.aiPrompt = aiPrompt;
    }

    public String getAiAudience() {
        return aiAudience;
    }

    public void setAiAudience(String aiAudience) {
        this.aiAudience = aiAudience;
    }

    public String getAiTheme() {
        return aiTheme;
    }

    public void setAiTheme(String aiTheme) {
        this.aiTheme = aiTheme;
    }

    public String getAiBannerType() {
        return aiBannerType;
    }

    public void setAiBannerType(String aiBannerType) {
        this.aiBannerType = aiBannerType;
    }

    public String getAiStyle() {
        return aiStyle;
    }

    public void setAiStyle(String aiStyle) {
        this.aiStyle = aiStyle;
    }

    public long getViews() {
        return views;
    }

    public void setViews(long views) {
        this.views = views;
    }

    public long getClicks() {
        return clicks;
    }

    public void setClicks(long clicks) {
        this.clicks = clicks;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}