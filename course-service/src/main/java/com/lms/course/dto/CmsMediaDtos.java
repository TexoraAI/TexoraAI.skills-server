package com.lms.course.dto;

import java.time.LocalDateTime;

/**
 * DTO holder for {@code CmsMediaAsset}-related response shapes. Note that
 * the raw file bytes are never included here — only {@code downloadUrl},
 * a computed pointer to the raw-bytes streaming endpoint — so list/search
 * responses stay small.
 */
public class CmsMediaDtos {

    private CmsMediaDtos() {
    }

    public static class Response {
        private Long id;
        private String fileName;
        private String originalFileName;
        private String contentType;
        private long sizeBytes;
        private LocalDateTime uploadedAt;
        private String downloadUrl;

        public Response() {
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getOriginalFileName() {
            return originalFileName;
        }

        public void setOriginalFileName(String originalFileName) {
            this.originalFileName = originalFileName;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public long getSizeBytes() {
            return sizeBytes;
        }

        public void setSizeBytes(long sizeBytes) {
            this.sizeBytes = sizeBytes;
        }

        public LocalDateTime getUploadedAt() {
            return uploadedAt;
        }

        public void setUploadedAt(LocalDateTime uploadedAt) {
            this.uploadedAt = uploadedAt;
        }

        public String getDownloadUrl() {
            return downloadUrl;
        }

        public void setDownloadUrl(String downloadUrl) {
            this.downloadUrl = downloadUrl;
        }
    }
}