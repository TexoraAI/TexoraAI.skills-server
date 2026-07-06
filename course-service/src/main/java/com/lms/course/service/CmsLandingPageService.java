package com.lms.course.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.course.dto.CmsComponentDtos;
import com.lms.course.dto.CmsMediaDtos;
import com.lms.course.dto.CmsNavDtos;
import com.lms.course.dto.CmsPageDtos;
import com.lms.course.dto.CmsSectionDtos;
import com.lms.course.exception.CmsResourceNotFoundException;
import com.lms.course.model.CmsComponent;
import com.lms.course.model.CmsMediaAsset;
import com.lms.course.model.CmsNavItem;
import com.lms.course.model.CmsPage;
import com.lms.course.model.CmsSection;
import com.lms.course.repository.CmsComponentRepository;
import com.lms.course.repository.CmsMediaAssetRepository;
import com.lms.course.repository.CmsNavItemRepository;
import com.lms.course.repository.CmsPageRepository;
import com.lms.course.repository.CmsSectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Single concrete service class powering the Super Admin CMS Management
 * panel: page settings, section/component CRUD + reordering, the global
 * media library (DB-stored bytes), and per-pageKey navigation lists.
 *
 * Intentionally has no interface / Impl split — this is the only class
 * that talks to the CMS repositories.
 */
@Service
public class CmsLandingPageService {

    private static final String DEFAULT_OPEN_IN = "same_tab";

    private final CmsPageRepository pageRepository;
    private final CmsSectionRepository sectionRepository;
    private final CmsComponentRepository componentRepository;
    private final CmsMediaAssetRepository mediaAssetRepository;
    private final CmsNavItemRepository navItemRepository;
    private final ObjectMapper objectMapper;

    public CmsLandingPageService(CmsPageRepository pageRepository,
                                  CmsSectionRepository sectionRepository,
                                  CmsComponentRepository componentRepository,
                                  CmsMediaAssetRepository mediaAssetRepository,
                                  CmsNavItemRepository navItemRepository,
                                  ObjectMapper objectMapper) {
        this.pageRepository = pageRepository;
        this.sectionRepository = sectionRepository;
        this.componentRepository = componentRepository;
        this.mediaAssetRepository = mediaAssetRepository;
        this.navItemRepository = navItemRepository;
        this.objectMapper = objectMapper;
    }

    // ────────────────────────────────────────────────────────────────
    // Page
    // ────────────────────────────────────────────────────────────────

    @Transactional
    public CmsPage getOrCreatePage(String pageKey) {
        return pageRepository.findByPageKey(pageKey)
                .orElseGet(() -> {
                    CmsPage page = new CmsPage(pageKey, defaultTitleFor(pageKey), null, false);
                    return pageRepository.save(page);
                });
    }

    @Transactional
    public CmsPageDtos.Response getPage(String pageKey) {
        CmsPage page = getOrCreatePage(pageKey);
        return toPageResponse(page, false);
    }

    @Transactional
    public CmsPageDtos.Response getPublishedPage(String pageKey) {
        CmsPage page = pageRepository.findByPageKey(pageKey)
                .orElseThrow(() -> new CmsResourceNotFoundException("Page not found: " + pageKey));
        if (!page.isPublished()) {
            throw new CmsResourceNotFoundException("Page not published: " + pageKey);
        }
        return toPageResponse(page, true);
    }

    @Transactional
    public CmsPageDtos.Response updatePageSettings(String pageKey, CmsPageDtos.SettingsRequest request) {
        CmsPage page = getOrCreatePage(pageKey);
        page.setTitle(request.getTitle());
        page.setDescription(request.getDescription());
        page.setPublished(request.isPublished());
        return toPageResponse(pageRepository.save(page), false);
    }

    // ────────────────────────────────────────────────────────────────
    // Sections
    // ────────────────────────────────────────────────────────────────

    @Transactional
    public CmsSectionDtos.Response addSection(String pageKey, CmsSectionDtos.CreateRequest request) {
        CmsPage page = getOrCreatePage(pageKey);
        int nextOrderIndex = page.getSections().size();

        CmsSection section = new CmsSection();
        section.setPage(page);
        section.setType(request.getType());
        section.setLabel(request.getLabel());
        section.setOrderIndex(nextOrderIndex);
        section.setVisible(true);
        section.setPublished(true);
        section.setData(writeJson(request.getData()));

        CmsSection saved = sectionRepository.save(section);
        return toSectionResponse(saved);
    }

