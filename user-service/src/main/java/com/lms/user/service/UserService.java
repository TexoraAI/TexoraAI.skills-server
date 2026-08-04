
package com.lms.user.service;

import org.springframework.transaction.annotation.Transactional;
import com.lms.user.dto.CreateUserRequest;
import com.lms.user.dto.UpdateUserRequest;
import com.lms.user.dto.UserResponse;
import com.lms.user.event.UserEvent;
import com.lms.user.exception.ResourceNotFoundException;
import com.lms.user.kafka.UserSyncEventProducer;
import com.lms.user.model.User;
import com.lms.user.repo.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import com.lms.user.dto.StudentProfileRequest;
import com.lms.user.dto.StudentProfileResponse;
import com.lms.user.dto.TrainerProfileRequest;
import com.lms.user.dto.TrainerProfileResponse;
import com.lms.user.model.StudentProfile;
import com.lms.user.model.TrainerProfile;
import com.lms.user.repo.StudentProfileRepository;
import com.lms.user.repo.TrainerProfileRepository;

// WHY: Central service for all user identity operations — all LMS services call this for user data
@Service
public class UserService {

    private final UserRepository repo;
    private final UserSyncEventProducer producer;
    private final StudentProfileRepository studentProfileRepo;
    private final TrainerProfileRepository trainerProfileRepo;

    public UserService(UserRepository repo,
                       UserSyncEventProducer producer,
                       StudentProfileRepository studentProfileRepo,
                       TrainerProfileRepository trainerProfileRepo) {
        this.repo               = repo;
        this.producer           = producer;
        this.studentProfileRepo = studentProfileRepo;
        this.trainerProfileRepo = trainerProfileRepo;
    }

    // WHY: Admin creates user accounts for students and trainers being added to LMS
    // OPTIMIZATION: Evicts users:id and users:email caches since new user changes list queries
    @Caching(evict = {
        @CacheEvict(value = "users:id", allEntries = true),
        @CacheEvict(value = "users:email", allEntries = true)
    })
    public UserResponse createUser(CreateUserRequest req) {
        Optional<User> exists = repo.findByEmail(req.getEmail());
        if (exists.isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        User u = new User();
        u.setEmail(req.getEmail());
        u.setDisplayName(req.getDisplayName());
        u.setTenantId(req.getTenantId());
        u.setRoles(req.getRoles());

        if (req.getOrganizationId() != null && !req.getOrganizationId().isBlank()) {
            u.setOrganizationId(req.getOrganizationId());
        }

        User saved = repo.save(u);
        return mapToResponse(saved);
    }

    // WHY: Used by all LMS services via internal HTTP calls to resolve user by id
    // OPTIMIZATION: Separate cache name "users:id" avoids key collision with email cache
    @Cacheable(value = "users:id", key = "#id")
    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        return repo.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
    }

