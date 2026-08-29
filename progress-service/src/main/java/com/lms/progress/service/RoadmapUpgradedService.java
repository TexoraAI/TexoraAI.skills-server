package com.lms.progress.service;
import com.lms.progress.repository.RoadmapUpgradedVideoCacheRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.progress.config.RoadmapUpgradedOpenAiClient;
import com.lms.progress.config.RoadmapUpgradedYoutubeClient;
import com.lms.progress.dto.RoadmapUpgradedAdminStatsDto;
import com.lms.progress.repository.RoadmapUpgradedVideoCacheRepository;
import com.lms.progress.model.RoadmapUpgradedVideoCache;
import java.util.Optional;
import com.lms.progress.dto.RoadmapUpgradedGenerateRequestDto;
import com.lms.progress.dto.RoadmapUpgradedMentorMessageDto;
import com.lms.progress.dto.RoadmapUpgradedMentorRequestDto;
import com.lms.progress.dto.RoadmapUpgradedMentorResponseDto;
import com.lms.progress.dto.RoadmapUpgradedModuleDto;
import com.lms.progress.dto.RoadmapUpgradedResourceDto;
import com.lms.progress.model.RoadmapUpgradedTopic;
import com.lms.progress.dto.RoadmapUpgradedTopicDto;
import com.lms.progress.dto.RoadmapUpgradedResponseDto;
import com.lms.progress.dto.RoadmapUpgradedSuperAdminStatsDto;
import com.lms.progress.dto.RoadmapUpgradedUserUsageDto;
import com.lms.progress.model.RoadmapUpgradedMentorMessage;
import com.lms.progress.model.RoadmapUpgradedModule;
import com.lms.progress.model.RoadmapUpgradedResource;
import com.lms.progress.model.RoadmapUpgradedSyllabus;
import com.lms.progress.repository.RoadmapUpgradedMentorRepository;
import com.lms.progress.repository.RoadmapUpgradedRepository;
import com.lms.progress.security.JwtUtil;
import jakarta.transaction.Transactional;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Single concrete service class - all RoadmapUpgraded business logic lives
 * here, per spec ("no interface/impl split").
 *
 * Two things in this class are NOT directly specified in the build doc and
 * had to be filled in to make the feature actually runnable; both are called
 * out inline where they occur:
 *   1. How the module/topic outline of a roadmap is produced before
 *      generateResourcesForTopic() can be called per-topic (section 6 assumes
 *      a topic list already exists - generateRoadmapOutline() below produces it).
 *   2. regenerateRemainingModules() has no stored contentSources to replay,
 *      since contentSources only exists on the generate request DTO, not on
 *      the syllabus entity - it defaults to all four resource types.
 *
 * NOTE: generateVideoResource() now calls the YouTube Data API v3 (via
 * RoadmapUpgradedYoutubeClient) to resolve a real video ID for the
 * AI-suggested title, instead of always leaving sourceUrl null. That lookup
 * is wrapped in its own try/catch so a failed/quota-exhausted YouTube call
 * degrades to the old behavior (sourceUrl left null) rather than failing the
 * whole resource - same defensive style as everywhere else in this class.
 *
 * NOTE: generateArticleResource() now asks the model to write the actual
 * article body (400-800 words), stored in contentBody, instead of just a
 * title suggestion. callAndParseJson() already falls back to an empty JSON
 * node on any parse failure, so a malformed/missing "body" field just
 * leaves contentBody null (title-only, same as the old behavior) rather
 * than failing generation.
 *
 * NOTE: generatePdfResource() now asks the model for real structured
 * reference content (heading/body sections) and renders it into an actual
 * PDF document via Apache PDFBox, stored Base64-encoded in pdfContent
 * (see RoadmapUpgradedResource). filePath/sourceUrl stay null - there is no
 * disk/object-storage pattern anywhere else in this codebase, so the PDF
 * lives in the DB like quizContentJson/contentBody and is streamed back out
 * through GET /resource/{id}/pdf (see getResourcePdf() below and
 * RoadmapUpgradedController), not a path. Same "store the real content, not
 * just a suggestion" upgrade the video/article generators already got, and
 * the PDF render is wrapped in its own try/catch so a rendering failure
 * degrades to a resource with no pdfContent rather than failing generation.
 */
@Service
public class RoadmapUpgradedService {

    private static final Logger log = LoggerFactory.getLogger(RoadmapUpgradedService.class);

    private static final List<String> ALL_CONTENT_SOURCES =
            List.of("VIDEO", "ARTICLE", "PDF", "QUIZ");

    private static final int MENTOR_HISTORY_TURNS = 6;
    private static final int TOP_USAGE_LIMIT = 5;
    private static final int TOP_PLATFORM_USAGE_LIMIT = 10;

    private final RoadmapUpgradedRepository repository;
    private final RoadmapUpgradedMentorRepository mentorRepository;
    private final JwtUtil jwtUtil;
    private final RoadmapUpgradedOpenAiClient openAiClient;
    private final RoadmapUpgradedYoutubeClient youtubeClient;
    private final RoadmapUpgradedVideoCacheRepository videoCacheRepository;
    private final ObjectMapper objectMapper;

    private static final Executor PARALLEL_EXECUTOR = Executors.newFixedThreadPool(8);
    private static final boolean SKIP_PDF = false;

    public RoadmapUpgradedService(RoadmapUpgradedRepository repository,
            RoadmapUpgradedMentorRepository mentorRepository,
            JwtUtil jwtUtil,
            RoadmapUpgradedOpenAiClient openAiClient,
            RoadmapUpgradedYoutubeClient youtubeClient,
            RoadmapUpgradedVideoCacheRepository videoCacheRepository,
            ObjectMapper objectMapper) {
this.repository = repository;
this.mentorRepository = mentorRepository;
this.jwtUtil = jwtUtil;
this.openAiClient = openAiClient;
this.youtubeClient = youtubeClient;
this.videoCacheRepository = videoCacheRepository;
this.objectMapper = objectMapper;
}

    // =========================================================================
    // Generation
    // =========================================================================

    @Transactional
    public RoadmapUpgradedResponseDto generateRoadmap(String token, RoadmapUpgradedGenerateRequestDto request) {
        Long userId = jwtUtil.extractUserId(token);
        String role = jwtUtil.extractRole(token);
        String organizationId = jwtUtil.extractOrganizationIdOrNull(token);

        boolean fromLibrary = Boolean.TRUE.equals(request.getFromLibrary());
        List<String> contentSources = (request.getContentSources() == null || request.getContentSources().isEmpty())
                ? ALL_CONTENT_SOURCES
                : request.getContentSources();

        RoadmapUpgradedSyllabus syllabus;

        if (fromLibrary) {
            Optional<RoadmapUpgradedSyllabus> cached = repository
                    .findFirstByTargetRoleAndSourceTypeAndStatus(request.getTargetRole(), "LIBRARY", "READY")
                    .stream()
                    .findFirst();

            if (cached.isPresent()) {
                syllabus = cloneSyllabusStructure(cached.get(), userId, role, organizationId, "LIBRARY", "READY");
            } else {
                syllabus = newSyllabusShell(userId, role, organizationId, request, "LIBRARY", "GENERATING");
                populateModulesAndResources(syllabus, request, contentSources);
                syllabus.setStatus("READY");
            }
        } else {
            syllabus = newSyllabusShell(userId, role, organizationId, request, "GENERATED", "GENERATING");
            populateModulesAndResources(syllabus, request, contentSources);
            syllabus.setStatus("READY");
        }

        RoadmapUpgradedSyllabus saved = repository.save(syllabus);
        return toResponseDto(saved);
    }

    private RoadmapUpgradedSyllabus newSyllabusShell(Long ownerId,
                                                       String ownerRole,
                                                       String organizationId,
                                                       RoadmapUpgradedGenerateRequestDto request,
                                                       String sourceType,
                                                       String status) {
        RoadmapUpgradedSyllabus syllabus = new RoadmapUpgradedSyllabus();
        syllabus.setOwnerId(ownerId);
        syllabus.setOwnerRole(ownerRole);
        syllabus.setOrganizationId(organizationId);
        syllabus.setDomain(request.getDomain());
        syllabus.setPathType(request.getPathType());
        syllabus.setTargetRole(request.getTargetRole());
        syllabus.setLanguage(request.getLanguage());
        syllabus.setSourceType(sourceType);
        syllabus.setStatus(status);
        syllabus.setCompletionPercent(0.0);
        syllabus.setCreatedAt(LocalDateTime.now());
        return syllabus;
    }


