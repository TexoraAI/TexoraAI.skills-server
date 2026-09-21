//package com.lms.course.service;
//
//import com.lms.course.dto.MentorFeedbackRequest;
//import com.lms.course.dto.MentorFeedbackResponse;
//import com.lms.course.dto.MentorFeedbackStatsResponse;
//import com.lms.course.dto.PageResponse;
//import com.lms.course.exception.ResourceNotFoundException;
//import com.lms.course.model.MentorFeedback;
//import com.lms.course.repository.MentorFeedbackRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@Transactional
//public class MentorFeedbackService {
//
//    private final MentorFeedbackRepository repository;
//
//    @Autowired
//    public MentorFeedbackService(MentorFeedbackRepository repository) {
//        this.repository = repository;
//    }
//
//    public MentorFeedbackResponse create(MentorFeedbackRequest request) {
//        MentorFeedback entity = new MentorFeedback();
//        entity.setCandidateName(request.getCandidateName());
//        entity.setDesignation(request.getDesignation());
//        entity.setCompany(request.getCompany());
//        entity.setRating(request.getRating());
//        entity.setFeedbackMessage(request.getFeedbackMessage());
//        entity.setProfileImage(request.getProfileImage());
//        entity.setStatus(parseStatus(request.getStatus()));
//        entity.setIsFeatured(Boolean.TRUE.equals(request.getIsFeatured()));
//
//        MentorFeedback saved = repository.save(entity);
//        return toResponse(saved);
//    }
//
//    public MentorFeedbackResponse update(Long id, MentorFeedbackRequest request) {
//        MentorFeedback entity = findEntity(id);
//
//        entity.setCandidateName(request.getCandidateName());
//        entity.setDesignation(request.getDesignation());
//        entity.setCompany(request.getCompany());
//        entity.setRating(request.getRating());
//        entity.setFeedbackMessage(request.getFeedbackMessage());
//        if (request.getProfileImage() != null) {
//            entity.setProfileImage(request.getProfileImage());
//        }
//        entity.setStatus(parseStatus(request.getStatus()));
//        entity.setIsFeatured(Boolean.TRUE.equals(request.getIsFeatured()));
//
//        MentorFeedback saved = repository.save(entity);
//        return toResponse(saved);
//    }
//
//    public void delete(Long id) {
//        MentorFeedback entity = findEntity(id);
//        repository.delete(entity);
//    }
//
//    @Transactional(readOnly = true)
//    public MentorFeedbackResponse getById(Long id) {
//        return toResponse(findEntity(id));
//    }
//
//    @Transactional(readOnly = true)
//    public PageResponse<MentorFeedbackResponse> getAll(
//            String search, String status, Integer rating, int page, int size
//    ) {
//        MentorFeedback.FeedbackStatus statusEnum = null;
//        if (status != null && !status.equalsIgnoreCase("all") && !status.isBlank()) {
//            statusEnum = parseStatus(status);
//        }
//
//        Pageable pageable = PageRequest.of(
//                Math.max(page - 1, 0), size, Sort.by(Sort.Direction.DESC, "createdAt")
//        );
//
//        Page<MentorFeedback> result = repository.search(
//                (search == null || search.isBlank()) ? null : search,
//                statusEnum,
//                rating,
//                pageable
//        );
//
//        List<MentorFeedbackResponse> content = result.getContent()
//                .stream()
//                .map(this::toResponse)
//                .collect(Collectors.toList());
//
//        PageResponse<MentorFeedbackResponse> response = new PageResponse<>();
//        response.setContent(content);
//        response.setTotalElements((int) result.getTotalElements());
//        response.setTotalPages(result.getTotalPages());
//        response.setNumber(page);
//
//        return response;
//    }
//    public MentorFeedbackResponse toggleStatus(Long id) {
//        MentorFeedback entity = findEntity(id);
//        entity.setStatus(entity.getStatus() == MentorFeedback.FeedbackStatus.ACTIVE
//                ? MentorFeedback.FeedbackStatus.INACTIVE
//                : MentorFeedback.FeedbackStatus.ACTIVE);
//        return toResponse(repository.save(entity));
//    }
//
//    public MentorFeedbackResponse toggleFeatured(Long id) {
//        MentorFeedback entity = findEntity(id);
//        entity.setIsFeatured(!Boolean.TRUE.equals(entity.getIsFeatured()));
//        return toResponse(repository.save(entity));
//    }
//
//    @Transactional(readOnly = true)
//    public MentorFeedbackStatsResponse getStats() {
//        long total = repository.count();
//        long active = repository.countByStatus(MentorFeedback.FeedbackStatus.ACTIVE);
//        long inactive = repository.countByStatus(MentorFeedback.FeedbackStatus.INACTIVE);
//        long featured = repository.countByIsFeaturedTrue();
//
//        double avgRating = repository.findAll().stream()
//                .mapToInt(MentorFeedback::getRating)
//                .average()
//                .orElse(0.0);
//
//        return new MentorFeedbackStatsResponse(
//                total, active, inactive, featured, Math.round(avgRating * 10.0) / 10.0
//        );
//    }
//
//    @Transactional(readOnly = true)
//    public List<MentorFeedbackResponse> getActiveForLandingPage() {
//        return repository.findActiveForLandingPage()
//                .stream()
//                .map(this::toResponse)
//                .collect(Collectors.toList());
//    }
//
//    // ── Helpers ──────────────────────────────────────────────
//
//    private MentorFeedback findEntity(Long id) {
//        return repository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Mentor feedback not found with id: " + id));
//    }
//
//    private MentorFeedback.FeedbackStatus parseStatus(String status) {
//        try {
//            return MentorFeedback.FeedbackStatus.valueOf(status.trim().toUpperCase());
//        } catch (Exception e) {
//            throw new IllegalArgumentException("Invalid status value: " + status);
//        }
//    }
//
//    private MentorFeedbackResponse toResponse(MentorFeedback entity) {
//        return new MentorFeedbackResponse(
//                entity.getId(),
//                entity.getCandidateName(),
//                entity.getDesignation(),
//                entity.getCompany(),
//                entity.getRating(),
//                entity.getFeedbackMessage(),
//                entity.getProfileImage(),
//                entity.getStatus().name().toLowerCase(),
//                entity.getIsFeatured(),
//                entity.getCreatedAt(),
//                entity.getUpdatedAt()
//        );
//    }
//}


