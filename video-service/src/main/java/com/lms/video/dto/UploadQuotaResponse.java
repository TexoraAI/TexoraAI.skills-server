package com.lms.video.dto;

// WHY: lets the frontend preview quota before attempting an upload, so it can
// disable the upload button / show a warning instead of letting the upload
// fail server-side.
public class UploadQuotaResponse {

    private final String tier;
    private final long storageUsedBytes;
    private final long storageCapBytes;
    private final long videoCount;
    private final int maxVideoCount;
    private final long maxVideoSizeBytes;

    public UploadQuotaResponse(String tier,
                                long storageUsedBytes,
                                long storageCapBytes,
                                long videoCount,
                                int maxVideoCount,
                                long maxVideoSizeBytes) {
        this.tier = tier;
        this.storageUsedBytes = storageUsedBytes;
        this.storageCapBytes = storageCapBytes;
        this.videoCount = videoCount;
        this.maxVideoCount = maxVideoCount;
        this.maxVideoSizeBytes = maxVideoSizeBytes;
    }

    public String getTier()              { return tier; }
    public long getStorageUsedBytes()    { return storageUsedBytes; }
    public long getStorageCapBytes()     { return storageCapBytes; }
    public long getVideoCount()          { return videoCount; }
    public int getMaxVideoCount()        { return maxVideoCount; }
    public long getMaxVideoSizeBytes()   { return maxVideoSizeBytes; }
}