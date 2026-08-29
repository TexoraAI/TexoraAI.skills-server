package com.lms.progress.dto;

import java.util.ArrayList;
import java.util.List;

public class RoadmapUpgradedTopicDto {

    private Long id;
    private Integer orderIndex;
    private String title;
    private Double progressPercent;
    private List<RoadmapUpgradedResourceDto> resources = new ArrayList<>();

    public RoadmapUpgradedTopicDto() {
    }

    public RoadmapUpgradedTopicDto(Long id,
                                    Integer orderIndex,
                                    String title,
                                    Double progressPercent,
                                    List<RoadmapUpgradedResourceDto> resources) {
        this.id = id;
        this.orderIndex = orderIndex;
        this.title = title;
        this.progressPercent = progressPercent;
        this.resources = resources != null ? resources : new ArrayList<>();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Double getProgressPercent() { return progressPercent; }
    public void setProgressPercent(Double progressPercent) { this.progressPercent = progressPercent; }

    public List<RoadmapUpgradedResourceDto> getResources() { return resources; }
    public void setResources(List<RoadmapUpgradedResourceDto> resources) { this.resources = resources; }
}