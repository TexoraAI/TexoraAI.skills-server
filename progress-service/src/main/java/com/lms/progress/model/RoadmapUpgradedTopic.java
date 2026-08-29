package com.lms.progress.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * One row per topic inside a module. Sits between RoadmapUpgradedModule and
 * RoadmapUpgradedResource: a module has 3-5 topics, and each topic (not the
 * module) is what actually gets one video + one article + one pdf + one
 * quiz generated for it.
 */
@Entity
@Table(name = "roadmap_upgraded_topic")
public class RoadmapUpgradedTopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id")
    private RoadmapUpgradedModule module;

    private Integer orderIndex;

    private String title;

    private Double progressPercent;

    @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<RoadmapUpgradedResource> resources = new ArrayList<>();

    public RoadmapUpgradedTopic() {
    }

    public RoadmapUpgradedTopic(Long id,
                                 RoadmapUpgradedModule module,
                                 Integer orderIndex,
                                 String title,
                                 Double progressPercent,
                                 List<RoadmapUpgradedResource> resources) {
        this.id = id;
        this.module = module;
        this.orderIndex = orderIndex;
        this.title = title;
        this.progressPercent = progressPercent;
        this.resources = resources != null ? resources : new ArrayList<>();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public RoadmapUpgradedModule getModule() { return module; }
    public void setModule(RoadmapUpgradedModule module) { this.module = module; }

    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Double getProgressPercent() { return progressPercent; }
    public void setProgressPercent(Double progressPercent) { this.progressPercent = progressPercent; }

    public List<RoadmapUpgradedResource> getResources() { return resources; }
    public void setResources(List<RoadmapUpgradedResource> resources) { this.resources = resources; }
}