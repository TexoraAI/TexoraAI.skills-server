package com.lms.progress.controller;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import com.lms.progress.dto.OrgRoadmapGraphResponse;
import com.lms.progress.dto.TemplateGraphResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lms.progress.dto.BatchUpdateProgressRequest;
import com.lms.progress.dto.BatchUpdateProgressResponse;
import com.lms.progress.dto.CloneTemplateRequest;
import com.lms.progress.dto.CreateCustomOrgRoadmapRequest;
import com.lms.progress.dto.CreateOrgNodeRequest;
import com.lms.progress.dto.CreateOrgResourceRequest;
import com.lms.progress.dto.CreateTemplateNodeRequest;
import com.lms.progress.dto.CreateTemplateRequest;
import com.lms.progress.dto.CreateTemplateResourceRequest;
import com.lms.progress.dto.NodeProgressResponse;
import com.lms.progress.dto.OrgNodeResponse;
import com.lms.progress.dto.OrgResourceResponse;
import com.lms.progress.dto.OrgRoadmapResponse;
import com.lms.progress.dto.PagedResponse;
import com.lms.progress.dto.RoadmapAnalyticsResponse;
import com.lms.progress.dto.RoadmapGraphResponse;
import com.lms.progress.dto.RoadmapListItemResponse;
import com.lms.progress.dto.StudentDashboardResponse;
import com.lms.progress.dto.StudentProgressSummaryResponse;
import com.lms.progress.dto.TemplateNodeResponse;
import com.lms.progress.dto.TemplateResourceResponse;
import com.lms.progress.dto.TemplateResponse;
import com.lms.progress.dto.UpdateNodeProgressRequest;
import com.lms.progress.dto.UpdateOrgNodeRequest;
import com.lms.progress.dto.UpdateOrgResourceRequest;
import com.lms.progress.dto.UpdateOrgRoadmapRequest;
import com.lms.progress.dto.UpdateTemplateNodeRequest;
import com.lms.progress.dto.UpdateTemplateRequest;
import com.lms.progress.dto.UpdateTemplateResourceRequest;
import com.lms.progress.exception.InsufficientRoleException;
import com.lms.progress.security.JwtUtil;
import com.lms.progress.service.RoadmapService;

@RestController
@RequestMapping("/api/progress/roadmaps")
public class RoadmapController {

    private final RoadmapService roadmapService;
    private final JwtUtil jwtUtil;

    public RoadmapController(RoadmapService roadmapService, JwtUtil jwtUtil) {
        this.roadmapService = roadmapService;
        this.jwtUtil = jwtUtil;
    }

    // =====================================================================================
    // JWT resolution helpers
    // =====================================================================================

    /** Plain internal holder — not a DTO, so no getters needed beyond direct field access. */
    private static final class CallerContext {
        final String role;
        final Long userId;
        final String orgId;

