package com.lms.course.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * JPA entity representing a single uploaded media asset (image/document)
 * used across CMS pages. Scoped globally (not per page) since the media
 * library is shared across all hubs. The raw file bytes are stored directly
 * in the database in {@code fileData} — no disk or S3 integration yet.
 * Access to that column is intentionally isolated to
 * {@link #getFileData()} / {@link #setFileData(byte[])} so the storage
 * backend can be swapped out later (e.g. to S3/GCS) by touching only this
 * class and the single service method that reads/writes it.
 */
@Entity
@Table(name = "cms_media_asset")
public class CmsMediaAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "size_bytes")
    private long sizeBytes;

    @Lob
    @Column(name = "file_data")
    private byte[] fileData;

    @CreationTimestamp
    @Column(name = "uploaded_at", updatable = false)
    private LocalDateTime uploadedAt;

    public CmsMediaAsset() {
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

    /**
     * Single access point for the raw file bytes stored in the database.
     * Keep all reads of the binary payload routed through here so the
     * storage backend (DB BLOB today, S3/GCS later) can be swapped without
     * touching callers elsewhere in the service.
     */
    public byte[] getFileData() {
        return fileData;
    }

    /**
     * Single access point for writing the raw file bytes into the database.
     * See {@link #getFileData()}.
     */
    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}