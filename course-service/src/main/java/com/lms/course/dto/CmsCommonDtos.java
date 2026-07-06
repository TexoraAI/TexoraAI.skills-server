package com.lms.course.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Small shared request DTOs reused across sections and components
 * (toggling visibility / published state).
 */
public class CmsCommonDtos {

    private CmsCommonDtos() {
    }

    public static class VisibilityRequest {
        @NotNull
        private Boolean visible;

        public VisibilityRequest() {
        }

        public Boolean getVisible() {
            return visible;
        }

        public void setVisible(Boolean visible) {
            this.visible = visible;
        }
    }

    public static class PublishRequest {
        @NotNull
        private Boolean published;

        public PublishRequest() {
        }

        public Boolean getPublished() {
            return published;
        }

        public void setPublished(Boolean published) {
            this.published = published;
        }
    }
}