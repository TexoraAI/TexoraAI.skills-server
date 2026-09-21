//package com.lms.course.service;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
//import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
//import software.amazon.awssdk.core.sync.RequestBody;
//import software.amazon.awssdk.regions.Region;
//import software.amazon.awssdk.services.s3.S3Client;
//import software.amazon.awssdk.services.s3.model.*;
//import software.amazon.awssdk.services.s3.presigner.S3Presigner;
//import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
//import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
//
//import java.io.IOException;
//import java.time.Duration;
//
///**
// * Same shape as file-service / video-service's S3Service — kept identical on
// * purpose so all services behave the same way against the shared
// * ilmora-media-storage bucket.
// */
//@Service
//public class S3Service {
//
//    @Value("${aws.access-key}")
//    private String accessKey;
//
//    @Value("${aws.secret-key}")
//    private String secretKey;
//
//    @Value("${aws.region}")
//    private String region;
//
//    @Value("${aws.s3.bucket}")
//    private String bucketName;
//
//    private S3Client s3Client;
//
//    private S3Client getClient() {
//        if (s3Client == null) {
//            s3Client = S3Client.builder()
//                    .region(Region.of(region))
//                    .credentialsProvider(StaticCredentialsProvider.create(
//                            AwsBasicCredentials.create(accessKey, secretKey)))
//                    .build();
//        }
//        return s3Client;
//    }
//
////    public void uploadFile(String key, MultipartFile file) throws IOException {
////        PutObjectRequest request = PutObjectRequest.builder()
////                .bucket(bucketName)
////                .key(key)
////                .contentType(file.getContentType())
////                .build();
////
////        getClient().putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
////    }
//    public void uploadFile(String key, MultipartFile file) throws IOException {
//        PutObjectRequest request = PutObjectRequest.builder()
//                .bucket(bucketName)
//                .key(key)
//                .contentType(file.getContentType())
//                .build();
//        getClient().putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
//    }
//
//    /**
//     * Uploads raw bytes directly (no MultipartFile) — used for AI-generated
//     * images, which arrive as base64-decoded bytes from OpenAI rather than
//     * as an uploaded file.
//     */
//    public void uploadBytes(String key, byte[] bytes, String contentType) {
//        PutObjectRequest request = PutObjectRequest.builder()
//                .bucket(bucketName)
//                .key(key)
//                .contentType(contentType)
//                .build();
//        getClient().putObject(request, RequestBody.fromBytes(bytes));
//    }
//
//    public String generatePresignedUrl(String key, Duration expiration) {
//        try (S3Presigner presigner = S3Presigner.builder()
//                .region(Region.of(region))
//                .credentialsProvider(StaticCredentialsProvider.create(
//                        AwsBasicCredentials.create(accessKey, secretKey)))
//                .build()) {
//
//            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
//                    .bucket(bucketName)
//                    .key(key)
//                    .build();
//
//            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
//                    .signatureDuration(expiration)
//                    .getObjectRequest(getObjectRequest)
//                    .build();
//
//            PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
//            return presignedRequest.url().toString();
//        }
//    }
//
//    public boolean exists(String key) {
//        try {
//            getClient().headObject(HeadObjectRequest.builder().bucket(bucketName).key(key).build());
//            return true;
//        } catch (NoSuchKeyException e) {
//            return false;
//        }
//    }
//
//    public void deleteFile(String key) {
//        if (key == null || key.isBlank()) return;
//        DeleteObjectRequest request = DeleteObjectRequest.builder()
//                .bucket(bucketName)
//                .key(key)
//                .build();
//        getClient().deleteObject(request);
//    }
//}

package com.lms.course.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
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
 * Same shape as file-service / video-service's S3Service — kept identical on
 * purpose so all services behave the same way against the shared
 * ilmora-media-storage bucket.
 */
@Service
public class S3Service {

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

//    public void uploadFile(String key, MultipartFile file) throws IOException {
//        PutObjectRequest request = PutObjectRequest.builder()
//                .bucket(bucketName)
//                .key(key)
//                .contentType(file.getContentType())
//                .build();
//
//        getClient().putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
//    }
    public void uploadFile(String key, MultipartFile file) throws IOException {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .build();
        getClient().putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
    }

    /**
     * Uploads raw bytes directly (no MultipartFile) — used for AI-generated
     * images, which arrive as base64-decoded bytes from OpenAI rather than
     * as an uploaded file.
     */
    public void uploadBytes(String key, byte[] bytes, String contentType) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();
        getClient().putObject(request, RequestBody.fromBytes(bytes));
    }

    /**
     * Permanent, non-expiring URL for an object — used for public marketing
     * assets (program thumbnails/banners/instructor photos/etc.) that should
     * keep working indefinitely, unlike generatePresignedUrl() below which
     * expires. Requires the bucket/prefix to allow public GetObject (see
     * bucket policy note in FeaturedProgramController). Virtual-hosted-style
     * URL, works for every AWS region since 2019.
     */
    public String getPublicUrl(String key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
    }

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

    public void deleteFile(String key) {
        if (key == null || key.isBlank()) return;
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        getClient().deleteObject(request);
    }
}