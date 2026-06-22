
package com.lms.user.controller;

import com.lms.user.dto.StudentProfileRequest;
import com.lms.user.dto.StudentProfileResponse;
import com.lms.user.dto.TrainerProfileRequest;
import com.lms.user.dto.TrainerProfileResponse;

import com.lms.user.dto.CreateUserRequest;
import com.lms.user.dto.UpdateUserRequest;
import com.lms.user.dto.UserResponse;
import com.lms.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    // ── LOGGED-IN USER ───────────────────────────────────────────────────────

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile() {
        String email = getEmail();
        return ResponseEntity.ok(service.getByEmail(email));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMyProfile(@RequestBody UpdateUserRequest req) {
        String email = getEmail();
        return ResponseEntity.ok(service.updateByEmail(email, req));
    }

    // ── CRUD ─────────────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest req) {
        return ResponseEntity.ok(service.createUser(req));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
                                                   @RequestBody UpdateUserRequest req) {
        return ResponseEntity.ok(service.updateUser(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        service.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> getByEmail(@PathVariable String email) {
        return ResponseEntity.ok(service.getByEmail(email));
    }

    @GetMapping
    public ResponseEntity<Page<UserResponse>> listUsers(
            @RequestParam(defaultValue = "0")   int    page,
            @RequestParam(defaultValue = "20")  int    size,
            @RequestParam(defaultValue = "id")  String sort,
            @RequestParam(defaultValue = "asc") String dir) {
        return ResponseEntity.ok(service.listUsers(page, size, sort, dir));
    }

    // ── INTERNAL (microservice-to-microservice) ──────────────────────────────

//    @GetMapping("/internal/trainers")
//    public ResponseEntity<List<UserResponse>> getAllTrainers() {
//        return ResponseEntity.ok(service.getUsersByRole("TRAINER"));
//    }
//
//    @GetMapping("/internal/students")
//    public ResponseEntity<List<UserResponse>> getAllStudents() {
//        return ResponseEntity.ok(service.getUsersByRole("STUDENT"));
//    }
    // OPTIMIZATION: Added pagination — internal APIs must not load all trainers/students at once
    // WHY: course-service and batch-service call this to populate trainer assignment dropdowns
    @GetMapping("/internal/trainers")
    public ResponseEntity<Page<UserResponse>> getAllTrainers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(service.getUsersByRole("TRAINER", page, size));
    }

    @GetMapping("/internal/students")
    public ResponseEntity<Page<UserResponse>> getAllStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(service.getUsersByRole("STUDENT", page, size));
    }

    // ── ORG-SCOPED USER LISTS (NEW) ──────────────────────────────────────────

    /**
     * SuperAdmin: GET /api/users/by-org/{orgId}
     * Returns all users (students + trainers) belonging to the given org UUID.
     */
//    @GetMapping("/by-org/{orgId}")
//    public ResponseEntity<List<UserResponse>> getUsersByOrg(@PathVariable String orgId) {
//        return ResponseEntity.ok(service.getUsersByOrg(orgId));
//    }
 // OPTIMIZATION: Added pagination parameters — org can have thousands of users
    // WHY: SuperAdmin lists all users in an organization for multi-tenant management
    @GetMapping("/by-org/{orgId}")
    public ResponseEntity<Page<UserResponse>> getUsersByOrg(
            @PathVariable String orgId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.getUsersByOrg(orgId, page, size));
    }

    /**
     * SuperAdmin: GET /api/users/by-org/{orgId}/role/{role}
     * Returns users of a specific role in the org.
     * e.g. role = STUDENT | TRAINER
     */
//    @GetMapping("/by-org/{orgId}/role/{role}")
//    public ResponseEntity<List<UserResponse>> getUsersByOrgAndRole(
//            @PathVariable String orgId,
//            @PathVariable String role) {
//        return ResponseEntity.ok(service.getUsersByOrgAndRole(orgId, role));
//    }
 // OPTIMIZATION: Added pagination
    // WHY: Role-filtered org user list for admin batch assignment workflows
    @GetMapping("/by-org/{orgId}/role/{role}")
    public ResponseEntity<Page<UserResponse>> getUsersByOrgAndRole(
            @PathVariable String orgId,
            @PathVariable String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.getUsersByOrgAndRole(orgId, role, page, size));
    }

    /**
     * TENANT_ADMIN: GET /api/users/my-org
     * Returns all users belonging to the logged-in admin's organization.
     * The admin's organizationId is read from their own user record.
     */
//    @GetMapping("/my-org")
//    public ResponseEntity<List<UserResponse>> getMyOrgUsers() {
//        String email = getEmail();
//        // Look up the admin's own user record to get their organizationId
//        UserResponse admin = service.getByEmail(email);
//        if (admin.getOrganizationId() == null || admin.getOrganizationId().isBlank()) {
//            return ResponseEntity.ok(List.of()); // no org linked yet
//        }
//        return ResponseEntity.ok(service.getUsersByOrg(admin.getOrganizationId()));
//    }

    // OPTIMIZATION: Added pagination
    // WHY: TenantAdmin sees all users in their own org — same large list risk
    @GetMapping("/my-org")
    public ResponseEntity<Page<UserResponse>> getMyOrgUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String email = getEmail();
        UserResponse admin = service.getByEmail(email);
        if (admin.getOrganizationId() == null || admin.getOrganizationId().isBlank()) {
            return ResponseEntity.ok(Page.empty());
        }
        return ResponseEntity.ok(service.getUsersByOrg(admin.getOrganizationId(), page, size));
    }


    /**
     * TENANT_ADMIN: GET /api/users/my-org/role/{role}
     * Returns users of a specific role in the logged-in admin's org.
     */
