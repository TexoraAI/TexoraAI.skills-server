package com.lms.course.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO holder for {@code CmsPage}-related request/response shapes. Grouped
 * as static nested classes so the CMS module doesn't sprawl into one
 * top-level file per DTO.
 */
public class CmsPageDtos {

    private CmsPageDtos() {
    }

    /** Full admin-facing view of a page, including its section tree. */
    public static class Response {
        private Long id;
        private String pageKey;
        private String title;
        private String description;
        private boolean published;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<CmsSectionDtos.Response> sections;

        public Response() {
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getPageKey() {
            return pageKey;
        }

        public void setPageKey(String pageKey) {
            this.pageKey = pageKey;
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

        public boolean isPublished() {
            return published;
        }

        public void setPublished(boolean published) {
            this.published = published;
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

        public List<CmsSectionDtos.Response> getSections() {
            return sections;
        }

        public void setSections(List<CmsSectionDtos.Response> sections) {
            this.sections = sections;
        }
    }

    /** Request body for updating a page's title/description/published flag. */
    public static class SettingsRequest {
        @NotBlank
        private String title;
        private String description;
        private boolean published;

        public SettingsRequest() {
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

        public boolean isPublished() {
            return published;
        }

        public void setPublished(boolean published) {
            this.published = published;
        }
    }
}