    private void populateModulesAndResources(RoadmapUpgradedSyllabus syllabus,
            RoadmapUpgradedGenerateRequestDto request,
            List<String> contentSources) {
        RoadmapOutline outline = generateRoadmapOutline(
                request.getDomain(), request.getPathType(), request.getTargetRole(), request.getLanguage());

        List<RoadmapUpgradedModule> modules = new ArrayList<>();
        int moduleOrderIndex = 0;

        for (ModuleOutline moduleOutline : outline.modules) {
            RoadmapUpgradedModule module = new RoadmapUpgradedModule();
            module.setSyllabus(syllabus);
            module.setOrderIndex(moduleOrderIndex);
            module.setTitle(moduleOutline.title);
            module.setLocked(moduleOrderIndex != 0);
            module.setProgressPercent(0.0);

            List<CompletableFuture<RoadmapUpgradedTopic>> topicFutures = new ArrayList<>();
            int topicOrderIndex = 0;

            for (String topicTitle : moduleOutline.topicTitles) {
                final int topicIdx = topicOrderIndex;

                CompletableFuture<RoadmapUpgradedTopic> topicFuture =
                        CompletableFuture.supplyAsync(() ->
                            generateTopicWithResourcesParallel(
                                    module, topicTitle, topicIdx,
                                    request.getDomain(), contentSources),
                            PARALLEL_EXECUTOR);

                topicFutures.add(topicFuture);
                topicOrderIndex++;
            }

            List<RoadmapUpgradedTopic> topics = topicFutures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());

            module.setTopics(topics);
            modules.add(module);
            moduleOrderIndex++;

            log.info("Module '{}' completed with {} topics (parallel)", moduleOutline.title, topics.size());
        }

        syllabus.setModules(modules);
        syllabus.setTotalModules(modules.size());
        syllabus.setTotalWeeks(outline.totalWeeks);
        log.info("Roadmap generation complete: {} modules, {} total topics",
                modules.size(),
                modules.stream().mapToInt(m -> m.getTopics().size()).sum());
    }
    /**
     * ASSUMPTION (not in spec): produces the module/topic breakdown for a
     * roadmap before per-topic resource generation can run. The spec's
     * section 6 says "for each topic in the target roadmap" but never
     * defines how that topic list is produced - this fills that gap via a
     * single JSON-mode OpenAI call. Falls back to a minimal one-module
     * outline if the model response can't be parsed, so generation never
     * hard-fails the request.
     */

    private static final int MIN_MODULES = 5;
    private static final int MAX_MODULES = 8;
    private static final int MIN_TOPICS_PER_MODULE = 3;
    private static final int MAX_TOPICS_PER_MODULE = 5;

    /**
     * ASSUMPTION (not in spec): produces the module -> topic breakdown for a
     * roadmap before per-topic resource generation can run. Now a two-level
     * outline (modules, each with their own topic titles) instead of a flat
     * module list, since each TOPIC (not each module) is what gets its own
     * video/article/pdf/quiz bundle. The model is asked for 5-8 modules of
     * 3-5 topics each; those bounds are also enforced in code afterward
     * (truncated, never padded with invented content) since models don't
     * reliably honor count constraints on their own - this was the exact
     * failure mode that produced 16 modules under the old flat-list prompt.
     */
    private RoadmapOutline generateRoadmapOutline(String domain, String pathType, String targetRole, String language) {
        String systemPrompt = "You are a curriculum designer for a technical learning platform. "
                + "Respond ONLY with a JSON object, no other text.";
        String userPrompt = String.format(
                "Design a learning roadmap outline for target role \"%s\" in domain \"%s\" (path type: %s, "
                        + "language: %s). Respond with JSON: "
                        + "{\"totalWeeks\": <int>, \"modules\": ["
                        + "{\"title\": \"...\", \"topics\": [\"...\", \"...\", \"...\"]}"
                        + "]}. "
                        + "Return EXACTLY %d to %d modules, ordered from foundational to advanced. "
                        + "Each module must have EXACTLY %d to %d topics, ordered so earlier topics are "
                        + "prerequisites for later ones in the same module. Do not exceed these counts.",
                targetRole, domain, pathType, language,
                MIN_MODULES, MAX_MODULES, MIN_TOPICS_PER_MODULE, MAX_TOPICS_PER_MODULE);

        try {
            String raw = openAiClient.completeJson(systemPrompt, userPrompt);
            JsonNode node = objectMapper.readTree(raw);
            int totalWeeks = node.path("totalWeeks").asInt(4);

            List<ModuleOutline> modules = new ArrayList<>();
            for (JsonNode moduleNode : node.path("modules")) {
                String moduleTitle = moduleNode.path("title").asText(null);
                if (moduleTitle == null || moduleTitle.isBlank()) {
                    continue;
                }
                List<String> topicTitles = new ArrayList<>();
                for (JsonNode topicNode : moduleNode.path("topics")) {
                    String t = topicNode.asText();
                    if (t != null && !t.isBlank()) {
                        topicTitles.add(t);
                    }
                }
                if (topicTitles.isEmpty()) {
                    topicTitles.add(moduleTitle);
                }
                if (topicTitles.size() > MAX_TOPICS_PER_MODULE) {
                    log.warn("Module '{}' returned {} topics, truncating to {}", moduleTitle, topicTitles.size(), MAX_TOPICS_PER_MODULE);
                    topicTitles = topicTitles.subList(0, MAX_TOPICS_PER_MODULE);
                }
                modules.add(new ModuleOutline(moduleTitle, topicTitles));
            }

            if (modules.isEmpty()) {
                modules.add(new ModuleOutline("Getting Started with " + targetRole,
                        List.of("Getting Started with " + targetRole)));
            }
            if (modules.size() > MAX_MODULES) {
                log.warn("Outline returned {} modules for targetRole={}, truncating to {}", modules.size(), targetRole, MAX_MODULES);
                modules = modules.subList(0, MAX_MODULES);
            }

            return new RoadmapOutline(totalWeeks, modules);
        } catch (Exception e) {
            log.warn("Failed to generate/parse roadmap outline for targetRole={}, falling back to default outline", targetRole, e);
            List<ModuleOutline> fallback = new ArrayList<>();
            fallback.add(new ModuleOutline("Getting Started with " + targetRole,
                    List.of("Getting Started with " + targetRole)));
            return new RoadmapOutline(4, fallback);
        }
    }

    private RoadmapUpgradedTopic generateTopicWithResourcesParallel(
            RoadmapUpgradedModule module,
            String topicTitle,
            int topicOrderIndex,
            String domain,
            List<String> contentSources) {

        RoadmapUpgradedTopic topic = new RoadmapUpgradedTopic();
        topic.setModule(module);
        topic.setOrderIndex(topicOrderIndex);
        topic.setTitle(topicTitle);
        topic.setProgressPercent(0.0);

        List<CompletableFuture<RoadmapUpgradedResource>> resourceFutures = new ArrayList<>();

        for (String contentSource : contentSources) {
            if ("PDF".equals(contentSource) && SKIP_PDF) {
                log.debug("Skipping PDF generation for topic '{}'", topicTitle);
                continue;
            }

            CompletableFuture<RoadmapUpgradedResource> resourceFuture =
                    CompletableFuture.supplyAsync(() -> {
                        try {
                            switch (contentSource.toUpperCase()) {
                                case "VIDEO":
                                    log.debug("Generating VIDEO for topic '{}'", topicTitle);
                                    return generateVideoResource(topicTitle, domain);
                                case "ARTICLE":
                                    log.debug("Generating ARTICLE for topic '{}'", topicTitle);
                                    return generateArticleResource(topicTitle, domain);
                                case "PDF":
                                    log.debug("Generating PDF for topic '{}'", topicTitle);
                                    return generatePdfResource(topicTitle, domain);
                                case "QUIZ":
                                    log.debug("Generating QUIZ for topic '{}'", topicTitle);
                                    return generateQuizResource(topicTitle, domain);
                                default:
                                    log.warn("Unknown content source type: '{}'", contentSource);
                                    return null;
                            }
                        } catch (Exception e) {
                            log.warn("Failed to generate {} resource for topic '{}': {}",
                                    contentSource, topicTitle, e.getMessage());
                            return null;
                        }
                    }, PARALLEL_EXECUTOR);

            resourceFutures.add(resourceFuture);
        }

        List<RoadmapUpgradedResource> resources = resourceFutures.stream()
                .map(CompletableFuture::join)
                .filter(r -> r != null)
                .collect(Collectors.toList());

        for (RoadmapUpgradedResource resource : resources) {
            resource.setTopic(topic);
        }
        topic.setResources(resources);

        log.debug("Topic '{}' generated with {} resources", topicTitle, resources.size());
        return topic;
    }

    /**
     * Builds a mixed-format resource bundle (video + article + pdf + quiz)
     * for a single topic, per spec section 6. Returned resources have no
     * module set yet - caller attaches the owning module.
     */
    public List<RoadmapUpgradedResource> generateResourcesForTopic(String topicTitle,
                                                                     String domain,
                                                                     List<String> requestedSourceTypes) {
        List<String> sourceTypes = (requestedSourceTypes == null || requestedSourceTypes.isEmpty())
                ? ALL_CONTENT_SOURCES
                : requestedSourceTypes;

        List<RoadmapUpgradedResource> resources = new ArrayList<>();
        for (String sourceType : sourceTypes) {
            try {
                switch (sourceType.toUpperCase()) {
                    case "VIDEO" -> resources.add(generateVideoResource(topicTitle, domain));
                    case "ARTICLE" -> resources.add(generateArticleResource(topicTitle, domain));
                    case "PDF" -> resources.add(generatePdfResource(topicTitle, domain));
                    case "QUIZ" -> resources.add(generateQuizResource(topicTitle, domain));
                    default -> log.warn("Unknown content source type '{}' requested for topic '{}'", sourceType, topicTitle);
                }
            } catch (Exception e) {
                log.warn("Failed to generate {} resource for topic '{}', skipping", sourceType, topicTitle, e);
            }
        }
        return resources;
    }

    private RoadmapUpgradedResource generateVideoResource(String topicTitle, String domain) {
        String system = "You suggest well-known, high-quality tutorial video concepts. Respond ONLY with JSON, no other text.";
        String user = String.format(
                "Suggest one well-known tutorial video concept for the topic \"%s\" in domain \"%s\". "
                        + "Respond with JSON: {\"title\": \"...\", \"durationOrLength\": \"12:40\"}.",
                topicTitle, domain);
        JsonNode node = callAndParseJson(system, user);

        String suggestedTitle = node.path("title").asText(topicTitle + " - Video Tutorial");

        RoadmapUpgradedResource resource = new RoadmapUpgradedResource();
        resource.setType("VIDEO");
        resource.setTitle(suggestedTitle);
        resource.setDurationOrLength(node.path("durationOrLength").asText(null));
        resource.setSourceUrl(resolveYoutubeVideoId(suggestedTitle, topicTitle));
        resource.setCompleted(false);
        return resource;
    }

    /**
     * Looks up a real YouTube video ID for the AI-suggested video title via
     * RoadmapUpgradedYoutubeClient. Wrapped in its own try/catch, separate
     * from the OpenAI call above, so a YouTube failure (missing/invalid key,
     * quota exceeded, network error) never breaks resource/roadmap
     * generation - it just leaves sourceUrl null, same as the previous
     * "no YouTube API key wired up" behavior.
     *
     * sourceUrl stores the bare video ID (not a full URL) - the frontend
     * embeds it as https://www.youtube.com/embed/{sourceUrl}.
     */