    @Transactional
    public CmsSectionDtos.Response updateSection(String pageKey, Long sectionId, CmsSectionDtos.UpdateRequest request) {
        CmsSection section = getSectionOwnedByPage(pageKey, sectionId);
        if (request.getLabel() != null) {
            section.setLabel(request.getLabel());
        }
        if (request.getData() != null) {
            section.setData(writeJson(request.getData()));
        }
        return toSectionResponse(sectionRepository.save(section));
    }

    @Transactional
    public void deleteSection(String pageKey, Long sectionId) {
        CmsSection section = getSectionOwnedByPage(pageKey, sectionId);
        sectionRepository.delete(section);
    }

    @Transactional
    public void reorderSections(String pageKey, CmsSectionDtos.ReorderRequest request) {
        CmsPage page = getOrCreatePage(pageKey);
        Map<Long, CmsSection> byId = page.getSections().stream()
                .collect(Collectors.toMap(CmsSection::getId, s -> s));

        List<Long> orderedIds = request.getOrderedSectionIds();
        for (int i = 0; i < orderedIds.size(); i++) {
            CmsSection section = byId.get(orderedIds.get(i));
            if (section == null) {
                throw new CmsResourceNotFoundException(
                        "Section " + orderedIds.get(i) + " does not belong to page " + pageKey);
            }
            section.setOrderIndex(i);
        }
        sectionRepository.saveAll(byId.values());
    }

    @Transactional
    public void setSectionVisibility(String pageKey, Long sectionId, boolean visible) {
        CmsSection section = getSectionOwnedByPage(pageKey, sectionId);
        section.setVisible(visible);
        sectionRepository.save(section);
    }

    @Transactional
    public void setSectionPublished(String pageKey, Long sectionId, boolean published) {
        CmsSection section = getSectionOwnedByPage(pageKey, sectionId);
        section.setPublished(published);
        sectionRepository.save(section);
    }

    // ────────────────────────────────────────────────────────────────
    // Components
    // ────────────────────────────────────────────────────────────────

    @Transactional
    public CmsComponentDtos.Response addComponent(Long sectionId, CmsComponentDtos.CreateRequest request) {
        CmsSection section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new CmsResourceNotFoundException("Section not found: " + sectionId));

        int nextOrderIndex = section.getComponents().size();

        CmsComponent component = new CmsComponent();
        component.setSection(section);
        component.setType(request.getType());
        component.setOrderIndex(nextOrderIndex);
        component.setVisible(true);
        component.setData(writeJson(request.getData()));