        CallerContext(String role, Long userId, String orgId) {
            this.role = role;
            this.userId = userId;
            this.orgId = orgId;
        }
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new InsufficientRoleException("Missing or malformed Authorization header.");
        }
        return header.substring("Bearer ".length());
    }

    private CallerContext resolveCaller(HttpServletRequest request) {
        String token = extractToken(request);
        if (!jwtUtil.validateToken(token)) {
            throw new InsufficientRoleException("Invalid or expired JWT.");
        }
        String role = jwtUtil.extractRole(token);
        Long userId = jwtUtil.extractUserId(token);
        String orgId = jwtUtil.extractOrganizationIdOrNull(token);
        return new CallerContext(role, userId, orgId);
    }

    /**
     * For request bodies that carry a client-supplied {@code orgId} (clone/custom
     * create): SUPER_ADMIN may legitimately target any org, including null.
     * Every other role has its body-supplied orgId ignored and overwritten with
     * their own JWT-derived organizationId, regardless of what was sent.
     */
    private String resolveTargetOrgId(String clientSuppliedOrgId, CallerContext caller) {
        if (jwtUtil.isSuperAdmin(caller.role)) {
            return clientSuppliedOrgId;
        }
        return caller.orgId;
    }

    // =====================================================================================
    // Super Admin — /admin/templates
    // =====================================================================================

    @PostMapping("/admin/templates")
    public ResponseEntity<TemplateResponse> createTemplate(@RequestBody CreateTemplateRequest request,
                                                             HttpServletRequest httpRequest) {
        CallerContext caller = resolveCaller(httpRequest);
        TemplateResponse response = roadmapService.createTemplate(request, caller.role, caller.userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/admin/templates")
    public ResponseEntity<PagedResponse<TemplateResponse>> listPublishedTemplates(Pageable pageable,
                                                                                    HttpServletRequest httpRequest) {
        CallerContext caller = resolveCaller(httpRequest);
        return ResponseEntity.ok(roadmapService.listPublishedTemplates(pageable, caller.role));
    }

    @GetMapping("/admin/templates/{id}")
    public ResponseEntity<TemplateResponse> getTemplate(@PathVariable Long id, HttpServletRequest httpRequest) {
        CallerContext caller = resolveCaller(httpRequest);
        return ResponseEntity.ok(roadmapService.getTemplate(id, caller.role));
    }

    @PutMapping("/admin/templates/{id}")
    public ResponseEntity<TemplateResponse> updateTemplate(@PathVariable Long id,
                                                             @RequestBody UpdateTemplateRequest request,
                                                             HttpServletRequest httpRequest) {
        CallerContext caller = resolveCaller(httpRequest);
        return ResponseEntity.ok(roadmapService.updateTemplate(id, request, caller.role));
    }

    @PostMapping("/admin/templates/{templateId}/nodes")
    public ResponseEntity<TemplateNodeResponse> createTemplateNode(@PathVariable Long templateId,
                                                                     @RequestBody CreateTemplateNodeRequest request,
                                                                     HttpServletRequest httpRequest) {
        CallerContext caller = resolveCaller(httpRequest);
        request.setTemplateId(templateId); // path is authoritative over any body value
        TemplateNodeResponse response = roadmapService.createTemplateNode(request, caller.role);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/admin/templates/nodes/{nodeId}")
    public ResponseEntity<TemplateNodeResponse> updateTemplateNode(@PathVariable Long nodeId,
                                                                     @RequestBody UpdateTemplateNodeRequest request,
                                                                     HttpServletRequest httpRequest) {
        CallerContext caller = resolveCaller(httpRequest);
        return ResponseEntity.ok(roadmapService.updateTemplateNode(nodeId, request, caller.role));
    }

    @DeleteMapping("/admin/templates/nodes/{nodeId}")
    public ResponseEntity<Void> deleteTemplateNode(@PathVariable Long nodeId, HttpServletRequest httpRequest) {
        CallerContext caller = resolveCaller(httpRequest);
        roadmapService.deleteTemplateNode(nodeId, caller.role);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/admin/templates/nodes/{nodeId}/resources")
    public ResponseEntity<TemplateResourceResponse> createTemplateResource(@PathVariable Long nodeId,
                                                                             @RequestBody CreateTemplateResourceRequest request,
                                                                             HttpServletRequest httpRequest) {
        CallerContext caller = resolveCaller(httpRequest);
        request.setNodeId(nodeId); // path is authoritative
        TemplateResourceResponse response = roadmapService.createTemplateResource(request, caller.role, caller.userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/admin/templates/resources/{resourceId}")
    public ResponseEntity<TemplateResourceResponse> updateTemplateResource(@PathVariable Long resourceId,
                                                                             @RequestBody UpdateTemplateResourceRequest request,
                                                                             HttpServletRequest httpRequest) {
        CallerContext caller = resolveCaller(httpRequest);
        return ResponseEntity.ok(roadmapService.updateTemplateResource(resourceId, request, caller.role));
    }

    @DeleteMapping("/admin/templates/resources/{resourceId}")
    public ResponseEntity<Void> deleteTemplateResource(@PathVariable Long resourceId, HttpServletRequest httpRequest) {
        CallerContext caller = resolveCaller(httpRequest);
        roadmapService.deleteTemplateResource(resourceId, caller.role);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/admin/templates/{templateId}/publish")
    public ResponseEntity<TemplateResponse> publishTemplate(@PathVariable Long templateId,
                                                              HttpServletRequest httpRequest) {
        CallerContext caller = resolveCaller(httpRequest);
        return ResponseEntity.ok(roadmapService.publishTemplate(templateId, caller.userId, caller.role));
    }

    @GetMapping("/admin/templates/{id}/graph")
    public ResponseEntity<TemplateGraphResponse> getTemplateGraphForEditing(@PathVariable Long id,
                                                                              HttpServletRequest httpRequest) {
        CallerContext caller = resolveCaller(httpRequest);
        return ResponseEntity.ok(roadmapService.getTemplateGraphForEditing(id, caller.role));
    }

    // =====================================================================================
    // Org Admin — /org (ADMIN/TENANT_ADMIN own-org, or SUPER_ADMIN any/null-org)
    // =====================================================================================

    @PostMapping("/org/clone/{templateId}")
    public ResponseEntity<OrgRoadmapResponse> cloneTemplate(@PathVariable Long templateId,
                                                              @RequestBody CloneTemplateRequest request,
                                                              HttpServletRequest httpRequest) {
        CallerContext caller = resolveCaller(httpRequest);
        request.setSourceTemplateId(templateId); // path is authoritative over any body value
        request.setOrgId(resolveTargetOrgId(request.getOrgId(), caller));
        OrgRoadmapResponse response = roadmapService.cloneTemplate(request, caller.role, caller.userId, caller.orgId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/org/custom")
    public ResponseEntity<OrgRoadmapResponse> createCustomOrgRoadmap(@RequestBody CreateCustomOrgRoadmapRequest request,
                                                                       HttpServletRequest httpRequest) {
        CallerContext caller = resolveCaller(httpRequest);
        request.setOrgId(resolveTargetOrgId(request.getOrgId(), caller));
        OrgRoadmapResponse response = roadmapService.createCustomOrgRoadmap(request, caller.role, caller.userId, caller.orgId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * ADMIN/TENANT_ADMIN are always scoped to their own JWT-derived org. SUPER_ADMIN
     * may optionally pass ?orgId= to browse a specific org (or omit it / pass none
     * for the null-org bucket). TRAINER/STUDENT are rejected here — this method's
     * service counterpart (Part 2A) does not itself gate by role, so the gate lives
     * in the controller.
     */
    @GetMapping("/org")
    public ResponseEntity<PagedResponse<RoadmapListItemResponse>> listOrgRoadmaps(
            @RequestParam(required = false) String orgId,
            Pageable pageable,
            HttpServletRequest httpRequest) {
        CallerContext caller = resolveCaller(httpRequest);
        boolean isSuperAdmin = jwtUtil.isSuperAdmin(caller.role);
        if (!isSuperAdmin && !jwtUtil.isOrgAdminRole(caller.role)) {
            throw new InsufficientRoleException(
                    "Role " + caller.role + " is not authorized to list org roadmaps.");
        }
        String targetOrgId = isSuperAdmin ? orgId : caller.orgId;
        return ResponseEntity.ok(roadmapService.listOrgRoadmaps(targetOrgId, isSuperAdmin, pageable));
    }

//    @GetMapping("/org/mine")
//    public ResponseEntity<PagedResponse<OrgRoadmapResponse>> listMyOrgRoadmaps(Pageable pageable,
//                                                                                 HttpServletRequest httpRequest) {
//        CallerContext caller = resolveCaller(httpRequest);
//        return ResponseEntity.ok(roadmapService.listMyOrgRoadmaps(caller.userId, pageable));
//    }
    @GetMapping("/org/mine")
    public ResponseEntity<PagedResponse<OrgRoadmapResponse>> listMyOrgRoadmaps(
        Pageable pageable,
        HttpServletRequest httpRequest) {
        try {
            System.out.println("→ /org/mine called");
            CallerContext caller = resolveCaller(httpRequest);
            System.out.println("✓ Caller resolved: userId=" + caller.userId + ", role=" + caller.role);
            PagedResponse<OrgRoadmapResponse> result = roadmapService.listMyOrgRoadmaps(caller.userId, pageable);
            System.out.println("✓ Got " + result.getContent().size() + " roadmaps");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("✗ ERROR: " + e.getClass().getSimpleName());
            System.err.println("✗ MESSAGE: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/org/{id}")
    public ResponseEntity<OrgRoadmapResponse> getOrgRoadmap(@PathVariable Long id, HttpServletRequest httpRequest) {
        CallerContext caller = resolveCaller(httpRequest);
        return ResponseEntity.ok(roadmapService.getOrgRoadmap(id, caller.role, caller.userId, caller.orgId));
    }

    @GetMapping("/org/{id}/graph")
    public ResponseEntity<OrgRoadmapGraphResponse> getOrgRoadmapGraphForEditing(@PathVariable Long id,
                                                                                  HttpServletRequest httpRequest) {
        CallerContext caller = resolveCaller(httpRequest);
        return ResponseEntity.ok(
                roadmapService.getOrgRoadmapGraphForEditing(id, caller.role, caller.userId, caller.orgId));
    }

    @PutMapping("/org/{id}")
    public ResponseEntity<OrgRoadmapResponse> updateOrgRoadmap(@PathVariable Long id,
                                                                 @RequestBody UpdateOrgRoadmapRequest request,
                                                                 HttpServletRequest httpRequest) {
        CallerContext caller = resolveCaller(httpRequest);
        return ResponseEntity.ok(roadmapService.updateOrgRoadmap(id, request, caller.role, caller.orgId));
    }

    @PostMapping("/org/{id}/publish")
    public ResponseEntity<OrgRoadmapResponse> publishOrgRoadmap(@PathVariable Long id, HttpServletRequest httpRequest) {
        CallerContext caller = resolveCaller(httpRequest);
        return ResponseEntity.ok(roadmapService.publishOrgRoadmap(id, caller.userId, caller.role, caller.orgId));
    }

    @PostMapping("/org/{id}/unpublish")
    public ResponseEntity<OrgRoadmapResponse> unpublishOrgRoadmap(@PathVariable Long id, HttpServletRequest httpRequest) {
        CallerContext caller = resolveCaller(httpRequest);
        return ResponseEntity.ok(roadmapService.unpublishOrgRoadmap(id, caller.role, caller.orgId));
    }

    @GetMapping("/org/{id}/analytics")
    public ResponseEntity<RoadmapAnalyticsResponse> getRoadmapAnalytics(@PathVariable Long id,
                                                                          HttpServletRequest httpRequest) {
        CallerContext caller = resolveCaller(httpRequest);
        return ResponseEntity.ok(roadmapService.getRoadmapAnalytics(id, caller.role, caller.orgId));
    }

    // =====================================================================================
    // Trainer — /org/{id}/... (ownership-checked in RoadmapService via createdBy == callerUserId)
    // =====================================================================================

    @PostMapping("/org/{id}/nodes")
    public ResponseEntity<OrgNodeResponse> createOrgNodeAsTrainer(@PathVariable Long id,
                                                                    @RequestBody CreateOrgNodeRequest request,
                                                                    HttpServletRequest httpRequest) {
        CallerContext caller = resolveCaller(httpRequest);
        request.setOrgRoadmapId(id); // path is authoritative over any body value
        OrgNodeResponse response = roadmapService.createOrgNodeAsTrainer(request, caller.userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/org/{id}/nodes/{nodeId}")
    public ResponseEntity<OrgNodeResponse> updateOrgNodeAsTrainer(@PathVariable Long id,
                                                                    @PathVariable Long nodeId,
                                                                    @RequestBody UpdateOrgNodeRequest request,
                                                                    HttpServletRequest httpRequest) {
        CallerContext caller = resolveCaller(httpRequest);
        return ResponseEntity.ok(roadmapService.updateOrgNodeAsTrainer(nodeId, request, caller.userId));
    }

    @DeleteMapping("/org/{id}/nodes/{nodeId}")
    public ResponseEntity<Void> deleteOrgNodeAsTrainer(@PathVariable Long id, @PathVariable Long nodeId,
                                                         HttpServletRequest httpRequest) {
        CallerContext caller = resolveCaller(httpRequest);
        roadmapService.deleteOrgNodeAsTrainer(nodeId, caller.userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/org/{id}/nodes/{nodeId}/resources")
    public ResponseEntity<OrgResourceResponse> createOrgResourceAsTrainer(@PathVariable Long id,
                                                                            @PathVariable Long nodeId,
                                                                            @RequestBody CreateOrgResourceRequest request,
                                                                            HttpServletRequest httpRequest) {
        CallerContext caller = resolveCaller(httpRequest);
        request.setNodeId(nodeId); // path is authoritative
        OrgResourceResponse response = roadmapService.createOrgResourceAsTrainer(request, caller.userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/org/{id}/resources/{resourceId}")
    public ResponseEntity<OrgResourceResponse> updateOrgResourceAsTrainer(@PathVariable Long id,
                                                                            @PathVariable Long resourceId,
                                                                            @RequestBody UpdateOrgResourceRequest request,
                                                                            HttpServletRequest httpRequest) {
        CallerContext caller = resolveCaller(httpRequest);
        return ResponseEntity.ok(roadmapService.updateOrgResourceAsTrainer(resourceId, request, caller.userId));
    }

    @DeleteMapping("/org/{id}/resources/{resourceId}")
    public ResponseEntity<Void> deleteOrgResourceAsTrainer(@PathVariable Long id, @PathVariable Long resourceId,
                                                             HttpServletRequest httpRequest) {
        CallerContext caller = resolveCaller(httpRequest);
        roadmapService.deleteOrgResourceAsTrainer(resourceId, caller.userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/org/{id}/students-progress")
    public ResponseEntity<PagedResponse<StudentProgressSummaryResponse>> getStudentsProgress(
            @PathVariable Long id, Pageable pageable, HttpServletRequest httpRequest) {
        CallerContext caller = resolveCaller(httpRequest);
        return ResponseEntity.ok(roadmapService.getStudentsProgress(id, caller.userId, pageable));
    }

    // =====================================================================================
    // Student — root prefix (/api/progress/roadmaps)
    // =====================================================================================

    @GetMapping
    public ResponseEntity<PagedResponse<RoadmapListItemResponse>> listPublishedRoadmapsForStudent(
            Pageable pageable, HttpServletRequest httpRequest) {
        CallerContext caller = resolveCaller(httpRequest);
        return ResponseEntity.ok(roadmapService.listPublishedRoadmapsForStudent(caller.orgId, pageable));
    }

    // NOTE: declared before "/{slug}" — Spring's request-mapping matcher already
    // prefers the more specific literal pattern over a variable one regardless of
    // declaration order, but keeping the literal route grouped here for readability.
    @GetMapping("/dashboard")
    public ResponseEntity<StudentDashboardResponse> getStudentDashboard(HttpServletRequest httpRequest) {
        CallerContext caller = resolveCaller(httpRequest);
        return ResponseEntity.ok(roadmapService.getStudentDashboard(caller.userId, caller.orgId));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<RoadmapGraphResponse> getRoadmapGraphForStudent(@PathVariable String slug,
                                                                            HttpServletRequest httpRequest) {
        CallerContext caller = resolveCaller(httpRequest);
        return ResponseEntity.ok(roadmapService.getRoadmapGraphForStudent(slug, caller.orgId, caller.userId));
    }

    /**
     * NOTE: this task lists RoadmapService#getStudentProgress as
     * (String slug, Long studentUserId). Part 2A's actual signature is
     * (String slug, String orgId, Long studentUserId) — an orgId parameter was added
     * there because OrgRoadmap slugs are only unique per-org (including the
     * null-org bucket), so slug alone can't safely resolve a roadmap. Calling the
     * real Part 2A signature here with the JWT-derived orgId.
     */
    @GetMapping("/{slug}/progress")
    public ResponseEntity<List<NodeProgressResponse>> getStudentProgress(@PathVariable String slug,
                                                                           HttpServletRequest httpRequest) {
        CallerContext caller = resolveCaller(httpRequest);
        return ResponseEntity.ok(roadmapService.getStudentProgress(slug, caller.orgId, caller.userId));
    }

    /**
     * Same orgId-signature note as getStudentProgress above. Path {@code nodeId} is
     * authoritative over the body's {@code nodeId}, per spec.
     */
    @PutMapping("/{slug}/nodes/{nodeId}/progress")
    public ResponseEntity<NodeProgressResponse> updateNodeProgress(@PathVariable String slug,
                                                                     @PathVariable Long nodeId,
                                                                     @RequestBody UpdateNodeProgressRequest request,
                                                                     HttpServletRequest httpRequest) {
        CallerContext caller = resolveCaller(httpRequest);
        request.setNodeId(nodeId); // path is authoritative over any body value
        NodeProgressResponse response =
                roadmapService.updateNodeProgress(slug, caller.orgId, caller.userId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{slug}/progress/batch")
    public ResponseEntity<BatchUpdateProgressResponse> batchUpdateProgress(@PathVariable String slug,
                                                                             @RequestBody BatchUpdateProgressRequest request,
                                                                             HttpServletRequest httpRequest) {
        CallerContext caller = resolveCaller(httpRequest);
        return ResponseEntity.ok(
                roadmapService.batchUpdateProgress(slug, caller.orgId, caller.userId, request));
    }
}