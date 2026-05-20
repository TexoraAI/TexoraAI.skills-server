package com.lms.course.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.course.dto.HomepageCourseDto;
import com.lms.course.model.HomepageCourse;
import com.lms.course.repository.HomepageCourseRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class HomepageCourseService {

    private final HomepageCourseRepository repository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${openai.api.key:}")
    private String openAiApiKey;

    @Value("${openai.model:gpt-4o-mini}")
    private String openAiModel;

    public HomepageCourseService(HomepageCourseRepository repository) {
        this.repository = repository;
        this.objectMapper = new ObjectMapper();
        this.restTemplate = new RestTemplate();
    }

    // ----------------------------------------------------------------
    // PUBLIC METHODS
    // ----------------------------------------------------------------

    public List<HomepageCourseDto> getHomepageCourses() {
        return repository.findByActiveTrueAndFeaturedTrueOrderByIdAsc()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public HomepageCourseDto getCourseById(Long id) {
        HomepageCourse course = findActiveCourse(id);
        return toDto(course);
    }

    public String getSyllabus(Long id) {
        HomepageCourse course = findActiveCourse(id);
        String syllabus = course.getSyllabusJson();
        if (syllabus == null || syllabus.isBlank()) {
            return "{\"weeks\":[]}";
        }
        return syllabus;
    }

    // ----------------------------------------------------------------
    // ADMIN METHODS
    // ----------------------------------------------------------------

    public HomepageCourseDto createCourse(HomepageCourseDto dto) {
        HomepageCourse entity = toEntity(dto);
        entity.setActive(true);
        HomepageCourse saved = repository.save(entity);
        return toDto(saved);
    }

    public HomepageCourseDto updateCourse(Long id, HomepageCourseDto dto) {
        HomepageCourse existing = findActiveCourse(id);
        copyDtoToEntity(dto, existing);
        HomepageCourse saved = repository.save(existing);
        return toDto(saved);
    }

    public void softDeleteCourse(Long id) {
        HomepageCourse course = findActiveCourse(id);
        course.setActive(false);
        repository.save(course);
    }

    public HomepageCourseDto updateSyllabus(Long id, String syllabusJson) {
        HomepageCourse course = findActiveCourse(id);
        course.setSyllabusJson(syllabusJson);
        HomepageCourse saved = repository.save(course);
        return toDto(saved);
    }

    // ----------------------------------------------------------------
    // AI METHODS
    // ----------------------------------------------------------------

    public HomepageCourseDto generateSyllabusWithAi(Long id, String rawText) {
        validateOpenAiKey();
        HomepageCourse course = findActiveCourse(id);
        String generatedJson = callOpenAi(rawText);
        course.setSyllabusJson(generatedJson);
        HomepageCourse saved = repository.save(course);
        return toDto(saved);
    }

    public HomepageCourseDto uploadSyllabusAndGenerate(Long id, MultipartFile file) {
        validateOpenAiKey();

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Uploaded file is empty.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".txt")) {
            throw new RuntimeException("Only .txt files are supported for syllabus upload.");
        }

        String rawText;
        try {
            rawText = new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded file: " + e.getMessage());
        }

        return generateSyllabusWithAi(id, rawText);
    }

    // ----------------------------------------------------------------
    // PRIVATE HELPERS
    // ----------------------------------------------------------------

    private HomepageCourse findActiveCourse(Long id) {
        return repository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + id));
    }

    private void validateOpenAiKey() {
        if (openAiApiKey == null || openAiApiKey.isBlank()) {
            throw new RuntimeException("OpenAI API key is not configured. Please set openai.api.key in application.yml.");
        }
    }

    private String callOpenAi(String rawText) {
        String prompt = """
                Convert the following raw syllabus text into STRICT JSON only.
                No markdown. No explanation. No code blocks.
                Return ONLY a valid JSON object in this exact structure:
                {
                  "weeks": [
                    {
                      "weekNumber": 1,
                      "title": "Week title",
                      "dateRange": "Jan 6 - Jan 11",
                      "phases": [
                        {
                          "title": "Phase title",
                          "description": "Phase description",
                          "items": [
                            {
                              "title": "Lesson title",
                              "type": "LIVE",
                              "scheduledTime": "FRI 19 • 11:30 PM – 12:30 AM GMT+5:30",
                              "description": "Lesson description"
                            }
                          ]
                        }
                      ]
                    }
                  ]
                }
                
                Raw syllabus text:
                """ + rawText;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", openAiModel);
        requestBody.put("messages", List.of(userMessage));
        requestBody.put("temperature", 0.2);

        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(
                    "https://api.openai.com/v1/chat/completions",
                    HttpMethod.POST,
                    httpEntity,
                    String.class
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to call OpenAI API: " + e.getMessage());
        }

        String responseBody = response.getBody();
        if (responseBody == null || responseBody.isBlank()) {
            throw new RuntimeException("Empty response received from OpenAI.");
        }

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String content = root
                    .path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();

            // Validate that the content is valid JSON
            objectMapper.readTree(content);
            return content;

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse OpenAI response as valid JSON: " + e.getMessage());
        }
    }

    private HomepageCourseDto toDto(HomepageCourse course) {
        HomepageCourseDto dto = new HomepageCourseDto();
        dto.setId(course.getId());
        dto.setTitle(course.getTitle());
        dto.setInstructor(course.getInstructor());
        dto.setDuration(course.getDuration());
        dto.setStudents(course.getStudents());
        dto.setRating(course.getRating());
        dto.setLevel(course.getLevel());
        dto.setShortDescription(course.getShortDescription());
        dto.setDescription(course.getDescription());
        dto.setPrice(course.getPrice());
        dto.setCategory(course.getCategory());
        dto.setLiveSessions(course.getLiveSessions());
        dto.setTotalLessons(course.getTotalLessons());
        dto.setProjects(course.getProjects());
        dto.setFeatured(course.getFeatured());
        dto.setActive(course.getActive());
        dto.setThumbnailUrl(course.getThumbnailUrl());
        dto.setLearningPointsJson(course.getLearningPointsJson());
        dto.setModulesJson(course.getModulesJson());
        dto.setHighlightsJson(course.getHighlightsJson());
        dto.setSyllabusJson(course.getSyllabusJson());
        dto.setCreatedAt(course.getCreatedAt());
        dto.setUpdatedAt(course.getUpdatedAt());
        return dto;
    }

    private HomepageCourse toEntity(HomepageCourseDto dto) {
        HomepageCourse entity = new HomepageCourse();
        copyDtoToEntity(dto, entity);
        return entity;
    }

    private void copyDtoToEntity(HomepageCourseDto dto, HomepageCourse entity) {
        entity.setTitle(dto.getTitle());
        entity.setInstructor(dto.getInstructor());
        entity.setDuration(dto.getDuration());
        entity.setStudents(dto.getStudents());
        entity.setRating(dto.getRating());
        entity.setLevel(dto.getLevel());
        entity.setShortDescription(dto.getShortDescription());
        entity.setDescription(dto.getDescription());
        entity.setPrice(dto.getPrice());
        entity.setCategory(dto.getCategory());
        entity.setLiveSessions(dto.getLiveSessions());
        entity.setTotalLessons(dto.getTotalLessons());
        entity.setProjects(dto.getProjects());
        entity.setFeatured(dto.getFeatured());
        entity.setActive(dto.getActive());
        entity.setThumbnailUrl(dto.getThumbnailUrl());
        entity.setLearningPointsJson(dto.getLearningPointsJson());
        entity.setModulesJson(dto.getModulesJson());
        entity.setHighlightsJson(dto.getHighlightsJson());
        entity.setSyllabusJson(dto.getSyllabusJson());
    }
}