
package com.lms.course.service;

import com.lms.course.dto.*;
import com.lms.course.exception.ResourceNotFoundException;
import com.lms.course.kafka.FeaturedProgramKafkaProducer;
import com.lms.course.model.FeaturedProgram;
import com.lms.course.model.FeaturedProgramFAQ;
import com.lms.course.model.ProgramProject;
import com.lms.course.model.SyllabusModule;
import com.lms.course.model.SyllabusSession;
import com.lms.course.model.SyllabusWeek;
import com.lms.course.repository.FeaturedProgramFAQRepository;
import com.lms.course.repository.FeaturedProgramRepository;
import com.lms.course.repository.ProgramProjectRepository;
import com.lms.course.repository.SyllabusWeekRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class FeaturedProgramService {

    private final FeaturedProgramRepository featuredProgramRepository;
    private final FeaturedProgramFAQRepository faqRepository;
    private final SyllabusWeekRepository syllabusWeekRepository;
    private final ProgramProjectRepository programProjectRepository;
    private final OpenAIService openAIService;
    private final FeaturedProgramKafkaProducer featuredProgramKafkaProducer;

    public FeaturedProgramService(FeaturedProgramRepository featuredProgramRepository,
                                   FeaturedProgramFAQRepository faqRepository,
                                   SyllabusWeekRepository syllabusWeekRepository,
                                   ProgramProjectRepository programProjectRepository,
                                   OpenAIService openAIService,
                                   FeaturedProgramKafkaProducer featuredProgramKafkaProducer) {
        this.featuredProgramRepository = featuredProgramRepository;
        this.faqRepository = faqRepository;
        this.syllabusWeekRepository = syllabusWeekRepository;
        this.programProjectRepository = programProjectRepository;
        this.openAIService = openAIService;
        this.featuredProgramKafkaProducer = featuredProgramKafkaProducer;
    }

    @Transactional(readOnly = true)
    public List<FeaturedProgramResponseDTO> getAllActivePrograms() {
        return featuredProgramRepository.findAllByStatusAndPublishStatusOrderByDisplayOrderAsc("Active", "Published")
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FeaturedProgramResponseDTO> getProgramsByCategory(String category) {
        return featuredProgramRepository.findAllByCategoryIgnoreCaseAndStatusAndPublishStatus(category, "Active", "Published")
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FeaturedProgramResponseDTO getProgramById(Long id) {
        FeaturedProgram program = featuredProgramRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Featured program not found with id: " + id));
        return mapToResponseDTO(program);
    }

    @Transactional(readOnly = true)
    public List<SyllabusWeekDto> getProgramSyllabus(Long id) {
        if (!featuredProgramRepository.existsById(id)) {
            throw new ResourceNotFoundException("Featured program not found with id: " + id);
        }
        List<SyllabusWeek> weeks = syllabusWeekRepository.findByProgramIdOrderByWeekNumberAsc(id);
        List<SyllabusWeekDto> result = new ArrayList<>();
        for (int i = 0; i < weeks.size(); i++) {
            result.add(mapWeekToSyllabusDto(weeks.get(i), i == 0));
        }
        return result;
    }

    // `locked` is computed here only, never persisted: false for the first module of the
    // first week, true for everything else.
    // HOOK POINT: replace this with an enrollment-service check in future.
    private SyllabusWeekDto mapWeekToSyllabusDto(SyllabusWeek week, boolean isFirstWeek) {
        List<SyllabusModuleDto> moduleDtos = new ArrayList<>();
        List<SyllabusModule> modules = week.getModules();
        for (int m = 0; m < modules.size(); m++) {
            SyllabusModule module = modules.get(m);
            boolean unlocked = isFirstWeek && m == 0;

            List<SyllabusSessionDto> sessionDtos = new ArrayList<>();
//            for (SyllabusSession session : module.getSessions()) {
//            	sessionDtos.add(new SyllabusSessionDto(
//                        session.getId(),
//                        session.getTitle(),
//                        session.getType(),
//                        session.getDuration(),
//                        session.getOrderIndex(),
//                        session.getVideoId(),
//                        session.getVideoTitle(),
//                        session.getVideoDescription(),
//                        session.getVideoUrl(),
//                        session.getVideoThumbnailUrl(),
//                        session.getVideoDurationSeconds(),
//                        session.getVideoStatus(),
//                        !unlocked));
//            	
//            }
            for (SyllabusSession session : module.getSessions()) {
            	SyllabusSessionDto sessionDto = new SyllabusSessionDto(
                        session.getId(),
                        session.getTitle(),
                        session.getType(),
                        session.getDuration(),
                        session.getOrderIndex(),
                        session.getVideoId(),
                        session.getVideoTitle(),
                        session.getVideoDescription(),
                        session.getVideoUrl(),
                        session.getVideoThumbnailUrl(),
                        session.getVideoDurationSeconds(),
                        session.getVideoStatus(),
                        !unlocked);
            	// ── NEW: file fields, same additive pattern as video above ──
            	sessionDto.setFileId(session.getFileId());
            	sessionDto.setFileUrl(session.getFileUrl());
            	sessionDto.setFileName(session.getFileName());
            	sessionDto.setFileStatus(session.getFileStatus());
            	sessionDtos.add(sessionDto);
            }
            moduleDtos.add(new SyllabusModuleDto(module.getId(), module.getTitle(), module.getOrderIndex(), sessionDtos));
        }
        return new SyllabusWeekDto(
                week.getId(),
                week.getWeekNumber(),
                week.getTitle(),
                week.getDateRange(),
                week.getItems(),
                moduleDtos);
    }

   
    @Transactional(readOnly = true)
    public List<FeaturedProgramResponseDTO> getAllProgramsForAdmin() {
        return featuredProgramRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AdminStatsDTO getAdminStats() {
        long total = featuredProgramRepository.count();
        long active = featuredProgramRepository.countByStatus("Active");
        long inactive = featuredProgramRepository.countByStatus("Inactive");
        long categories = featuredProgramRepository.findDistinctCategories().size();
        return new AdminStatsDTO(total, active, inactive, categories);
    }

    @Transactional
    public FeaturedProgramResponseDTO createProgram(FeaturedProgramRequestDTO dto) {
        FeaturedProgram program = new FeaturedProgram();
        applyDtoToEntity(dto, program);

        if (program.getSlug() == null || program.getSlug().isBlank()) {
            program.setSlug(generateSlug(program.getTitle()));
        }

//        attachFaqs(dto, program);
//        attachSyllabusWeeks(dto, program);
//        attachProjects(dto, program);
        attachFaqs(dto, program);
        syncSyllabusWeeks(dto, program);
        attachProjects(dto, program);

        FeaturedProgram saved = featuredProgramRepository.save(program);
        return mapToResponseDTO(saved);
    }

    @Transactional
    public FeaturedProgramResponseDTO updateProgram(Long id, FeaturedProgramRequestDTO dto) {
        FeaturedProgram program = featuredProgramRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Featured program not found with id: " + id));

        applyDtoToEntity(dto, program);

        if (dto.getSlug() != null && !dto.getSlug().isBlank()) {
            program.setSlug(dto.getSlug());
        }

//        program.getFaqs().clear();
//        program.getSyllabusWeeks().clear();
//        program.getProjectsList().clear();
//
//        attachFaqs(dto, program);
//        attachSyllabusWeeks(dto, program);
//        attachProjects(dto, program);
        program.getFaqs().clear();
        program.getProjectsList().clear();

        attachFaqs(dto, program);
        syncSyllabusWeeks(dto, program);   // merges by id — does NOT clear+rebuild, preserves video fields
        attachProjects(dto, program);

        FeaturedProgram saved = featuredProgramRepository.save(program);
        return mapToResponseDTO(saved);
    }

    @Transactional
    public void deleteProgram(Long id) {
        FeaturedProgram program = featuredProgramRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Featured program not found with id: " + id));

        // Gather every session id under this program (Week -> Module -> Session) before
        // deletion, since video-service cannot resolve programId -> sessionIds on its own.
        List<Long> sessionIds = program.getSyllabusWeeks().stream()
                .flatMap(week -> week.getModules().stream())
                .flatMap(module -> module.getSessions().stream())
                .map(SyllabusSession::getId)
                .collect(Collectors.toList());

        if (!sessionIds.isEmpty()) {
            featuredProgramKafkaProducer.publishFeaturedProgramDeleted(id, sessionIds);
        }

        featuredProgramRepository.delete(program);
    }

    @Transactional
    public FeaturedProgramResponseDTO publishProgram(Long id) {
        FeaturedProgram program = featuredProgramRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Featured program not found with id: " + id));
        program.setPublishStatus("Published");
        FeaturedProgram saved = featuredProgramRepository.save(program);
        return mapToResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public FeaturedProgramRequestDTO generateWithAI(String topic, String category, String level) {
        return openAIService.generateProgramContent(topic, category, level);
    }

    private void applyDtoToEntity(FeaturedProgramRequestDTO dto, FeaturedProgram program) {
        program.setTitle(dto.getTitle());
        if (dto.getSlug() != null && !dto.getSlug().isBlank()) {
            program.setSlug(dto.getSlug());
        }
        program.setCategory(dto.getCategory());
        program.setInstructorName(dto.getInstructorName());
        program.setCompany(dto.getCompany());
        program.setLevel(dto.getLevel() != null ? dto.getLevel() : "Beginner");
        program.setStatus(dto.getStatus() != null ? dto.getStatus() : "Active");
        program.setDisplayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : 1);
        program.setDurationWeeks(dto.getDurationWeeks());
        program.setLessons(dto.getLessons());
        program.setLiveSessions(dto.getLiveSessions());
        program.setProjects(dto.getProjects());
        program.setStudentsEnrolled(dto.getStudentsEnrolled());
        program.setRating(dto.getRating());
        program.setPrice(dto.getPrice());
        program.setOfferText(dto.getOfferText());
        program.setEnrollmentButtonText(dto.getEnrollmentButtonText());
        program.setEnrollmentUrl(dto.getEnrollmentUrl());
        program.setSyllabusButtonText(dto.getSyllabusButtonText());
        program.setShortDescription(dto.getShortDescription());
        program.setFullDescription(dto.getFullDescription());
        program.setLearningOutcomes(dto.getLearningOutcomes() != null ? dto.getLearningOutcomes() : new ArrayList<>());
        program.setHighlights(dto.getHighlights() != null ? dto.getHighlights() : new ArrayList<>());
        program.setVideoUrl(dto.getVideoUrl());
        program.setThumbnailUrl(dto.getThumbnailUrl());
        program.setInstructorRole(dto.getInstructorRole());
        program.setExperience(dto.getExperience());
        program.setStudentCount(dto.getStudentCount());
        program.setLearnersCount(dto.getLearnersCount());
        program.setPublishDate(dto.getPublishDate());
        program.setShowLiveBadge(dto.getShowLiveBadge() != null ? dto.getShowLiveBadge() : false);

        // ===== NEW: Basic info =====
        program.setBannerUrl(dto.getBannerUrl());
        program.setInstructorPhotoUrl(dto.getInstructorPhotoUrl());
        program.setInstructorLinkedIn(dto.getInstructorLinkedIn());

        // ===== NEW: Pricing (Course Details tab) =====
        program.setOriginalPrice(dto.getOriginalPrice());
        program.setDiscountPercent(dto.getDiscountPercent());
        program.setCurrency(dto.getCurrency() != null ? dto.getCurrency() : "INR");
        program.setEmiAvailable(dto.getEmiAvailable() != null ? dto.getEmiAvailable() : false);
        program.setFreeTrial(dto.getFreeTrial() != null ? dto.getFreeTrial() : false);

        // ===== NEW: Course statistics (Course Details tab) =====
        program.setAssignmentsCount(dto.getAssignmentsCount());
        program.setQuizzesCount(dto.getQuizzesCount());
        program.setReviewsCount(dto.getReviewsCount());

        // ===== NEW: Instructor bio / career info (About tab) =====
        program.setInstructorBio(dto.getInstructorBio());
        program.setJobRoles(dto.getJobRoles() != null ? dto.getJobRoles() : new ArrayList<>());
        program.setSalaryRange(dto.getSalaryRange());
        program.setHiringCompanies(dto.getHiringCompanies() != null ? dto.getHiringCompanies() : new ArrayList<>());
        program.setPlacementSupport(dto.getPlacementSupport() != null ? dto.getPlacementSupport() : false);
        program.setCareerAssistance(dto.getCareerAssistance());

        // ===== NEW: Outcomes tab =====
        program.setSkills(dto.getSkills() != null ? dto.getSkills() : new ArrayList<>());

        // ===== NEW: Highlights tab - certificate =====
        program.setCertificateTitle(dto.getCertificateTitle());
        program.setCertificateImageUrl(dto.getCertificateImageUrl());
        program.setCertificateVerificationUrl(dto.getCertificateVerificationUrl());

        // ===== NEW: FAQs tab - SEO =====
        program.setMetaTitle(dto.getMetaTitle());
        program.setMetaDescription(dto.getMetaDescription());
        program.setMetaKeywords(dto.getMetaKeywords() != null ? dto.getMetaKeywords() : new ArrayList<>());
        program.setOgImageUrl(dto.getOgImageUrl());

        // ===== NEW: Syllabus tab - display settings =====
        program.setShowOnHomepage(dto.getShowOnHomepage() != null ? dto.getShowOnHomepage() : false);
        program.setIsFeatured(dto.getIsFeatured() != null ? dto.getIsFeatured() : false);
        program.setIsTrending(dto.getIsTrending() != null ? dto.getIsTrending() : false);
        program.setIsBestseller(dto.getIsBestseller() != null ? dto.getIsBestseller() : false);
        program.setIsPopular(dto.getIsPopular() != null ? dto.getIsPopular() : false);
        program.setIsRecommended(dto.getIsRecommended() != null ? dto.getIsRecommended() : false);
        program.setIsComingSoon(dto.getIsComingSoon() != null ? dto.getIsComingSoon() : false);

        // ===== NEW: Draft / publish workflow =====
        // Note: dedicated /publish endpoint is the only way to flip Draft -> Published;
        // create/update still honor an explicit publishStatus if the caller supplies one
        // (e.g. saving as Draft), defaulting to "Draft" for new/unspecified programs.
        program.setPublishStatus(dto.getPublishStatus() != null ? dto.getPublishStatus() : "Draft");
    }

    private void attachFaqs(FeaturedProgramRequestDTO dto, FeaturedProgram program) {
        if (dto.getFaqs() == null) {
            return;
        }
        List<FeaturedProgramFAQ> faqEntities = new ArrayList<>();
        int index = 0;
        for (FAQDto faqDto : dto.getFaqs()) {
            FeaturedProgramFAQ faq = new FeaturedProgramFAQ();
            faq.setQuestion(faqDto.getQuestion());
            faq.setAnswer(faqDto.getAnswer());
            faq.setOrderIndex(faqDto.getOrderIndex() != null ? faqDto.getOrderIndex() : index);
            faq.setProgram(program);
            faqEntities.add(faq);
            index++;
        }
        program.getFaqs().addAll(faqEntities);
    }

 // Merges incoming syllabusWeeks into the program's existing week/module/session
 // graph instead of clearing and rebuilding from scratch. Weeks are matched by
 // weekNumber (the client never sends a week id), modules/sessions are matched by
 // id when present. Matched rows keep their entity identity — critical because
 // SyllabusSession carries video fields (videoId/videoUrl/videoStatus/etc.) that
 // are server/Kafka-owned and never sent by the client; a clear+rebuild would
 // wipe them on every save. Unmatched incoming rows become new entities; existing
 // rows no longer present in the DTO are dropped via orphanRemoval.
 private void syncSyllabusWeeks(FeaturedProgramRequestDTO dto, FeaturedProgram program) {
     if (dto.getSyllabusWeeks() == null) {
         return;
     }

     Map<Integer, SyllabusWeek> existingWeeksByNumber = program.getSyllabusWeeks().stream()
             .filter(w -> w.getWeekNumber() != null)
             .collect(Collectors.toMap(SyllabusWeek::getWeekNumber, w -> w, (a, b) -> a));

     List<SyllabusWeek> mergedWeeks = new ArrayList<>();

     for (SyllabusWeekDto weekDto : dto.getSyllabusWeeks()) {
         SyllabusWeek week = existingWeeksByNumber.get(weekDto.getWeekNumber());
         if (week == null) {
             week = new SyllabusWeek();
             week.setProgram(program);
         }
         week.setWeekNumber(weekDto.getWeekNumber());
         week.setTitle(weekDto.getTitle());
         week.setDateRange(weekDto.getDateRange());
         week.setItems(weekDto.getItems() != null ? weekDto.getItems() : new ArrayList<>());

         Map<Long, SyllabusModule> existingModulesById = week.getModules().stream()
                 .filter(m -> m.getId() != null)
                 .collect(Collectors.toMap(SyllabusModule::getId, m -> m, (a, b) -> a));

         List<SyllabusModule> mergedModules = new ArrayList<>();
         List<SyllabusModuleDto> moduleDtos = weekDto.getModules() != null ? weekDto.getModules() : new ArrayList<>();

         for (SyllabusModuleDto moduleDto : moduleDtos) {
             SyllabusModule module = moduleDto.getId() != null ? existingModulesById.get(moduleDto.getId()) : null;
             if (module == null) {
                 module = new SyllabusModule();
             }
             module.setWeek(week);
             module.setTitle(moduleDto.getTitle());
             module.setOrderIndex(moduleDto.getOrderIndex());

             Map<Long, SyllabusSession> existingSessionsById = module.getSessions().stream()
                     .filter(s -> s.getId() != null)
                     .collect(Collectors.toMap(SyllabusSession::getId, s -> s, (a, b) -> a));

             List<SyllabusSession> mergedSessions = new ArrayList<>();
             List<SyllabusSessionDto> sessionDtos = moduleDto.getSessions() != null ? moduleDto.getSessions() : new ArrayList<>();

             for (SyllabusSessionDto sessionDto : sessionDtos) {
                 SyllabusSession session = sessionDto.getId() != null ? existingSessionsById.get(sessionDto.getId()) : null;
                 if (session == null) {
                     session = new SyllabusSession(); // new row: video fields stay at entity defaults
                 }
                 session.setModule(module);
                 session.setTitle(sessionDto.getTitle());
                 session.setType(sessionDto.getType());
                 session.setDuration(sessionDto.getDuration());
                 session.setOrderIndex(sessionDto.getOrderIndex());
                 // videoId/videoUrl/videoThumbnailUrl/videoDurationSeconds/videoStatus are
                 // intentionally NOT touched — server/Kafka-owned, client never sends them.
                 mergedSessions.add(session);
             }

             module.getSessions().clear();
             module.getSessions().addAll(mergedSessions);
             mergedModules.add(module);
         }

         week.getModules().clear();
         week.getModules().addAll(mergedModules);
         mergedWeeks.add(week);
     }

     program.getSyllabusWeeks().clear();
     program.getSyllabusWeeks().addAll(mergedWeeks);
 }

    private void attachProjects(FeaturedProgramRequestDTO dto, FeaturedProgram program) {
        if (dto.getProjectsList() == null) {
            return;
        }
        List<ProgramProject> projectEntities = new ArrayList<>();
        int index = 0;
        for (ProgramProjectDto projectDto : dto.getProjectsList()) {
            ProgramProject project = new ProgramProject();
            project.setTitle(projectDto.getTitle());
            project.setDescription(projectDto.getDescription());
            project.setImage(projectDto.getImage());
            project.setDifficulty(projectDto.getDifficulty());
            project.setDisplayOrder(index);
            project.setProgram(program);
            projectEntities.add(project);
            index++;
        }
        program.getProjectsList().addAll(projectEntities);
    }

    private FeaturedProgramResponseDTO mapToResponseDTO(FeaturedProgram program) {
        FeaturedProgramResponseDTO dto = new FeaturedProgramResponseDTO();
        dto.setId(program.getId());
        dto.setTitle(program.getTitle());
        dto.setSlug(program.getSlug());
        dto.setCategory(program.getCategory());
        dto.setInstructorName(program.getInstructorName());
        dto.setCompany(program.getCompany());
        dto.setLevel(program.getLevel());
        dto.setStatus(program.getStatus());
        dto.setDisplayOrder(program.getDisplayOrder());
        dto.setDurationWeeks(program.getDurationWeeks());
        dto.setLessons(program.getLessons());
        dto.setLiveSessions(program.getLiveSessions());
        dto.setProjects(program.getProjects());
        dto.setStudentsEnrolled(program.getStudentsEnrolled());
        dto.setRating(program.getRating());
        dto.setPrice(program.getPrice());
        dto.setOfferText(program.getOfferText());
        dto.setEnrollmentButtonText(program.getEnrollmentButtonText());
        dto.setEnrollmentUrl(program.getEnrollmentUrl());
        dto.setSyllabusButtonText(program.getSyllabusButtonText());
        dto.setShortDescription(program.getShortDescription());
        dto.setFullDescription(program.getFullDescription());
        dto.setLearningOutcomes(program.getLearningOutcomes());
        dto.setHighlights(program.getHighlights());

        List<FAQDto> faqDtos = program.getFaqs().stream()
                .map(f -> new FAQDto(f.getId(), f.getQuestion(), f.getAnswer(), f.getOrderIndex()))
                .sorted((a, b) -> {
                    Integer ai = a.getOrderIndex() != null ? a.getOrderIndex() : 0;
                    Integer bi = b.getOrderIndex() != null ? b.getOrderIndex() : 0;
                    return ai.compareTo(bi);
                })
                .collect(Collectors.toList());
        dto.setFaqs(faqDtos);

//        List<SyllabusWeekDto> weekDtos = program.getSyllabusWeeks().stream()
//                .map(w -> new SyllabusWeekDto(w.getId(), w.getWeekNumber(), w.getTitle(), w.getDateRange(), w.getItems()))
//                .sorted((a, b) -> {
        List<SyllabusWeekDto> weekDtos = program.getSyllabusWeeks().stream()
                .map(this::mapWeekToResponseDto)
                .sorted((a, b) -> {
                    Integer aw = a.getWeekNumber() != null ? a.getWeekNumber() : 0;
                    Integer bw = b.getWeekNumber() != null ? b.getWeekNumber() : 0;
                    return aw.compareTo(bw);
                })
                .collect(Collectors.toList());
        dto.setSyllabusWeeks(weekDtos);

        dto.setVideoUrl(program.getVideoUrl());
        dto.setThumbnailUrl(program.getThumbnailUrl());
        dto.setInstructorRole(program.getInstructorRole());
        dto.setExperience(program.getExperience());
        dto.setStudentCount(program.getStudentCount());
        dto.setLearnersCount(program.getLearnersCount());
        dto.setPublishDate(program.getPublishDate());
        dto.setShowLiveBadge(program.getShowLiveBadge());
        dto.setCreatedAt(program.getCreatedAt());

        // ===== NEW: Basic info =====
        dto.setBannerUrl(program.getBannerUrl());
        dto.setInstructorPhotoUrl(program.getInstructorPhotoUrl());
        dto.setInstructorLinkedIn(program.getInstructorLinkedIn());

        // ===== NEW: Pricing (Course Details tab) =====
        dto.setOriginalPrice(program.getOriginalPrice());
        dto.setDiscountPercent(program.getDiscountPercent());
        dto.setCurrency(program.getCurrency());
        dto.setEmiAvailable(program.getEmiAvailable());
        dto.setFreeTrial(program.getFreeTrial());

        // ===== NEW: Course statistics (Course Details tab) =====
        dto.setAssignmentsCount(program.getAssignmentsCount());
        dto.setQuizzesCount(program.getQuizzesCount());
        dto.setReviewsCount(program.getReviewsCount());

        // ===== NEW: Instructor bio / career info (About tab) =====
        dto.setInstructorBio(program.getInstructorBio());
        dto.setJobRoles(program.getJobRoles());
        dto.setSalaryRange(program.getSalaryRange());
        dto.setHiringCompanies(program.getHiringCompanies());
        dto.setPlacementSupport(program.getPlacementSupport());
        dto.setCareerAssistance(program.getCareerAssistance());

        // ===== NEW: Outcomes tab =====
        dto.setSkills(program.getSkills());

        List<ProgramProjectDto> projectDtos = program.getProjectsList().stream()
                .map(p -> new ProgramProjectDto(p.getId(), p.getTitle(), p.getDescription(), p.getImage(), p.getDifficulty()))
                .collect(Collectors.toList());
        dto.setProjectsList(projectDtos);

        // ===== NEW: Highlights tab - certificate =====
        dto.setCertificateTitle(program.getCertificateTitle());
        dto.setCertificateImageUrl(program.getCertificateImageUrl());
        dto.setCertificateVerificationUrl(program.getCertificateVerificationUrl());

        // ===== NEW: FAQs tab - SEO =====
        dto.setMetaTitle(program.getMetaTitle());
        dto.setMetaDescription(program.getMetaDescription());
        dto.setMetaKeywords(program.getMetaKeywords());
        dto.setOgImageUrl(program.getOgImageUrl());

        // ===== NEW: Syllabus tab - display settings =====
        dto.setShowOnHomepage(program.getShowOnHomepage());
        dto.setIsFeatured(program.getIsFeatured());
        dto.setIsTrending(program.getIsTrending());
        dto.setIsBestseller(program.getIsBestseller());
        dto.setIsPopular(program.getIsPopular());
        dto.setIsRecommended(program.getIsRecommended());
        dto.setIsComingSoon(program.getIsComingSoon());

        // ===== NEW: Draft / publish workflow =====
        dto.setPublishStatus(program.getPublishStatus());

        return dto;
    }

    private String generateSlug(String title) {
        if (title == null) {
            return "program-" + System.currentTimeMillis();
        }
        String baseSlug = title.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");

        String slug = baseSlug;
        int suffix = 1;
        while (featuredProgramRepository.findBySlug(slug).isPresent()) {
            slug = baseSlug + "-" + suffix;
            suffix++;
        }
        return slug;
    }
    
 // Same shape as mapWeekToSyllabusDto but for create/update/publish responses:
 // no `locked` computation (that's only meaningful on the public syllabus endpoint),
 // video fields pass through as-is so isPersisted can be derived correctly on the client.
 private SyllabusWeekDto mapWeekToResponseDto(SyllabusWeek week) {
     List<SyllabusModuleDto> moduleDtos = week.getModules().stream()
             .map(m -> new SyllabusModuleDto(
                     m.getId(),
                     m.getTitle(),
                     m.getOrderIndex(),
//                     m.getSessions().stream()
//                     .map(s -> new SyllabusSessionDto(
//                             s.getId(),
//                             s.getTitle(),
//                             s.getType(),
//                             s.getDuration(),
//                             s.getOrderIndex(),
//                             s.getVideoId(),
//                             s.getVideoTitle(),
//                             s.getVideoDescription(),
//                             s.getVideoUrl(),
//                             s.getVideoThumbnailUrl(),
//                             s.getVideoDurationSeconds(),
//                             s.getVideoStatus(),
//                             null))
//                             .collect(Collectors.toList())))
//             .collect(Collectors.toList());
                     m.getSessions().stream()
                     .map(s -> {
                         SyllabusSessionDto dto = new SyllabusSessionDto(
                             s.getId(),
                             s.getTitle(),
                             s.getType(),
                             s.getDuration(),
                             s.getOrderIndex(),
                             s.getVideoId(),
                             s.getVideoTitle(),
                             s.getVideoDescription(),
                             s.getVideoUrl(),
                             s.getVideoThumbnailUrl(),
                             s.getVideoDurationSeconds(),
                             s.getVideoStatus(),
                             null);
                         // ── NEW: file fields, same additive pattern as video above ──
                         dto.setFileId(s.getFileId());
                         dto.setFileUrl(s.getFileUrl());
                         dto.setFileName(s.getFileName());
                         dto.setFileStatus(s.getFileStatus());
                         return dto;
                     })
                     .collect(Collectors.toList())))
             .collect(Collectors.toList());
     return new SyllabusWeekDto(week.getId(), week.getWeekNumber(), week.getTitle(), week.getDateRange(),
             week.getItems(), moduleDtos);
 }
 @Transactional(readOnly = true)
 public List<FeaturedProgramSummaryDTO> getAllActiveProgramsSummary() {
     return featuredProgramRepository.findSummaryByStatusAndPublishStatus("Active", "Published");
 }
}