package com.lms.course.dto;

import jakarta.validation.constraints.NotBlank;

public class AIGenerateRequestDTO {

    @NotBlank(message = "Topic is required")
    private String topic;

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Level is required")
    private String level;

    public AIGenerateRequestDTO() {
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }
}