//    @GetMapping("/my-org/role/{role}")
//    public ResponseEntity<List<UserResponse>> getMyOrgUsersByRole(@PathVariable String role) {
//        String email = getEmail();
//        UserResponse admin = service.getByEmail(email);
//        if (admin.getOrganizationId() == null || admin.getOrganizationId().isBlank()) {
//            return ResponseEntity.ok(List.of());
//        }
//        return ResponseEntity.ok(service.getUsersByOrgAndRole(admin.getOrganizationId(), role));
//    }
    // OPTIMIZATION: Added pagination
    @GetMapping("/my-org/role/{role}")
    public ResponseEntity<Page<UserResponse>> getMyOrgUsersByRole(
            @PathVariable String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String email = getEmail();
        UserResponse admin = service.getByEmail(email);
        if (admin.getOrganizationId() == null || admin.getOrganizationId().isBlank()) {
            return ResponseEntity.ok(Page.empty());
        }
        return ResponseEntity.ok(service.getUsersByOrgAndRole(admin.getOrganizationId(), role, page, size));
    }

    // ── STUDENT PROFILE ──────────────────────────────────────────────────────

    @GetMapping("/me/profile")
    public ResponseEntity<StudentProfileResponse> getMyStudentProfile() {
        return ResponseEntity.ok(service.getStudentProfile(getEmail()));
    }

    @PutMapping("/me/profile")
    public ResponseEntity<StudentProfileResponse> updateMyStudentProfile(
            @RequestBody StudentProfileRequest req) {
        return ResponseEntity.ok(service.updateStudentProfile(getEmail(), req));
    }

    @GetMapping("/profile/by-email/{email:.+}")
    public ResponseEntity<StudentProfileResponse> getStudentProfileByEmail(@PathVariable String email) {
        return ResponseEntity.ok(service.getStudentProfile(email));
    }

    @PutMapping("/profile/by-email/{email:.+}")
    public ResponseEntity<StudentProfileResponse> updateStudentProfileByEmail(
            @PathVariable String email, @RequestBody StudentProfileRequest req) {
        return ResponseEntity.ok(service.updateStudentProfile(email, req));
    }

    // ── TRAINER PROFILE ──────────────────────────────────────────────────────

//    @GetMapping("/me/trainer-profile")
//    public ResponseEntity<TrainerProfileResponse> getMyTrainerProfile() {
//        return ResponseEntity.ok(service.getTrainerProfile(getEmail()));
//    }
    @GetMapping("/me/trainer-profile")
    public ResponseEntity<TrainerProfileResponse> getMyTrainerProfile() {
        try {
            String email = getEmail();
            System.out.println(">>> TRAINER PROFILE REQUEST for email: " + email);
            TrainerProfileResponse response = service.getTrainerProfile(email);
            System.out.println(">>> TRAINER PROFILE SUCCESS: " + response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println(">>> TRAINER PROFILE ERROR: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @PutMapping("/me/trainer-profile")
    public ResponseEntity<TrainerProfileResponse> updateMyTrainerProfile(
            @RequestBody TrainerProfileRequest req) {
        return ResponseEntity.ok(service.updateTrainerProfile(getEmail(), req));
    }

    @GetMapping("/trainer-profile/by-email/{email:.+}")
    public ResponseEntity<TrainerProfileResponse> getTrainerProfileByEmail(@PathVariable String email) {
        return ResponseEntity.ok(service.getTrainerProfile(email));
    }

    @PutMapping("/trainer-profile/by-email/{email:.+}")
    public ResponseEntity<TrainerProfileResponse> updateTrainerProfileByEmail(
            @PathVariable String email, @RequestBody TrainerProfileRequest req) {
        return ResponseEntity.ok(service.updateTrainerProfile(email, req));
    }

    // ── ADMIN PROFILE ─────────────────────────────────────────────────────────
//
//    @GetMapping("/me/admin-profile")
//    public ResponseEntity<AdminProfileResponse> getMyAdminProfile() {
//        return ResponseEntity.ok(service.getAdminProfile(getEmail()));
//    }
//
//    @PutMapping("/me/admin-profile")
//    public ResponseEntity<AdminProfileResponse> updateMyAdminProfile(
//            @RequestBody AdminProfileRequest req) {
//        return ResponseEntity.ok(service.updateAdminProfile(getEmail(), req));
//    }
//
//    @GetMapping("/admin-profile/by-email/{email:.+}")
//    public ResponseEntity<AdminProfileResponse> getAdminProfileByEmail(@PathVariable String email) {
//        return ResponseEntity.ok(service.getAdminProfile(email));
//    }
//
//    @PutMapping("/admin-profile/by-email/{email:.+}")
//    public ResponseEntity<AdminProfileResponse> updateAdminProfileByEmail(
//            @PathVariable String email, @RequestBody AdminProfileRequest req) {
//        return ResponseEntity.ok(service.updateAdminProfile(email, req));
//    }

    // ── HELPER ───────────────────────────────────────────────────────────────
    private String getEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null)
            throw new RuntimeException("Authentication is null or username missing");
        return auth.getName();
    }
    
    /**
     * SuperAdmin: GET /api/users/no-org/role/{role}
     * Returns users with no organization (Google login or superadmin-created) of a given role.
     */
    @GetMapping("/no-org/role/{role}")
    public ResponseEntity<Page<UserResponse>> getUsersWithoutOrgByRole(
            @PathVariable String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(service.getUsersWithoutOrgByRole(role, page, size));
    }
}