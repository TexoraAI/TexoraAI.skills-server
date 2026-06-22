//
//
//package com.lms.course.controller;
//
//import com.lms.course.model.Course;
//import com.lms.course.security.JwtUtil;
//import com.lms.course.service.CourseService;
//import jakarta.servlet.http.HttpServletRequest;
//import org.springframework.http.HttpStatus;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.core.Authentication;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.server.ResponseStatusException;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/courses")
//public class CourseController {
//
//    private final CourseService service;
//    private final JwtUtil       jwtUtil;  // NEW — same JwtUtil already used in Course Service security filter
//
//    public CourseController(CourseService service, JwtUtil jwtUtil) {
//        this.service = service;
//        this.jwtUtil = jwtUtil;
//    }
//
//    // ============================
//    // CREATE COURSE
//    // ============================
//    // organizationId is NOT in the request body — it is extracted silently from
//    // the JWT token the trainer already sends with every request.
//    // org-based trainers  : orgId present in JWT → tenant-isolated batch validation.
//    // non-org trainers    : orgId null in JWT    → existing batch-only validation.
//    @PostMapping
//    public Course create(@RequestBody Course course,
//                         Authentication auth,
//                         HttpServletRequest request) {
//
//        if (auth == null || auth.getName() == null) {
//            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
//        }
//
//        // Extract organizationId from JWT — null for non-org users, handled gracefully in service
//        String organizationId = extractOrgId(request);
//
//        return service.create(course, auth.getName(), organizationId);
//    }
//
//    // ============================
//    // MY COURSES (Trainer)
//    // ============================
//    @GetMapping("/my")
//    public List<Course> myCourses(Authentication auth) {
//        if (auth == null || auth.getName() == null) {
//            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
//        }
//        return service.getTrainerCourses(auth.getName());
//    }
//
//    // ============================
//    // STUDENT COURSES
//    // ============================
//    @GetMapping("/student")
//    public List<Course> studentCourses(Authentication auth) {
//        if (auth == null) {
//            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
//        }
//        return service.getStudentCourses(auth.getName());
//    }
//
//    // ============================
//    // GET COURSE BY ID
//    // ============================
//    @GetMapping("/{id}")
//    public Course getById(@PathVariable Long id, Authentication auth) {
//        return service.getById(
//                id,
//                auth.getName(),
//                auth.getAuthorities().iterator().next().getAuthority()
//        );
//    }
//
//    // ============================
//    // UPDATE COURSE
//    // ============================
//    @PutMapping("/{id}")
//    public Course update(@PathVariable Long id, @RequestBody Course updated) {
//        return service.update(id, updated);
//    }
//
//    // ============================
//    // DELETE COURSE
//    // ============================
//    @DeleteMapping("/{id}")
//    public String delete(@PathVariable Long id) {
//        return service.delete(id);
//    }
//
//    // ============================
//    // ADMIN — GET ALL COURSES
//    // ============================
////    @PreAuthorize("hasRole('ADMIN')")
////    @GetMapping("/admin")
////    public List<Course> getAllCourses(Authentication auth) {
////        if (auth == null) {
////            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
////        }
////        boolean isAdmin = auth.getAuthorities().stream()
////                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
////        if (!isAdmin) {
////            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied - Admin Only");
////        }
////        return service.getAllCoursesForAdmin();
////    }
////
////    // ============================
////    // ADMIN — GET COURSES BY CATEGORY
////    // ============================
////    @GetMapping("/admin/category/{category}")
////    public List<Course> getByCategory(@PathVariable String category, Authentication auth) {
////        if (auth == null) {
////            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
////        }
////        boolean isAdmin = auth.getAuthorities().stream()
////                .anyMatch(a -> a.getAuthority().equals("ADMIN"));
////        if (!isAdmin) {
////            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied - Admin Only");
////        }
////        return service.getByCategory(category);
////    }
////
// // ORG ADMIN — GET COURSES BY ORG
// // ============================
// @GetMapping("/org-admin")
// public List<Course> getCoursesForOrgAdmin(
//         Authentication auth,
//         HttpServletRequest request) {
//
//     if (auth == null)
//         throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
//
//     String organizationId = extractOrgId(request);
//     if (organizationId == null || organizationId.isBlank())
//         throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No organization found in token");
//
//     return service.getCoursesByOrganization(organizationId);
// }
// 
////SUPER ADMIN — get courses by specific organizationId
//@GetMapping("/org/{organizationId}")
//public List<Course> getCoursesByOrgId(
//      @PathVariable String organizationId,
//      Authentication auth) {
//  if (auth == null)
//      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
//  return service.getCoursesByOrganization(organizationId);
//}
//    @GetMapping("/categories")
//    public List<String> getAllCategories(Authentication auth) {
//        if (auth == null)
//            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
//        return service.getAllCategories();
//    }
//    
// // ============================
// // SUPER ADMIN — independent trainer courses (organizationId IS NULL)
// // ============================
// // ============================
// @GetMapping("/super-admin/independent")
// public List<Course> getIndependentCourses(Authentication auth) {
// if (auth == null)
// throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
// return service.getIndependentTrainerCourses();
// }
// 
////SUPER ADMIN — categories from independent trainer courses (organizationId IS NULL)
//@GetMapping("/super-admin/independent-categories")
//public List<String> getIndependentCategories(Authentication auth) {
//  if (auth == null)
//      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
//  return service.getIndependentTrainerCategories();
//}
//    // ============================
//    // PRIVATE HELPER
//    // ============================
//    // Reads the raw JWT from the Authorization header and delegates to JwtUtil —
//    // exact same pattern already used in Batch Service's JwtUtil.extractOrganizationId().
//    // Returns null cleanly when claim is absent (non-org user). Never throws.
//    private String extractOrgId(HttpServletRequest request) {
//        try {
//            String header = request.getHeader("Authorization");
//            if (header != null && header.startsWith("Bearer ")) {
//                String token = header.substring(7);
//                return jwtUtil.extractOrganizationId(token);
//            }
//        } catch (Exception e) {
//            System.out.println("Could not extract organizationId from JWT: " + e.getMessage());
//        }
//        return null;
//    }
//}

// ─────────────────────────────────────────────────────────────────────────────
// PATCH for CourseController — add these changes to your existing controller.
//
// WHAT CHANGES:
//   1. Inject CourseFeatureFlagsService
//   2. Call featureFlagsService.enforce(...) at the top of gated endpoints
//   3. organizationId is already extracted from JWT in your controller via jwtUtil
//
// WHICH ENDPOINTS ARE GATED (trainer/student/admin only):
//   TRAINER : create, update, delete, getMyCourses
//   STUDENT : getStudentCourses
//   ADMIN   : getAllCourses, getCoursesByCategory, manageFeaturedCourses
//
// WHICH ARE NOT GATED (public / super-admin):
//   getById           — public preview, permitAll in SecurityConfig
//   getByOrg          — super admin only, no org context to check
//   getIndependent*   — super admin only
//   getAllCategories   — super admin only
// ─────────────────────────────────────────────────────────────────────────────

package com.lms.course.controller;

import com.lms.course.constants.CourseFeatureKeys;
import com.lms.course.dto.CourseFeatureFlagsDTO;
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

    public CourseController(CourseService courseService,
                            JwtUtil jwtUtil,
                            CourseFeatureFlagsService featureFlagsService) { // NEW
        this.courseService       = courseService;
        this.jwtUtil             = jwtUtil;
        this.featureFlagsService = featureFlagsService; // NEW
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
}