package com.lms.course.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
}