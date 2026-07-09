package com.lms.course.dto;

/**
 * Result of an AI banner-copy generation call, matching the "ai-result" /
 * "mock-banner" preview block in AIStudioSection.jsx (eyebrow, title, sub, cta).
 * This is a preview only — nothing is persisted until the user calls
 * POST /api/banners/ai-generate/save (or similar "Add to Banners" action).
 */
public class BannerStudioAiGenerateResponseDTO {

    private String eyebrow;
    private String title;
    private String sub;
    private String cta;
    private String gradient;
    private String emoji;

    // echoed back so the frontend can show "Generated for {audience} · {style} style"
    private String audience;
    private String theme;
    private String bannerType;
    private String style;

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

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public String getCta() {
        return cta;
    }

    public void setCta(String cta) {
        this.cta = cta;
    }

    public String getGradient() {
        return gradient;
    }

    public void setGradient(String gradient) {
        this.gradient = gradient;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getBannerType() {
        return bannerType;
    }

    public void setBannerType(String bannerType) {
        this.bannerType = bannerType;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }
}