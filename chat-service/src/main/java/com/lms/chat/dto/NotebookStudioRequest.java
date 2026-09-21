package com.lms.chat.dto;

import java.util.List;

/**
 * Request body for POST /notebooks/{id}/studio/{type}.
 *
 * NOTE: this file is a best-effort reconstruction, not the real source. Only
 * NotebookStudioOutputResponse (the *response* DTO) was provided for this
 * change; the fields below are inferred from how NotebookController already
 * reads this class. If your real NotebookStudioRequest differs in structure
 * (extra fields, Lombok, a record, etc.), just add the NEW fields below
 * (formatTitle, description, topic, cardCountLevel, questionCountLevel,
 * difficulty) + getters/setters to your actual file instead of swapping this
 * one in wholesale.
 */
public class NotebookStudioRequest {

    private String language;

    // Infographic-only fields
    private String style;
    private String aspectRatio;
    private String levelOfDetail;
    private String guidance;

    // Shared between Audio, Video, and (as of this change) Slide Deck — for
    // Slide Deck, "format" takes "detaileddeck"/"presenterslides" instead of
    // Video's "short"/"explainer", and "length" takes "short"/"default".
    // Dispatch differentiates by studio type, not by field name.
    private String format;

    // Shared between Audio and Slide Deck
    private String length;

    // Shared between Audio and Video
    private String focus;
    private List<Long> sourceIds;

    // Video-only visual style: "auto-select" | "custom" | "classic" | "whiteboard" | "kawaii"
    private String visualStyle;

    // NEW — Reports: the chosen format title. One of the 3 fixed built-ins
    // ("Briefing Doc" | "Study Guide" | "Blog Post"), "Create Your Own", or
    // any title returned by GET .../studio/report-format-suggestions.
    private String formatTitle;

    // NEW — free-text guidance. Reports: structure/focus guidance (primary
    // driver for "Create Your Own"/suggested formats, optional extra for the
    // 3 fixed formats). Slide Deck: style/structure guidance. Data Table:
    // what to build (e.g. "columns: title, author, key result").
    private String description;

    // NEW — optional scoping topic. Mind Map: scopes the map to this topic
    // (blank = whole source material). Flashcards/Quiz: optional topic scope.
    private String topic;

    // NEW — Flashcards card count: "fewer" (5-7) | "standard" (8-12, default) | "more" (13-18)
    private String cardCountLevel;

    // NEW — Quiz question count: "fewer" (3-4) | "standard" (5-7, default) | "more" (8-12)
    private String questionCountLevel;

    // NEW — shared by Flashcards and Quiz: "easy" | "medium" (default) | "hard".
    // (Required by NotebookStudioService.generateFlashcards/generateQuiz even
    // though not explicitly called out in the original field list — see the
    // task write-up for this deviation.)
    private String difficulty;

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getStyle() { return style; }
    public void setStyle(String style) { this.style = style; }

    public String getAspectRatio() { return aspectRatio; }
    public void setAspectRatio(String aspectRatio) { this.aspectRatio = aspectRatio; }

    public String getLevelOfDetail() { return levelOfDetail; }
    public void setLevelOfDetail(String levelOfDetail) { this.levelOfDetail = levelOfDetail; }

    public String getGuidance() { return guidance; }
    public void setGuidance(String guidance) { this.guidance = guidance; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }

    public String getLength() { return length; }
    public void setLength(String length) { this.length = length; }

    public String getFocus() { return focus; }
    public void setFocus(String focus) { this.focus = focus; }

    public List<Long> getSourceIds() { return sourceIds; }
    public void setSourceIds(List<Long> sourceIds) { this.sourceIds = sourceIds; }

    public String getVisualStyle() { return visualStyle; }
    public void setVisualStyle(String visualStyle) { this.visualStyle = visualStyle; }

    public String getFormatTitle() { return formatTitle; }
    public void setFormatTitle(String formatTitle) { this.formatTitle = formatTitle; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getCardCountLevel() { return cardCountLevel; }
    public void setCardCountLevel(String cardCountLevel) { this.cardCountLevel = cardCountLevel; }

    public String getQuestionCountLevel() { return questionCountLevel; }
    public void setQuestionCountLevel(String questionCountLevel) { this.questionCountLevel = questionCountLevel; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
}