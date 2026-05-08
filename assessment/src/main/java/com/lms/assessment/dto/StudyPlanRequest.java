package com.lms.assessment.dto;

import java.util.List;

public class StudyPlanRequest {

    private String title;
    private String description;
    private Long batchId;
    private String thumbnailColor;
    private String icon;
    private String dueDate;
    private List<SectionRequest> sections;

    public static class SectionRequest {
        private String title;
        private String description;
        private int orderIndex;
        private List<ItemRequest> items;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public int getOrderIndex() { return orderIndex; }
        public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
        public List<ItemRequest> getItems() { return items; }
        public void setItems(List<ItemRequest> items) { this.items = items; }
    }

    public static class ItemRequest {
        private Long problemId;
        private int orderIndex;

        public Long getProblemId() { return problemId; }
        public void setProblemId(Long problemId) { this.problemId = problemId; }
        public int getOrderIndex() { return orderIndex; }
        public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public String getThumbnailColor() { return thumbnailColor; }
    public void setThumbnailColor(String thumbnailColor) { this.thumbnailColor = thumbnailColor; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    public List<SectionRequest> getSections() { return sections; }
    public void setSections(List<SectionRequest> sections) { this.sections = sections; }
}