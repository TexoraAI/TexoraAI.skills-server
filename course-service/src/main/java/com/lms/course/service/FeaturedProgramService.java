//package com.lms.course.service;
//
//import com.lms.course.dto.*;
//import com.lms.course.exception.ResourceNotFoundException;
//import com.lms.course.model.FeaturedProgram;
//import com.lms.course.model.FeaturedProgramFAQ;
//import com.lms.course.model.SyllabusWeek;
//import com.lms.course.repository.FeaturedProgramFAQRepository;
//import com.lms.course.repository.FeaturedProgramRepository;
//import com.lms.course.repository.SyllabusWeekRepository;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Locale;
//import java.util.stream.Collectors;
//
//@Service
//public class FeaturedProgramService {
//
//    private final FeaturedProgramRepository featuredProgramRepository;
//    private final FeaturedProgramFAQRepository faqRepository;
//    private final SyllabusWeekRepository syllabusWeekRepository;
//    private final OpenAIService openAIService;
//
//    public FeaturedProgramService(FeaturedProgramRepository featuredProgramRepository,
//                                   FeaturedProgramFAQRepository faqRepository,
//                                   SyllabusWeekRepository syllabusWeekRepository,
//                                   OpenAIService openAIService) {
//        this.featuredProgramRepository = featuredProgramRepository;
//        this.faqRepository = faqRepository;
//        this.syllabusWeekRepository = syllabusWeekRepository;
//        this.openAIService = openAIService;
//    }
//
//    @Transactional(readOnly = true)
//    public List<FeaturedProgramResponseDTO> getAllActivePrograms() {
//        return featuredProgramRepository.findAllByStatusOrderByDisplayOrderAsc("Active")
//                .stream()
//                .map(this::mapToResponseDTO)
//                .collect(Collectors.toList());
//    }
//
//    @Transactional(readOnly = true)
//    public List<FeaturedProgramResponseDTO> getProgramsByCategory(String category) {
//        return featuredProgramRepository.findAllByCategoryIgnoreCaseAndStatus(category, "Active")
//                .stream()
//                .map(this::mapToResponseDTO)
//                .collect(Collectors.toList());
//    }
//
//    @Transactional(readOnly = true)
//    public FeaturedProgramResponseDTO getProgramById(Long id) {
//        FeaturedProgram program = featuredProgramRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Featured program not found with id: " + id));
//        return mapToResponseDTO(program);
//    }
//
//    @Transactional(readOnly = true)
//    public List<SyllabusWeekDto> getProgramSyllabus(Long id) {
//        if (!featuredProgramRepository.existsById(id)) {
//            throw new ResourceNotFoundException("Featured program not found with id: " + id);
//        }
//        return syllabusWeekRepository.findByProgramIdOrderByWeekNumberAsc(id)
//                .stream()
//                .map(week -> new SyllabusWeekDto(
//                        week.getId(),
//                        week.getWeekNumber(),
//                        week.getTitle(),
//                        week.getDateRange(),
//                        week.getItems()))
//                .collect(Collectors.toList());
//    }
//
//   
//
//    @Transactional(readOnly = true)
//    public List<FeaturedProgramResponseDTO> getAllProgramsForAdmin() {
//        return featuredProgramRepository.findAllByOrderByDisplayOrderAsc()
//                .stream()
//                .map(this::mapToResponseDTO)
//                .collect(Collectors.toList());
//    }
//
//    @Transactional(readOnly = true)
//    public AdminStatsDTO getAdminStats() {
//        long total = featuredProgramRepository.count();
//        long active = featuredProgramRepository.countByStatus("Active");
//        long inactive = featuredProgramRepository.countByStatus("Inactive");
//        long categories = featuredProgramRepository.findDistinctCategories().size();
//        return new AdminStatsDTO(total, active, inactive, categories);
//    }
//
//    @Transactional
//    public FeaturedProgramResponseDTO createProgram(FeaturedProgramRequestDTO dto) {
//        FeaturedProgram program = new FeaturedProgram();
//        applyDtoToEntity(dto, program);
//
//        if (program.getSlug() == null || program.getSlug().isBlank()) {
//            program.setSlug(generateSlug(program.getTitle()));
//        }
//
//        attachFaqs(dto, program);
//        attachSyllabusWeeks(dto, program);
//
//        FeaturedProgram saved = featuredProgramRepository.save(program);
//        return mapToResponseDTO(saved);
//    }
//
//    @Transactional
//    public FeaturedProgramResponseDTO updateProgram(Long id, FeaturedProgramRequestDTO dto) {
//        FeaturedProgram program = featuredProgramRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Featured program not found with id: " + id));
//
//        applyDtoToEntity(dto, program);
//
//        if (dto.getSlug() != null && !dto.getSlug().isBlank()) {
//            program.setSlug(dto.getSlug());
//        }
//
//        program.getFaqs().clear();
//        program.getSyllabusWeeks().clear();
//
//        attachFaqs(dto, program);
//        attachSyllabusWeeks(dto, program);
//
//        FeaturedProgram saved = featuredProgramRepository.save(program);
//        return mapToResponseDTO(saved);
//    }
//
//    @Transactional
//    public void deleteProgram(Long id) {
//        FeaturedProgram program = featuredProgramRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Featured program not found with id: " + id));
//        featuredProgramRepository.delete(program);
//    }
//
//    @Transactional(readOnly = true)
//    public FeaturedProgramRequestDTO generateWithAI(String topic, String category, String level) {
//        return openAIService.generateProgramContent(topic, category, level);
//    }
//
//    private void applyDtoToEntity(FeaturedProgramRequestDTO dto, FeaturedProgram program) {
//        program.setTitle(dto.getTitle());
//        if (dto.getSlug() != null && !dto.getSlug().isBlank()) {
//            program.setSlug(dto.getSlug());
//        }
//        program.setCategory(dto.getCategory());
//        program.setInstructorName(dto.getInstructorName());
//        program.setCompany(dto.getCompany());
//        program.setLevel(dto.getLevel() != null ? dto.getLevel() : "Beginner");
//        program.setStatus(dto.getStatus() != null ? dto.getStatus() : "Active");
//        program.setDisplayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : 1);
//        program.setDurationWeeks(dto.getDurationWeeks());
//        program.setLessons(dto.getLessons());
//        program.setLiveSessions(dto.getLiveSessions());
//        program.setProjects(dto.getProjects());
//        program.setStudentsEnrolled(dto.getStudentsEnrolled());
//        program.setRating(dto.getRating());
//        program.setPrice(dto.getPrice());
//        program.setOfferText(dto.getOfferText());
//        program.setEnrollmentButtonText(dto.getEnrollmentButtonText());
//        program.setEnrollmentUrl(dto.getEnrollmentUrl());
//        program.setSyllabusButtonText(dto.getSyllabusButtonText());
//        program.setShortDescription(dto.getShortDescription());
//        program.setFullDescription(dto.getFullDescription());
//        program.setLearningOutcomes(dto.getLearningOutcomes() != null ? dto.getLearningOutcomes() : new ArrayList<>());
//        program.setHighlights(dto.getHighlights() != null ? dto.getHighlights() : new ArrayList<>());
//        program.setVideoUrl(dto.getVideoUrl());
//        program.setThumbnailUrl(dto.getThumbnailUrl());
//        program.setInstructorRole(dto.getInstructorRole());
//        program.setExperience(dto.getExperience());
//        program.setStudentCount(dto.getStudentCount());
//        program.setLearnersCount(dto.getLearnersCount());
//        program.setPublishDate(dto.getPublishDate());
//        program.setShowLiveBadge(dto.getShowLiveBadge() != null ? dto.getShowLiveBadge() : false);
//    }
//
//    private void attachFaqs(FeaturedProgramRequestDTO dto, FeaturedProgram program) {
//        if (dto.getFaqs() == null) {
//            return;
//        }
//        List<FeaturedProgramFAQ> faqEntities = new ArrayList<>();
//        int index = 0;
//        for (FAQDto faqDto : dto.getFaqs()) {
//            FeaturedProgramFAQ faq = new FeaturedProgramFAQ();
//            faq.setQuestion(faqDto.getQuestion());
//            faq.setAnswer(faqDto.getAnswer());
//            faq.setOrderIndex(faqDto.getOrderIndex() != null ? faqDto.getOrderIndex() : index);
//            faq.setProgram(program);
//            faqEntities.add(faq);
//            index++;
//        }
//        program.getFaqs().addAll(faqEntities);
//    }
//
//    private void attachSyllabusWeeks(FeaturedProgramRequestDTO dto, FeaturedProgram program) {
//        if (dto.getSyllabusWeeks() == null) {
//            return;
//        }
//        List<SyllabusWeek> weekEntities = new ArrayList<>();
//        for (SyllabusWeekDto weekDto : dto.getSyllabusWeeks()) {
//            SyllabusWeek week = new SyllabusWeek();
//            week.setWeekNumber(weekDto.getWeekNumber());
//            week.setTitle(weekDto.getTitle());
//            week.setDateRange(weekDto.getDateRange());
//            week.setItems(weekDto.getItems() != null ? weekDto.getItems() : new ArrayList<>());
//            week.setProgram(program);
//            weekEntities.add(week);
//        }
//        program.getSyllabusWeeks().addAll(weekEntities);
//    }
//
//    private FeaturedProgramResponseDTO mapToResponseDTO(FeaturedProgram program) {
//        FeaturedProgramResponseDTO dto = new FeaturedProgramResponseDTO();
//        dto.setId(program.getId());
//        dto.setTitle(program.getTitle());
//        dto.setSlug(program.getSlug());
//        dto.setCategory(program.getCategory());
//        dto.setInstructorName(program.getInstructorName());
//        dto.setCompany(program.getCompany());
//        dto.setLevel(program.getLevel());
//        dto.setStatus(program.getStatus());
//        dto.setDisplayOrder(program.getDisplayOrder());
//        dto.setDurationWeeks(program.getDurationWeeks());
//        dto.setLessons(program.getLessons());
//        dto.setLiveSessions(program.getLiveSessions());
//        dto.setProjects(program.getProjects());
//        dto.setStudentsEnrolled(program.getStudentsEnrolled());
//        dto.setRating(program.getRating());
//        dto.setPrice(program.getPrice());
//        dto.setOfferText(program.getOfferText());
//        dto.setEnrollmentButtonText(program.getEnrollmentButtonText());
//        dto.setEnrollmentUrl(program.getEnrollmentUrl());
//        dto.setSyllabusButtonText(program.getSyllabusButtonText());
//        dto.setShortDescription(program.getShortDescription());
//        dto.setFullDescription(program.getFullDescription());
//        dto.setLearningOutcomes(program.getLearningOutcomes());
//        dto.setHighlights(program.getHighlights());
//
//        List<FAQDto> faqDtos = program.getFaqs().stream()
//                .map(f -> new FAQDto(f.getId(), f.getQuestion(), f.getAnswer(), f.getOrderIndex()))
//                .sorted((a, b) -> {
//                    Integer ai = a.getOrderIndex() != null ? a.getOrderIndex() : 0;
//                    Integer bi = b.getOrderIndex() != null ? b.getOrderIndex() : 0;
//                    return ai.compareTo(bi);
//                })
//                .collect(Collectors.toList());
//        dto.setFaqs(faqDtos);
//
//        List<SyllabusWeekDto> weekDtos = program.getSyllabusWeeks().stream()
//                .map(w -> new SyllabusWeekDto(w.getId(), w.getWeekNumber(), w.getTitle(), w.getDateRange(), w.getItems()))
//                .sorted((a, b) -> {
//                    Integer aw = a.getWeekNumber() != null ? a.getWeekNumber() : 0;
//                    Integer bw = b.getWeekNumber() != null ? b.getWeekNumber() : 0;
//                    return aw.compareTo(bw);
//                })
//                .collect(Collectors.toList());
//        dto.setSyllabusWeeks(weekDtos);
//
//        dto.setVideoUrl(program.getVideoUrl());
//        dto.setThumbnailUrl(program.getThumbnailUrl());
//        dto.setInstructorRole(program.getInstructorRole());
//        dto.setExperience(program.getExperience());
//        dto.setStudentCount(program.getStudentCount());
//        dto.setLearnersCount(program.getLearnersCount());
//        dto.setPublishDate(program.getPublishDate());
//        dto.setShowLiveBadge(program.getShowLiveBadge());
//        dto.setCreatedAt(program.getCreatedAt());
//
//        return dto;
//    }
//
//    private String generateSlug(String title) {
//        if (title == null) {
//            return "program-" + System.currentTimeMillis();
//        }
//        String baseSlug = title.toLowerCase(Locale.ROOT)
//                .replaceAll("[^a-z0-9\\s-]", "")
//                .trim()
//                .replaceAll("\\s+", "-")
//                .replaceAll("-+", "-");
//
//        String slug = baseSlug;
//        int suffix = 1;
//        while (featuredProgramRepository.findBySlug(slug).isPresent()) {
//            slug = baseSlug + "-" + suffix;
//            suffix++;
//        }
//        return slug;
//    }
//}
package com.lms.course.service;

