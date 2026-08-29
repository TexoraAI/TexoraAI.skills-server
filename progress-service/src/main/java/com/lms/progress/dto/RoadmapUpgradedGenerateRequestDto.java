package com.lms.progress.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Input for both the wizard (Path B, live generation) and library
 * "Use this roadmap" (Path A) clicks.
 */
public class RoadmapUpgradedGenerateRequestDto {

    private String domain;
    private String pathType;
    private String targetRole;
    private String language;
    private List<String> contentSources = new ArrayList<>();
    private Boolean fromLibrary;

    public RoadmapUpgradedGenerateRequestDto() {
    }

    public RoadmapUpgradedGenerateRequestDto(String domain,
                                              String pathType,
                                              String targetRole,
                                              String language,
                                              List<String> contentSources,
                                              Boolean fromLibrary) {
        this.domain = domain;
        this.pathType = pathType;
        this.targetRole = targetRole;
        this.language = language;
        this.contentSources = contentSources != null ? contentSources : new ArrayList<>();
        this.fromLibrary = fromLibrary;
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

    public List<String> getContentSources() {
        return contentSources;
    }

    public void setContentSources(List<String> contentSources) {
        this.contentSources = contentSources;
    }

    public Boolean getFromLibrary() {
        return fromLibrary;
    }

    public void setFromLibrary(Boolean fromLibrary) {
        this.fromLibrary = fromLibrary;
    }
}
