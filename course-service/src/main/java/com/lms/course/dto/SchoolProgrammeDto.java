package com.lms.course.dto;

import java.time.LocalDateTime;

public class SchoolProgrammeDto {

    // ─────────────────────────────────────────────────────────────────────────
    // BOARD DTO
    // ─────────────────────────────────────────────────────────────────────────

    public static class BoardDto {

        private Long id;
        private String boardKey;
        private String name;
        private String fullName;
        private String color;
        private String accent;
        private String tagline;
        private String abbr;
        private String logoUrl;
        private Integer displayOrder;
        private Boolean active;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public BoardDto() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getBoardKey() { return boardKey; }
        public void setBoardKey(String boardKey) { this.boardKey = boardKey; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }

        public String getAccent() { return accent; }
        public void setAccent(String accent) { this.accent = accent; }

        public String getTagline() { return tagline; }
        public void setTagline(String tagline) { this.tagline = tagline; }

        public String getAbbr() { return abbr; }
        public void setAbbr(String abbr) { this.abbr = abbr; }

        public String getLogoUrl() { return logoUrl; }
        public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

        public Integer getDisplayOrder() { return displayOrder; }
        public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CLASS DTO
    // ─────────────────────────────────────────────────────────────────────────

    public static class ClassDto {

        private Long id;
        private Long boardId;
        private String boardName;
        private Integer classNumber;
        private String label;
        private String tagline;
        private String description;
        private String highlightsJson;
        private String streamsJson;
        private Integer displayOrder;
        private Boolean active;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public ClassDto() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Long getBoardId() { return boardId; }
        public void setBoardId(Long boardId) { this.boardId = boardId; }

        public String getBoardName() { return boardName; }
        public void setBoardName(String boardName) { this.boardName = boardName; }

        public Integer getClassNumber() { return classNumber; }
        public void setClassNumber(Integer classNumber) { this.classNumber = classNumber; }

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }

        public String getTagline() { return tagline; }
        public void setTagline(String tagline) { this.tagline = tagline; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getHighlightsJson() { return highlightsJson; }
        public void setHighlightsJson(String highlightsJson) { this.highlightsJson = highlightsJson; }

        public String getStreamsJson() { return streamsJson; }
        public void setStreamsJson(String streamsJson) { this.streamsJson = streamsJson; }

        public Integer getDisplayOrder() { return displayOrder; }
        public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SUBJECT DTO
    // ─────────────────────────────────────────────────────────────────────────

    public static class SubjectDto {

        private Long id;
        private Long schoolClassId;
        private String schoolClassLabel;
        private String boardName;
        private String name;
        private String stream;
        private String iconKey;
        private String chaptersJson;
        private String syllabusJson;
        private Integer displayOrder;
        private Boolean active;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public SubjectDto() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Long getSchoolClassId() { return schoolClassId; }
        public void setSchoolClassId(Long schoolClassId) { this.schoolClassId = schoolClassId; }

        public String getSchoolClassLabel() { return schoolClassLabel; }
        public void setSchoolClassLabel(String schoolClassLabel) { this.schoolClassLabel = schoolClassLabel; }

        public String getBoardName() { return boardName; }
        public void setBoardName(String boardName) { this.boardName = boardName; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getStream() { return stream; }
        public void setStream(String stream) { this.stream = stream; }

        public String getIconKey() { return iconKey; }
        public void setIconKey(String iconKey) { this.iconKey = iconKey; }

        public String getChaptersJson() { return chaptersJson; }
        public void setChaptersJson(String chaptersJson) { this.chaptersJson = chaptersJson; }

        public String getSyllabusJson() { return syllabusJson; }
        public void setSyllabusJson(String syllabusJson) { this.syllabusJson = syllabusJson; }

        public Integer getDisplayOrder() { return displayOrder; }
        public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }
}