package com.lms.course.service;

import com.lms.course.dto.MentorFeedbackRequest;
import com.lms.course.dto.MentorFeedbackResponse;
import com.lms.course.dto.MentorFeedbackStatsResponse;
import com.lms.course.dto.PageResponse;
import com.lms.course.exception.ResourceNotFoundException;
import com.lms.course.model.MentorFeedback;
import com.lms.course.repository.MentorFeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class MentorFeedbackService {

    private final MentorFeedbackRepository repository;
    private final S3Service s3Service;

    @Value("${image.mentor.s3-prefix}")
    private String mentorImagePrefix;

    @Value("${image.mentor.presign-expiry-minutes:15}")
    private long presignExpiryMinutes;

    @Autowired
    public MentorFeedbackService(MentorFeedbackRepository repository, S3Service s3Service) {
        this.repository = repository;
        this.s3Service = s3Service;
    }

    /**
     * Uploads a mentor profile image to S3 and returns the S3 key.
     * Frontend calls this first, gets a key back, then sends that key
     * as `profileImage` in the create/update payload.
     */
    public String uploadProfileImage(MultipartFile file) throws IOException {
        String safeName = (file.getOriginalFilename() == null ? "image" : file.getOriginalFilename())
                .replaceAll("[^a-zA-Z0-9._-]", "_");
        String key = mentorImagePrefix + System.currentTimeMillis() + "_" + UUID.randomUUID() + "_" + safeName;
        s3Service.uploadFile(key, file);
        return key;
    }

    public MentorFeedbackResponse create(MentorFeedbackRequest request) {
        MentorFeedback entity = new MentorFeedback();
        entity.setCandidateName(request.getCandidateName());
        entity.setDesignation(request.getDesignation());
        entity.setCompany(request.getCompany());
        entity.setRating(request.getRating());
        entity.setFeedbackMessage(request.getFeedbackMessage());
        entity.setProfileImage(request.getProfileImage()); // S3 key, not a URL
        entity.setStatus(parseStatus(request.getStatus()));
        entity.setIsFeatured(Boolean.TRUE.equals(request.getIsFeatured()));

        MentorFeedback saved = repository.save(entity);
        return toResponse(saved);
    }

    public MentorFeedbackResponse update(Long id, MentorFeedbackRequest request) {
        MentorFeedback entity = findEntity(id);
        String oldKey = entity.getProfileImage();

        entity.setCandidateName(request.getCandidateName());
        entity.setDesignation(request.getDesignation());
        entity.setCompany(request.getCompany());
        entity.setRating(request.getRating());
        entity.setFeedbackMessage(request.getFeedbackMessage());

        if (request.getProfileImage() != null && !request.getProfileImage().isBlank()) {
            entity.setProfileImage(request.getProfileImage());
            // if the key actually changed, clean up the old S3 object
            if (oldKey != null && !oldKey.equals(request.getProfileImage())) {
                s3Service.deleteFile(oldKey);
            }
        }

        entity.setStatus(parseStatus(request.getStatus()));
        entity.setIsFeatured(Boolean.TRUE.equals(request.getIsFeatured()));

        MentorFeedback saved = repository.save(entity);
        return toResponse(saved);
    }

    public void delete(Long id) {
        MentorFeedback entity = findEntity(id);
        if (entity.getProfileImage() != null && !entity.getProfileImage().isBlank()) {
            s3Service.deleteFile(entity.getProfileImage());
        }
        repository.delete(entity);
    }

    @Transactional(readOnly = true)
    public MentorFeedbackResponse getById(Long id) {
        return toResponse(findEntity(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<MentorFeedbackResponse> getAll(
            String search, String status, Integer rating, int page, int size
    ) {
        MentorFeedback.FeedbackStatus statusEnum = null;
        if (status != null && !status.equalsIgnoreCase("all") && !status.isBlank()) {
            statusEnum = parseStatus(status);
        }

        Pageable pageable = PageRequest.of(
                Math.max(page - 1, 0), size, Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<MentorFeedback> result = repository.search(
                (search == null || search.isBlank()) ? null : search,
                statusEnum,
                rating,
                pageable
        );

        List<MentorFeedbackResponse> content = result.getContent()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        PageResponse<MentorFeedbackResponse> response = new PageResponse<>();
        response.setContent(content);
        response.setTotalElements((int) result.getTotalElements());
        response.setTotalPages(result.getTotalPages());
        response.setNumber(page);

        return response;
    }

    public MentorFeedbackResponse toggleStatus(Long id) {
        MentorFeedback entity = findEntity(id);
        entity.setStatus(entity.getStatus() == MentorFeedback.FeedbackStatus.ACTIVE
                ? MentorFeedback.FeedbackStatus.INACTIVE
                : MentorFeedback.FeedbackStatus.ACTIVE);
        return toResponse(repository.save(entity));
    }

    public MentorFeedbackResponse toggleFeatured(Long id) {
        MentorFeedback entity = findEntity(id);
        entity.setIsFeatured(!Boolean.TRUE.equals(entity.getIsFeatured()));
        return toResponse(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public MentorFeedbackStatsResponse getStats() {
        long total = repository.count();
        long active = repository.countByStatus(MentorFeedback.FeedbackStatus.ACTIVE);
        long inactive = repository.countByStatus(MentorFeedback.FeedbackStatus.INACTIVE);
        long featured = repository.countByIsFeaturedTrue();

        double avgRating = repository.findAll().stream()
                .mapToInt(MentorFeedback::getRating)
                .average()
                .orElse(0.0);

        return new MentorFeedbackStatsResponse(
                total, active, inactive, featured, Math.round(avgRating * 10.0) / 10.0
        );
    }

    @Transactional(readOnly = true)
    public List<MentorFeedbackResponse> getActiveForLandingPage() {
        return repository.findActiveForLandingPage()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── Helpers ──────────────────────────────────────────────

    private MentorFeedback findEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mentor feedback not found with id: " + id));
    }

    private MentorFeedback.FeedbackStatus parseStatus(String status) {
        try {
            return MentorFeedback.FeedbackStatus.valueOf(status.trim().toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid status value: " + status);
        }
    }

    private MentorFeedbackResponse toResponse(MentorFeedback entity) {
        String imageUrl = null;
        if (entity.getProfileImage() != null && !entity.getProfileImage().isBlank()) {
            imageUrl = s3Service.generatePresignedUrl(
                    entity.getProfileImage(), Duration.ofMinutes(presignExpiryMinutes));
        }

        return new MentorFeedbackResponse(
                entity.getId(),
                entity.getCandidateName(),
                entity.getDesignation(),
                entity.getCompany(),
                entity.getRating(),
                entity.getFeedbackMessage(),
                imageUrl, // presigned URL, not the raw S3 key
                entity.getStatus().name().toLowerCase(),
                entity.getIsFeatured(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}