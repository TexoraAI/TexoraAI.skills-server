package com.lms.progress.dto;

import java.util.ArrayList;
import java.util.List;

public class RoadmapUpgradedModuleDto {

    private Long id;
    private Integer orderIndex;
    private String title;
    private Boolean locked;
    private Double progressPercent;
    private List<RoadmapUpgradedTopicDto> topics = new ArrayList<>();

    public RoadmapUpgradedModuleDto() {
    }

    public RoadmapUpgradedModuleDto(Long id,
                                    Integer orderIndex,
                                    String title,
                                    Boolean locked,
                                    Double progressPercent,
                                    List<RoadmapUpgradedTopicDto> topics) {
        this.id = id;
        this.orderIndex = orderIndex;
        this.title = title;
        this.locked = locked;
        this.progressPercent = progressPercent;
        this.topics = topics != null ? topics : new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean getLocked() {
        return locked;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    public Double getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(Double progressPercent) {
        this.progressPercent = progressPercent;
    }

    public List<RoadmapUpgradedTopicDto> getTopics() {
        return topics;
    }

    public void setTopics(List<RoadmapUpgradedTopicDto> topics) {
        this.topics = topics != null ? topics : new ArrayList<>();
    }
}