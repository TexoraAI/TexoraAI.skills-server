package com.lms.assessment.dto;

import java.time.LocalDateTime;
import java.util.List;

public class StudyPlanResponse {

    private Long id;
    private String title;
    private String description;
    private String trainerEmail;
    private Long batchId;
    private String thumbnailColor;
    private String icon;
    private boolean active;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<SectionResponse> sections;
    private int totalProblems;
    private int completedProblems;

    // ── nested ──────────────────────────────────────────────────────

    public static class SectionResponse {
        private Long id;
        private String title;
        private String description;
        private int orderIndex;
        private List<ItemResponse> items;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public int getOrderIndex() { return orderIndex; }
        public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
        public List<ItemResponse> getItems() { return items; }
        public void setItems(List<ItemResponse> items) { this.items = items; }
    }

    public static class ItemResponse {
        private Long id;
        private Long problemId;
        private String problemTitle;
        private String problemDifficulty;
        private Integer problemTotalMarks;
        private int orderIndex;
        private boolean completed;
        private Integer marksObtained;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getProblemId() { return problemId; }
        public void setProblemId(Long problemId) { this.problemId = problemId; }
        public String getProblemTitle() { return problemTitle; }
        public void setProblemTitle(String problemTitle) { this.problemTitle = problemTitle; }
        public String getProblemDifficulty() { return problemDifficulty; }
        public void setProblemDifficulty(String problemDifficulty) { this.problemDifficulty = problemDifficulty; }
        public Integer getProblemTotalMarks() { return problemTotalMarks; }
        public void setProblemTotalMarks(Integer v) { this.problemTotalMarks = v; }
        public int getOrderIndex() { return orderIndex; }
        public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }
        public Integer getMarksObtained() { return marksObtained; }
        public void setMarksObtained(Integer marksObtained) { this.marksObtained = marksObtained; }
    }

    // ── root getters/setters ────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getTrainerEmail() { return trainerEmail; }
    public void setTrainerEmail(String trainerEmail) { this.trainerEmail = trainerEmail; }
    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public String getThumbnailColor() { return thumbnailColor; }
    public void setThumbnailColor(String thumbnailColor) { this.thumbnailColor = thumbnailColor; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<SectionResponse> getSections() { return sections; }
    public void setSections(List<SectionResponse> sections) { this.sections = sections; }
    public int getTotalProblems() { return totalProblems; }
    public void setTotalProblems(int totalProblems) { this.totalProblems = totalProblems; }
    public int getCompletedProblems() { return completedProblems; }
    public void setCompletedProblems(int completedProblems) { this.completedProblems = completedProblems; }
}