    // WHY: Role updates propagate to auth-service so JWT roles stay in sync
    // OPTIMIZATION: Evict only the specific user — not all entries
    @Caching(evict = {
        @CacheEvict(value = "users:id", key = "#id"),
        @CacheEvict(value = "users:email", allEntries = true)
    })
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest req) {
        User u = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));

        boolean roleChanged = false;

        if (req.getDisplayName() != null)       u.setDisplayName(req.getDisplayName());
        if (req.getRoles() != null && !req.getRoles().equals(u.getRoles())) {
            u.setRoles(req.getRoles());
            roleChanged = true;
        }
        if (req.getPhotoUrl() != null && !req.getPhotoUrl().isEmpty()) {
            u.setPhotoUrl(req.getPhotoUrl());
        }

        User saved = repo.save(u);

        // WHY: Notifies auth-service of display name change for JWT claim sync
        UserEvent updatedEvent = new UserEvent("USER_UPDATED", saved.getEmail(), saved.getDisplayName(), null);
        updatedEvent.setUserId(saved.getId());
        producer.send(updatedEvent);

        if (roleChanged) {
            // WHY: auth-service must update its user record so new JWT tokens carry correct role
            UserEvent roleEvent = new UserEvent("USER_ROLE_CHANGED", saved.getEmail(), null,
                    saved.getRoles().replace("ROLE_", ""));
            roleEvent.setUserId(saved.getId());
            producer.send(roleEvent);
        }

        return mapToResponse(saved);
    }

    // WHY: Full user deletion cascades to student/trainer profiles and notifies auth-service
    // OPTIMIZATION: Evict by specific id and email key instead of allEntries
    @Caching(evict = {
        @CacheEvict(value = "users:id", key = "#id"),
        @CacheEvict(value = "users:email", allEntries = true)
    })
    @Transactional
    public void deleteUser(Long id) {
        User user = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));

        // WHY: Profile cleanup before user delete prevents orphaned records
        studentProfileRepo.deleteByUserId(id);
        trainerProfileRepo.deleteByUserId(id);

        repo.delete(user);

        // WHY: auth-service must delete its own user record when user-service deletes
        UserEvent deleteEvent = new UserEvent("USER_DELETED", user.getEmail(), null, null);
        deleteEvent.setUserId(user.getId());
        producer.send(deleteEvent);
    }

    // WHY: Admin user management page — paginated to handle large user bases
    @Transactional(readOnly = true)
    public Page<UserResponse> listUsers(int page, int size, String sort, String dir) {
        Sort.Direction direction = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort s = Sort.by(direction, (sort == null || sort.isEmpty()) ? "id" : sort);
        Pageable p = PageRequest.of(page, size, s);
        return repo.findAll(p).map(this::mapToResponse);
    }

    // WHY: API Gateway forwards JWT email claim; this is the primary lookup path on every request
    // OPTIMIZATION: Separate cache name "users:email" avoids collision with id-keyed cache
    @Cacheable(value = "users:email", key = "#email")
    @Transactional(readOnly = true)
    public UserResponse getByEmail(String email) {
        User user = repo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email " + email));
        return mapToResponse(user);
    }

    // WHY: Students and trainers update their own display name/photo through this path
    // OPTIMIZATION: Evict specific email key + id entries (id unknown here so evict all id cache)
    @Caching(evict = {
        @CacheEvict(value = "users:email", key = "#email"),
        @CacheEvict(value = "users:id", allEntries = true)
    })
    @Transactional
    public UserResponse updateByEmail(String email, UpdateUserRequest req) {
        User u = repo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email " + email));

        boolean roleChanged = false;

        if (req.getDisplayName() != null) u.setDisplayName(req.getDisplayName());
        if (req.getRoles() != null && !req.getRoles().equals(u.getRoles())) {
            u.setRoles(req.getRoles());
            roleChanged = true;
        }
        if (req.getPhotoUrl() != null && !req.getPhotoUrl().isEmpty()) {
            u.setPhotoUrl(req.getPhotoUrl());
        }

        User saved = repo.save(u);

        UserEvent updatedEvent = new UserEvent("USER_UPDATED", saved.getEmail(), saved.getDisplayName(), null);
        updatedEvent.setUserId(saved.getId());
        producer.send(updatedEvent);

        if (roleChanged) {
            UserEvent roleEvent = new UserEvent("USER_ROLE_CHANGED", saved.getEmail(), null,
                    saved.getRoles().replace("ROLE_", ""));
            roleEvent.setUserId(saved.getId());
            producer.send(roleEvent);
        }

        return mapToResponse(saved);
    }

    // WHY: Internal API used by course-service and batch-service to list all trainers
    // OPTIMIZATION: Added Pageable to prevent full table load at scale
    @Transactional(readOnly = true)
    public Page<UserResponse> getUsersByRole(String role, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        // OPTIMIZATION: Using Pageable overload of findUsersByRole
        return repo.findUsersByRole(role, pageable).map(this::mapToResponse);
    }

    // WHY: SuperAdmin views all users in an organization for multi-tenant management
    // OPTIMIZATION: Added Pageable — org can have thousands of students
    @Transactional(readOnly = true)
    public Page<UserResponse> getUsersByOrg(String organizationId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return repo.findByOrganizationId(organizationId, pageable).map(this::mapToResponse);
    }

    // WHY: TenantAdmin filters students/trainers within their own organization
    @Transactional(readOnly = true)
    public Page<UserResponse> getUsersByOrgAndRole(String organizationId, String role, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return repo.findByOrganizationIdAndRolesContaining(organizationId, role, pageable).map(this::mapToResponse);
    }

    private UserResponse mapToResponse(User u) {
        UserResponse r = new UserResponse();
        r.setId(u.getId());
        r.setEmail(u.getEmail());
        r.setDisplayName(u.getDisplayName());
        r.setRoles(u.getRoles());
        r.setTenantId(u.getTenantId());
        r.setCreatedAt(u.getCreatedAt());
        r.setPhotoUrl(u.getPhotoUrl());
        r.setOrganizationId(u.getOrganizationId());
        return r;
    }

    // WHY: Student fills profile after LMS registration — used by admin for batch assignment
    @Cacheable(value = "student:profile", key = "#email")
    @Transactional(readOnly = true)
    public StudentProfileResponse getStudentProfile(String email) {
        return studentProfileRepo.findByUser_Email(email)
                .map(this::mapToStudentProfileResponse)
                .orElseGet(StudentProfileResponse::new);
    }

    // WHY: Student updates contact/education info used in batch assignment and analytics
    @CacheEvict(value = "student:profile", key = "#email")
    @Transactional
    public StudentProfileResponse updateStudentProfile(String email, StudentProfileRequest req) {
        User user = repo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email " + email));

        StudentProfile profile = studentProfileRepo.findByUser_Email(email)
                .orElseGet(() -> { StudentProfile p = new StudentProfile(); p.setUser(user); return p; });

        profile.setMobileNumber(req.getMobileNumber());
        profile.setDateOfBirth(req.getDateOfBirth());
        profile.setGender(req.getGender());
        profile.setCity(req.getCity());
        profile.setState(req.getState());
        profile.setCountry(req.getCountry());
        profile.setQualification(req.getQualification());
        profile.setCollegeName(req.getCollegeName());
        profile.setYearOfPassing(req.getYearOfPassing());
        profile.setDomain(req.getDomain());
        profile.setExperience(req.getExperience());

        return mapToStudentProfileResponse(studentProfileRepo.save(profile));
    }

    private StudentProfileResponse mapToStudentProfileResponse(StudentProfile p) {
        StudentProfileResponse r = new StudentProfileResponse();
        r.setMobileNumber(p.getMobileNumber());
        r.setDateOfBirth(p.getDateOfBirth());
        r.setGender(p.getGender());
        r.setCity(p.getCity());
        r.setState(p.getState());
        r.setCountry(p.getCountry());
        r.setQualification(p.getQualification());
        r.setCollegeName(p.getCollegeName());
        r.setYearOfPassing(p.getYearOfPassing());
        r.setDomain(p.getDomain());
        r.setExperience(p.getExperience());
        return r;
    }


    @Transactional(readOnly = true)
    public TrainerProfileResponse getTrainerProfile(String email) {
        System.out.println(">>> getTrainerProfile called for: " + email);
        try {
            var result = trainerProfileRepo.findByUser_Email(email);
            System.out.println(">>> findByUser_Email result present: " + result.isPresent());
            if (result.isPresent()) {
                TrainerProfile p = result.get();
                System.out.println(">>> TrainerProfile id: " + p.getId());
                System.out.println(">>> platforms: " + p.getPlatforms());
                TrainerProfileResponse resp = mapToTrainerProfileResponse(p);
                System.out.println(">>> mapped response: " + resp);
                return resp;
            } else {
                System.out.println(">>> No trainer profile found, returning empty");
                return new TrainerProfileResponse();
            }
        } catch (Exception e) {
            System.err.println(">>> EXCEPTION in getTrainerProfile: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // WHY: Trainer updates professional context used in batch matching and student-facing bio
    @CacheEvict(value = "trainer:profile", key = "#email")
    @Transactional
    public TrainerProfileResponse updateTrainerProfile(String email, TrainerProfileRequest req) {
        User user = repo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        TrainerProfile profile = trainerProfileRepo.findByUser_Email(email)
                .orElseGet(() -> { TrainerProfile p = new TrainerProfile(); p.setUser(user); return p; });

        profile.setLinkedinUrl(req.getLinkedinUrl());
        profile.setCountry(req.getCountry());
        profile.setAudienceSize(req.getAudienceSize());
        profile.setFullTimeRole(req.getFullTimeRole());
        profile.setCourseTopic(req.getCourseTopic());
        if (req.getPlatforms() != null) profile.setPlatforms(req.getPlatforms());

        return mapToTrainerProfileResponse(trainerProfileRepo.save(profile));
    }

    private TrainerProfileResponse mapToTrainerProfileResponse(TrainerProfile p) {
        TrainerProfileResponse r = new TrainerProfileResponse();
        r.setLinkedinUrl(p.getLinkedinUrl());
        r.setCountry(p.getCountry());
        r.setAudienceSize(p.getAudienceSize());
        r.setFullTimeRole(p.getFullTimeRole());
        r.setCourseTopic(p.getCourseTopic());
//        r.setPlatforms(p.getPlatforms());
     // ✅ null-safe
        r.setPlatforms(p.getPlatforms() != null ? p.getPlatforms() : new java.util.ArrayList<>());
        return r;
    }
    
    //super admin endpoint servcie for users without any org id 
 // WHY: SuperAdmin batch assignment for users with no organization (Google login / superadmin-created)
    @Transactional(readOnly = true)
    public Page<UserResponse> getUsersWithoutOrgByRole(String role, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return repo.findByOrganizationIdIsNullAndRolesContaining(role, pageable).map(this::mapToResponse);
    }
}