import com.lms.course.dto.*;
import com.lms.course.exception.ResourceNotFoundException;
import com.lms.course.model.FeaturedProgram;
import com.lms.course.model.FeaturedProgramFAQ;
import com.lms.course.model.ProgramProject;
import com.lms.course.model.SyllabusWeek;
import com.lms.course.repository.FeaturedProgramFAQRepository;
import com.lms.course.repository.FeaturedProgramRepository;
import com.lms.course.repository.ProgramProjectRepository;
import com.lms.course.repository.SyllabusWeekRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public FeaturedProgramService(FeaturedProgramRepository featuredProgramRepository,
                                   FeaturedProgramFAQRepository faqRepository,
                                   SyllabusWeekRepository syllabusWeekRepository,
                                   ProgramProjectRepository programProjectRepository,
                                   OpenAIService openAIService) {
        this.featuredProgramRepository = featuredProgramRepository;
        this.faqRepository = faqRepository;
        this.syllabusWeekRepository = syllabusWeekRepository;
        this.programProjectRepository = programProjectRepository;
        this.openAIService = openAIService;
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
        return syllabusWeekRepository.findByProgramIdOrderByWeekNumberAsc(id)
                .stream()
                .map(week -> new SyllabusWeekDto(
                        week.getId(),
                        week.getWeekNumber(),
                        week.getTitle(),
                        week.getDateRange(),
                        week.getItems()))
                .collect(Collectors.toList());
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

        attachFaqs(dto, program);
        attachSyllabusWeeks(dto, program);
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

        program.getFaqs().clear();
        program.getSyllabusWeeks().clear();
        program.getProjectsList().clear();

        attachFaqs(dto, program);
        attachSyllabusWeeks(dto, program);
        attachProjects(dto, program);

        FeaturedProgram saved = featuredProgramRepository.save(program);
        return mapToResponseDTO(saved);
    }

    @Transactional
    public void deleteProgram(Long id) {
        FeaturedProgram program = featuredProgramRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Featured program not found with id: " + id));
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

    private void attachSyllabusWeeks(FeaturedProgramRequestDTO dto, FeaturedProgram program) {
        if (dto.getSyllabusWeeks() == null) {
            return;
        }
        List<SyllabusWeek> weekEntities = new ArrayList<>();
        for (SyllabusWeekDto weekDto : dto.getSyllabusWeeks()) {
            SyllabusWeek week = new SyllabusWeek();
            week.setWeekNumber(weekDto.getWeekNumber());
            week.setTitle(weekDto.getTitle());
            week.setDateRange(weekDto.getDateRange());
            week.setItems(weekDto.getItems() != null ? weekDto.getItems() : new ArrayList<>());
            week.setProgram(program);
            weekEntities.add(week);
        }
        program.getSyllabusWeeks().addAll(weekEntities);
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

        List<SyllabusWeekDto> weekDtos = program.getSyllabusWeeks().stream()
                .map(w -> new SyllabusWeekDto(w.getId(), w.getWeekNumber(), w.getTitle(), w.getDateRange(), w.getItems()))
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
}