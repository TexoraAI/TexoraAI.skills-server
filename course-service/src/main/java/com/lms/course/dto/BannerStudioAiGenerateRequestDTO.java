package com.lms.course.dto;

/**
 * Payload sent from AIStudioSection.jsx's "Generate with AI" form.
 */
public class BannerStudioAiGenerateRequestDTO {

    private String prompt;
    private String audience;
    private String theme;
    private String bannerType;
    private String style;

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
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