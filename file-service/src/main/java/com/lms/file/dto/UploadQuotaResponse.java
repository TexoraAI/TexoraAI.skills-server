package com.lms.file.dto;

/**
 * Read-only snapshot of the caller's current plan tier and upload quota
 * usage for file-service. Mirrors video-service's UploadQuotaResponse shape.
 */
public class UploadQuotaResponse {

    private String tier;
    private long storageUsedBytes;
    private long storageCapBytes;
    private long fileCount;
    private int maxFileCount;
    private long maxFileSizeBytes;

    public UploadQuotaResponse() {
    }

    public UploadQuotaResponse(String tier, long storageUsedBytes, long storageCapBytes,
                                long fileCount, int maxFileCount, long maxFileSizeBytes) {
        this.tier = tier;
        this.storageUsedBytes = storageUsedBytes;
        this.storageCapBytes = storageCapBytes;
        this.fileCount = fileCount;
        this.maxFileCount = maxFileCount;
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public long getStorageUsedBytes() {
        return storageUsedBytes;
    }

    public void setStorageUsedBytes(long storageUsedBytes) {
        this.storageUsedBytes = storageUsedBytes;
    }

    public long getStorageCapBytes() {
        return storageCapBytes;
    }

    public void setStorageCapBytes(long storageCapBytes) {
        this.storageCapBytes = storageCapBytes;
    }

    public long getFileCount() {
        return fileCount;
    }

    public void setFileCount(long fileCount) {
        this.fileCount = fileCount;
    }

    public int getMaxFileCount() {
        return maxFileCount;
    }

    public void setMaxFileCount(int maxFileCount) {
        this.maxFileCount = maxFileCount;
    }

    public long getMaxFileSizeBytes() {
        return maxFileSizeBytes;
    }

    public void setMaxFileSizeBytes(long maxFileSizeBytes) {
        this.maxFileSizeBytes = maxFileSizeBytes;
    }
}
