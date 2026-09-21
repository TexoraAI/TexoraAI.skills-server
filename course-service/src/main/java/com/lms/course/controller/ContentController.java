package com.lms.course.controller;
import org.springframework.http.ResponseEntity;
import com.lms.course.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import com.lms.course.model.ContentItem;
import com.lms.course.service.ContentService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
 // ✅ React
@RestController
@RequestMapping("/api/content")
public class ContentController {

    private final ContentService service;
    private final JwtUtil jwtUtil; // NEW

    public ContentController(ContentService service, JwtUtil jwtUtil) { // NEW param
        this.service = service;
        this.jwtUtil = jwtUtil; // NEW
    }

    // 🔐 Only logged-in users can add content
    @PostMapping
    public ContentItem create(
            @RequestBody ContentItem item,
            Authentication auth
    ) {
        return service.create(item, auth.getName());
    }

//    @GetMapping("/course/{courseId}")
//    public List<ContentItem> getByCourse(
//            @PathVariable Long courseId,
//            Authentication auth
//    ) {
//        return service.getByCourse(courseId, auth.getName());
//    }
    @GetMapping("/course/{courseId}")
    public List<ContentItem> getByCourse(
            @PathVariable Long courseId,
            Authentication auth
    ) {
        // 🔓 Public preview
        if (auth == null) {
            return service.getPublicByCourse(courseId);
        }

        // 🔐 Trainer / owner access
        return service.getByCourse(courseId, auth.getName());
    }

    
    @GetMapping("/student/course/{courseId}")
    public List<ContentItem> getCourseForStudent(@PathVariable Long courseId) {
        return service.getByCourseForStudents(courseId);
    }


    @PutMapping("/{id}")
    public ContentItem update(
            @PathVariable Long id,
            @RequestBody ContentItem updated,
            Authentication auth
    ) {
        return service.update(id, updated, auth.getName());
    }

    @DeleteMapping("/{id}")
    public String delete(
            @PathVariable Long id,
            Authentication auth
    ) {
        return service.delete(id, auth.getName());
    }
 // Student marks a content item as completed
    @PostMapping("/{contentId}/complete")
    public ResponseEntity<String> markComplete(
            @PathVariable Long contentId,
            Authentication auth) {

        service.markContentComplete(contentId, auth.getName());
        return ResponseEntity.ok("Progress updated");
    }
    
    // ── Module usage (for quota pill) ──────────────────────────────────────
    @GetMapping("/usage/{courseId}")
    public java.util.Map<String, Object> getModuleUsage(
            @PathVariable Long courseId,
            HttpServletRequest request,
            Authentication auth
    ) {
        String header = request.getHeader("Authorization");
        String token = (header != null && header.startsWith("Bearer ")) ? header.substring(7) : null;
        String organizationId = token != null ? jwtUtil.extractOrganizationId(token) : null;

        return service.getModuleUsage(courseId, auth.getName(), organizationId);
    }
}