        CmsComponent saved = componentRepository.save(component);
        return toComponentResponse(saved);
    }

    @Transactional
    public CmsComponentDtos.Response updateComponent(Long componentId, CmsComponentDtos.UpdateRequest request) {
        CmsComponent component = getComponentOrThrow(componentId);
        if (request.getData() != null) {
            component.setData(writeJson(request.getData()));
        }
        return toComponentResponse(componentRepository.save(component));
    }

    @Transactional
    public void deleteComponent(Long componentId) {
        CmsComponent component = getComponentOrThrow(componentId);
        componentRepository.delete(component);
    }

    @Transactional
    public void setComponentVisibility(Long componentId, boolean visible) {
        CmsComponent component = getComponentOrThrow(componentId);
        component.setVisible(visible);
        componentRepository.save(component);
    }

    // ────────────────────────────────────────────────────────────────
    // Media
    // ────────────────────────────────────────────────────────────────

    @Transactional
    public List<CmsMediaDtos.Response> listMedia(String search) {
        List<CmsMediaAsset> assets = (search == null || search.isBlank())
                ? mediaAssetRepository.findAll()
                : mediaAssetRepository.findByOriginalFileNameContainingIgnoreCase(search);

        return assets.stream()
                .map(this::toMediaResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CmsMediaDtos.Response uploadMedia(MultipartFile file) {
        CmsMediaAsset asset = new CmsMediaAsset();
        asset.setOriginalFileName(file.getOriginalFilename());
        asset.setFileName(buildStoredFileName(file.getOriginalFilename()));
        asset.setContentType(file.getContentType());
        asset.setSizeBytes(file.getSize());
        try {
            asset.setFileData(file.getBytes());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read uploaded file bytes", e);
        }

        CmsMediaAsset saved = mediaAssetRepository.save(asset);
        return toMediaResponse(saved);
    }

    @Transactional
    public CmsMediaAsset getMediaRaw(Long mediaId) {
        return mediaAssetRepository.findById(mediaId)
                .orElseThrow(() -> new CmsResourceNotFoundException("Media asset not found: " + mediaId));
    }

    @Transactional
    public void deleteMedia(Long mediaId) {
        CmsMediaAsset asset = mediaAssetRepository.findById(mediaId)
                .orElseThrow(() -> new CmsResourceNotFoundException("Media asset not found: " + mediaId));
        mediaAssetRepository.delete(asset);
    }

    // ────────────────────────────────────────────────────────────────
    // Navigation
    // ────────────────────────────────────────────────────────────────

    @Transactional
    public List<CmsNavDtos.Response> listNavItems(String pageKey) {
        return navItemRepository.findByPageKeyOrderByOrderIndexAsc(pageKey).stream()
                .map(this::toNavResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CmsNavDtos.Response addNavItem(String pageKey, CmsNavDtos.ItemRequest request) {
        int nextOrderIndex = navItemRepository.findByPageKeyOrderByOrderIndexAsc(pageKey).size();

        CmsNavItem item = new CmsNavItem();
        item.setPageKey(pageKey);
        item.setLabel(request.getLabel());
        item.setHref(request.getHref());
        item.setOpenIn(request.getOpenIn() != null ? request.getOpenIn() : DEFAULT_OPEN_IN);
        item.setOrderIndex(nextOrderIndex);

        return toNavResponse(navItemRepository.save(item));
    }

    @Transactional
    public CmsNavDtos.Response updateNavItem(String pageKey, Long itemId, CmsNavDtos.ItemRequest request) {
        CmsNavItem item = getNavItemOwnedByPage(pageKey, itemId);
        item.setLabel(request.getLabel());
        item.setHref(request.getHref());
        if (request.getOpenIn() != null) {
            item.setOpenIn(request.getOpenIn());
        }
        return toNavResponse(navItemRepository.save(item));
    }

    @Transactional
    public void deleteNavItem(String pageKey, Long itemId) {
        CmsNavItem item = getNavItemOwnedByPage(pageKey, itemId);
        navItemRepository.delete(item);
    }

    @Transactional
    public void reorderNavItems(String pageKey, CmsNavDtos.ReorderRequest request) {
        List<CmsNavItem> current = navItemRepository.findByPageKeyOrderByOrderIndexAsc(pageKey);
        Map<Long, CmsNavItem> byId = current.stream()
                .collect(Collectors.toMap(CmsNavItem::getId, i -> i));

        List<Long> orderedIds = request.getOrderedNavItemIds();
        for (int i = 0; i < orderedIds.size(); i++) {
            CmsNavItem item = byId.get(orderedIds.get(i));
            if (item == null) {
                throw new CmsResourceNotFoundException(
                        "Nav item " + orderedIds.get(i) + " does not belong to page " + pageKey);
            }
            item.setOrderIndex(i);
        }
        navItemRepository.saveAll(byId.values());
    }

    // ────────────────────────────────────────────────────────────────
    // Internal helpers
    // ────────────────────────────────────────────────────────────────

    private CmsSection getSectionOwnedByPage(String pageKey, Long sectionId) {
        CmsSection section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new CmsResourceNotFoundException("Section not found: " + sectionId));
        if (!section.getPage().getPageKey().equals(pageKey)) {
            throw new CmsResourceNotFoundException(
                    "Section " + sectionId + " does not belong to page " + pageKey);
        }
        return section;
    }

    private CmsComponent getComponentOrThrow(Long componentId) {
        return componentRepository.findById(componentId)
                .orElseThrow(() -> new CmsResourceNotFoundException("Component not found: " + componentId));
    }

    private CmsNavItem getNavItemOwnedByPage(String pageKey, Long itemId) {
        CmsNavItem item = navItemRepository.findById(itemId)
                .orElseThrow(() -> new CmsResourceNotFoundException("Nav item not found: " + itemId));
        if (!item.getPageKey().equals(pageKey)) {
            throw new CmsResourceNotFoundException(
                    "Nav item " + itemId + " does not belong to page " + pageKey);
        }
        return item;
    }

    private String defaultTitleFor(String pageKey) {
        if (pageKey == null || pageKey.isBlank()) {
            return "Untitled Page";
        }
        String[] parts = pageKey.replace('_', '-').split("-");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1)).append(' ');
        }
        return sb.toString().trim();
    }

    private String buildStoredFileName(String originalFileName) {
        String safeOriginal = originalFileName == null ? "file" : originalFileName;
        return System.currentTimeMillis() + "-" + safeOriginal.replaceAll("\\s+", "_");
    }

    // ── JSON (de)serialization for the `data` column ──

    private String writeJson(Map<String, Object> data) {
        Map<String, Object> safe = (data == null) ? Collections.emptyMap() : data;
        try {
            return objectMapper.writeValueAsString(safe);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize CMS data payload", e);
        }
    }

    private Map<String, Object> readJson(String json) {
        if (json == null || json.isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (IOException e) {
            throw new IllegalStateException("Failed to deserialize CMS data payload", e);
        }
    }

    // ── Entity -> DTO mapping ──

    private CmsPageDtos.Response toPageResponse(CmsPage page, boolean publicViewOnly) {
        CmsPageDtos.Response response = new CmsPageDtos.Response();
        response.setId(page.getId());
        response.setPageKey(page.getPageKey());
        response.setTitle(page.getTitle());
        response.setDescription(page.getDescription());
        response.setPublished(page.isPublished());
        response.setCreatedAt(page.getCreatedAt());
        response.setUpdatedAt(page.getUpdatedAt());

        List<CmsSectionDtos.Response> sections = page.getSections().stream()
                .filter(section -> !publicViewOnly || (section.isVisible() && section.isPublished()))
                .sorted((a, b) -> Integer.compare(a.getOrderIndex(), b.getOrderIndex()))
                .map(section -> toSectionResponse(section, publicViewOnly))
                .collect(Collectors.toList());
        response.setSections(sections);

        return response;
    }

    private CmsSectionDtos.Response toSectionResponse(CmsSection section) {
        return toSectionResponse(section, false);
    }

    private CmsSectionDtos.Response toSectionResponse(CmsSection section, boolean publicViewOnly) {
        CmsSectionDtos.Response response = new CmsSectionDtos.Response();
        response.setId(section.getId());
        response.setType(section.getType());
        response.setLabel(section.getLabel());
        response.setOrderIndex(section.getOrderIndex());
        response.setVisible(section.isVisible());
        response.setPublished(section.isPublished());
        response.setData(readJson(section.getData()));

        List<CmsComponentDtos.Response> components = section.getComponents().stream()
                .filter(component -> !publicViewOnly || component.isVisible())
                .sorted((a, b) -> Integer.compare(a.getOrderIndex(), b.getOrderIndex()))
                .map(this::toComponentResponse)
                .collect(Collectors.toList());
        response.setComponents(components);

        return response;
    }

    private CmsComponentDtos.Response toComponentResponse(CmsComponent component) {
        CmsComponentDtos.Response response = new CmsComponentDtos.Response();
        response.setId(component.getId());
        response.setSectionId(component.getSection().getId());
        response.setType(component.getType());
        response.setOrderIndex(component.getOrderIndex());
        response.setVisible(component.isVisible());
        response.setData(readJson(component.getData()));
        return response;
    }

    private CmsMediaDtos.Response toMediaResponse(CmsMediaAsset asset) {
        CmsMediaDtos.Response response = new CmsMediaDtos.Response();
        response.setId(asset.getId());
        response.setFileName(asset.getFileName());
        response.setOriginalFileName(asset.getOriginalFileName());
        response.setContentType(asset.getContentType());
        response.setSizeBytes(asset.getSizeBytes());
        response.setUploadedAt(asset.getUploadedAt());
        response.setDownloadUrl("/api/v1/cmslandinghubs/media/" + asset.getId() + "/raw");
        return response;
    }

    private CmsNavDtos.Response toNavResponse(CmsNavItem item) {
        CmsNavDtos.Response response = new CmsNavDtos.Response();
        response.setId(item.getId());
        response.setPageKey(item.getPageKey());
        response.setLabel(item.getLabel());
        response.setHref(item.getHref());
        response.setOpenIn(item.getOpenIn());
        response.setOrderIndex(item.getOrderIndex());
        return response;
    }
}