//    private String resolveYoutubeVideoId(String suggestedTitle, String topicTitle) {
//        try {
//            String videoId = youtubeClient.searchFirstVideoId(suggestedTitle);
//            if (videoId == null || videoId.isBlank()) {
//                log.warn("YouTube search returned no results for '{}'", suggestedTitle);
//                return null;
//            }
//            return videoId;
//        } catch (Exception e) {
//            log.warn("YouTube lookup failed for topic '{}' (query='{}'), leaving sourceUrl null", topicTitle, suggestedTitle, e);
//            return null;
//        }
//    }
    private static final int POSITIVE_CACHE_DAYS = 60;
    private static final int NEGATIVE_CACHE_DAYS = 7;
    private static volatile LocalDateTime youtubeQuotaExhaustedUntil = null;

    private String resolveYoutubeVideoId(String suggestedTitle, String topicTitle) {
        String searchKey = normalizeSearchKey(suggestedTitle);

        Optional<RoadmapUpgradedVideoCache> cached = videoCacheRepository.findBySearchKey(searchKey);
        if (cached.isPresent()) {
            RoadmapUpgradedVideoCache entry = cached.get();
            int maxAgeDays = entry.getVideoId() != null ? POSITIVE_CACHE_DAYS : NEGATIVE_CACHE_DAYS;
            if (entry.getCreatedAt().isAfter(LocalDateTime.now().minusDays(maxAgeDays))) {
                log.debug("YouTube cache hit for '{}' -> {}", searchKey, entry.getVideoId());
                return entry.getVideoId();
            }
        }

        if (youtubeQuotaExhaustedUntil != null && LocalDateTime.now().isBefore(youtubeQuotaExhaustedUntil)) {
            log.debug("Skipping YouTube search for '{}', quota known exhausted until {}", searchKey, youtubeQuotaExhaustedUntil);
            return null;
        }

        try {
            String videoId = youtubeClient.searchFirstVideoId(suggestedTitle);
            saveToCache(searchKey, videoId);
            if (videoId == null || videoId.isBlank()) {
                log.warn("YouTube search returned no results for '{}'", suggestedTitle);
                return null;
            }
            return videoId;
        } catch (org.springframework.web.client.HttpClientErrorException.TooManyRequests e) {
            youtubeQuotaExhaustedUntil = LocalDateTime.now().plusHours(24);
            log.warn("YouTube quota exhausted, pausing YouTube lookups for topic '{}'", topicTitle);
            return null;
        } catch (Exception e) {
            log.warn("YouTube lookup failed for topic '{}' (query='{}'), leaving sourceUrl null", topicTitle, suggestedTitle, e);
            return null;
        }
    }

    private String normalizeSearchKey(String title) {
        return title == null ? "" : title.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    @Transactional
    public void saveToCache(String searchKey, String videoId) {
        try {
            RoadmapUpgradedVideoCache entry = videoCacheRepository.findBySearchKey(searchKey)
                    .orElseGet(RoadmapUpgradedVideoCache::new);
            entry.setSearchKey(searchKey);
            entry.setVideoId(videoId);
            entry.setCreatedAt(LocalDateTime.now());
            videoCacheRepository.save(entry);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.debug("Cache entry for '{}' already written by a concurrent thread, ignoring", searchKey);
        }
    }

    private RoadmapUpgradedResource generateArticleResource(String topicTitle, String domain) {
        // NOTE: this used to just ask for a title/concept (sourceUrl always
        // null, nothing to actually read). Now asks the model to write the
        // full article body too, stored in contentBody, so the frontend can
        // render a real in-page reader - same "store the real content, not
        // just a suggestion" upgrade that generateVideoResource() got with
        // the YouTube lookup.
        String system = "You write clear, accurate reference articles for a learning platform. "
                + "Respond ONLY with JSON, no other text.";
        String user = String.format(
                "Write one reference article for the topic \"%s\" in domain \"%s\". The article body "
                        + "should be 400 to 800 words, well-structured plain text (short paragraphs, no "
                        + "markdown headers needed). Respond with JSON: {\"title\": \"...\", "
                        + "\"durationOrLength\": \"8 min read\", \"body\": \"...\"}.",
                topicTitle, domain);
        JsonNode node = callAndParseJson(system, user);

        RoadmapUpgradedResource resource = new RoadmapUpgradedResource();
        resource.setType("ARTICLE");
        resource.setTitle(node.path("title").asText(topicTitle + " - Article"));
        resource.setDurationOrLength(node.path("durationOrLength").asText(null));
        // Falls back to null (title-only, same as before) if the model
        // response couldn't be parsed or omitted "body" - callAndParseJson
        // already returns an empty node on parse failure, so this never
        // throws, it just leaves contentBody unset like the old behavior.
        String body = node.path("body").asText(null);
        resource.setContentBody(body);
        resource.setCompleted(false);
        return resource;
    }

    private RoadmapUpgradedResource generatePdfResource(String topicTitle, String domain) {
        // Upgrade over the old "title suggestion only, no real file"
        // behavior: ask the model for real structured reference content
        // (heading + body sections, 400-800 words total), then render an
        // actual PDF server-side via Apache PDFBox and store it
        // Base64-encoded on the resource (pdfContent column) - same "store
        // the real content, not just a suggestion" upgrade
        // generateVideoResource() and generateArticleResource() already
        // got. filePath/sourceUrl stay null: there's no disk/object-storage
        // pattern anywhere else in this codebase, so the PDF lives in the
        // DB like quizContentJson and is streamed back out through
        // GET /resource/{id}/pdf, not a path.
        String system = "You write clear, well-structured reference documents for a learning platform. "
                + "Respond ONLY with JSON, no other text.";
        String user = String.format(
                "Write one reference document for the topic \"%s\" in domain \"%s\". Structure it as 3 to 6 "
                        + "sections, each with a short heading and a body paragraph. Combined body text across "
                        + "all sections should be 400 to 800 words total. Respond with JSON: "
                        + "{\"title\": \"...\", \"sections\": [{\"heading\": \"...\", \"body\": \"...\"}]}.",
                topicTitle, domain);
        JsonNode node = callAndParseJson(system, user);

        String title = node.path("title").asText(topicTitle + " - Reference");
        List<PdfSection> sections = new ArrayList<>();
        for (JsonNode sectionNode : node.path("sections")) {
            String heading = sectionNode.path("heading").asText("");
            String body = sectionNode.path("body").asText("");
            if (!heading.isBlank() || !body.isBlank()) {
                sections.add(new PdfSection(heading, body));
            }
        }
        if (sections.isEmpty()) {
            // Same "never hard-fail generation" fallback style as
            // generateRoadmapOutline() - still produce a usable resource row
            // even if the model response was empty/unparseable.
            sections.add(new PdfSection("Overview",
                    "Reference content for " + topicTitle + " is not available right now."));
        }

        RoadmapUpgradedResource resource = new RoadmapUpgradedResource();
        resource.setType("PDF");
        resource.setTitle(title);
        resource.setDurationOrLength(estimatePageCount(sections) + " pages");
        resource.setFilePath(null);
        resource.setSourceUrl(null);
        resource.setCompleted(false);

        // PDF rendering is wrapped in its own try/catch, separate from the
        // OpenAI call above, so a PDFBox failure (bad content, encoding
        // issue, etc.) never breaks resource/roadmap generation - it just
        // leaves pdfContent null, same defensive pattern as
        // resolveYoutubeVideoId() degrading to a null sourceUrl.
        try {
            byte[] pdfBytes = buildPdfDocument(title, sections);
            resource.setPdfContent(Base64.getEncoder().encodeToString(pdfBytes));
        } catch (Exception e) {
            log.warn("Failed to render PDF for topic '{}', leaving pdfContent null", topicTitle, e);
        }

        return resource;
    }

    private int estimatePageCount(List<PdfSection> sections) {
        int totalWords = sections.stream()
                .mapToInt(s -> (s.body == null || s.body.isBlank()) ? 0 : s.body.trim().split("\\s+").length)
                .sum();
        return Math.max(1, (int) Math.ceil(totalWords / 350.0));
    }

    /**
     * Renders a title + heading/body sections into an actual PDF using
     * Apache PDFBox, paginating automatically as content overflows a page.
     * The standard-14 PDF fonts (Helvetica etc.) only support WinAnsi/
     * ISO-8859-1 characters, so all text is sanitized first - AI output can
     * contain smart quotes/em-dashes/other unicode that would otherwise
     * throw when PDFBox tries to encode it.
     */
    private byte[] buildPdfDocument(String title, List<PdfSection> sections) throws IOException {
        try (PDDocument document = new PDDocument()) {
            // PDFBox 3.x removed the static PDType1Font.HELVETICA_BOLD /
            // .HELVETICA constants that existed in 2.x - standard-14 fonts
            // are now built via the Standard14Fonts.FontName enum instead.
            PDFont titleFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDFont headingFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDFont bodyFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            float margin = 50;
            float titleFontSize = 18;
            float headingFontSize = 13;
            float bodyFontSize = 11;
            float leading = 1.4f;

            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            float maxWidth = page.getMediaBox().getWidth() - 2 * margin;
            float yPosition = page.getMediaBox().getHeight() - margin;

            List<String> titleLines = wrapText(sanitizeForPdf(title), titleFont, titleFontSize, maxWidth);
            for (String line : titleLines) {
                contentStream.beginText();
                contentStream.setFont(titleFont, titleFontSize);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText(line);
                contentStream.endText();
                yPosition -= titleFontSize * leading;
            }
            yPosition -= titleFontSize * 0.6f;

            for (PdfSection section : sections) {
                List<String> headingLines = wrapText(sanitizeForPdf(section.heading), headingFont, headingFontSize, maxWidth);
                for (String line : headingLines) {
                    if (yPosition < margin + headingFontSize) {
                        contentStream.close();
                        page = new PDPage(PDRectangle.LETTER);
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        yPosition = page.getMediaBox().getHeight() - margin;
                    }
                    contentStream.beginText();
                    contentStream.setFont(headingFont, headingFontSize);
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText(line);
                    contentStream.endText();
                    yPosition -= headingFontSize * leading;
                }
                yPosition -= bodyFontSize * 0.5f;

                List<String> bodyLines = wrapText(sanitizeForPdf(section.body), bodyFont, bodyFontSize, maxWidth);
                for (String line : bodyLines) {
                    if (yPosition < margin + bodyFontSize) {
                        contentStream.close();
                        page = new PDPage(PDRectangle.LETTER);
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        yPosition = page.getMediaBox().getHeight() - margin;
                    }
                    contentStream.beginText();
                    contentStream.setFont(bodyFont, bodyFontSize);
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText(line);
                    contentStream.endText();
                    yPosition -= bodyFontSize * leading;
                }
                yPosition -= bodyFontSize;
            }
            contentStream.close();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        }
    }

    private List<String> wrapText(String text, PDFont font, float fontSize, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return lines;
        }
        for (String paragraph : text.split("\n")) {
            StringBuilder currentLine = new StringBuilder();
            for (String word : paragraph.trim().split("\\s+")) {
                String candidate = currentLine.isEmpty() ? word : currentLine + " " + word;
                float width = font.getStringWidth(candidate) / 1000 * fontSize;
                if (width > maxWidth && !currentLine.isEmpty()) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    currentLine = new StringBuilder(candidate);
                }
            }
            if (!currentLine.isEmpty()) {
                lines.add(currentLine.toString());
            }
        }
        return lines;
    }

    /**
     * The standard-14 PDF fonts only encode WinAnsi/ISO-8859-1. Strips or
     * substitutes anything outside that range so AI-generated text (smart
     * quotes, em-dashes, emoji, etc.) never throws mid-render.
     */
    private String sanitizeForPdf(String text) {
        if (text == null) {
            return "";
        }
        String normalized = text
                .replace('\u2018', '\'').replace('\u2019', '\'')
                .replace('\u201C', '"').replace('\u201D', '"')
                .replace('\u2013', '-').replace('\u2014', '-')
                .replace('\u2026', '.');
        StringBuilder sb = new StringBuilder(normalized.length());
        for (char c : normalized.toCharArray()) {
            sb.append(c <= 0xFF ? c : '?');
        }
        return sb.toString();
    }

    private static class PdfSection {
        final String heading;
        final String body;

        PdfSection(String heading, String body) {
            this.heading = heading;
            this.body = body;
        }
    }

    private RoadmapUpgradedResource generateQuizResource(String topicTitle, String domain) {
        String system = "You write multiple-choice quizzes for a learning platform. Respond ONLY with JSON, no other text.";
        String user = String.format(
                "Write 5 to 10 multiple-choice questions with correct answers for the topic \"%s\" in domain \"%s\". "
                        + "Respond with JSON: {\"questions\": [{\"question\": \"...\", \"options\": [\"...\"], "
                        + "\"correctOptionIndex\": 0}]}.",
                topicTitle, domain);
        String raw = openAiClient.completeJson(system, user);

        RoadmapUpgradedResource resource = new RoadmapUpgradedResource();
        resource.setType("QUIZ");
        resource.setTitle(topicTitle + " Quiz");
        resource.setQuizContentJson(raw);
        resource.setCompleted(false);
        return resource;
    }

    private JsonNode callAndParseJson(String systemPrompt, String userPrompt) {
        String raw = openAiClient.completeJson(systemPrompt, userPrompt);
        try {
            return objectMapper.readTree(raw);
        } catch (Exception e) {
            log.warn("Failed to parse OpenAI JSON response, using empty node", e);
            return objectMapper.createObjectNode();
        }
    }

    // =========================================================================
    // Fetch / list
    // =========================================================================

    public List<RoadmapUpgradedResponseDto> getMyRoadmaps(String token) {
        Long userId = jwtUtil.extractUserId(token);
        return repository.findByOwnerId(userId).stream()
                .map(this::toResponseDto)
                .toList();
    }

    public RoadmapUpgradedResponseDto getRoadmapById(String token, Long roadmapId) {
        RoadmapUpgradedSyllabus syllabus = getSyllabusOrThrow(roadmapId);
        verifyAccess(token, syllabus);
        return toResponseDto(syllabus);
    }

    // =========================================================================
    // Progress tracking
    // =========================================================================

    @Transactional
    public RoadmapUpgradedResponseDto markResourceComplete(String token, Long resourceId, Integer quizScoreIfAny) {
        RoadmapUpgradedSyllabus syllabus = repository.findSyllabusByResourceId(resourceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found"));
        verifyAccess(token, syllabus);

//        RoadmapUpgradedModule targetModule = null;
//        RoadmapUpgradedResource targetResource = null;
//        for (RoadmapUpgradedModule module : syllabus.getModules()) {
//            for (RoadmapUpgradedResource resource : module.getResources()) {
//                if (resource.getId() != null && resource.getId().equals(resourceId)) {
//                    targetModule = module;
//                    targetResource = resource;
//                    break;
//                }
//            }
//            if (targetResource != null) {
//                break;
//            }
//        }
//
//        if (targetResource == null) {
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found");
//        }
//
//        targetResource.setCompleted(true);
//        targetResource.setCompletedAt(LocalDateTime.now());
//        if ("QUIZ".equalsIgnoreCase(targetResource.getType()) && quizScoreIfAny != null) {
//            targetResource.setQuizScore(quizScoreIfAny);
//        }
//
//        recomputeModuleProgress(targetModule);
//        unlockNextModuleIfComplete(syllabus, targetModule);
//        recomputeSyllabusCompletion(syllabus);
        RoadmapUpgradedModule targetModule = null;
        RoadmapUpgradedResource targetResource = null;
        outer:
        for (RoadmapUpgradedModule module : syllabus.getModules()) {
            for (RoadmapUpgradedTopic topic : module.getTopics()) {
                for (RoadmapUpgradedResource resource : topic.getResources()) {
                    if (resource.getId() != null && resource.getId().equals(resourceId)) {
                        targetModule = module;
                        targetResource = resource;
                        break outer;
                    }
                }
            }
        }

        if (targetResource == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found");
        }

        targetResource.setCompleted(true);
        targetResource.setCompletedAt(LocalDateTime.now());
        if ("QUIZ".equalsIgnoreCase(targetResource.getType()) && quizScoreIfAny != null) {
            targetResource.setQuizScore(quizScoreIfAny);
        }

        recomputeModuleProgress(targetModule);
        unlockNextModuleIfComplete(syllabus, targetModule);
        recomputeSyllabusCompletion(syllabus);

        RoadmapUpgradedSyllabus saved = repository.save(syllabus);
        return toResponseDto(saved);
    }

//    private void recomputeModuleProgress(RoadmapUpgradedModule module) {
//        List<RoadmapUpgradedResource> resources = module.getResources();
//        if (resources == null || resources.isEmpty()) {
//            module.setProgressPercent(0.0);
//            return;
//        }
//        long completedCount = resources.stream().filter(r -> Boolean.TRUE.equals(r.getCompleted())).count();
//        double percent = (completedCount * 100.0) / resources.size();
//        module.setProgressPercent(percent);
//    }
    /**
     * Module progress is now resource-weighted across ALL of its topics
     * (same math as before: completed-resources / total-resources * 100),
     * not just an average of topic percentages - so a module with one huge
     * topic and one tiny topic still reflects actual work done. Topic-level
     * progressPercent is recomputed here too, since the frontend now needs a
     * per-topic bar as well as the module-level one.
     */
    private void recomputeModuleProgress(RoadmapUpgradedModule module) {
        List<RoadmapUpgradedTopic> topics = module.getTopics();
        if (topics == null || topics.isEmpty()) {
            module.setProgressPercent(0.0);
            return;
        }

        int totalResources = 0;
        int completedResources = 0;
        for (RoadmapUpgradedTopic topic : topics) {
            recomputeTopicProgress(topic);
            List<RoadmapUpgradedResource> resources = topic.getResources();
            if (resources == null) {
                continue;
            }
            totalResources += resources.size();
            completedResources += (int) resources.stream().filter(r -> Boolean.TRUE.equals(r.getCompleted())).count();
        }

        double percent = totalResources == 0 ? 0.0 : (completedResources * 100.0) / totalResources;
        module.setProgressPercent(percent);
    }

    private void recomputeTopicProgress(RoadmapUpgradedTopic topic) {
        List<RoadmapUpgradedResource> resources = topic.getResources();
        if (resources == null || resources.isEmpty()) {
            topic.setProgressPercent(0.0);
            return;
        }
        long completedCount = resources.stream().filter(r -> Boolean.TRUE.equals(r.getCompleted())).count();
        topic.setProgressPercent((completedCount * 100.0) / resources.size());
    }
    
    
    private void unlockNextModuleIfComplete(RoadmapUpgradedSyllabus syllabus, RoadmapUpgradedModule module) {
        if (module.getProgressPercent() == null || module.getProgressPercent() < 100.0) {
            return;
        }
        int nextIndex = module.getOrderIndex() == null ? -1 : module.getOrderIndex() + 1;
        for (RoadmapUpgradedModule candidate : syllabus.getModules()) {
            if (candidate.getOrderIndex() != null && candidate.getOrderIndex().equals(nextIndex)) {
                candidate.setLocked(false);
                break;
            }
        }
    }

//    private void recomputeSyllabusCompletion(RoadmapUpgradedSyllabus syllabus) {
//        int totalResources = 0;
//        int completedResources = 0;
//        for (RoadmapUpgradedModule module : syllabus.getModules()) {
//            if (module.getResources() == null) {
//                continue;
//            }
//            totalResources += module.getResources().size();
//            completedResources += (int) module.getResources().stream()
//                    .filter(r -> Boolean.TRUE.equals(r.getCompleted()))
//                    .count();
//        }
//        double percent = totalResources == 0 ? 0.0 : (completedResources * 100.0) / totalResources;
//        syllabus.setCompletionPercent(percent);
//
//        if (percent >= 100.0) {
//            syllabus.setStatus("COMPLETED");
//        } else if (percent > 0.0) {
//            syllabus.setStatus("IN_PROGRESS");
//        }
//    }
    private void recomputeSyllabusCompletion(RoadmapUpgradedSyllabus syllabus) {
        int totalResources = 0;
        int completedResources = 0;
        for (RoadmapUpgradedModule module : syllabus.getModules()) {
            if (module.getTopics() == null) {
                continue;
            }
            for (RoadmapUpgradedTopic topic : module.getTopics()) {
                if (topic.getResources() == null) {
                    continue;
                }
                totalResources += topic.getResources().size();
                completedResources += (int) topic.getResources().stream()
                        .filter(r -> Boolean.TRUE.equals(r.getCompleted()))
                        .count();
            }
        }
        double percent = totalResources == 0 ? 0.0 : (completedResources * 100.0) / totalResources;
        syllabus.setCompletionPercent(percent);

        if (percent >= 100.0) {
            syllabus.setStatus("COMPLETED");
        } else if (percent > 0.0) {
            syllabus.setStatus("IN_PROGRESS");
        }
    }
    // =========================================================================
    // PDF serving
    // =========================================================================

    /**
     * Backs GET /resource/{id}/pdf. Reuses the exact same access model as
     * markResourceComplete()/getMentorHistory() - owner, same-org admin, or
     * super admin - then decodes the Base64-stored pdfContent back to raw
     * bytes for streaming.
     */
    public byte[] getResourcePdf(String token, Long resourceId) {
        RoadmapUpgradedSyllabus syllabus = repository.findSyllabusByResourceId(resourceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found"));
        verifyAccess(token, syllabus);

//        RoadmapUpgradedResource targetResource = syllabus.getModules().stream()
//                .flatMap(m -> m.getResources().stream())
        RoadmapUpgradedResource targetResource = syllabus.getModules().stream()
                .flatMap(m -> m.getTopics().stream())
                .flatMap(t -> t.getResources().stream())
                .filter(r -> r.getId() != null && r.getId().equals(resourceId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found"));

        if (!"PDF".equalsIgnoreCase(targetResource.getType())
                || targetResource.getPdfContent() == null
                || targetResource.getPdfContent().isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No PDF available for this resource");
        }

        try {
            return Base64.getDecoder().decode(targetResource.getPdfContent());
        } catch (IllegalArgumentException e) {
            log.warn("Stored pdfContent for resourceId={} is not valid Base64", resourceId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Stored PDF is corrupted");
        }
    }

    // =========================================================================
    // Regeneration
    // =========================================================================

    @Transactional
    public RoadmapUpgradedResponseDto regenerateRemainingModules(String token, Long roadmapId) {
        RoadmapUpgradedSyllabus syllabus = getSyllabusOrThrow(roadmapId);
        verifyAccess(token, syllabus);

//        for (RoadmapUpgradedModule module : syllabus.getModules()) {
//            boolean fullyComplete = module.getProgressPercent() != null && module.getProgressPercent() >= 100.0;
//            if (fullyComplete) {
//                continue;
//            }
//            // ASSUMPTION (not in spec): contentSources isn't stored on the
//            // syllabus entity (only on the generate request DTO), so there is
//            // nothing to replay here - defaults to all four resource types.
//            List<RoadmapUpgradedResource> freshResources =
//                    generateResourcesForTopic(module.getTitle(), syllabus.getDomain(), ALL_CONTENT_SOURCES);
//            for (RoadmapUpgradedResource resource : freshResources) {
//                resource.setModule(module);
//            }
//            module.getResources().clear();
//            module.getResources().addAll(freshResources);
//            module.setProgressPercent(0.0);
//        }
        for (RoadmapUpgradedModule module : syllabus.getModules()) {
            boolean fullyComplete = module.getProgressPercent() != null && module.getProgressPercent() >= 100.0;
            if (fullyComplete) {
                continue;
            }
            // ASSUMPTION (not in spec): contentSources isn't stored on the
            // syllabus entity (only on the generate request DTO), so there is
            // nothing to replay here - defaults to all four resource types.
            // Regenerates per TOPIC now, not per module - each topic keeps its
            // own title/order, only its resource bundle is refreshed.
            for (RoadmapUpgradedTopic topic : module.getTopics()) {
                List<RoadmapUpgradedResource> freshResources =
                        generateResourcesForTopic(topic.getTitle(), syllabus.getDomain(), ALL_CONTENT_SOURCES);
                for (RoadmapUpgradedResource resource : freshResources) {
                    resource.setTopic(topic);
                }
                topic.getResources().clear();
                topic.getResources().addAll(freshResources);
                topic.setProgressPercent(0.0);
            }
            module.setProgressPercent(0.0);
        }

        recomputeSyllabusCompletion(syllabus);
        syllabus.setLastRegeneratedAt(LocalDateTime.now());

        RoadmapUpgradedSyllabus saved = repository.save(syllabus);
        return toResponseDto(saved);
    }

    // =========================================================================
    // Clone (trainer batch template)
    // =========================================================================

    @Transactional
    public RoadmapUpgradedResponseDto cloneAsTemplate(String token, Long roadmapId) {
        RoadmapUpgradedSyllabus source = getSyllabusOrThrow(roadmapId);
        verifyAccess(token, source);

        Long userId = jwtUtil.extractUserId(token);
        String role = jwtUtil.extractRole(token);
        String organizationId = jwtUtil.extractOrganizationIdOrNull(token);

        RoadmapUpgradedSyllabus clone = cloneSyllabusStructure(
                source, userId, role, organizationId, source.getSourceType(), "READY");

        RoadmapUpgradedSyllabus saved = repository.save(clone);
        return toResponseDto(saved);
    }

    /**
     * Deep-copies a syllabus tree (modules + resources) under a new owner,
     * with progress reset to a fresh start (nothing completed, only the
     * first module unlocked). Used both by the "clone as template" endpoint
     * and by the library fast-path in generateRoadmap().
     */
    private RoadmapUpgradedSyllabus cloneSyllabusStructure(RoadmapUpgradedSyllabus source,
                                                             Long newOwnerId,
                                                             String newOwnerRole,
                                                             String newOrganizationId,
                                                             String sourceType,
                                                             String status) {
        RoadmapUpgradedSyllabus clone = new RoadmapUpgradedSyllabus();
        clone.setOwnerId(newOwnerId);
        clone.setOwnerRole(newOwnerRole);
        clone.setOrganizationId(newOrganizationId);
        clone.setDomain(source.getDomain());
        clone.setPathType(source.getPathType());
        clone.setTargetRole(source.getTargetRole());
        clone.setLanguage(source.getLanguage());
        clone.setSourceType(sourceType);
        clone.setStatus(status);
        clone.setTotalWeeks(source.getTotalWeeks());
        clone.setTotalModules(source.getTotalModules());
        clone.setCompletionPercent(0.0);
        clone.setCreatedAt(LocalDateTime.now());

//        List<RoadmapUpgradedModule> clonedModules = new ArrayList<>();
//        for (RoadmapUpgradedModule sourceModule : source.getModules()) {
//            RoadmapUpgradedModule clonedModule = new RoadmapUpgradedModule();
//            clonedModule.setSyllabus(clone);
//            clonedModule.setOrderIndex(sourceModule.getOrderIndex());
//            clonedModule.setTitle(sourceModule.getTitle());
//            clonedModule.setPrerequisiteModuleId(sourceModule.getPrerequisiteModuleId());
//            clonedModule.setLocked(sourceModule.getOrderIndex() != null && sourceModule.getOrderIndex() != 0);
//            clonedModule.setProgressPercent(0.0);
//
//            List<RoadmapUpgradedResource> clonedResources = new ArrayList<>();
//            for (RoadmapUpgradedResource sourceResource : sourceModule.getResources()) {
//                RoadmapUpgradedResource clonedResource = new RoadmapUpgradedResource();
//                clonedResource.setModule(clonedModule);
//                clonedResource.setType(sourceResource.getType());
//                clonedResource.setTitle(sourceResource.getTitle());
//                clonedResource.setSourceUrl(sourceResource.getSourceUrl());
//                clonedResource.setFilePath(sourceResource.getFilePath());
//                clonedResource.setDurationOrLength(sourceResource.getDurationOrLength());
//                clonedResource.setQuizContentJson(sourceResource.getQuizContentJson());
//                clonedResource.setContentBody(sourceResource.getContentBody());
//                clonedResource.setPdfContent(sourceResource.getPdfContent());
//                clonedResource.setCompleted(false);
//                clonedResource.setQuizScore(null);
//                clonedResource.setCompletedAt(null);
//                clonedResources.add(clonedResource);
//            }
//            clonedModule.setResources(clonedResources);
//            clonedModules.add(clonedModule);
//        }
//        clone.setModules(clonedModules);
        List<RoadmapUpgradedModule> clonedModules = new ArrayList<>();
        for (RoadmapUpgradedModule sourceModule : source.getModules()) {
            RoadmapUpgradedModule clonedModule = new RoadmapUpgradedModule();
            clonedModule.setSyllabus(clone);
            clonedModule.setOrderIndex(sourceModule.getOrderIndex());
            clonedModule.setTitle(sourceModule.getTitle());
            clonedModule.setPrerequisiteModuleId(sourceModule.getPrerequisiteModuleId());
            clonedModule.setLocked(sourceModule.getOrderIndex() != null && sourceModule.getOrderIndex() != 0);
            clonedModule.setProgressPercent(0.0);

            List<RoadmapUpgradedTopic> clonedTopics = new ArrayList<>();
            for (RoadmapUpgradedTopic sourceTopic : sourceModule.getTopics()) {
                RoadmapUpgradedTopic clonedTopic = new RoadmapUpgradedTopic();
                clonedTopic.setModule(clonedModule);
                clonedTopic.setOrderIndex(sourceTopic.getOrderIndex());
                clonedTopic.setTitle(sourceTopic.getTitle());
                clonedTopic.setProgressPercent(0.0);

                List<RoadmapUpgradedResource> clonedResources = new ArrayList<>();
                for (RoadmapUpgradedResource sourceResource : sourceTopic.getResources()) {
                    RoadmapUpgradedResource clonedResource = new RoadmapUpgradedResource();
                    clonedResource.setTopic(clonedTopic);
                    clonedResource.setType(sourceResource.getType());
                    clonedResource.setTitle(sourceResource.getTitle());
                    clonedResource.setSourceUrl(sourceResource.getSourceUrl());
                    clonedResource.setFilePath(sourceResource.getFilePath());
                    clonedResource.setDurationOrLength(sourceResource.getDurationOrLength());
                    clonedResource.setQuizContentJson(sourceResource.getQuizContentJson());
                    clonedResource.setContentBody(sourceResource.getContentBody());
                    clonedResource.setPdfContent(sourceResource.getPdfContent());
                    clonedResource.setCompleted(false);
                    clonedResource.setQuizScore(null);
                    clonedResource.setCompletedAt(null);
                    clonedResources.add(clonedResource);
                }
                clonedTopic.setResources(clonedResources);
                clonedTopics.add(clonedTopic);
            }
            clonedModule.setTopics(clonedTopics);
            clonedModules.add(clonedModule);
        }
        clone.setModules(clonedModules);

        return clone;
    }

    // =========================================================================
    // Admin stats (org-scoped)
    // =========================================================================

    public RoadmapUpgradedAdminStatsDto getAdminStats(String token) {
        String role = jwtUtil.extractRole(token);
        if (!jwtUtil.isOrgAdminRole(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
        }
        String organizationId = jwtUtil.extractOrganizationIdOrNull(token);
        if (organizationId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Admin account is missing an organizationId - this account is misconfigured");
        }
        return buildAdminStatsForOrg(organizationId);
    }

    private RoadmapUpgradedAdminStatsDto buildAdminStatsForOrg(String organizationId) {
        Long totalRoadmapsInOrg = repository.countByOrganizationId(organizationId);
        Long totalStudentsInOrg = repository.countDistinctOwnersByOrganizationIdAndOwnerRole(organizationId, "STUDENT");
        Long totalTrainersInOrg = repository.countDistinctOwnersByOrganizationIdAndOwnerRole(organizationId, "TRAINER");

        Map<String, Long> pathTypeBreakdown = new LinkedHashMap<>();
        for (Object[] row : repository.findPathTypeBreakdownByOrganizationId(organizationId)) {
            String pathType = (String) row[0];
            Long count = (Long) row[1];
            pathTypeBreakdown.put(pathType, count);
        }

        List<RoadmapUpgradedUserUsageDto> topStudents =
                buildUsageList(repository.findTopUsersByOrgAndRole(organizationId, "STUDENT"), "STUDENT", organizationId);
        List<RoadmapUpgradedUserUsageDto> topTrainers =
                buildUsageList(repository.findTopUsersByOrgAndRole(organizationId, "TRAINER"), "TRAINER", organizationId);

        return new RoadmapUpgradedAdminStatsDto(
                organizationId, totalRoadmapsInOrg, totalStudentsInOrg, totalTrainersInOrg,
                pathTypeBreakdown, topStudents, topTrainers);
    }

    private List<RoadmapUpgradedUserUsageDto> buildUsageList(List<Object[]> ownerIdCountRows,
                                                               String role,
                                                               String organizationId) {
        List<RoadmapUpgradedUserUsageDto> usage = new ArrayList<>();
        int limit = Math.min(ownerIdCountRows.size(), TOP_USAGE_LIMIT);
        for (int i = 0; i < limit; i++) {
            Object[] row = ownerIdCountRows.get(i);
            Long ownerId = (Long) row[0];
            Long count = (Long) row[1];
            Double avgCompletion = repository.findAvgCompletionPercentByOwnerId(ownerId);
            usage.add(new RoadmapUpgradedUserUsageDto(
                    ownerId, null, role, organizationId, count, avgCompletion == null ? 0.0 : avgCompletion));
        }
        return usage;
    }

    // =========================================================================
    // Super admin stats (cross-org)
    // =========================================================================

    public RoadmapUpgradedSuperAdminStatsDto getSuperAdminStats(String token) {
        String role = jwtUtil.extractRole(token);
        if (!jwtUtil.isSuperAdmin(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Super admin access required");
        }

        List<String> orgIds = repository.findAllDistinctOrganizationIds();
        List<RoadmapUpgradedAdminStatsDto> perOrgBreakdown = orgIds.stream()
                .map(this::buildAdminStatsForOrg)
                .toList();

        List<RoadmapUpgradedUserUsageDto> nullOrgStudents =
                buildUsageList(repository.findTopNullOrgUsersByRole("STUDENT"), "STUDENT", null);
        List<RoadmapUpgradedUserUsageDto> nullOrgTrainers =
                buildUsageList(repository.findTopNullOrgUsersByRole("TRAINER"), "TRAINER", null);

        List<RoadmapUpgradedUserUsageDto> topUsersPlatformWide = new ArrayList<>();
        List<Object[]> platformRows = repository.findTopUsersPlatformWide();
        int limit = Math.min(platformRows.size(), TOP_PLATFORM_USAGE_LIMIT);
        for (int i = 0; i < limit; i++) {
            Object[] row = platformRows.get(i);
            Long ownerId = (Long) row[0];
            String ownerRole = (String) row[1];
            String orgId = (String) row[2];
            Long count = (Long) row[3];
            Double avgCompletion = repository.findAvgCompletionPercentByOwnerId(ownerId);
            topUsersPlatformWide.add(new RoadmapUpgradedUserUsageDto(
                    ownerId, null, ownerRole, orgId, count, avgCompletion == null ? 0.0 : avgCompletion));
        }

        return new RoadmapUpgradedSuperAdminStatsDto(
                (long) orgIds.size(),
                repository.count(),
                perOrgBreakdown,
                nullOrgStudents,
                nullOrgTrainers,
                topUsersPlatformWide);
    }

    // =========================================================================
    // AI Mentor
    // =========================================================================

    @Transactional
    public RoadmapUpgradedMentorResponseDto askMentor(String token, RoadmapUpgradedMentorRequestDto request) {
        Long userId = jwtUtil.extractUserId(token);
        RoadmapUpgradedSyllabus syllabus = getSyllabusOrThrow(request.getSyllabusId());
        verifyAccess(token, syllabus);

        String role = jwtUtil.extractRole(token);
        String systemPrompt = buildMentorSystemPrompt(syllabus, role);

        List<RoadmapUpgradedMentorMessage> history = mentorRepository
                .findBySyllabusIdOrderBySentAtAsc(syllabus.getId());
        List<RoadmapUpgradedOpenAiClient.RoadmapUpgradedChatTurn> recentTurns = history.stream()
                .skip(Math.max(0, history.size() - MENTOR_HISTORY_TURNS))
                .map(m -> new RoadmapUpgradedOpenAiClient.RoadmapUpgradedChatTurn(
                        "USER".equalsIgnoreCase(m.getSender()) ? "user" : "assistant",
                        m.getMessageText()))
                .toList();

        String rawJson = openAiClient.completeJsonWithHistory(systemPrompt, recentTurns, request.getMessage());

        String reply;
        List<String> suggestedFollowUps = new ArrayList<>();
        try {
            JsonNode node = objectMapper.readTree(rawJson);
            reply = node.path("reply").asText();
            for (JsonNode followUp : node.path("suggestedFollowUps")) {
                suggestedFollowUps.add(followUp.asText());
            }
        } catch (Exception e) {
            log.warn("Failed to parse mentor JSON response for syllabusId={}, using raw text as reply", syllabus.getId(), e);
            reply = rawJson;
        }

        LocalDateTime now = LocalDateTime.now();
        RoadmapUpgradedMentorMessage userMessage = new RoadmapUpgradedMentorMessage(
                null, syllabus.getId(), userId, "USER", request.getMessage(), now);
        RoadmapUpgradedMentorMessage mentorMessage = new RoadmapUpgradedMentorMessage(
                null, syllabus.getId(), userId, "MENTOR", reply, now.plusNanos(1));
        mentorRepository.save(userMessage);
        mentorRepository.save(mentorMessage);

        return new RoadmapUpgradedMentorResponseDto(reply, suggestedFollowUps);
    }

    public List<RoadmapUpgradedMentorMessageDto> getMentorHistory(String token, Long syllabusId) {
        RoadmapUpgradedSyllabus syllabus = getSyllabusOrThrow(syllabusId);
        verifyAccess(token, syllabus);

        return mentorRepository.findBySyllabusIdOrderBySentAtAsc(syllabusId).stream()
                .map(m -> new RoadmapUpgradedMentorMessageDto(m.getId(), m.getSender(), m.getMessageText(), m.getSentAt()))
                .toList();
    }

    private String buildMentorSystemPrompt(RoadmapUpgradedSyllabus syllabus, String role) {
        RoadmapUpgradedModule currentModule = syllabus.getModules().stream()
                .filter(m -> m.getProgressPercent() == null || m.getProgressPercent() < 100.0)
                .min(Comparator.comparing(m -> m.getOrderIndex() == null ? Integer.MAX_VALUE : m.getOrderIndex()))
                .orElse(syllabus.getModules().isEmpty() ? null : syllabus.getModules().get(syllabus.getModules().size() - 1));

        String currentModuleTitle = currentModule != null ? currentModule.getTitle() : "N/A";
        Integer currentModuleIndex = currentModule != null ? currentModule.getOrderIndex() : null;

//        RoadmapUpgradedResource lastResource = syllabus.getModules().stream()
//                .flatMap(m -> m.getResources().stream())
        RoadmapUpgradedResource lastResource = syllabus.getModules().stream()
                .flatMap(m -> m.getTopics().stream())
                .flatMap(t -> t.getResources().stream())
                .filter(r -> Boolean.TRUE.equals(r.getCompleted()) && r.getCompletedAt() != null)
                .max(Comparator.comparing(RoadmapUpgradedResource::getCompletedAt))
                .orElse(null);

        String lastResourceTitle = lastResource != null ? lastResource.getTitle() : "N/A";
        String lastResourceType = lastResource != null ? lastResource.getType() : "N/A";

        return String.format(
                "You are an expert, encouraging AI learning mentor inside a roadmap-based " +
                        "learning platform. You are currently helping a %s with their roadmap " +
                        "titled \"%s\" (%s).\n\n" +
                        "Current progress: %.1f%% complete, currently on Module %s: \"%s\".\n" +
                        "Recent resource: \"%s\" (%s).\n\n" +
                        "Rules for your replies:\n" +
                        "- Be specific to this exact module and topic, never generic filler.\n" +
                        "- If the learner seems stuck, offer one concrete next action (a specific " +
                        "resource, a smaller sub-step, or a mental model), not vague encouragement.\n" +
                        "- Keep replies to 2-4 sentences unless the learner explicitly asks for " +
                        "something longer (like a full explanation or a study plan).\n" +
                        "- Warm and encouraging tone, but never patronizing or generic (\"great " +
                        "job!\" alone is not acceptable - always pair encouragement with " +
                        "something actually useful).\n" +
                        "- If asked about material outside this roadmap's scope, answer briefly " +
                        "and helpfully anyway, then gently connect it back to the current " +
                        "module if relevant.\n\n" +
                        "Respond ONLY with a JSON object of the form: " +
                        "{\"reply\": \"...\", \"suggestedFollowUps\": [\"...\", \"...\", \"...\"]}. " +
                        "suggestedFollowUps should be 2 to 3 short natural follow-up questions the " +
                        "learner might want to tap next.",
                role, syllabus.getTargetRole(), syllabus.getDomain(),
                syllabus.getCompletionPercent() == null ? 0.0 : syllabus.getCompletionPercent(),
                currentModuleIndex == null ? "N/A" : currentModuleIndex, currentModuleTitle,
                lastResourceTitle, lastResourceType
        );
    }

    // =========================================================================
    // Access control
    // =========================================================================

    private RoadmapUpgradedSyllabus getSyllabusOrThrow(Long roadmapId) {
        return repository.findById(roadmapId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Roadmap not found"));
    }

    /**
     * Enforces the access model from spec section 1:
     *  - owner always has access to their own roadmap
     *  - ADMIN/TENANT_ADMIN has access to roadmaps within their own organizationId
     *  - SUPER_ADMIN has access to everything, including null-org roadmaps
     */
    private void verifyAccess(String token, RoadmapUpgradedSyllabus syllabus) {
        Long userId = jwtUtil.extractUserId(token);
        String role = jwtUtil.extractRole(token);
        String organizationId = jwtUtil.extractOrganizationIdOrNull(token);

        if (syllabus.getOwnerId() != null && syllabus.getOwnerId().equals(userId)) {
            return;
        }
        if (jwtUtil.isSuperAdmin(role)) {
            return;
        }
        if (jwtUtil.isOrgAdminRole(role) && organizationId != null && organizationId.equals(syllabus.getOrganizationId())) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this roadmap");
    }

    // =========================================================================
    // Entity -> DTO mapping
    // =========================================================================

    private RoadmapUpgradedResponseDto toResponseDto(RoadmapUpgradedSyllabus syllabus) {
        List<RoadmapUpgradedModuleDto> moduleDtos = syllabus.getModules().stream()
                .map(this::toModuleDto)
                .toList();

        return new RoadmapUpgradedResponseDto(
                syllabus.getId(),
                syllabus.getTargetRole(),
                syllabus.getDomain(),
                syllabus.getPathType(),
                syllabus.getStatus(),
                syllabus.getTotalWeeks(),
                syllabus.getTotalModules(),
                syllabus.getCompletionPercent(),
                syllabus.getOwnerRole(),
                syllabus.getOrganizationId(),
                moduleDtos
        );
    }

//    private RoadmapUpgradedModuleDto toModuleDto(RoadmapUpgradedModule module) {
//        List<RoadmapUpgradedResourceDto> resourceDtos = module.getResources().stream()
//                .map(this::toResourceDto)
//                .toList();
//
//        return new RoadmapUpgradedModuleDto(
//                module.getId(),
//                module.getOrderIndex(),
//                module.getTitle(),
//                module.getLocked(),
//                module.getProgressPercent(),
//                resourceDtos
//        );
//    }
    private RoadmapUpgradedModuleDto toModuleDto(RoadmapUpgradedModule module) {
        List<RoadmapUpgradedTopicDto> topicDtos = module.getTopics().stream()
                .map(this::toTopicDto)
                .toList();

        return new RoadmapUpgradedModuleDto(
                module.getId(),
                module.getOrderIndex(),
                module.getTitle(),
                module.getLocked(),
                module.getProgressPercent(),
                topicDtos
        );
    }

    private RoadmapUpgradedTopicDto toTopicDto(RoadmapUpgradedTopic topic) {
        List<RoadmapUpgradedResourceDto> resourceDtos = topic.getResources().stream()
                .map(this::toResourceDto)
                .toList();

        return new RoadmapUpgradedTopicDto(
                topic.getId(),
                topic.getOrderIndex(),
                topic.getTitle(),
                topic.getProgressPercent(),
                resourceDtos
        );
    }

    private RoadmapUpgradedResourceDto toResourceDto(RoadmapUpgradedResource resource) {
        boolean hasPdf = "PDF".equals(resource.getType())
                && resource.getPdfContent() != null
                && !resource.getPdfContent().isBlank();

        return new RoadmapUpgradedResourceDto(
                resource.getId(),
                resource.getType(),
                resource.getTitle(),
                resource.getSourceUrl(),
                resource.getFilePath(),
                resource.getDurationOrLength(),
                resource.getCompleted(),
                resource.getQuizScore(),
                // Only send quiz content for quiz resources, and never once
                // completed - no reason to keep shipping the answer key down
                // to the client after it's been graded.
                "QUIZ".equals(resource.getType()) && !Boolean.TRUE.equals(resource.getCompleted())
                        ? resource.getQuizContentJson()
                        : null,
                // Only send the article body for article resources. Unlike
                // the quiz answer key, there's no reason to hide this after
                // completion - keep showing it so a learner can reread it.
                "ARTICLE".equals(resource.getType())
                        ? resource.getContentBody()
                        : null,
                // hasPdf tells the frontend whether to fetch/open the PDF
                // (GET /resource/{id}/pdf) - the (Base64) bytes themselves
                // are never sent inline here.
                hasPdf
        );
    }

    /**
     * Internal holder for the AI-generated outline (see generateRoadmapOutline).
     */
    private static class RoadmapOutline {
        final Integer totalWeeks;
        final List<ModuleOutline> modules;

        RoadmapOutline(Integer totalWeeks, List<ModuleOutline> modules) {
            this.totalWeeks = totalWeeks;
            this.modules = modules;
        }
    }

    /**
     * One module's worth of outline: a title plus its 3-5 topic titles.
     */
    private static class ModuleOutline {
        final String title;
        final List<String> topicTitles;

        ModuleOutline(String title, List<String> topicTitles) {
            this.title = title;
            this.topicTitles = topicTitles;
        }
    }
    /**
     * Backs GET /{id}/export-pdf - a single PDF of the ENTIRE roadmap
     * (every module, every topic, every resource title + completion state).
     * This is a new, separate feature from getResourcePdf() above, which
     * only ever streams one resource's own generated PDF. Reuses
     * buildPdfDocument()/PdfSection exactly as-is; no new PDF rendering code.
     * Same access model as every other per-roadmap endpoint.
     */
    public byte[] exportRoadmapPdf(String token, Long roadmapId) {
        RoadmapUpgradedSyllabus syllabus = getSyllabusOrThrow(roadmapId);
        verifyAccess(token, syllabus);

        List<PdfSection> sections = new ArrayList<>();
        for (RoadmapUpgradedModule module : syllabus.getModules()) {
            int moduleNumber = (module.getOrderIndex() == null ? 0 : module.getOrderIndex()) + 1;
            StringBuilder moduleIntro = new StringBuilder();
            moduleIntro.append(module.getTopics().size()).append(" topic")
                    .append(module.getTopics().size() == 1 ? "" : "s").append(" · ")
                    .append(Math.round(module.getProgressPercent() == null ? 0.0 : module.getProgressPercent()))
                    .append("% complete");
            sections.add(new PdfSection("Module " + moduleNumber + ": " + module.getTitle(), moduleIntro.toString()));

            for (RoadmapUpgradedTopic topic : module.getTopics()) {
                StringBuilder body = new StringBuilder();
                for (RoadmapUpgradedResource resource : topic.getResources()) {
                    body.append("- [").append(resource.getType()).append("] ").append(resource.getTitle());
                    if (Boolean.TRUE.equals(resource.getCompleted())) {
                        body.append("  (Completed)");
                    }
                    body.append("\n");
                }
                if (body.isEmpty()) {
                    body.append("No resources generated for this topic.");
                }
                sections.add(new PdfSection("  " + topic.getTitle(), body.toString()));
            }
        }

        if (sections.isEmpty()) {
            sections.add(new PdfSection("No modules yet", "This roadmap has no generated content."));
        }

        try {
            return buildPdfDocument(syllabus.getTargetRole() + " - Full Roadmap", sections);
        } catch (IOException e) {
            log.warn("Failed to render full-roadmap PDF for roadmapId={}", roadmapId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate roadmap PDF");
        }
    }
    @Transactional
    public RoadmapUpgradedResponseDto startRoadmapGeneration(String token, RoadmapUpgradedGenerateRequestDto request) {
        Long userId = jwtUtil.extractUserId(token);
        String role = jwtUtil.extractRole(token);
        String organizationId = jwtUtil.extractOrganizationIdOrNull(token);

        boolean fromLibrary = Boolean.TRUE.equals(request.getFromLibrary());

        if (fromLibrary) {
            Optional<RoadmapUpgradedSyllabus> cached = repository
                    .findFirstByTargetRoleAndSourceTypeAndStatus(request.getTargetRole(), "LIBRARY", "READY")
                    .stream()
                    .findFirst();
            if (cached.isPresent()) {
                // Fast path: cloning is cheap (no AI/YouTube calls), stays synchronous.
                RoadmapUpgradedSyllabus clone = cloneSyllabusStructure(
                        cached.get(), userId, role, organizationId, "LIBRARY", "READY");
                RoadmapUpgradedSyllabus saved = repository.save(clone);
                return toResponseDto(saved);
            }
        }

        // Slow path: create the shell row immediately, generate in the
        // background, return right away with status=GENERATING.
        List<String> contentSources = (request.getContentSources() == null || request.getContentSources().isEmpty())
                ? ALL_CONTENT_SOURCES
                : request.getContentSources();

        RoadmapUpgradedSyllabus shell = newSyllabusShell(userId, role, organizationId, request,
                fromLibrary ? "LIBRARY" : "GENERATED", "GENERATING");
        RoadmapUpgradedSyllabus savedShell = repository.save(shell);

        CompletableFuture.runAsync(
                () -> populateInBackground(savedShell.getId(), request, contentSources),
                PARALLEL_EXECUTOR
        ).exceptionally(ex -> {
            log.error("Background roadmap generation failed for syllabusId={}", savedShell.getId(), ex);
            markGenerationFailed(savedShell.getId());
            return null;
        });

        return toResponseDto(savedShell);
    }

    @Transactional
    public void populateInBackground(Long syllabusId, RoadmapUpgradedGenerateRequestDto request, List<String> contentSources) {
        RoadmapUpgradedSyllabus syllabus = repository.findById(syllabusId)
                .orElseThrow(() -> new IllegalStateException("Syllabus shell disappeared before generation: " + syllabusId));
        populateModulesAndResources(syllabus, request, contentSources);
        syllabus.setStatus("READY");
        repository.save(syllabus);
    }

    @Transactional
    public void markGenerationFailed(Long syllabusId) {
        repository.findById(syllabusId).ifPresent(s -> {
            s.setStatus("FAILED");
            repository.save(s);
        });
    }
}