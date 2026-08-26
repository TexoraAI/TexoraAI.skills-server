package com.lms.progress.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lms.progress.enrollment.BatchEnrollment;
import com.lms.progress.enrollment.BatchEnrollmentRepository;
import com.lms.progress.dto.BatchUpdateProgressRequest;
import com.lms.progress.dto.BatchUpdateProgressResponse;
import com.lms.progress.dto.CloneTemplateRequest;
import com.lms.progress.dto.CreateCustomOrgRoadmapRequest;
import com.lms.progress.dto.CreateOrgNodeRequest;
import com.lms.progress.dto.CreateOrgResourceRequest;
import com.lms.progress.dto.CreateTemplateNodeRequest;
import com.lms.progress.dto.CreateTemplateRequest;
import com.lms.progress.dto.CreateTemplateResourceRequest;
import com.lms.progress.dto.EnrolledRoadmapSummary;
import com.lms.progress.dto.GraphEdgeResponse;
import com.lms.progress.dto.GraphNodeResponse;
import com.lms.progress.dto.NodeBottleneckStat;
import com.lms.progress.dto.NodeProgressResponse;
import com.lms.progress.dto.NodeStatusPair;
import com.lms.progress.dto.OrgNodeResponse;
import com.lms.progress.dto.OrgNodeWithResourcesResponse;
import com.lms.progress.dto.OrgResourceResponse;
import com.lms.progress.dto.OrgRoadmapGraphResponse;
import com.lms.progress.dto.OrgRoadmapResponse;
import com.lms.progress.dto.PagedResponse;
import com.lms.progress.dto.RoadmapAnalyticsResponse;
import com.lms.progress.dto.RoadmapGraphResponse;
import com.lms.progress.dto.RoadmapListItemResponse;
import com.lms.progress.dto.StudentDashboardResponse;
import com.lms.progress.dto.StudentProgressSummaryResponse;
import com.lms.progress.dto.TemplateGraphResponse;
import com.lms.progress.dto.TemplateNodeResponse;
import com.lms.progress.dto.TemplateNodeWithResourcesResponse;
import com.lms.progress.dto.TemplateResourceResponse;
import com.lms.progress.dto.TemplateResponse;
import com.lms.progress.dto.UpdateNodeProgressRequest;
import com.lms.progress.dto.UpdateOrgNodeRequest;
import com.lms.progress.dto.UpdateOrgRoadmapRequest;
import com.lms.progress.dto.UpdateOrgResourceRequest;
import com.lms.progress.dto.UpdateTemplateNodeRequest;
import com.lms.progress.dto.UpdateTemplateRequest;
import com.lms.progress.dto.UpdateTemplateResourceRequest;
import com.lms.progress.exception.CrossOrgAccessDeniedException;
import com.lms.progress.exception.DuplicateSlugException;
import com.lms.progress.exception.InsufficientRoleException;
import com.lms.progress.exception.OwnershipViolationException;
import com.lms.progress.exception.RoadmapCycleException;
import com.lms.progress.exception.RoadmapNotFoundException;
import com.lms.progress.model.NodeStatus;
import com.lms.progress.model.OrgRoadmap;
import com.lms.progress.model.OrgRoadmapNode;
import com.lms.progress.model.OrgRoadmapResource;
import com.lms.progress.model.RoadmapTemplate;
import com.lms.progress.model.RoadmapTemplateNode;
import com.lms.progress.model.RoadmapTemplateResource;
import com.lms.progress.model.UserRoadmapProgress;
import com.lms.progress.repository.OrgRoadmapNodeRepository;
import com.lms.progress.repository.OrgRoadmapRepository;
import com.lms.progress.repository.OrgRoadmapResourceRepository;
import com.lms.progress.repository.RoadmapTemplateNodeRepository;
import com.lms.progress.repository.RoadmapTemplateRepository;
import com.lms.progress.repository.RoadmapTemplateResourceRepository;
import com.lms.progress.repository.UserRoadmapProgressRepository;
import com.lms.progress.security.JwtUtil;

/**
 * Single concrete service for the roadmap feature: templates (global, SUPER_ADMIN
 * authored), org roadmaps (cloned from templates or built from scratch, managed by
 * org admins / super-admin), trainer content authoring, and student progress
 * tracking.
 *
 * <p><b>Naming/convention assumptions made because Part 1 source was not visible to
 * this service:</b> getters/setters are assumed to follow standard JavaBean
 * convention. For {@code boolean} (primitive) entity fields such as
 * {@code isPublished}, {@code isArchived}, {@code isOptional}, {@code hasQuiz},
 * {@code hasProject}, {@code isFeatured}, this code calls {@code isX()} /
 * {@code setX(boolean)} (e.g. {@code isPublished()} / {@code setPublished(boolean)}).
 * For {@code Boolean} (wrapper, nullable) DTO fields used for partial patches, this
 * code calls {@code getIsX()} (e.g. {@code getIsPublished()}). If your generated
 * Part 1 code used different accessor names, a project-wide find/replace should be
 * all that's needed — the logic does not depend on the exact accessor spelling.
 *
 * <p><b>Known spec gap:</b> {@code OrgRoadmap} (as defined in Part 1) has no
 * {@code version} field — only {@code RoadmapTemplate} does. The "increment version
 * on edit of an already-published entity" rule is therefore implemented for
 * templates only; see the comment on {@link #bumpVersionIfWasPublished} and its
 * absence from the org-roadmap edit methods.
 */
@Service
public class RoadmapService {

    private final RoadmapTemplateRepository templateRepository;
    private final RoadmapTemplateNodeRepository templateNodeRepository;
    private final RoadmapTemplateResourceRepository templateResourceRepository;
    private final OrgRoadmapRepository orgRoadmapRepository;
    private final OrgRoadmapNodeRepository orgRoadmapNodeRepository;
    private final OrgRoadmapResourceRepository orgRoadmapResourceRepository;
    private final UserRoadmapProgressRepository userRoadmapProgressRepository;
    private final JwtUtil jwtUtil;

    // ASSUMPTION (see BatchEnrollment*): isolated, read-only, does not touch any
    // existing student_trainer_batch_map-style repository.
    private final BatchEnrollmentRepository batchEnrollmentRepository;

    public RoadmapService(RoadmapTemplateRepository templateRepository,
                           RoadmapTemplateNodeRepository templateNodeRepository,
                           RoadmapTemplateResourceRepository templateResourceRepository,
                           OrgRoadmapRepository orgRoadmapRepository,
                           OrgRoadmapNodeRepository orgRoadmapNodeRepository,
                           OrgRoadmapResourceRepository orgRoadmapResourceRepository,
                           UserRoadmapProgressRepository userRoadmapProgressRepository,
                           JwtUtil jwtUtil,
                           BatchEnrollmentRepository batchEnrollmentRepository) {
        this.templateRepository = templateRepository;
        this.templateNodeRepository = templateNodeRepository;
        this.templateResourceRepository = templateResourceRepository;
        this.orgRoadmapRepository = orgRoadmapRepository;
        this.orgRoadmapNodeRepository = orgRoadmapNodeRepository;
        this.orgRoadmapResourceRepository = orgRoadmapResourceRepository;
        this.userRoadmapProgressRepository = userRoadmapProgressRepository;
        this.jwtUtil = jwtUtil;
        this.batchEnrollmentRepository = batchEnrollmentRepository;
    }

    // =====================================================================================
    // Authorization helpers
    // =====================================================================================

    private void requireSuperAdmin(String callerRole) {
        if (!jwtUtil.isSuperAdmin(callerRole)) {
            throw new InsufficientRoleException("Only SUPER_ADMIN may manage global roadmap templates.");
        }
    }

    /**
     * Implements the authorization rule stated in the task spec:
     * <ul>
     *   <li>target orgId != null -&gt; ADMIN/TENANT_ADMIN whose JWT organizationId matches it, or SUPER_ADMIN (always)</li>
     *   <li>target orgId == null -&gt; SUPER_ADMIN only, even if caller's own JWT organizationId is also null</li>
     * </ul>
     */
    private void authorizeOrgRoadmapAccess(String targetOrgId, String callerRole, String callerOrgId) {
        if (jwtUtil.isSuperAdmin(callerRole)) {
            return;
        }
        if (jwtUtil.isOrgAdminRole(callerRole)) {
            if (targetOrgId == null) {
                throw new CrossOrgAccessDeniedException(
                        "Null-org roadmaps may only be managed by SUPER_ADMIN, not ADMIN/TENANT_ADMIN.");
            }
            if (!targetOrgId.equals(callerOrgId)) {
                throw new CrossOrgAccessDeniedException(
                        "Caller's organizationId does not match the target roadmap's orgId.");
            }
            return;
        }
        throw new InsufficientRoleException(
                "Role " + callerRole + " is not authorized to manage org roadmaps.");
    }

    private void authorizeTrainerOwnership(OrgRoadmap roadmap, Long trainerUserId) {
        if (roadmap.getCreatedBy() == null || !roadmap.getCreatedBy().equals(trainerUserId)) {
            throw new OwnershipViolationException(
                    "Trainer " + trainerUserId + " does not own OrgRoadmap " + roadmap.getId() + ".");
        }
    }

    // =====================================================================================
    // Slug generation
    // =====================================================================================

    private String slugify(String title) {
        if (title == null) {
            return "roadmap";
        }
        String lowered = title.toLowerCase();
        String hyphenated = lowered.trim().replaceAll("\\s+", "-");
        String stripped = hyphenated.replaceAll("[^a-z0-9-]", "");
        String collapsed = stripped.replaceAll("-{2,}", "-").replaceAll("^-+|-+$", "");
        return collapsed.isEmpty() ? "roadmap" : collapsed;
    }

    private String generateUniqueTemplateSlug(String title) {
        String base = slugify(title);
        String candidate = base;
        int suffix = 2;
        while (templateRepository.existsBySlug(candidate)) {
            candidate = base + "-" + suffix;
            suffix++;
        }
        return candidate;
    }

    private String generateUniqueOrgRoadmapSlug(String title, String orgId) {
        String base = slugify(title);
        String candidate = base;
        int suffix = 2;
        while (orgId == null
                ? orgRoadmapRepository.existsBySlugAndOrgIdIsNull(candidate)
                : orgRoadmapRepository.existsBySlugAndOrgId(candidate, orgId)) {
            candidate = base + "-" + suffix;
            suffix++;
        }
        return candidate;
    }

    // =====================================================================================
    // Cycle detection (shared DFS over a childId -> parentIds adjacency map)
    // =====================================================================================

