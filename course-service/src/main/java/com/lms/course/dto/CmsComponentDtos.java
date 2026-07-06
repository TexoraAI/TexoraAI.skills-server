package com.lms.course.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

/**
 * DTO holder for {@code CmsComponent}-related request/response shapes.
 */
public class CmsComponentDtos {

    private CmsComponentDtos() {
    }

    public static class Response {
        private Long id;
        private Long sectionId;
        private String type;
        private int orderIndex;
        private boolean visible;
        private Map<String, Object> data;

        public Response() {
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getSectionId() {
            return sectionId;
        }

        public void setSectionId(Long sectionId) {
            this.sectionId = sectionId;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
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

        public Map<String, Object> getData() {
            return data;
        }

        public void setData(Map<String, Object> data) {
            this.data = data;
        }
    }

    public static class CreateRequest {
        @NotBlank
        private String type;
        private Map<String, Object> data;

        public CreateRequest() {
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Map<String, Object> getData() {
            return data;
        }

        public void setData(Map<String, Object> data) {
            this.data = data;
        }
    }

    public static class UpdateRequest {
        private Map<String, Object> data;

        public UpdateRequest() {
        }

        public Map<String, Object> getData() {
            return data;
        }

        public void setData(Map<String, Object> data) {
            this.data = data;
        }
    }
}