
package com.lms.course.controller;
import com.lms.course.client.UserClient;
import com.lms.course.constants.CourseFeatureKeys;
import com.lms.course.dto.CourseFeatureFlagsDTO;
import com.lms.course.dto.PageResponse;
import com.lms.course.dto.TrainerDTO;
import com.lms.course.model.Course;
import com.lms.course.security.JwtUtil;
import com.lms.course.service.CourseFeatureFlagsService;
import com.lms.course.service.CourseService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;
    private final JwtUtil jwtUtil;
    private final CourseFeatureFlagsService featureFlagsService; // NEW
    private final UserClient userClient;

    public CourseController(CourseService courseService,
                            JwtUtil jwtUtil,
                            CourseFeatureFlagsService featureFlagsService,
                            UserClient userClient) {
        this.courseService       = courseService;
        this.jwtUtil             = jwtUtil;
        this.featureFlagsService = featureFlagsService;
        this.userClient          = userClient;
    }

    // ── helpers ───────────────────────────────────────────────────────────────
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) return header.substring(7);
        return null;
    }

    // ── TRAINER: Get my courses ───────────────────────────────────────────────
    @GetMapping("/my")
    public ResponseEntity<List<Course>> getMyCourses(HttpServletRequest request) {
        String token          = extractToken(request);
        String email          = jwtUtil.extractEmail(token);
        String organizationId = jwtUtil.extractOrganizationId(token);

        // FEATURE GATE: trainer must have get_my_courses enabled
        featureFlagsService.enforce(organizationId, email,
                CourseFeatureKeys.GET_MY_COURSES);

        return ResponseEntity.ok(courseService.getByEmail(email));
    }

    // ── TRAINER: Create course ────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<Course> create(@RequestBody Course course,
                                         HttpServletRequest request) {
        String token          = extractToken(request);
        String email          = jwtUtil.extractEmail(token);
        String organizationId = jwtUtil.extractOrganizationId(token);

        // FEATURE GATE: trainer must have create_course enabled
        featureFlagsService.enforce(organizationId, email,
                CourseFeatureKeys.CREATE_COURSE);

        return ResponseEntity.ok(courseService.create(course, email, organizationId));
    }

    // ── TRAINER: Update course ────────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<Course> update(@PathVariable Long id,
                                         @RequestBody Course updated,
                                         HttpServletRequest request) {
        String token          = extractToken(request);
        String email          = jwtUtil.extractEmail(token);
        String organizationId = jwtUtil.extractOrganizationId(token);

        // FEATURE GATE: trainer must have update_course enabled
        featureFlagsService.enforce(organizationId, email,
                CourseFeatureKeys.UPDATE_COURSE);

        return ResponseEntity.ok(courseService.update(id, updated));
    }

    // ── TRAINER: Delete course ────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id,
                                         HttpServletRequest request) {
        String token          = extractToken(request);
        String email          = jwtUtil.extractEmail(token);
        String organizationId = jwtUtil.extractOrganizationId(token);

        // FEATURE GATE: trainer must have delete_course enabled
        featureFlagsService.enforce(organizationId, email,
                CourseFeatureKeys.DELETE_COURSE);

        return ResponseEntity.ok(courseService.delete(id));
    }

    // ── STUDENT: Get enrolled courses ─────────────────────────────────────────
    @GetMapping("/student")
    public ResponseEntity<List<Course>> getStudentCourses(HttpServletRequest request) {
        String token          = extractToken(request);
        String email          = jwtUtil.extractEmail(token);
        String organizationId = jwtUtil.extractOrganizationId(token);

        // FEATURE GATE: student must have get_student_courses enabled
        featureFlagsService.enforce(organizationId, email,
                CourseFeatureKeys.GET_STUDENT_COURSES);

        return ResponseEntity.ok(courseService.getStudentCourses(email));
    }

    // ── ADMIN: Get all courses ────────────────────────────────────────────────
    @GetMapping("/admin")
    public ResponseEntity<List<Course>> getAllCoursesForAdmin(HttpServletRequest request) {
        String token          = extractToken(request);
        String email          = jwtUtil.extractEmail(token);
        String organizationId = jwtUtil.extractOrganizationId(token);

        // FEATURE GATE: admin must have get_all_courses enabled
        featureFlagsService.enforce(organizationId, email,
                CourseFeatureKeys.GET_ALL_COURSES);

        // NOTE: implement getAllCoursesForAdmin() in CourseService if not present
        return ResponseEntity.ok(courseService.getByEmail(email)); // replace with actual admin method
    }

    // ── ADMIN: Get courses by category ────────────────────────────────────────
    @GetMapping("/admin/category/{category}")
    public ResponseEntity<List<Course>> getCoursesByCategory(
            @PathVariable String category,
            HttpServletRequest request) {
        String token          = extractToken(request);
        String email          = jwtUtil.extractEmail(token);
        String organizationId = jwtUtil.extractOrganizationId(token);

        // FEATURE GATE: admin must have get_courses_by_category enabled
        featureFlagsService.enforce(organizationId, email,
                CourseFeatureKeys.GET_COURSES_BY_CATEGORY);

        // NOTE: implement getCoursesByCategory() in CourseService if not present
        return ResponseEntity.ok(courseService.getCoursesByOrganization(organizationId));
    }

    // ── SUPER ADMIN / PUBLIC — NOT GATED ──────────────────────────────────────
    // These are either public (permitAll) or super-admin only (no org context)

    @GetMapping("/{id}")
    public ResponseEntity<Course> getById(@PathVariable Long id,
                                          HttpServletRequest request) {
        String token = extractToken(request);
        String email = token != null ? jwtUtil.extractEmail(token) : null;
        String role  = token != null ? jwtUtil.extractRole(token)  : "PUBLIC";
        return ResponseEntity.ok(courseService.getById(id, email, role));
    }

    @GetMapping("/org/{organizationId}")
    public ResponseEntity<List<Course>> getCoursesByOrg(
            @PathVariable String organizationId) {
        return ResponseEntity.ok(courseService.getCoursesByOrganization(organizationId));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        return ResponseEntity.ok(courseService.getAllCategories());
    }

    @GetMapping("/super-admin/independent")
    public ResponseEntity<List<Course>> getIndependentTrainerCourses() {
        return ResponseEntity.ok(courseService.getIndependentTrainerCourses());
    }

    @GetMapping("/super-admin/independent-categories")
    public ResponseEntity<List<String>> getIndependentTrainerCategories() {
        return ResponseEntity.ok(courseService.getIndependentTrainerCategories());
    }

    @GetMapping("/trainer")
    public ResponseEntity<List<Course>> getTrainerCourses(HttpServletRequest request) {
        String token = extractToken(request);
        String email = jwtUtil.extractEmail(token);
        return ResponseEntity.ok(courseService.getTrainerCourses(email));
    }
    
 // ADMIN: load trainers in his org (for course-assign dropdown)
    @GetMapping("/admin/trainers")
    public ResponseEntity<List<TrainerDTO>> getOrgTrainers(HttpServletRequest request) {
        String token = extractToken(request);
        String orgId = jwtUtil.extractOrganizationId(token);

        PageResponse<TrainerDTO> page = userClient.getTrainersByOrg(orgId, "TRAINER");
        return ResponseEntity.ok(page.getContent());
    }
    
 // ADMIN: create + assign course to a trainer
    @PostMapping("/admin/assign")
    public ResponseEntity<Course> adminCreateCourse(
            @RequestBody Course course,
            HttpServletRequest request) {

        String token = extractToken(request);
        String email = jwtUtil.extractEmail(token);
        String orgId = jwtUtil.extractOrganizationId(token);

        featureFlagsService.enforce(orgId, email,
                CourseFeatureKeys.CREATE_COURSE);

        return ResponseEntity.ok(courseService.adminCreate(course, email, orgId));
    }

    // ADMIN: click trainer email → see their courses
    @GetMapping("/admin/trainer/{trainerEmail}")
    public ResponseEntity<List<Course>> getCoursesByTrainer(
            @PathVariable String trainerEmail,
            HttpServletRequest request) {

        String token = extractToken(request);
        String orgId = jwtUtil.extractOrganizationId(token);

        return ResponseEntity.ok(
                courseService.getCoursesByAssignedTrainer(trainerEmail, orgId));
    }

    // TRAINER: own + admin-assigned courses
    @GetMapping("/trainer/all")
    public ResponseEntity<List<Course>> getTrainerAllCourses(
            HttpServletRequest request) {

        String token = extractToken(request);
        String email = jwtUtil.extractEmail(token);
        String orgId = jwtUtil.extractOrganizationId(token);

        return ResponseEntity.ok(
                courseService.getTrainerAllCourses(email, orgId));
    }
 // Add this endpoint in CourseController
    @GetMapping("/org-admin")
    public ResponseEntity<List<Course>> getOrgAdminCourses(HttpServletRequest request) {
        String token = extractToken(request);
        String orgId = jwtUtil.extractOrganizationId(token);
        return ResponseEntity.ok(courseService.getCoursesByOrganization(orgId));
    }
}