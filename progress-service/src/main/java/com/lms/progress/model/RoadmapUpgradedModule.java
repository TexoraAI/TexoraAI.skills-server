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
import jakarta.persistence.OrderBy;
/**
 * One row per module inside a syllabus.
 */
@Entity
@Table(name = "roadmap_upgraded_module")
public class RoadmapUpgradedModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "syllabus_id")
    private RoadmapUpgradedSyllabus syllabus;

    private Integer orderIndex;

    private String title;

    /**
     * Self-referential by id only (not a FK object) to keep things simple,
     * per spec.
     */
    private Long prerequisiteModuleId;

    private Boolean locked;

    private Double progressPercent;
//
//    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<RoadmapUpgradedResource> resources = new ArrayList<>();
    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<RoadmapUpgradedTopic> topics = new ArrayList<>();
    
    public RoadmapUpgradedModule() {
    }

    public RoadmapUpgradedModule(Long id,
                                  RoadmapUpgradedSyllabus syllabus,
                                  Integer orderIndex,
                                  String title,
                                  Long prerequisiteModuleId,
                                  Boolean locked,
                                  Double progressPercent,
                                  List<RoadmapUpgradedTopic> topics) {
        this.id = id;
        this.syllabus = syllabus;
        this.orderIndex = orderIndex;
        this.title = title;
        this.prerequisiteModuleId = prerequisiteModuleId;
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

    public RoadmapUpgradedSyllabus getSyllabus() {
        return syllabus;
    }

    public void setSyllabus(RoadmapUpgradedSyllabus syllabus) {
        this.syllabus = syllabus;
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

    public Long getPrerequisiteModuleId() {
        return prerequisiteModuleId;
    }

    public void setPrerequisiteModuleId(Long prerequisiteModuleId) {
        this.prerequisiteModuleId = prerequisiteModuleId;
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

    public List<RoadmapUpgradedTopic> getTopics() { return topics; }
    public void setTopics(List<RoadmapUpgradedTopic> topics) { this.topics = topics != null ? topics : new ArrayList<>(); }
}
