package com.lms.progress.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "roadmap_template_node")
public class RoadmapTemplateNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "template_id", nullable = false)
    private Long templateId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NodeType type;

    @Column(name = "position_x")
    private Double positionX;

    @Column(name = "position_y")
    private Double positionY;

    @Column(name = "is_optional", nullable = false)
    private boolean isOptional;

    @Column(name = "estimated_hours")
    private Integer estimatedHours;

    @Column(name = "order_index")
    private Integer orderIndex;

    @Column(name = "has_quiz", nullable = false)
    private boolean hasQuiz;

    @Column(name = "has_project", nullable = false)
    private boolean hasProject;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "roadmap_template_node_parents",
            joinColumns = @JoinColumn(name = "node_id"),
            inverseJoinColumns = @JoinColumn(name = "parent_node_id")
    )
    private List<RoadmapTemplateNode> parentNodes = new ArrayList<>();

    public RoadmapTemplateNode() {
    }

    public RoadmapTemplateNode(Long id, Long templateId, String title, String description, NodeType type,
                                Double positionX, Double positionY, boolean isOptional, Integer estimatedHours,
                                Integer orderIndex, boolean hasQuiz, boolean hasProject,
                                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.templateId = templateId;
        this.title = title;
        this.description = description;
        this.type = type;
        this.positionX = positionX;
        this.positionY = positionY;
        this.isOptional = isOptional;
        this.estimatedHours = estimatedHours;
        this.orderIndex = orderIndex;
        this.hasQuiz = hasQuiz;
        this.hasProject = hasProject;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public NodeType getType() {
        return type;
    }

    public void setType(NodeType type) {
        this.type = type;
    }

    public Double getPositionX() {
        return positionX;
    }

    public void setPositionX(Double positionX) {
        this.positionX = positionX;
    }

    public Double getPositionY() {
        return positionY;
    }

    public void setPositionY(Double positionY) {
        this.positionY = positionY;
    }

    public boolean isOptional() {
        return isOptional;
    }

    public void setOptional(boolean optional) {
        isOptional = optional;
    }

    public Integer getEstimatedHours() {
        return estimatedHours;
    }

    public void setEstimatedHours(Integer estimatedHours) {
        this.estimatedHours = estimatedHours;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public boolean isHasQuiz() {
        return hasQuiz;
    }

    public void setHasQuiz(boolean hasQuiz) {
        this.hasQuiz = hasQuiz;
    }

    public boolean isHasProject() {
        return hasProject;
    }

    public void setHasProject(boolean hasProject) {
        this.hasProject = hasProject;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Underlying entity relationship used by JPA for the self-referencing
     * many-to-many join table. Callers should generally prefer the
     * plain List&lt;Long&gt; accessors {@link #getParentNodeIds()} /
     * {@link #setParentNodeIds(List)} below.
     */
    public List<RoadmapTemplateNode> getParentNodes() {
        return parentNodes;
    }

    public void setParentNodes(List<RoadmapTemplateNode> parentNodes) {
        this.parentNodes = parentNodes;
    }

    @Transient
    public List<Long> getParentNodeIds() {
        List<Long> ids = new ArrayList<>();
        for (RoadmapTemplateNode parent : parentNodes) {
            ids.add(parent.getId());
        }
        return ids;
    }

    public void setParentNodeIds(List<Long> parentNodeIds) {
        List<RoadmapTemplateNode> refs = new ArrayList<>();
        if (parentNodeIds != null) {
            for (Long parentId : parentNodeIds) {
                RoadmapTemplateNode ref = new RoadmapTemplateNode();
                ref.setId(parentId);
                refs.add(ref);
            }
        }
        this.parentNodes = refs;
    }
}
