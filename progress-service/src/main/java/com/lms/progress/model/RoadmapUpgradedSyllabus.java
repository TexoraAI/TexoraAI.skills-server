package com.lms.progress.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * The roadmap itself - one row per generated/library roadmap instance owned by a user.
 */
@Entity
@Table(name = "roadmap_upgraded_syllabus")
public class RoadmapUpgradedSyllabus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long ownerId;

    private String ownerRole;

    private String organizationId;

    private String domain;

    private String pathType;

    private String targetRole;

    private String language;

    private String sourceType;

    private String status;

    private Integer totalWeeks;

    private Integer totalModules;

    private Double completionPercent;

    private LocalDateTime createdAt;

    private LocalDateTime lastRegeneratedAt;

    @OneToMany(mappedBy = "syllabus", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<RoadmapUpgradedModule> modules = new ArrayList<>();

    public RoadmapUpgradedSyllabus() {
    }

    public RoadmapUpgradedSyllabus(Long id,
                                    Long ownerId,
                                    String ownerRole,
                                    String organizationId,
                                    String domain,
                                    String pathType,
                                    String targetRole,
                                    String language,
                                    String sourceType,
                                    String status,
                                    Integer totalWeeks,
                                    Integer totalModules,
                                    Double completionPercent,
                                    LocalDateTime createdAt,
                                    LocalDateTime lastRegeneratedAt,
                                    List<RoadmapUpgradedModule> modules) {
        this.id = id;
        this.ownerId = ownerId;
        this.ownerRole = ownerRole;
        this.organizationId = organizationId;
        this.domain = domain;
        this.pathType = pathType;
        this.targetRole = targetRole;
        this.language = language;
        this.sourceType = sourceType;
        this.status = status;
        this.totalWeeks = totalWeeks;
        this.totalModules = totalModules;
        this.completionPercent = completionPercent;
        this.createdAt = createdAt;
        this.lastRegeneratedAt = lastRegeneratedAt;
        this.modules = modules != null ? modules : new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerRole() {
        return ownerRole;
    }

    public void setOwnerRole(String ownerRole) {
        this.ownerRole = ownerRole;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getPathType() {
        return pathType;
    }

    public void setPathType(String pathType) {
        this.pathType = pathType;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getTotalWeeks() {
        return totalWeeks;
    }

    public void setTotalWeeks(Integer totalWeeks) {
        this.totalWeeks = totalWeeks;
    }

    public Integer getTotalModules() {
        return totalModules;
    }

    public void setTotalModules(Integer totalModules) {
        this.totalModules = totalModules;
    }

    public Double getCompletionPercent() {
        return completionPercent;
    }

    public void setCompletionPercent(Double completionPercent) {
        this.completionPercent = completionPercent;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastRegeneratedAt() {
        return lastRegeneratedAt;
    }

    public void setLastRegeneratedAt(LocalDateTime lastRegeneratedAt) {
        this.lastRegeneratedAt = lastRegeneratedAt;
    }

    public List<RoadmapUpgradedModule> getModules() {
        return modules;
    }

    public void setModules(List<RoadmapUpgradedModule> modules) {
        this.modules = modules;
    }
}