    /**
     * Generic cycle check: builds the proposed adjacency (existing graph + the one
     * node being created/updated), then runs DFS-with-recursion-stack from every
     * node. If any node is revisited while still on the current DFS stack, a cycle
     * exists in the proposed graph and we throw without persisting anything.
     */
    private void assertNoCycle(Map<Long, List<Long>> childToParents, String graphDescription) {
        Set<Long> visited = new HashSet<>();
        Set<Long> inStack = new HashSet<>();
        for (Long start : childToParents.keySet()) {
            if (hasCycleDfs(start, childToParents, visited, inStack)) {
                throw new RoadmapCycleException("Cycle detected in " + graphDescription + " node graph.");
            }
        }
    }

    private boolean hasCycleDfs(Long node, Map<Long, List<Long>> childToParents,
                                 Set<Long> visited, Set<Long> inStack) {
        if (inStack.contains(node)) {
            return true;
        }
        if (visited.contains(node)) {
            return false;
        }
        visited.add(node);
        inStack.add(node);
        List<Long> parents = childToParents.get(node);
        if (parents != null) {
            for (Long parent : parents) {
                if (hasCycleDfs(parent, childToParents, visited, inStack)) {
                    return true;
                }
            }
        }
        inStack.remove(node);
        return false;
    }

    // Sentinel key used to represent a not-yet-persisted node during create-time
    // cycle checks (cannot collide with a real DB id).
    private static final Long NEW_NODE_SENTINEL_ID = -1L;

    private void assertNoCycleForTemplateNode(Long templateId, Long nodeIdBeingSaved, List<Long> proposedParentIds) {
        List<RoadmapTemplateNode> allNodes = templateNodeRepository.findByTemplateId(templateId);
        Map<Long, List<Long>> childToParents = new HashMap<>();
        for (RoadmapTemplateNode node : allNodes) {
            List<Long> parents = node.getParentNodeIds();
            childToParents.put(node.getId(), parents == null ? new ArrayList<>() : new ArrayList<>(parents));
        }
        childToParents.put(nodeIdBeingSaved,
                proposedParentIds == null ? new ArrayList<>() : new ArrayList<>(proposedParentIds));
        assertNoCycle(childToParents, "template " + templateId);
    }

    private void assertNoCycleForOrgNode(Long orgRoadmapId, Long nodeIdBeingSaved, List<Long> proposedParentIds) {
        List<OrgRoadmapNode> allNodes = orgRoadmapNodeRepository.findByOrgRoadmapId(orgRoadmapId);
        Map<Long, List<Long>> childToParents = new HashMap<>();
        for (OrgRoadmapNode node : allNodes) {
            List<Long> parents = node.getParentNodeIds();
            childToParents.put(node.getId(), parents == null ? new ArrayList<>() : new ArrayList<>(parents));
        }
        childToParents.put(nodeIdBeingSaved,
                proposedParentIds == null ? new ArrayList<>() : new ArrayList<>(proposedParentIds));
        assertNoCycle(childToParents, "org roadmap " + orgRoadmapId);
    }

    // =====================================================================================
    // Versioning
    // =====================================================================================

    /**
     * Templates only — see class-level javadoc re: OrgRoadmap having no version
     * field. Call BEFORE mutating isPublished on the entity, since the rule is
     * keyed off whether the entity WAS published prior to this edit.
     */
    private void bumpVersionIfWasPublished(RoadmapTemplate template) {
        if (template.isPublished()) {
            Integer current = template.getVersion();
            template.setVersion(current == null ? 2 : current + 1);
        }
    }

    // =====================================================================================
    // Small find-or-throw helpers
    // =====================================================================================

    private RoadmapTemplate findTemplateOrThrow(Long templateId) {
        return templateRepository.findById(templateId)
                .orElseThrow(() -> new RoadmapNotFoundException("RoadmapTemplate " + templateId + " not found."));
    }

    private RoadmapTemplateNode findTemplateNodeOrThrow(Long nodeId) {
        return templateNodeRepository.findById(nodeId)
                .orElseThrow(() -> new RoadmapNotFoundException("RoadmapTemplateNode " + nodeId + " not found."));
    }

    private RoadmapTemplateResource findTemplateResourceOrThrow(Long resourceId) {
        return templateResourceRepository.findById(resourceId)
                .orElseThrow(() -> new RoadmapNotFoundException("RoadmapTemplateResource " + resourceId + " not found."));
    }

    private OrgRoadmap findOrgRoadmapOrThrow(Long orgRoadmapId) {
        return orgRoadmapRepository.findById(orgRoadmapId)
                .orElseThrow(() -> new RoadmapNotFoundException("OrgRoadmap " + orgRoadmapId + " not found."));
    }

    private OrgRoadmapNode findOrgNodeOrThrow(Long nodeId) {
        return orgRoadmapNodeRepository.findById(nodeId)
                .orElseThrow(() -> new RoadmapNotFoundException("OrgRoadmapNode " + nodeId + " not found."));
    }

    private OrgRoadmapResource findOrgResourceOrThrow(Long resourceId) {
        return orgRoadmapResourceRepository.findById(resourceId)
                .orElseThrow(() -> new RoadmapNotFoundException("OrgRoadmapResource " + resourceId + " not found."));
    }

    private <T> T saveWithDuplicateSlugGuard(java.util.function.Supplier<T> saveCall, String entityDescription) {
        try {
            return saveCall.get();
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateSlugException(
                    "Slug collision while saving " + entityDescription + " despite server-side dedup.", ex);
        }
    }

    // Built via setters rather than an assumed all-args constructor, for consistency
    // with how every other DTO in this file is populated.
    private <T> PagedResponse<T> buildPagedResponse(List<T> content, int pageNumber, int pageSize,
                                                      long totalElements, int totalPages) {
        PagedResponse<T> response = new PagedResponse<>();
        response.setContent(content);
        response.setPageNumber(pageNumber);
        response.setPageSize(pageSize);
        response.setTotalElements(totalElements);
        response.setTotalPages(totalPages);
        return response;
    }

    // =====================================================================================
    // Template methods (SUPER_ADMIN only)
    // =====================================================================================

    public TemplateResponse createTemplate(CreateTemplateRequest request, String callerRole, Long callerUserId) {
        requireSuperAdmin(callerRole);

        RoadmapTemplate template = new RoadmapTemplate();
        template.setTitle(request.getTitle());
        template.setSlug(generateUniqueTemplateSlug(request.getTitle()));
        template.setDescription(request.getDescription());
        template.setCategory(request.getCategory());
        template.setThumbnailUrl(request.getThumbnailUrl());
        template.setPublished(false);
        template.setArchived(false);
        template.setVersion(1);
        template.setTotalNodes(0);
        template.setCreatedBy(callerUserId);
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());

