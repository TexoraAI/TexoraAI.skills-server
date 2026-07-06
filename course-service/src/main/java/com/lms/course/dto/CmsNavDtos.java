package com.lms.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * DTO holder for {@code CmsNavItem}-related request/response shapes.
 */
public class CmsNavDtos {

    private CmsNavDtos() {
    }

    public static class Response {
        private Long id;
        private String pageKey;
        private String label;
        private String href;
        private String openIn;
        private int orderIndex;

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

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }

        public String getOpenIn() {
            return openIn;
        }

        public void setOpenIn(String openIn) {
            this.openIn = openIn;
        }

        public int getOrderIndex() {
            return orderIndex;
        }

        public void setOrderIndex(int orderIndex) {
            this.orderIndex = orderIndex;
        }
    }

    public static class ItemRequest {
        @NotBlank
        private String label;
        @NotBlank
        private String href;
        private String openIn;

        public ItemRequest() {
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }

        public String getOpenIn() {
            return openIn;
        }

        public void setOpenIn(String openIn) {
            this.openIn = openIn;
        }
    }

    public static class ReorderRequest {
        @NotEmpty
        private List<Long> orderedNavItemIds;

        public ReorderRequest() {
        }

        public List<Long> getOrderedNavItemIds() {
            return orderedNavItemIds;
        }

        public void setOrderedNavItemIds(List<Long> orderedNavItemIds) {
            this.orderedNavItemIds = orderedNavItemIds;
        }
    }
}