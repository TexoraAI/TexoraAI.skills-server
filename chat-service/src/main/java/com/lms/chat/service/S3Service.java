package com.lms.chat.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;

/**
 * Same shape as file-service's S3Service — kept identical on purpose so both
 * services behave the same way against the shared ilmora-media-storage bucket.
 *
 * Chat-service (notebook) uploads use the NOTEBOOK_KEY_PREFIX below for their
 * S3 keys.
 */
@Service
public class S3Service {

    public static final String NOTEBOOK_KEY_PREFIX = "files/notebook-service-files/";

    @Value("${aws.access-key}")
    private String accessKey;

    @Value("${aws.secret-key}")
    private String secretKey;

    @Value("${aws.region}")
    private String region;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    private S3Client s3Client;

    private S3Client getClient() {
        if (s3Client == null) {
            s3Client = S3Client.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKey, secretKey)))
                    .build();
        }
        return s3Client;
    }

    /**
     * Uploads a file to S3 under the given key
     * (e.g. "files/notebook-service-files/1735689600000_notes.pdf").
     */
    public void uploadFile(String key, MultipartFile file) throws IOException {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .build();

        getClient().putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
    }

    /**
     * Uploads raw bytes to S3 under the given key. Use this (instead of
     * uploadFile) for programmatically generated content — TTS audio, .pptx
     * decks, rendered video — where there's no incoming MultipartFile.
     */
    public void uploadBytes(String key, byte[] data, String contentType) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

        getClient().putObject(request, RequestBody.fromBytes(data));
    }

    /**
     * Downloads a file's bytes from S3. Same return type as the old
     * Files.readAllBytes(...) call it replaces.
     */
    public byte[] downloadFile(String key) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        try (ResponseInputStream<GetObjectResponse> response = getClient().getObject(request)) {
            return response.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file from S3: " + key, e);
        }
    }

    /**
     * Generates a temporary, secure URL for viewing/downloading a file
     * directly from S3 without proxying bytes through this service.
     */
    public String generatePresignedUrl(String key, Duration expiration) {
        try (S3Presigner presigner = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build()) {

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(expiration)
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
            return presignedRequest.url().toString();
        }
    }

    public boolean exists(String key) {
        try {
            getClient().headObject(HeadObjectRequest.builder().bucket(bucketName).key(key).build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    /**
     * Deletes a file from S3. Safe to call even if the key doesn't exist.
     */
    public void deleteFile(String key) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        getClient().deleteObject(request);
    }
}