        RoadmapTemplate saved = saveWithDuplicateSlugGuard(
                () -> templateRepository.save(template), "RoadmapTemplate");
        return toTemplateResponse(saved);
    }

    public TemplateResponse updateTemplate(Long templateId, UpdateTemplateRequest request, String callerRole) {
        requireSuperAdmin(callerRole);
        RoadmapTemplate template = findTemplateOrThrow(templateId);

        // Version bump is keyed on state BEFORE this edit is applied.
        bumpVersionIfWasPublished(template);

        if (request.getTitle() != null) {
            template.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            template.setDescription(request.getDescription());
        }
        if (request.getCategory() != null) {
            template.setCategory(request.getCategory());
        }
        if (request.getThumbnailUrl() != null) {
            template.setThumbnailUrl(request.getThumbnailUrl());
        }
        if (request.getIsPublished() != null) {
            template.setPublished(request.getIsPublished());
        }
        if (request.getIsArchived() != null) {
            template.setArchived(request.getIsArchived());
        }
        template.setUpdatedAt(LocalDateTime.now());

        RoadmapTemplate saved = templateRepository.save(template);
        return toTemplateResponse(saved);
    }

    public TemplateResponse getTemplate(Long templateId, String callerRole) {
        requireSuperAdmin(callerRole);
        return toTemplateResponse(findTemplateOrThrow(templateId));
    }

    public PagedResponse<TemplateResponse> listPublishedTemplates(Pageable pageable, String callerRole) {
        if (!jwtUtil.isSuperAdmin(callerRole) && !jwtUtil.isOrgAdminRole(callerRole)) {
            throw new InsufficientRoleException(
                    "Role " + callerRole + " is not authorized to view roadmap templates.");
        }
        Page<RoadmapTemplate> page = templateRepository.findByIsPublishedTrueAndIsArchivedFalse(pageable);
        List<TemplateResponse> content = page.getContent().stream()
                .map(this::toTemplateResponse)
                .collect(Collectors.toList());
        return buildPagedResponse(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }

    @Transactional
    public TemplateNodeResponse createTemplateNode(CreateTemplateNodeRequest request, String callerRole) {
        requireSuperAdmin(callerRole);
        RoadmapTemplate template = findTemplateOrThrow(request.getTemplateId());

        assertNoCycleForTemplateNode(request.getTemplateId(), NEW_NODE_SENTINEL_ID, request.getParentNodeIds());

        RoadmapTemplateNode node = new RoadmapTemplateNode();
        node.setTemplateId(request.getTemplateId());
        node.setTitle(request.getTitle());
        node.setDescription(request.getDescription());
        node.setType(request.getType());
        node.setPositionX(request.getPositionX());
        node.setPositionY(request.getPositionY());
        node.setOptional(request.isOptional());
        node.setEstimatedHours(request.getEstimatedHours());
        node.setOrderIndex(request.getOrderIndex());
        node.setHasQuiz(request.isHasQuiz());
        node.setHasProject(request.isHasProject());
        node.setParentNodeIds(request.getParentNodeIds());
        node.setCreatedAt(LocalDateTime.now());
        node.setUpdatedAt(LocalDateTime.now());

        RoadmapTemplateNode saved = templateNodeRepository.save(node);

        template.setTotalNodes(template.getTotalNodes() == null ? 1 : template.getTotalNodes() + 1);
        bumpVersionIfWasPublished(template);
        template.setUpdatedAt(LocalDateTime.now());
        templateRepository.save(template);

        return toTemplateNodeResponse(saved);
    }

    @Transactional
    public TemplateNodeResponse updateTemplateNode(Long nodeId, UpdateTemplateNodeRequest request, String callerRole) {
        requireSuperAdmin(callerRole);
        RoadmapTemplateNode node = findTemplateNodeOrThrow(nodeId);
        RoadmapTemplate template = findTemplateOrThrow(node.getTemplateId());

        if (request.getParentNodeIds() != null) {
            assertNoCycleForTemplateNode(node.getTemplateId(), nodeId, request.getParentNodeIds());
            node.setParentNodeIds(request.getParentNodeIds());
        }
        if (request.getTitle() != null) {
            node.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            node.setDescription(request.getDescription());
        }
        if (request.getType() != null) {
            node.setType(request.getType());
        }
        if (request.getPositionX() != null) {
            node.setPositionX(request.getPositionX());
        }
        if (request.getPositionY() != null) {
            node.setPositionY(request.getPositionY());
        }
        if (request.getIsOptional() != null) {
            node.setOptional(request.getIsOptional());
        }
        if (request.getEstimatedHours() != null) {
            node.setEstimatedHours(request.getEstimatedHours());
        }
        if (request.getOrderIndex() != null) {
            node.setOrderIndex(request.getOrderIndex());
        }
        if (request.getHasQuiz() != null) {
            node.setHasQuiz(request.getHasQuiz());
        }
        if (request.getHasProject() != null) {
            node.setHasProject(request.getHasProject());
        }
        node.setUpdatedAt(LocalDateTime.now());
        RoadmapTemplateNode saved = templateNodeRepository.save(node);

        bumpVersionIfWasPublished(template);
        template.setUpdatedAt(LocalDateTime.now());
        templateRepository.save(template);

        return toTemplateNodeResponse(saved);
    }

    public void deleteTemplateNode(Long nodeId, String callerRole) {
        requireSuperAdmin(callerRole);
        RoadmapTemplateNode node = findTemplateNodeOrThrow(nodeId);
        RoadmapTemplate template = findTemplateOrThrow(node.getTemplateId());

        // Cascade cleanup: remove this node's resources.
        List<RoadmapTemplateResource> resources = templateResourceRepository.findByNodeId(nodeId);
        templateResourceRepository.deleteAll(resources);

        // Keep the graph consistent: strip this node out of any sibling's parentNodeIds.
        List<RoadmapTemplateNode> siblings = templateNodeRepository.findByTemplateId(node.getTemplateId());
        for (RoadmapTemplateNode sibling : siblings) {
            List<Long> parents = sibling.getParentNodeIds();
            if (parents != null && parents.remove(nodeId)) {
                sibling.setParentNodeIds(parents);
                sibling.setUpdatedAt(LocalDateTime.now());
                templateNodeRepository.save(sibling);
            }
        }

        templateNodeRepository.delete(node);

        int currentTotal = template.getTotalNodes() == null ? 0 : template.getTotalNodes();
        template.setTotalNodes(Math.max(0, currentTotal - 1));
        bumpVersionIfWasPublished(template);
        template.setUpdatedAt(LocalDateTime.now());
        templateRepository.save(template);
    }

    public TemplateResourceResponse createTemplateResource(CreateTemplateResourceRequest request, String callerRole, Long callerUserId) {
        requireSuperAdmin(callerRole);
        // Validate parent node exists.
        findTemplateNodeOrThrow(request.getNodeId());

        RoadmapTemplateResource resource = new RoadmapTemplateResource();
        resource.setNodeId(request.getNodeId());
        resource.setType(request.getType());
        resource.setTitle(request.getTitle());
        resource.setUrl(request.getUrl());
        resource.setDescription(request.getDescription());
        resource.setDurationMinutes(request.getDurationMinutes());
        resource.setDifficulty(request.getDifficulty());
        resource.setUpvotes(0);
        resource.setFeatured(false);
        resource.setAddedBy(callerUserId);
        resource.setCreatedAt(LocalDateTime.now());

        RoadmapTemplateResource saved = templateResourceRepository.save(resource);
        return toTemplateResourceResponse(saved);
    }
    public TemplateResourceResponse updateTemplateResource(Long resourceId, UpdateTemplateResourceRequest request, String callerRole) {
        requireSuperAdmin(callerRole);
        RoadmapTemplateResource resource = findTemplateResourceOrThrow(resourceId);

        if (request.getType() != null) {
            resource.setType(request.getType());
        }
        if (request.getTitle() != null) {
            resource.setTitle(request.getTitle());
        }
        if (request.getUrl() != null) {
            resource.setUrl(request.getUrl());
        }
        if (request.getDescription() != null) {
            resource.setDescription(request.getDescription());
        }
        if (request.getDurationMinutes() != null) {
            resource.setDurationMinutes(request.getDurationMinutes());
        }
        if (request.getDifficulty() != null) {
            resource.setDifficulty(request.getDifficulty());
        }

        RoadmapTemplateResource saved = templateResourceRepository.save(resource);
        return toTemplateResourceResponse(saved);
    }

    public void deleteTemplateResource(Long resourceId, String callerRole) {
        requireSuperAdmin(callerRole);
        RoadmapTemplateResource resource = findTemplateResourceOrThrow(resourceId);
        templateResourceRepository.delete(resource);
    }

    public TemplateResponse publishTemplate(Long templateId, Long callerUserId, String callerRole) {
        requireSuperAdmin(callerRole);
        RoadmapTemplate template = findTemplateOrThrow(templateId);

        // Publishing itself does not touch version — only edits to an
        // already-published template do.
        template.setPublished(true);
        template.setPublishedAt(LocalDateTime.now());
        template.setPublishedBy(callerUserId);
        template.setUpdatedAt(LocalDateTime.now());

        RoadmapTemplate saved = templateRepository.save(template);
        return toTemplateResponse(saved);
    }

    /**
     * NEW (Part 2D, item 4): full node+resource graph for a template, for the
     * Super Admin editor. Unlike getRoadmapGraphForStudent, there is no
     * published-only restriction (drafts must be editable) and no per-user
     * progress merging (templates have no per-user progress).
     */
    public TemplateGraphResponse getTemplateGraphForEditing(Long templateId, String callerRole) {
        requireSuperAdmin(callerRole);
        RoadmapTemplate template = findTemplateOrThrow(templateId);

        List<RoadmapTemplateNode> nodes = templateNodeRepository.findByTemplateId(templateId);
        nodes.sort(Comparator.comparing(n -> n.getOrderIndex() == null ? Integer.MAX_VALUE : n.getOrderIndex()));

        List<Long> nodeIds = nodes.stream().map(RoadmapTemplateNode::getId).collect(Collectors.toList());
        List<RoadmapTemplateResource> allResources = nodeIds.isEmpty()
                ? new ArrayList<>()
                : templateResourceRepository.findByNodeIdIn(nodeIds);
        Map<Long, List<RoadmapTemplateResource>> resourcesByNodeId = allResources.stream()
                .collect(Collectors.groupingBy(RoadmapTemplateResource::getNodeId));

        List<TemplateNodeWithResourcesResponse> nodeResponses = new ArrayList<>();
        List<GraphEdgeResponse> edges = new ArrayList<>();

        for (RoadmapTemplateNode node : nodes) {
            TemplateNodeResponse base = toTemplateNodeResponse(node);
            TemplateNodeWithResourcesResponse withResources = new TemplateNodeWithResourcesResponse();
            withResources.setId(base.getId());
            withResources.setTemplateId(base.getTemplateId());
            withResources.setTitle(base.getTitle());
            withResources.setDescription(base.getDescription());
            withResources.setType(base.getType());
            withResources.setPositionX(base.getPositionX());
            withResources.setPositionY(base.getPositionY());
            withResources.setOptional(base.isOptional());   // not setIsOptional  // NOT isIsOptional()
            withResources.setEstimatedHours(base.getEstimatedHours());
            withResources.setOrderIndex(base.getOrderIndex());
            withResources.setHasQuiz(base.isHasQuiz());
            withResources.setHasProject(base.isHasProject());
            withResources.setParentNodeIds(base.getParentNodeIds());
            withResources.setCreatedAt(base.getCreatedAt());
            withResources.setUpdatedAt(base.getUpdatedAt());

            List<TemplateResourceResponse> resourceResponses = resourcesByNodeId
                    .getOrDefault(node.getId(), new ArrayList<>()).stream()
                    .map(this::toTemplateResourceResponse)
                    .collect(Collectors.toList());
            withResources.setResources(resourceResponses);
            nodeResponses.add(withResources);

            List<Long> parentIds = node.getParentNodeIds();
            if (parentIds != null) {
                for (Long parentId : parentIds) {
                    GraphEdgeResponse edge = new GraphEdgeResponse();
                    edge.setFromNodeId(parentId);
                    edge.setToNodeId(node.getId());
                    edges.add(edge);
                }
            }
        }

        TemplateGraphResponse response = new TemplateGraphResponse();
        response.setTemplateId(template.getId());
        response.setTitle(template.getTitle());
        response.setSlug(template.getSlug());
        response.setNodes(nodeResponses);
        response.setEdges(edges);
        return response;
    }
    // =====================================================================================
    // Org roadmap methods (Admin/TenantAdmin own-org, or SuperAdmin any-org/null-org)
    // =====================================================================================

    @Transactional
    public OrgRoadmapResponse cloneTemplate(CloneTemplateRequest request, String callerRole,
                                             Long callerUserId, String callerOrgId) {
        authorizeOrgRoadmapAccess(request.getOrgId(), callerRole, callerOrgId);
        RoadmapTemplate sourceTemplate = findTemplateOrThrow(request.getSourceTemplateId());

        OrgRoadmap roadmap = new OrgRoadmap();
        roadmap.setOrgId(request.getOrgId());
        roadmap.setSourceTemplateId(sourceTemplate.getId());
        roadmap.setTitle(request.getTitle() != null ? request.getTitle() : sourceTemplate.getTitle());
        // Client-supplied slug is always ignored, regardless of whether it was set.
        roadmap.setSlug(generateUniqueOrgRoadmapSlug(roadmap.getTitle(), request.getOrgId()));
        roadmap.setDescription(request.getDescription() != null ? request.getDescription() : sourceTemplate.getDescription());
        roadmap.setCategory(request.getCategory() != null ? request.getCategory() : sourceTemplate.getCategory());
        roadmap.setThumbnailUrl(request.getThumbnailUrl() != null ? request.getThumbnailUrl() : sourceTemplate.getThumbnailUrl());
        roadmap.setPublished(false);
        roadmap.setArchived(false);
        roadmap.setTotalNodes(sourceTemplate.getTotalNodes());
        roadmap.setTotalStudents(0);
        roadmap.setCreatedBy(callerUserId);
        roadmap.setCreatedAt(LocalDateTime.now());
        roadmap.setUpdatedAt(LocalDateTime.now());

        OrgRoadmap savedRoadmap = saveWithDuplicateSlugGuard(
                () -> orgRoadmapRepository.save(roadmap), "OrgRoadmap");

        cloneTemplateNodesInto(sourceTemplate.getId(), savedRoadmap.getId(), callerUserId);

        return toOrgRoadmapResponse(savedRoadmap);
    }

    /**
     * Two-pass copy: first create every node with no parents (to obtain new ids),
     * then re-save each with parentNodeIds remapped from old template-node ids to
     * new org-node ids. Also copies each node's resources.
     *
     * <p>UPDATED (Part 2D, item 9): resource lookups are now a single batch
     * {@code findByNodeIdIn} call instead of one {@code findByNodeId} query per
     * node — matters once roadmaps approach ~1000 nodes.
     */
    private void cloneTemplateNodesInto(Long sourceTemplateId, Long newOrgRoadmapId, Long callerUserId) {
        List<RoadmapTemplateNode> sourceNodes = templateNodeRepository.findByTemplateId(sourceTemplateId);
        Map<Long, Long> oldIdToNewId = new HashMap<>();

        for (RoadmapTemplateNode sourceNode : sourceNodes) {
            OrgRoadmapNode newNode = new OrgRoadmapNode();
            newNode.setOrgRoadmapId(newOrgRoadmapId);
            newNode.setSourceNodeId(sourceNode.getId());
            newNode.setTitle(sourceNode.getTitle());
            newNode.setDescription(sourceNode.getDescription());
            newNode.setType(sourceNode.getType());
            newNode.setPositionX(sourceNode.getPositionX());
            newNode.setPositionY(sourceNode.getPositionY());
            newNode.setOptional(sourceNode.isOptional());
            newNode.setEstimatedHours(sourceNode.getEstimatedHours());
            newNode.setOrderIndex(sourceNode.getOrderIndex());
            newNode.setHasQuiz(sourceNode.isHasQuiz());
            newNode.setHasProject(sourceNode.isHasProject());
            newNode.setParentNodeIds(new ArrayList<>());
            newNode.setCreatedAt(LocalDateTime.now());
            newNode.setUpdatedAt(LocalDateTime.now());
            OrgRoadmapNode saved = orgRoadmapNodeRepository.save(newNode);
            oldIdToNewId.put(sourceNode.getId(), saved.getId());
        }

        // Batch-fetch all resources for every source node in one query instead of
        // one query per node (was: per-node findByNodeId in a loop).
        List<Long> sourceNodeIds = sourceNodes.stream()
                .map(RoadmapTemplateNode::getId)
                .collect(Collectors.toList());
        List<RoadmapTemplateResource> allSourceResources = sourceNodeIds.isEmpty()
                ? new ArrayList<>()
                : templateResourceRepository.findByNodeIdIn(sourceNodeIds);
        Map<Long, List<RoadmapTemplateResource>> resourcesBySourceNodeId = allSourceResources.stream()
                .collect(Collectors.groupingBy(RoadmapTemplateResource::getNodeId));

        for (RoadmapTemplateNode sourceNode : sourceNodes) {
            List<Long> sourceParents = sourceNode.getParentNodeIds();
            Long newNodeId = oldIdToNewId.get(sourceNode.getId());

            if (sourceParents != null && !sourceParents.isEmpty()) {
                List<Long> remappedParents = sourceParents.stream()
                        .map(oldIdToNewId::get)
                        .filter(java.util.Objects::nonNull)
                        .collect(Collectors.toList());
                OrgRoadmapNode newNode = findOrgNodeOrThrow(newNodeId);
                newNode.setParentNodeIds(remappedParents);
                newNode.setUpdatedAt(LocalDateTime.now());
                orgRoadmapNodeRepository.save(newNode);
            }

            List<RoadmapTemplateResource> sourceResources =
                    resourcesBySourceNodeId.getOrDefault(sourceNode.getId(), new ArrayList<>());
            for (RoadmapTemplateResource sourceResource : sourceResources) {
                OrgRoadmapResource newResource = new OrgRoadmapResource();
                newResource.setNodeId(newNodeId);
                newResource.setType(sourceResource.getType());
                newResource.setTitle(sourceResource.getTitle());
                newResource.setUrl(sourceResource.getUrl());
                newResource.setDescription(sourceResource.getDescription());
                newResource.setDurationMinutes(sourceResource.getDurationMinutes());
                newResource.setDifficulty(sourceResource.getDifficulty());
                newResource.setUpvotes(0);
                newResource.setFeatured(false);
                newResource.setAddedBy(callerUserId);
                newResource.setCreatedAt(LocalDateTime.now());
                orgRoadmapResourceRepository.save(newResource);
            }
        }
    }

    public OrgRoadmapResponse createCustomOrgRoadmap(CreateCustomOrgRoadmapRequest request, String callerRole,
                                                       Long callerUserId, String callerOrgId) {
        authorizeOrgRoadmapAccess(request.getOrgId(), callerRole, callerOrgId);

        OrgRoadmap roadmap = new OrgRoadmap();
        roadmap.setOrgId(request.getOrgId());
        roadmap.setSourceTemplateId(null);
        roadmap.setTitle(request.getTitle());
        // Client-supplied slug is always ignored.
        roadmap.setSlug(generateUniqueOrgRoadmapSlug(request.getTitle(), request.getOrgId()));
        roadmap.setDescription(request.getDescription());
        roadmap.setCategory(request.getCategory());
        roadmap.setThumbnailUrl(request.getThumbnailUrl());
        roadmap.setPublished(false);
        roadmap.setArchived(false);
        roadmap.setTotalNodes(0);
        roadmap.setTotalStudents(0);
        roadmap.setCreatedBy(callerUserId);
        roadmap.setCreatedAt(LocalDateTime.now());
        roadmap.setUpdatedAt(LocalDateTime.now());

        OrgRoadmap saved = saveWithDuplicateSlugGuard(
                () -> orgRoadmapRepository.save(roadmap), "OrgRoadmap");
        return toOrgRoadmapResponse(saved);
    }

    public OrgRoadmapResponse updateOrgRoadmap(Long orgRoadmapId, UpdateOrgRoadmapRequest request,
                                                String callerRole, String callerOrgId) {
        OrgRoadmap roadmap = findOrgRoadmapOrThrow(orgRoadmapId);
        authorizeOrgRoadmapAccess(roadmap.getOrgId(), callerRole, callerOrgId);

        // NOTE: no version bump here — OrgRoadmap (per Part 1) has no version field.
        if (request.getTitle() != null) {
            roadmap.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            roadmap.setDescription(request.getDescription());
        }
        if (request.getCategory() != null) {
            roadmap.setCategory(request.getCategory());
        }
        if (request.getThumbnailUrl() != null) {
            roadmap.setThumbnailUrl(request.getThumbnailUrl());
        }
        if (request.getIsPublished() != null) {
            roadmap.setPublished(request.getIsPublished());
        }
        if (request.getIsArchived() != null) {
            roadmap.setArchived(request.getIsArchived());
        }
        roadmap.setUpdatedAt(LocalDateTime.now());

        OrgRoadmap saved = orgRoadmapRepository.save(roadmap);
        return toOrgRoadmapResponse(saved);
    }

    /**
     * UPDATED (Part 2D, item 6): previously this threw for a TRAINER caller even
     * on their own roadmap, since it only ever called authorizeOrgRoadmapAccess
     * (admin/super-admin only). A TRAINER whose createdBy matches callerUserId is
     * now allowed through, so the editor header/title can load. Signature gained
     * callerUserId; the SUPER_ADMIN / org-admin path is otherwise unchanged.
     */
    public OrgRoadmapResponse getOrgRoadmap(Long orgRoadmapId, String callerRole, Long callerUserId, String callerOrgId) {
        OrgRoadmap roadmap = findOrgRoadmapOrThrow(orgRoadmapId);

        if (jwtUtil.isSuperAdmin(callerRole)) {
            return toOrgRoadmapResponse(roadmap);
        }
        if (jwtUtil.isOrgAdminRole(callerRole)) {
            authorizeOrgRoadmapAccess(roadmap.getOrgId(), callerRole, callerOrgId);
            return toOrgRoadmapResponse(roadmap);
        }
        if ("TRAINER".equals(callerRole) && roadmap.getCreatedBy() != null
                && roadmap.getCreatedBy().equals(callerUserId)) {
            return toOrgRoadmapResponse(roadmap);
        }
        throw new InsufficientRoleException(
                "Role " + callerRole + " is not authorized to view OrgRoadmap " + orgRoadmapId + ".");
    }

    /**
     * NEW (Part 2D, item 5): full node+resource graph for an org roadmap, for the
     * trainer/org-admin editor. Broadens access beyond authorizeOrgRoadmapAccess:
     * a TRAINER who owns the roadmap (createdBy == callerUserId) is also allowed,
     * since trainers must be able to load their own roadmap to edit it. No
     * published-only restriction; no per-user progress merging.
     */
    public OrgRoadmapGraphResponse getOrgRoadmapGraphForEditing(Long orgRoadmapId, String callerRole,
            Long callerUserId, String callerOrgId) {
OrgRoadmap roadmap = findOrgRoadmapOrThrow(orgRoadmapId);

boolean authorized;
try {
authorizeOrgRoadmapAccess(roadmap.getOrgId(), callerRole, callerOrgId);
authorized = true;
} catch (RuntimeException ex) {
authorized = "TRAINER".equals(callerRole)
&& roadmap.getCreatedBy() != null
&& roadmap.getCreatedBy().equals(callerUserId);
if (!authorized) {
throw ex;
}
}

List<OrgRoadmapNode> nodes = orgRoadmapNodeRepository.findByOrgRoadmapId(orgRoadmapId);
nodes.sort(Comparator.comparing(n -> n.getOrderIndex() == null ? Integer.MAX_VALUE : n.getOrderIndex()));

List<Long> nodeIds = nodes.stream().map(OrgRoadmapNode::getId).collect(Collectors.toList());
List<OrgRoadmapResource> allResources = nodeIds.isEmpty()
? new ArrayList<>()
: orgRoadmapResourceRepository.findByNodeIdIn(nodeIds);
Map<Long, List<OrgRoadmapResource>> resourcesByNodeId = allResources.stream()
.collect(Collectors.groupingBy(OrgRoadmapResource::getNodeId));

List<OrgNodeWithResourcesResponse> nodeResponses = new ArrayList<>();
List<GraphEdgeResponse> edges = new ArrayList<>();

for (OrgRoadmapNode node : nodes) {
OrgNodeResponse base = toOrgNodeResponse(node);
OrgNodeWithResourcesResponse withResources = new OrgNodeWithResourcesResponse();
withResources.setId(base.getId());
withResources.setOrgRoadmapId(base.getOrgRoadmapId());
withResources.setSourceNodeId(base.getSourceNodeId());
withResources.setTitle(base.getTitle());
withResources.setDescription(base.getDescription());
withResources.setType(base.getType());
withResources.setPositionX(base.getPositionX());
withResources.setPositionY(base.getPositionY());
withResources.setOptional(base.isOptional());   // not setIsOptional  // NOT isIsOptional()
withResources.setEstimatedHours(base.getEstimatedHours());
withResources.setOrderIndex(base.getOrderIndex());
withResources.setHasQuiz(base.isHasQuiz());
withResources.setHasProject(base.isHasProject());
withResources.setParentNodeIds(base.getParentNodeIds());
withResources.setCreatedAt(base.getCreatedAt());
withResources.setUpdatedAt(base.getUpdatedAt());

List<OrgResourceResponse> resourceResponses = resourcesByNodeId
.getOrDefault(node.getId(), new ArrayList<>()).stream()
.map(this::toOrgResourceResponse)
.collect(Collectors.toList());
withResources.setResources(resourceResponses);
nodeResponses.add(withResources);

List<Long> parentIds = node.getParentNodeIds();
if (parentIds != null) {
for (Long parentId : parentIds) {
GraphEdgeResponse edge = new GraphEdgeResponse();
edge.setFromNodeId(parentId);
edge.setToNodeId(node.getId());
edges.add(edge);
}
}
}

OrgRoadmapGraphResponse response = new OrgRoadmapGraphResponse();
response.setOrgRoadmapId(roadmap.getId());
response.setOrgId(roadmap.getOrgId());
response.setTitle(roadmap.getTitle());
response.setSlug(roadmap.getSlug());
response.setNodes(nodeResponses);
response.setEdges(edges);
return response;
}
    /**
     * NOTE: the repository methods given only expose published+non-archived
     * listing (findByOrgIdAndIsPublishedTrueAndIsArchivedFalse /
     * findByOrgIdIsNullAndIsPublishedTrueAndIsArchivedFalse). A true admin
     * "manage everything including drafts" view would need an unfiltered
     * findByOrgId(Long, Pageable) — intentionally not added here since it wasn't
     * called out as strictly necessary; getOrgRoadmap/getRoadmapAnalytics cover
     * single-roadmap detail (including drafts) once the id is known.
     */
    public PagedResponse<RoadmapListItemResponse> listOrgRoadmaps(String orgId, boolean isSuperAdmin, Pageable pageable) {
        Page<OrgRoadmap> page = orgId == null
                ? orgRoadmapRepository.findByOrgIdIsNullAndIsPublishedTrueAndIsArchivedFalse(pageable)
                : orgRoadmapRepository.findByOrgIdAndIsPublishedTrueAndIsArchivedFalse(orgId, pageable);
        List<RoadmapListItemResponse> content = page.getContent().stream()
                .map(this::toRoadmapListItemResponse)
                .collect(Collectors.toList());
        return buildPagedResponse(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }

    /**
     * NEW (Part 2D, item 7): lets a trainer discover which OrgRoadmap ids they
     * own, including unpublished drafts. Uses the full toOrgRoadmapResponse
     * mapper (not the lightweight list-item mapper) since the trainer needs
     * isPublished/isArchived to manage drafts.
     */
    public PagedResponse<OrgRoadmapResponse> listMyOrgRoadmaps(Long callerUserId, Pageable pageable) {
        Page<OrgRoadmap> page = orgRoadmapRepository.findByCreatedBy(callerUserId, pageable);
        List<OrgRoadmapResponse> content = page.getContent().stream()
                .map(this::toOrgRoadmapResponse)
                .collect(Collectors.toList());
        return buildPagedResponse(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }

    public OrgRoadmapResponse publishOrgRoadmap(Long orgRoadmapId, Long callerUserId, String callerRole, String callerOrgId) {
        OrgRoadmap roadmap = findOrgRoadmapOrThrow(orgRoadmapId);
        authorizeOrgRoadmapAccess(roadmap.getOrgId(), callerRole, callerOrgId);

        roadmap.setPublished(true);
        roadmap.setPublishedAt(LocalDateTime.now());
        roadmap.setPublishedBy(callerUserId);
        roadmap.setUpdatedAt(LocalDateTime.now());

        OrgRoadmap saved = orgRoadmapRepository.save(roadmap);
        return toOrgRoadmapResponse(saved);
    }

    public OrgRoadmapResponse unpublishOrgRoadmap(Long orgRoadmapId, String callerRole, String callerOrgId) {
        OrgRoadmap roadmap = findOrgRoadmapOrThrow(orgRoadmapId);
        authorizeOrgRoadmapAccess(roadmap.getOrgId(), callerRole, callerOrgId);

        roadmap.setPublished(false);
        roadmap.setUpdatedAt(LocalDateTime.now());

        OrgRoadmap saved = orgRoadmapRepository.save(roadmap);
        return toOrgRoadmapResponse(saved);
    }

    // -------------------------------------------------------------------------------------
    // Analytics
    //
    // Depends on UserRoadmapProgressRepository#findByOrgRoadmapId(Long), which is present
    // on the repository interface. Everything else below is computed in-memory from that
    // one query plus the node list, to avoid needing further repository additions.
    // -------------------------------------------------------------------------------------

    public RoadmapAnalyticsResponse getRoadmapAnalytics(Long orgRoadmapId, String callerRole, String callerOrgId) {
        OrgRoadmap roadmap = findOrgRoadmapOrThrow(orgRoadmapId);
        authorizeOrgRoadmapAccess(roadmap.getOrgId(), callerRole, callerOrgId);

        List<OrgRoadmapNode> nodes = orgRoadmapNodeRepository.findByOrgRoadmapId(orgRoadmapId);
        nodes.sort(Comparator.comparing(n -> n.getOrderIndex() == null ? Integer.MAX_VALUE : n.getOrderIndex()));

        List<UserRoadmapProgress> allProgress = userRoadmapProgressRepository.findByOrgRoadmapId(orgRoadmapId);

        Map<Long, Map<Long, UserRoadmapProgress>> byStudentThenNode = new HashMap<>();
        for (UserRoadmapProgress progress : allProgress) {
            byStudentThenNode
                    .computeIfAbsent(progress.getUserId(), k -> new HashMap<>())
                    .put(progress.getNodeId(), progress);
        }

        Set<Long> nonOptionalNodeIds = nodes.stream()
                .filter(n -> !n.isOptional())
                .map(OrgRoadmapNode::getId)
                .collect(Collectors.toSet());

        int totalStudents = byStudentThenNode.size();

        // "Stuck" = a student's furthest-incomplete node in orderIndex order (first node,
        // walking in orderIndex order, whose status is not DONE; a missing progress row
        // counts as NOT_STARTED). Students with everything DONE contribute no stuck node.
        Map<Long, Integer> stuckCountByNode = new HashMap<>();
        Map<Long, Integer> notStartedCountByNode = new HashMap<>();
        Map<Long, Integer> inProgressCountByNode = new HashMap<>();
        Map<Long, Integer> doneCountByNode = new HashMap<>();
        Map<Long, Long> timeSpentSumByNode = new HashMap<>();
        Map<Long, Integer> timeSpentSampleCountByNode = new HashMap<>();

        int studentsFullyDone = 0;

        for (Map.Entry<Long, Map<Long, UserRoadmapProgress>> studentEntry : byStudentThenNode.entrySet()) {
            Map<Long, UserRoadmapProgress> nodeProgressForStudent = studentEntry.getValue();
            boolean allNonOptionalDone = true;
            Long stuckNodeId = null;

            for (OrgRoadmapNode node : nodes) {
                UserRoadmapProgress progress = nodeProgressForStudent.get(node.getId());
                NodeStatus status = progress != null ? progress.getStatus() : NodeStatus.NOT_STARTED;

                switch (status) {
                    case NOT_STARTED:
                        notStartedCountByNode.merge(node.getId(), 1, Integer::sum);
                        break;
                    case IN_PROGRESS:
                        inProgressCountByNode.merge(node.getId(), 1, Integer::sum);
                        break;
                    case DONE:
                        doneCountByNode.merge(node.getId(), 1, Integer::sum);
                        break;
                    case SKIPPED:
                        break;
                    default:
                        break;
                }

                if (progress != null && progress.getTimeSpentMinutes() != null) {
                    timeSpentSumByNode.merge(node.getId(), (long) progress.getTimeSpentMinutes(), Long::sum);
                    timeSpentSampleCountByNode.merge(node.getId(), 1, Integer::sum);
                }

                if (stuckNodeId == null && status != NodeStatus.DONE && status != NodeStatus.SKIPPED) {
                    stuckNodeId = node.getId();
                }
                if (nonOptionalNodeIds.contains(node.getId()) && status != NodeStatus.DONE) {
                    allNonOptionalDone = false;
                }
            }

            if (stuckNodeId != null) {
                stuckCountByNode.merge(stuckNodeId, 1, Integer::sum);
            }
            if (allNonOptionalDone) {
                studentsFullyDone++;
            }
        }

        double completionPercent = totalStudents == 0 ? 0.0 : (100.0 * studentsFullyDone / totalStudents);

        List<NodeBottleneckStat> bottleneckStats = new ArrayList<>();
        for (OrgRoadmapNode node : nodes) {
            long sum = timeSpentSumByNode.getOrDefault(node.getId(), 0L);
            int sampleCount = timeSpentSampleCountByNode.getOrDefault(node.getId(), 0);
            double averageTimeSpent = sampleCount == 0 ? 0.0 : ((double) sum / sampleCount);

            NodeBottleneckStat stat = new NodeBottleneckStat();
            stat.setNodeId(node.getId());
            stat.setNodeTitle(node.getTitle());
            stat.setStuckCount(stuckCountByNode.getOrDefault(node.getId(), 0));
            stat.setNotStartedCount(notStartedCountByNode.getOrDefault(node.getId(), 0));
            stat.setInProgressCount(inProgressCountByNode.getOrDefault(node.getId(), 0));
            stat.setDoneCount(doneCountByNode.getOrDefault(node.getId(), 0));
            stat.setAverageTimeSpentMinutes(averageTimeSpent);
            bottleneckStats.add(stat);
        }

        RoadmapAnalyticsResponse response = new RoadmapAnalyticsResponse();
        response.setOrgRoadmapId(orgRoadmapId);
        response.setOrgId(roadmap.getOrgId());
        response.setTotalStudents(totalStudents);
        response.setCompletionPercent(completionPercent);
        response.setNodeBottleneckStats(bottleneckStats);
        return response;
    }

    // =====================================================================================
    // Trainer methods (ownership-checked via createdBy == callerUserId on parent OrgRoadmap)
    // =====================================================================================

    @Transactional
    public OrgNodeResponse createOrgNodeAsTrainer(CreateOrgNodeRequest request, Long trainerUserId) {
        OrgRoadmap roadmap = findOrgRoadmapOrThrow(request.getOrgRoadmapId());
        authorizeTrainerOwnership(roadmap, trainerUserId);

        assertNoCycleForOrgNode(request.getOrgRoadmapId(), NEW_NODE_SENTINEL_ID, request.getParentNodeIds());

        OrgRoadmapNode node = new OrgRoadmapNode();
        node.setOrgRoadmapId(request.getOrgRoadmapId());
        node.setSourceNodeId(request.getSourceNodeId());
        node.setTitle(request.getTitle());
        node.setDescription(request.getDescription());
        node.setType(request.getType());
        node.setPositionX(request.getPositionX());
        node.setPositionY(request.getPositionY());
        node.setOptional(request.isOptional());
        node.setEstimatedHours(request.getEstimatedHours());
        node.setOrderIndex(request.getOrderIndex());
        node.setHasQuiz(request.isHasQuiz());
        node.setHasProject(request.isHasProject());
        node.setParentNodeIds(request.getParentNodeIds());
        node.setCreatedAt(LocalDateTime.now());
        node.setUpdatedAt(LocalDateTime.now());

        OrgRoadmapNode saved = orgRoadmapNodeRepository.save(node);

        roadmap.setTotalNodes(roadmap.getTotalNodes() == null ? 1 : roadmap.getTotalNodes() + 1);
        // NOTE: no version bump — OrgRoadmap has no version field (see class javadoc).
        roadmap.setUpdatedAt(LocalDateTime.now());
        orgRoadmapRepository.save(roadmap);

        return toOrgNodeResponse(saved);
    }

    @Transactional
    public OrgNodeResponse updateOrgNodeAsTrainer(Long nodeId, UpdateOrgNodeRequest request, Long trainerUserId) {
        OrgRoadmapNode node = findOrgNodeOrThrow(nodeId);
        OrgRoadmap roadmap = findOrgRoadmapOrThrow(node.getOrgRoadmapId());
        authorizeTrainerOwnership(roadmap, trainerUserId);

        if (request.getParentNodeIds() != null) {
            assertNoCycleForOrgNode(node.getOrgRoadmapId(), nodeId, request.getParentNodeIds());
            node.setParentNodeIds(request.getParentNodeIds());
        }
        if (request.getTitle() != null) {
            node.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            node.setDescription(request.getDescription());
        }
        if (request.getType() != null) {
            node.setType(request.getType());
        }
        if (request.getPositionX() != null) {
            node.setPositionX(request.getPositionX());
        }
        if (request.getPositionY() != null) {
            node.setPositionY(request.getPositionY());
        }
        if (request.getIsOptional() != null) {
            node.setOptional(request.getIsOptional());
        }
        if (request.getEstimatedHours() != null) {
            node.setEstimatedHours(request.getEstimatedHours());
        }
        if (request.getOrderIndex() != null) {
            node.setOrderIndex(request.getOrderIndex());
        }
        if (request.getHasQuiz() != null) {
            node.setHasQuiz(request.getHasQuiz());
        }
        if (request.getHasProject() != null) {
            node.setHasProject(request.getHasProject());
        }
        node.setUpdatedAt(LocalDateTime.now());

        OrgRoadmapNode saved = orgRoadmapNodeRepository.save(node);
        roadmap.setUpdatedAt(LocalDateTime.now());
        orgRoadmapRepository.save(roadmap);

        return toOrgNodeResponse(saved);
    }

    public void deleteOrgNodeAsTrainer(Long nodeId, Long trainerUserId) {
        OrgRoadmapNode node = findOrgNodeOrThrow(nodeId);
        OrgRoadmap roadmap = findOrgRoadmapOrThrow(node.getOrgRoadmapId());
        authorizeTrainerOwnership(roadmap, trainerUserId);

        List<OrgRoadmapResource> resources = orgRoadmapResourceRepository.findByNodeId(nodeId);
        orgRoadmapResourceRepository.deleteAll(resources);

        List<OrgRoadmapNode> siblings = orgRoadmapNodeRepository.findByOrgRoadmapId(node.getOrgRoadmapId());
        for (OrgRoadmapNode sibling : siblings) {
            List<Long> parents = sibling.getParentNodeIds();
            if (parents != null && parents.remove(nodeId)) {
                sibling.setParentNodeIds(parents);
                sibling.setUpdatedAt(LocalDateTime.now());
                orgRoadmapNodeRepository.save(sibling);
            }
        }

        orgRoadmapNodeRepository.delete(node);

        int currentTotal = roadmap.getTotalNodes() == null ? 0 : roadmap.getTotalNodes();
        roadmap.setTotalNodes(Math.max(0, currentTotal - 1));
        roadmap.setUpdatedAt(LocalDateTime.now());
        orgRoadmapRepository.save(roadmap);
    }

    public OrgResourceResponse createOrgResourceAsTrainer(CreateOrgResourceRequest request, Long trainerUserId) {
        OrgRoadmapNode node = findOrgNodeOrThrow(request.getNodeId());
        OrgRoadmap roadmap = findOrgRoadmapOrThrow(node.getOrgRoadmapId());
        authorizeTrainerOwnership(roadmap, trainerUserId);

        OrgRoadmapResource resource = new OrgRoadmapResource();
        resource.setNodeId(request.getNodeId());
        resource.setType(request.getType());
        resource.setTitle(request.getTitle());
        resource.setUrl(request.getUrl());
        resource.setDescription(request.getDescription());
        resource.setDurationMinutes(request.getDurationMinutes());
        resource.setDifficulty(request.getDifficulty());
        resource.setUpvotes(0);
        resource.setFeatured(false);
        resource.setAddedBy(trainerUserId);
        resource.setCreatedAt(LocalDateTime.now());

        OrgRoadmapResource saved = orgRoadmapResourceRepository.save(resource);
        return toOrgResourceResponse(saved);
    }

    public OrgResourceResponse updateOrgResourceAsTrainer(Long resourceId, UpdateOrgResourceRequest request, Long trainerUserId) {
        OrgRoadmapResource resource = findOrgResourceOrThrow(resourceId);
        OrgRoadmapNode node = findOrgNodeOrThrow(resource.getNodeId());
        OrgRoadmap roadmap = findOrgRoadmapOrThrow(node.getOrgRoadmapId());
        authorizeTrainerOwnership(roadmap, trainerUserId);

        if (request.getType() != null) {
            resource.setType(request.getType());
        }
        if (request.getTitle() != null) {
            resource.setTitle(request.getTitle());
        }
        if (request.getUrl() != null) {
            resource.setUrl(request.getUrl());
        }
        if (request.getDescription() != null) {
            resource.setDescription(request.getDescription());
        }
        if (request.getDurationMinutes() != null) {
            resource.setDurationMinutes(request.getDurationMinutes());
        }
        if (request.getDifficulty() != null) {
            resource.setDifficulty(request.getDifficulty());
        }

        OrgRoadmapResource saved = orgRoadmapResourceRepository.save(resource);
        return toOrgResourceResponse(saved);
    }

    public void deleteOrgResourceAsTrainer(Long resourceId, Long trainerUserId) {
        OrgRoadmapResource resource = findOrgResourceOrThrow(resourceId);
        OrgRoadmapNode node = findOrgNodeOrThrow(resource.getNodeId());
        OrgRoadmap roadmap = findOrgRoadmapOrThrow(node.getOrgRoadmapId());
        authorizeTrainerOwnership(roadmap, trainerUserId);

        orgRoadmapResourceRepository.delete(resource);
    }

    /**
     * Resolves "students assigned to this trainer" via the isolated
     * BatchEnrollment assumption query, then joins against
     * UserRoadmapProgressRepository#findByOrgRoadmapIdAndUserIn for this roadmap.
     * Pagination is applied in-memory since the enrollment/progress join isn't a
     * single paged repository query.
     */
    public PagedResponse<StudentProgressSummaryResponse> getStudentsProgress(Long orgRoadmapId, Long trainerUserId,
                                                                               Pageable pageable) {
        OrgRoadmap roadmap = findOrgRoadmapOrThrow(orgRoadmapId);
        authorizeTrainerOwnership(roadmap, trainerUserId);

        // ASSUMPTION: adjust table/column names to match actual enrollment schema.
        List<BatchEnrollment> enrollments = batchEnrollmentRepository.findByTrainerUserId(trainerUserId);
        List<Long> studentUserIds = enrollments.stream()
                .map(BatchEnrollment::getStudentUserId)
                .distinct()
                .collect(Collectors.toList());

        List<OrgRoadmapNode> nodes = orgRoadmapNodeRepository.findByOrgRoadmapId(orgRoadmapId);
        Set<Long> nonOptionalNodeIds = nodes.stream()
                .filter(n -> !n.isOptional())
                .map(OrgRoadmapNode::getId)
                .collect(Collectors.toSet());
        int nonOptionalTotal = nonOptionalNodeIds.size();

        List<UserRoadmapProgress> progressRows = studentUserIds.isEmpty()
                ? new ArrayList<>()
                : userRoadmapProgressRepository.findByOrgRoadmapIdAndUserIdIn(orgRoadmapId, studentUserIds);

        Map<Long, List<UserRoadmapProgress>> progressByStudent = progressRows.stream()
                .collect(Collectors.groupingBy(UserRoadmapProgress::getUserId));

        List<StudentProgressSummaryResponse> summaries = new ArrayList<>();
        for (Long studentUserId : studentUserIds) {
            List<UserRoadmapProgress> studentProgress = progressByStudent.getOrDefault(studentUserId, new ArrayList<>());

            long doneNonOptionalCount = studentProgress.stream()
                    .filter(p -> nonOptionalNodeIds.contains(p.getNodeId()) && p.getStatus() == NodeStatus.DONE)
                    .count();
            double completionPercent = nonOptionalTotal == 0 ? 0.0 : (100.0 * doneNonOptionalCount / nonOptionalTotal);

            LocalDateTime lastActiveAt = studentProgress.stream()
                    .map(UserRoadmapProgress::getLastAccessedAt)
                    .filter(java.util.Objects::nonNull)
                    .max(Comparator.naturalOrder())
                    .orElse(null);

            StudentProgressSummaryResponse summary = new StudentProgressSummaryResponse();
            summary.setStudentId(studentUserId);
            // ASSUMPTION: no user-profile lookup is available to this service; studentName
            // is left null. Wire in a UserRepository/UserService lookup if one exists.
            summary.setStudentName(null);
            summary.setOrgId(roadmap.getOrgId());
            summary.setOrgRoadmapId(orgRoadmapId);
            summary.setCompletionPercent(completionPercent);
            summary.setLastActiveAt(lastActiveAt);
            summaries.add(summary);
        }

        int pageSize = pageable.getPageSize();
        int pageNumber = pageable.getPageNumber();
        int fromIndex = Math.min(pageNumber * pageSize, summaries.size());
        int toIndex = Math.min(fromIndex + pageSize, summaries.size());
        List<StudentProgressSummaryResponse> pageContent = summaries.subList(fromIndex, toIndex);
        int totalPages = pageSize == 0 ? 0 : (int) Math.ceil((double) summaries.size() / pageSize);

        return buildPagedResponse(new ArrayList<>(pageContent), pageNumber, pageSize, summaries.size(), totalPages);
    }

    // =====================================================================================
    // Student methods
    // =====================================================================================

    public PagedResponse<RoadmapListItemResponse> listPublishedRoadmapsForStudent(String orgId, Pageable pageable) {
        Page<OrgRoadmap> page = orgId == null
                ? orgRoadmapRepository.findByOrgIdIsNullAndIsPublishedTrueAndIsArchivedFalse(pageable)
                : orgRoadmapRepository.findByOrgIdAndIsPublishedTrueAndIsArchivedFalse(orgId, pageable);
        List<RoadmapListItemResponse> content = page.getContent().stream()
                .map(this::toRoadmapListItemResponse)
                .collect(Collectors.toList());
        return buildPagedResponse(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }

    private OrgRoadmap findPublishedOrgRoadmapBySlugOrThrow(String slug, String orgId) {
        OrgRoadmap roadmap = (orgId == null
                ? orgRoadmapRepository.findBySlugAndOrgIdIsNull(slug)
                : orgRoadmapRepository.findBySlugAndOrgId(slug, orgId))
                .orElseThrow(() -> new RoadmapNotFoundException(
                        "OrgRoadmap with slug '" + slug + "' not found for orgId=" + orgId + "."));
        if (!roadmap.isPublished() || roadmap.isArchived()) {
            throw new RoadmapNotFoundException(
                    "OrgRoadmap with slug '" + slug + "' is not currently available to students.");
        }
        return roadmap;
    }

    /**
     * UPDATED (Part 2D, item 9): resource lookups are now a single batch
     * findByNodeIdIn call instead of one findByNodeId query per node in the loop.
     */
    public RoadmapGraphResponse getRoadmapGraphForStudent(String slug, String orgId, Long studentUserId) {
        OrgRoadmap roadmap = findPublishedOrgRoadmapBySlugOrThrow(slug, orgId);

        List<OrgRoadmapNode> nodes = orgRoadmapNodeRepository.findByOrgRoadmapId(roadmap.getId());
        nodes.sort(Comparator.comparing(n -> n.getOrderIndex() == null ? Integer.MAX_VALUE : n.getOrderIndex()));

        List<UserRoadmapProgress> studentProgress =
                userRoadmapProgressRepository.findByUserIdAndOrgRoadmapId(studentUserId, roadmap.getId());
        Map<Long, UserRoadmapProgress> progressByNodeId = studentProgress.stream()
                .collect(Collectors.toMap(UserRoadmapProgress::getNodeId, p -> p));

        List<Long> nodeIds = nodes.stream().map(OrgRoadmapNode::getId).collect(Collectors.toList());
        List<OrgRoadmapResource> allResources = nodeIds.isEmpty()
                ? new ArrayList<>()
                : orgRoadmapResourceRepository.findByNodeIdIn(nodeIds);
        Map<Long, List<OrgRoadmapResource>> resourcesByNodeId = allResources.stream()
                .collect(Collectors.groupingBy(OrgRoadmapResource::getNodeId));

        List<GraphNodeResponse> graphNodes = new ArrayList<>();
        List<GraphEdgeResponse> graphEdges = new ArrayList<>();

        Set<Long> nonOptionalNodeIds = new HashSet<>();
        int doneNonOptionalCount = 0;

        for (OrgRoadmapNode node : nodes) {
            List<OrgRoadmapResource> resources = resourcesByNodeId.getOrDefault(node.getId(), new ArrayList<>());
            List<OrgResourceResponse> resourceResponses = resources.stream()
                    .map(this::toOrgResourceResponse)
                    .collect(Collectors.toList());

            UserRoadmapProgress progress = progressByNodeId.get(node.getId());

            GraphNodeResponse graphNode = new GraphNodeResponse();
            graphNode.setId(node.getId());
            graphNode.setOrgId(roadmap.getOrgId());
            graphNode.setTitle(node.getTitle());
            graphNode.setDescription(node.getDescription());
            graphNode.setType(node.getType());
            graphNode.setPositionX(node.getPositionX());
            graphNode.setPositionY(node.getPositionY());
            graphNode.setOptional(node.isOptional());
            graphNode.setEstimatedHours(node.getEstimatedHours());
            graphNode.setOrderIndex(node.getOrderIndex());
            graphNode.setHasQuiz(node.isHasQuiz());
            graphNode.setHasProject(node.isHasProject());
            graphNode.setParentNodeIds(node.getParentNodeIds());
            graphNode.setResources(resourceResponses);
            graphNode.setProgressStatus(progress != null ? progress.getStatus() : NodeStatus.NOT_STARTED);
            graphNode.setProgressCompletedAt(progress != null ? progress.getCompletedAt() : null);
            graphNode.setProgressLastAccessedAt(progress != null ? progress.getLastAccessedAt() : null);
            graphNodes.add(graphNode);

            if (!node.isOptional()) {
                nonOptionalNodeIds.add(node.getId());
                if (progress != null && progress.getStatus() == NodeStatus.DONE) {
                    doneNonOptionalCount++;
                }
            }

            List<Long> parentIds = node.getParentNodeIds();
            if (parentIds != null) {
                for (Long parentId : parentIds) {
                    GraphEdgeResponse edge = new GraphEdgeResponse();
                    edge.setFromNodeId(parentId);
                    edge.setToNodeId(node.getId());
                    graphEdges.add(edge);
                }
            }
        }

        double overallCompletionPercent = nonOptionalNodeIds.isEmpty()
                ? 0.0
                : (100.0 * doneNonOptionalCount / nonOptionalNodeIds.size());

        RoadmapGraphResponse response = new RoadmapGraphResponse();
        response.setOrgRoadmapId(roadmap.getId());
        response.setOrgId(roadmap.getOrgId());
        response.setTitle(roadmap.getTitle());
        response.setSlug(roadmap.getSlug());
        response.setDescription(roadmap.getDescription());
        response.setNodes(graphNodes);
        response.setEdges(graphEdges);
        response.setOverallCompletionPercent(overallCompletionPercent);
        return response;
    }

    /**
     * DEVIATION FROM LITERAL SPEC SIGNATURE: the task lists this as
     * getStudentProgress(String slug, Long studentUserId). OrgRoadmap slugs are
     * only guaranteed unique per-org (including the null-org bucket) per the
     * uniqueness rules above, so resolving by slug alone — without knowing which
     * org's roadmap the caller means — is ambiguous and unsafe once more than one
     * org has cloned/created a roadmap with the same title. An orgId parameter is
     * added here, matching the pattern already used by getRoadmapGraphForStudent.
     */
    public List<NodeProgressResponse> getStudentProgress(String slug, String orgId, Long studentUserId) {
        OrgRoadmap roadmap = findPublishedOrgRoadmapBySlugOrThrow(slug, orgId);
        List<UserRoadmapProgress> progressRows =
                userRoadmapProgressRepository.findByUserIdAndOrgRoadmapId(studentUserId, roadmap.getId());
        return progressRows.stream()
                .map(p -> toNodeProgressResponse(p, roadmap.getOrgId()))
                .collect(Collectors.toList());
    }

    /**
     * DEVIATION FROM LITERAL SPEC SIGNATURE: see getStudentProgress javadoc above —
     * an orgId parameter is added for the same reason (slug is only unique
     * per-org, not globally).
     */
    @Transactional
    public NodeProgressResponse updateNodeProgress(String slug, String orgId, Long studentUserId,
                                                     UpdateNodeProgressRequest request) {
        OrgRoadmap roadmap = findPublishedOrgRoadmapBySlugOrThrow(slug, orgId);
        OrgRoadmapNode node = findOrgNodeOrThrow(request.getNodeId());
        if (!node.getOrgRoadmapId().equals(roadmap.getId())) {
            throw new RoadmapNotFoundException(
                    "Node " + request.getNodeId() + " does not belong to roadmap slug '" + slug + "'.");
        }

        UserRoadmapProgress progress = userRoadmapProgressRepository
                .findByUserIdAndNodeId(studentUserId, request.getNodeId())
                .orElseGet(() -> {
                    UserRoadmapProgress fresh = new UserRoadmapProgress();
                    fresh.setUserId(studentUserId);
                    fresh.setOrgId(roadmap.getOrgId());
                    fresh.setOrgRoadmapId(roadmap.getId());
                    fresh.setNodeId(request.getNodeId());
                    fresh.setStatus(NodeStatus.NOT_STARTED);
                    fresh.setTimeSpentMinutes(0);
                    fresh.setResourceClicks(0);
                    fresh.setCreatedAt(LocalDateTime.now());
                    return fresh;
                });

        if (request.getStatus() != null) {
            progress.setStatus(request.getStatus());
            if (request.getStatus() == NodeStatus.DONE) {
                progress.setCompletedAt(LocalDateTime.now());
            }
        }
        if (request.getAdditionalTimeSpentMinutes() != null) {
            int current = progress.getTimeSpentMinutes() == null ? 0 : progress.getTimeSpentMinutes();
            progress.setTimeSpentMinutes(current + request.getAdditionalTimeSpentMinutes());
        }
        if (Boolean.TRUE.equals(request.getIncrementResourceClick())) {
            int current = progress.getResourceClicks() == null ? 0 : progress.getResourceClicks();
            progress.setResourceClicks(current + 1);
        }
        progress.setLastAccessedAt(LocalDateTime.now());
        progress.setUpdatedAt(LocalDateTime.now());

        UserRoadmapProgress saved = userRoadmapProgressRepository.save(progress);
        return toNodeProgressResponse(saved, roadmap.getOrgId());
    }

    /**
     * UPDATED (Part 2D, item 3): previously resolved the target roadmap via
     * findOrgRoadmapOrThrow(request.getOrgRoadmapId()) with no org/published
     * check — any authenticated student could write progress against any
     * orgRoadmapId, including another org's roadmap or an unpublished draft, just
     * by putting it in the request body. Now resolves the roadmap the same way
     * updateNodeProgress does (via slug + orgId, published-only), and validates
     * that request.getOrgRoadmapId() (if present) matches the resolved roadmap's
     * id. Every node lookup and progress upsert inside the loop uses the
     * resolved roadmap's id, never the client-supplied one.
     */
    @Transactional
    public BatchUpdateProgressResponse batchUpdateProgress(String slug, String orgId, Long studentUserId,
                                                             BatchUpdateProgressRequest request) {
        OrgRoadmap roadmap = findPublishedOrgRoadmapBySlugOrThrow(slug, orgId);

        if (request.getOrgRoadmapId() != null && !request.getOrgRoadmapId().equals(roadmap.getId())) {
            throw new RoadmapNotFoundException(
                    "Request orgRoadmapId=" + request.getOrgRoadmapId()
                            + " does not match the roadmap resolved from slug '" + slug + "'.");
        }

        List<NodeProgressResponse> updatedResponses = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;

        for (NodeStatusPair pair : request.getUpdates()) {
            try {
                OrgRoadmapNode node = findOrgNodeOrThrow(pair.getNodeId());
                if (!node.getOrgRoadmapId().equals(roadmap.getId())) {
                    throw new RoadmapNotFoundException(
                            "Node " + pair.getNodeId() + " does not belong to orgRoadmapId=" + roadmap.getId() + ".");
                }

                UserRoadmapProgress progress = userRoadmapProgressRepository
                        .findByUserIdAndNodeId(studentUserId, pair.getNodeId())
                        .orElseGet(() -> {
                            UserRoadmapProgress fresh = new UserRoadmapProgress();
                            fresh.setUserId(studentUserId);
                            fresh.setOrgId(roadmap.getOrgId());
                            fresh.setOrgRoadmapId(roadmap.getId());
                            fresh.setNodeId(pair.getNodeId());
                            fresh.setTimeSpentMinutes(0);
                            fresh.setResourceClicks(0);
                            fresh.setCreatedAt(LocalDateTime.now());
                            return fresh;
                        });

                progress.setStatus(pair.getStatus());
                if (pair.getStatus() == NodeStatus.DONE) {
                    progress.setCompletedAt(LocalDateTime.now());
                }
                progress.setLastAccessedAt(LocalDateTime.now());
                progress.setUpdatedAt(LocalDateTime.now());

                UserRoadmapProgress saved = userRoadmapProgressRepository.save(progress);
                updatedResponses.add(toNodeProgressResponse(saved, roadmap.getOrgId()));
                successCount++;
            } catch (RuntimeException ex) {
                failureCount++;
            }
        }

        BatchUpdateProgressResponse response = new BatchUpdateProgressResponse();
        response.setOrgId(roadmap.getOrgId());
        response.setOrgRoadmapId(roadmap.getId());
        response.setUpdated(updatedResponses);
        response.setSuccessCount(successCount);
        response.setFailureCount(failureCount);
        return response;
    }

    /**
     * "Enrolled" is interpreted as: a published roadmap in the student's org for
     * which at least one UserRoadmapProgress row exists (i.e. the student has
     * touched at least one node). There is no explicit roadmap-level enrollment
     * table in the given schema to query instead.
     */
    public StudentDashboardResponse getStudentDashboard(Long studentUserId, String orgId) {
        List<OrgRoadmap> publishedRoadmaps = orgId == null
                ? orgRoadmapRepository.findByOrgIdIsNullAndIsPublishedTrueAndIsArchivedFalse(Pageable.unpaged()).getContent()
                : orgRoadmapRepository.findByOrgIdAndIsPublishedTrueAndIsArchivedFalse(orgId, Pageable.unpaged()).getContent();

        List<EnrolledRoadmapSummary> enrolledSummaries = new ArrayList<>();
        double completionPercentSum = 0.0;

        for (OrgRoadmap roadmap : publishedRoadmaps) {
            List<UserRoadmapProgress> progressRows =
                    userRoadmapProgressRepository.findByUserIdAndOrgRoadmapId(studentUserId, roadmap.getId());
            if (progressRows.isEmpty()) {
                continue;
            }

            List<OrgRoadmapNode> nodes = orgRoadmapNodeRepository.findByOrgRoadmapId(roadmap.getId());
            Set<Long> nonOptionalNodeIds = nodes.stream()
                    .filter(n -> !n.isOptional())
                    .map(OrgRoadmapNode::getId)
                    .collect(Collectors.toSet());
            Map<Long, UserRoadmapProgress> progressByNodeId = progressRows.stream()
                    .collect(Collectors.toMap(UserRoadmapProgress::getNodeId, p -> p));

            long doneNonOptionalCount = nonOptionalNodeIds.stream()
                    .filter(nodeId -> {
                        UserRoadmapProgress p = progressByNodeId.get(nodeId);
                        return p != null && p.getStatus() == NodeStatus.DONE;
                    })
                    .count();
            double completionPercent = nonOptionalNodeIds.isEmpty()
                    ? 0.0
                    : (100.0 * doneNonOptionalCount / nonOptionalNodeIds.size());

            EnrolledRoadmapSummary summary = new EnrolledRoadmapSummary();
            summary.setOrgRoadmapId(roadmap.getId());
            summary.setOrgId(roadmap.getOrgId());
            summary.setSlug(roadmap.getSlug());
            summary.setTitle(roadmap.getTitle());
            summary.setThumbnailUrl(roadmap.getThumbnailUrl());
            summary.setCompletionPercent(completionPercent);
            enrolledSummaries.add(summary);

            completionPercentSum += completionPercent;
        }

        double averageCompletionPercent = enrolledSummaries.isEmpty()
                ? 0.0
                : (completionPercentSum / enrolledSummaries.size());

        StudentDashboardResponse response = new StudentDashboardResponse();
        response.setUserId(studentUserId);
        response.setOrgId(orgId);
        response.setEnrolledRoadmaps(enrolledSummaries);
        response.setTotalEnrolled(enrolledSummaries.size());
        response.setAverageCompletionPercent(averageCompletionPercent);
        return response;
    }

    // =====================================================================================
    // Entity -> DTO mappers
    // =====================================================================================

    private TemplateResponse toTemplateResponse(RoadmapTemplate t) {
        TemplateResponse r = new TemplateResponse();
        r.setId(t.getId());
        // TemplateResponse.orgId intentionally left unset — always null for global templates.
        r.setTitle(t.getTitle());
        r.setSlug(t.getSlug());
        r.setDescription(t.getDescription());
        r.setCategory(t.getCategory());
        r.setThumbnailUrl(t.getThumbnailUrl());
        r.setPublished(t.isPublished());
        r.setVersion(t.getVersion());
        r.setTotalNodes(t.getTotalNodes());
        r.setArchived(t.isArchived());
        r.setCreatedBy(t.getCreatedBy());
        r.setPublishedBy(t.getPublishedBy());
        r.setPublishedAt(t.getPublishedAt());
        r.setCreatedAt(t.getCreatedAt());
        r.setUpdatedAt(t.getUpdatedAt());
        return r;
    }

    private TemplateNodeResponse toTemplateNodeResponse(RoadmapTemplateNode n) {
        TemplateNodeResponse r = new TemplateNodeResponse();
        r.setId(n.getId());
        r.setTemplateId(n.getTemplateId());
        r.setTitle(n.getTitle());
        r.setDescription(n.getDescription());
        r.setType(n.getType());
        r.setPositionX(n.getPositionX());
        r.setPositionY(n.getPositionY());
        r.setOptional(n.isOptional());
        r.setEstimatedHours(n.getEstimatedHours());
        r.setOrderIndex(n.getOrderIndex());
        r.setHasQuiz(n.isHasQuiz());
        r.setHasProject(n.isHasProject());
        r.setParentNodeIds(n.getParentNodeIds());
        r.setCreatedAt(n.getCreatedAt());
        r.setUpdatedAt(n.getUpdatedAt());
        return r;
    }

    private TemplateResourceResponse toTemplateResourceResponse(RoadmapTemplateResource res) {
        TemplateResourceResponse r = new TemplateResourceResponse();
        r.setId(res.getId());
        r.setNodeId(res.getNodeId());
        r.setType(res.getType());
        r.setTitle(res.getTitle());
        r.setUrl(res.getUrl());
        r.setDescription(res.getDescription());
        r.setDurationMinutes(res.getDurationMinutes());
        r.setDifficulty(res.getDifficulty());
        r.setUpvotes(res.getUpvotes());
        r.setFeatured(res.isFeatured());
        r.setAddedBy(res.getAddedBy());
        r.setCreatedAt(res.getCreatedAt());
        return r;
    }

    private OrgRoadmapResponse toOrgRoadmapResponse(OrgRoadmap o) {
        OrgRoadmapResponse r = new OrgRoadmapResponse();
        r.setId(o.getId());
        r.setOrgId(o.getOrgId());
        r.setSourceTemplateId(o.getSourceTemplateId());
        r.setTitle(o.getTitle());
        r.setSlug(o.getSlug());
        r.setDescription(o.getDescription());
        r.setCategory(o.getCategory());
        r.setThumbnailUrl(o.getThumbnailUrl());
        r.setPublished(o.isPublished());
        r.setArchived(o.isArchived());
        r.setTotalNodes(o.getTotalNodes());
        r.setTotalStudents(o.getTotalStudents());
        r.setCreatedBy(o.getCreatedBy());
        r.setPublishedBy(o.getPublishedBy());
        r.setPublishedAt(o.getPublishedAt());
        r.setCreatedAt(o.getCreatedAt());
        r.setUpdatedAt(o.getUpdatedAt());
        return r;
    }

    private OrgNodeResponse toOrgNodeResponse(OrgRoadmapNode n) {
        OrgNodeResponse r = new OrgNodeResponse();
        r.setId(n.getId());
        r.setOrgRoadmapId(n.getOrgRoadmapId());
        r.setSourceNodeId(n.getSourceNodeId());
        r.setTitle(n.getTitle());
        r.setDescription(n.getDescription());
        r.setType(n.getType());
        r.setPositionX(n.getPositionX());
        r.setPositionY(n.getPositionY());
        r.setOptional(n.isOptional());
        r.setEstimatedHours(n.getEstimatedHours());
        r.setOrderIndex(n.getOrderIndex());
        r.setHasQuiz(n.isHasQuiz());
        r.setHasProject(n.isHasProject());
        r.setParentNodeIds(n.getParentNodeIds());
        r.setCreatedAt(n.getCreatedAt());
        r.setUpdatedAt(n.getUpdatedAt());
        return r;
    }

    private OrgResourceResponse toOrgResourceResponse(OrgRoadmapResource res) {
        OrgResourceResponse r = new OrgResourceResponse();
        r.setId(res.getId());
        r.setNodeId(res.getNodeId());
        r.setType(res.getType());
        r.setTitle(res.getTitle());
        r.setUrl(res.getUrl());
        r.setDescription(res.getDescription());
        r.setDurationMinutes(res.getDurationMinutes());
        r.setDifficulty(res.getDifficulty());
        r.setUpvotes(res.getUpvotes());
        r.setFeatured(res.isFeatured());
        r.setAddedBy(res.getAddedBy());
        r.setCreatedAt(res.getCreatedAt());
        return r;
    }

    private RoadmapListItemResponse toRoadmapListItemResponse(OrgRoadmap o) {
        RoadmapListItemResponse r = new RoadmapListItemResponse();
        r.setId(o.getId());
        r.setOrgId(o.getOrgId());
        r.setSlug(o.getSlug());
        r.setTitle(o.getTitle());
        r.setCategory(o.getCategory());
        r.setThumbnailUrl(o.getThumbnailUrl());
        r.setTotalNodes(o.getTotalNodes());
        return r;
    }

    private NodeProgressResponse toNodeProgressResponse(UserRoadmapProgress p, String orgId) {
        NodeProgressResponse r = new NodeProgressResponse();
        r.setId(p.getId());
        r.setOrgId(orgId);
        r.setUserId(p.getUserId());
        r.setOrgRoadmapId(p.getOrgRoadmapId());
        r.setNodeId(p.getNodeId());
        r.setStatus(p.getStatus());
        r.setCompletedAt(p.getCompletedAt());
        r.setLastAccessedAt(p.getLastAccessedAt());
        r.setTimeSpentMinutes(p.getTimeSpentMinutes());
        r.setResourceClicks(p.getResourceClicks());
        return r;
    }
}