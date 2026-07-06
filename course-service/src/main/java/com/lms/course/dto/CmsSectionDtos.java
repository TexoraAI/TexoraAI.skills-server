package com.lms.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.Map;

/**
 * DTO holder for {@code CmsSection}-related request/response shapes.
 */
public class CmsSectionDtos {

    private CmsSectionDtos() {
    }

    public static class Response {
        private Long id;
        private String type;
        private String label;
        private int orderIndex;
        private boolean visible;
        private boolean published;
        private Map<String, Object> data;
        private List<CmsComponentDtos.Response> components;

        public Response() {
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public int getOrderIndex() {
            return orderIndex;
        }

        public void setOrderIndex(int orderIndex) {
            this.orderIndex = orderIndex;
        }

        public boolean isVisible() {
            return visible;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
        }

        public boolean isPublished() {
            return published;
        }

        public void setPublished(boolean published) {
            this.published = published;
        }

        public Map<String, Object> getData() {
            return data;
        }

        public void setData(Map<String, Object> data) {
            this.data = data;
        }

        public List<CmsComponentDtos.Response> getComponents() {
            return components;
        }

        public void setComponents(List<CmsComponentDtos.Response> components) {
            this.components = components;
        }
    }

    public static class CreateRequest {
        @NotBlank
        private String type;
        private String label;
        private Map<String, Object> data;

        public CreateRequest() {
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public Map<String, Object> getData() {
            return data;
        }

        public void setData(Map<String, Object> data) {
            this.data = data;
        }
    }

    public static class UpdateRequest {
        private String label;
        private Map<String, Object> data;

        public UpdateRequest() {
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public Map<String, Object> getData() {
            return data;
        }

        public void setData(Map<String, Object> data) {
            this.data = data;
        }
    }

    public static class ReorderRequest {
        @NotEmpty
        private List<Long> orderedSectionIds;

        public ReorderRequest() {
        }

        public List<Long> getOrderedSectionIds() {
            return orderedSectionIds;
        }

        public void setOrderedSectionIds(List<Long> orderedSectionIds) {
            this.orderedSectionIds = orderedSectionIds;
        }
    }
}