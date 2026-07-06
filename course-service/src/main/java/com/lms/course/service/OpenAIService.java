package com.lms.course.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.course.dto.ExtractedModuleDto;
import com.lms.course.dto.ExtractedSessionDto;
import com.lms.course.dto.ExtractedWeekDto;
import com.lms.course.dto.FAQDto;
import com.lms.course.dto.FeaturedProgramRequestDTO;
import com.lms.course.dto.SyllabusWeekDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Single responsibility: all OpenAI communication.
 * Two independent capabilities live here:
 *  1) generateProgramContent      -> full program content from a topic (existing)
 *  2) generateSyllabusFromExtractedText -> structured syllabus from raw file text (new)
 */
@Service
public class OpenAIService {

    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Value("${openai.model}")
    private String openaiModel;

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public OpenAIService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    // ===================== EXISTING: full program generation =====================

    public FeaturedProgramRequestDTO generateProgramContent(String topic, String category, String level) {
        if (openaiApiKey == null || openaiApiKey.isBlank()) {
            throw new RuntimeException("OpenAI API key is not configured");
        }

        String prompt = buildPrompt(topic, category, level);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + openaiApiKey);

            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);

            Map<String, Object> body = new HashMap<>();
            body.put("model", openaiModel);
            body.put("messages", List.of(message));
            body.put("temperature", 0.7);
            body.put("response_format", Map.of("type", "json_object"));

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    OPENAI_URL,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            if (response.getBody() == null) {
                throw new RuntimeException("OpenAI API returned empty response");
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            String content = root.path("choices").get(0).path("message").path("content").asText();

            return parseGeneratedContent(content, category, level);

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate program content using OpenAI: " + e.getMessage(), e);
        }
    }

    private String buildPrompt(String topic, String category, String level) {
        return "Generate a complete online course program for topic: " + topic +
                "\nCategory: " + category + ", Level: " + level +
                "\nReturn ONLY valid JSON with these exact fields:\n" +
                "title, shortDescription (max 300 chars), fullDescription (HTML), " +
                "learningOutcomes (array of 8 strings), " +
                "highlights (array of 6 strings), " +
                "faqs (array of 5 objects with question and answer), " +
                "syllabusWeeks (array of 8 objects with weekNumber, title, dateRange, items array). " +
                "Do not include any text outside the JSON object.";
    }

    private FeaturedProgramRequestDTO parseGeneratedContent(String content, String category, String level) {
        try {
            JsonNode node = objectMapper.readTree(content);

            FeaturedProgramRequestDTO dto = new FeaturedProgramRequestDTO();
            dto.setTitle(node.path("title").asText());
            dto.setCategory(category);
            dto.setLevel(level);
            dto.setShortDescription(node.path("shortDescription").asText());
            dto.setFullDescription(node.path("fullDescription").asText());

            List<String> learningOutcomes = new ArrayList<>();
            if (node.has("learningOutcomes") && node.path("learningOutcomes").isArray()) {
                for (JsonNode outcome : node.path("learningOutcomes")) {
                    learningOutcomes.add(outcome.asText());
                }
            }
            dto.setLearningOutcomes(learningOutcomes);

            List<String> highlights = new ArrayList<>();
            if (node.has("highlights") && node.path("highlights").isArray()) {
                for (JsonNode highlight : node.path("highlights")) {
                    highlights.add(highlight.asText());
                }
            }
            dto.setHighlights(highlights);

            List<FAQDto> faqs = new ArrayList<>();
            if (node.has("faqs") && node.path("faqs").isArray()) {
                int index = 0;
                for (JsonNode faqNode : node.path("faqs")) {
                    FAQDto faq = new FAQDto();
                    faq.setQuestion(faqNode.path("question").asText());
                    faq.setAnswer(faqNode.path("answer").asText());
                    faq.setOrderIndex(index++);
                    faqs.add(faq);
                }
            }
            dto.setFaqs(faqs);

            List<SyllabusWeekDto> syllabusWeeks = new ArrayList<>();
            if (node.has("syllabusWeeks") && node.path("syllabusWeeks").isArray()) {
                for (JsonNode weekNode : node.path("syllabusWeeks")) {
                    SyllabusWeekDto week = new SyllabusWeekDto();
                    week.setWeekNumber(weekNode.path("weekNumber").asInt());
                    week.setTitle(weekNode.path("title").asText());
                    week.setDateRange(weekNode.path("dateRange").asText());

                    List<String> items = new ArrayList<>();
                    if (weekNode.has("items") && weekNode.path("items").isArray()) {
                        for (JsonNode item : weekNode.path("items")) {
                            items.add(item.asText());
                        }
                    }
                    week.setItems(items);
                    syllabusWeeks.add(week);
                }
            }
            dto.setSyllabusWeeks(syllabusWeeks);

            return dto;

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse OpenAI generated content: " + e.getMessage(), e);
        }
    }

    // ===================== NEW: syllabus extraction from uploaded file text =====================

    public List<ExtractedWeekDto> generateSyllabusFromExtractedText(String extractedText) {
        if (openaiApiKey == null || openaiApiKey.isBlank()) {
            throw new RuntimeException("OpenAI API key is not configured");
        }

        String prompt = buildSyllabusPrompt(extractedText);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + openaiApiKey);

            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);

            Map<String, Object> body = new HashMap<>();
            body.put("model", openaiModel);
            body.put("messages", List.of(message));
            body.put("temperature", 0.2);
            body.put("response_format", Map.of("type", "json_object"));

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    OPENAI_URL, HttpMethod.POST, requestEntity, String.class);

            if (response.getBody() == null) {
                throw new RuntimeException("OpenAI API returned empty response");
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            String content = root.path("choices").get(0).path("message").path("content").asText();

            return parseSyllabusJson(content);

        } catch (Exception e) {
            throw new RuntimeException("Failed to extract syllabus using OpenAI: " + e.getMessage(), e);
        }
    }

    private String buildSyllabusPrompt(String extractedText) {
        String trimmed = extractedText.length() > 12000
                ? extractedText.substring(0, 12000)
                : extractedText;

        return "You are given raw text extracted from a course syllabus/training document.\n" +
                "Text:\n\"\"\"\n" + trimmed + "\n\"\"\"\n\n" +
                "Convert this into a structured weekly syllabus.\n" +
                "Return ONLY valid JSON with this exact shape, and nothing else:\n" +
                "{\n" +
                "  \"weeks\": [\n" +
                "    {\n" +
                "      \"title\": \"Week 1: ...\",\n" +
                "      \"modules\": [\n" +
                "        {\n" +
                "          \"title\": \"Module title\",\n" +
                "          \"sessions\": [\n" +
                "            { \"title\": \"Session title\", \"type\": \"Video\", \"duration\": \"20 min\" }\n" +
                "          ]\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}\n" +
                "Rules:\n" +
                "- \"type\" must be one of: Video, Live, Assignment, Quiz, Reading.\n" +
                "- If the document has numbered sections (e.g. \"01. Introduction\"), treat each numbered " +
                "section as a module, and group modules logically into weeks.\n" +
                "- If duration isn't stated, use an empty string for it.\n" +
                "- Do not include any text outside the JSON object.";
    }

    private List<ExtractedWeekDto> parseSyllabusJson(String content) {
        try {
            JsonNode root = objectMapper.readTree(content);
            List<ExtractedWeekDto> weeks = new ArrayList<>();

            JsonNode weeksNode = root.path("weeks");
            if (weeksNode.isArray()) {
                for (JsonNode weekNode : weeksNode) {
                    ExtractedWeekDto week = new ExtractedWeekDto();
                    week.setTitle(weekNode.path("title").asText());

                    List<ExtractedModuleDto> modules = new ArrayList<>();
                    JsonNode modulesNode = weekNode.path("modules");
                    if (modulesNode.isArray()) {
                        for (JsonNode moduleNode : modulesNode) {
                            ExtractedModuleDto module = new ExtractedModuleDto();
                            module.setTitle(moduleNode.path("title").asText());

                            List<ExtractedSessionDto> sessions = new ArrayList<>();
                            JsonNode sessionsNode = moduleNode.path("sessions");
                            if (sessionsNode.isArray()) {
                                for (JsonNode sessionNode : sessionsNode) {
                                    sessions.add(new ExtractedSessionDto(
                                            sessionNode.path("title").asText(),
                                            sessionNode.path("type").asText("Reading"),
                                            sessionNode.path("duration").asText("")
                                    ));
                                }
                            }
                            module.setSessions(sessions);
                            modules.add(module);
                        }
                    }
                    week.setModules(modules);
                    weeks.add(week);
                }
            }
            return weeks;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse syllabus JSON from OpenAI: " + e.getMessage(), e);
        }
    }
}