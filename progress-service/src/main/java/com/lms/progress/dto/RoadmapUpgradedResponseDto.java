package com.lms.progress.dto;

import java.util.ArrayList;
import java.util.List;

public class RoadmapUpgradedResponseDto {

    private Long id;
    private String targetRole;
    private String domain;
    private String pathType;
    private String status;
    private Integer totalWeeks;
    private Integer totalModules;
    private Double completionPercent;
    private String ownerRole;
    private String organizationId;
    private List<RoadmapUpgradedModuleDto> modules = new ArrayList<>();

    public RoadmapUpgradedResponseDto() {
    }

    public RoadmapUpgradedResponseDto(Long id,
                                       String targetRole,
                                       String domain,
                                       String pathType,
                                       String status,
                                       Integer totalWeeks,
                                       Integer totalModules,
                                       Double completionPercent,
                                       String ownerRole,
                                       String organizationId,
                                       List<RoadmapUpgradedModuleDto> modules) {
        this.id = id;
        this.targetRole = targetRole;
        this.domain = domain;
        this.pathType = pathType;
        this.status = status;
        this.totalWeeks = totalWeeks;
        this.totalModules = totalModules;
        this.completionPercent = completionPercent;
        this.ownerRole = ownerRole;
        this.organizationId = organizationId;
        this.modules = modules != null ? modules : new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
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

    public List<RoadmapUpgradedModuleDto> getModules() {
        return modules;
    }

    public void setModules(List<RoadmapUpgradedModuleDto> modules) {
        this.modules = modules;
    }
}
