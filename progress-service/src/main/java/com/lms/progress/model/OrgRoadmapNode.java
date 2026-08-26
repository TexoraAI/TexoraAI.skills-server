package com.lms.progress.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "org_roadmap_node")
public class OrgRoadmapNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "org_roadmap_id", nullable = false)
    private Long orgRoadmapId;

    @Column(name = "source_node_id")
    private Long sourceNodeId;

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
            name = "org_roadmap_node_parents",
            joinColumns = @JoinColumn(name = "node_id"),
            inverseJoinColumns = @JoinColumn(name = "parent_node_id")
    )
    private List<OrgRoadmapNode> parentNodes = new ArrayList<>();

    public OrgRoadmapNode() {
    }

    public OrgRoadmapNode(Long id, Long orgRoadmapId, Long sourceNodeId, String title, String description,
                           NodeType type, Double positionX, Double positionY, boolean isOptional,
                           Integer estimatedHours, Integer orderIndex, boolean hasQuiz, boolean hasProject,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.orgRoadmapId = orgRoadmapId;
        this.sourceNodeId = sourceNodeId;
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

    public Long getOrgRoadmapId() {
        return orgRoadmapId;
    }

    public void setOrgRoadmapId(Long orgRoadmapId) {
        this.orgRoadmapId = orgRoadmapId;
    }

    public Long getSourceNodeId() {
        return sourceNodeId;
    }

    public void setSourceNodeId(Long sourceNodeId) {
        this.sourceNodeId = sourceNodeId;
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
    public List<OrgRoadmapNode> getParentNodes() {
        return parentNodes;
    }

    public void setParentNodes(List<OrgRoadmapNode> parentNodes) {
        this.parentNodes = parentNodes;
    }

    @Transient
    public List<Long> getParentNodeIds() {
        List<Long> ids = new ArrayList<>();
        for (OrgRoadmapNode parent : parentNodes) {
            ids.add(parent.getId());
        }
        return ids;
    }

    public void setParentNodeIds(List<Long> parentNodeIds) {
        List<OrgRoadmapNode> refs = new ArrayList<>();
        if (parentNodeIds != null) {
            for (Long parentId : parentNodeIds) {
                OrgRoadmapNode ref = new OrgRoadmapNode();
                ref.setId(parentId);
                refs.add(ref);
            }
        }
        this.parentNodes = refs;
    }
}
