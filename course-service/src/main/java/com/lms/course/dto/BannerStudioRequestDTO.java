package com.lms.course.dto;

/**
 * Payload for creating or updating a banner (upload flow / manual builder save).
 * Mirrors the fields collected by BannerSidePanel + BuilderSection on the frontend.
 */
public class BannerStudioRequestDTO {

    private String name;
    private String emoji;
    private String gradient;
    private String eyebrow;
    private String title;
    private String subtitle;
    private String ctaText;
    private String ctaLink;

    /** draft | scheduled | active | expired (case-insensitive) */
    private String status;
    private boolean active;

    private String startDate;   // yyyy-MM-dd
    private String startTime;   // HH:mm
    private String endDate;     // yyyy-MM-dd

    private String desktopImageUrl;
    private String tabletImageUrl;
    private String mobileImageUrl;

    private Integer titleSize;
    private String titleWeight;
    private String titleColor;
    private Integer canvasPadding;
    private String align;
    private Integer canvasRadius;
    private Integer ctaRadius;
    private String animation;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
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
}