package com.lms.live_session.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * NEW FILE
 * Path: src/main/java/com/lms/live_session/service/OpenAiClientService.java
 *
 * Centralised OpenAI API caller.
 * Reads config from application.properties:
 *   openai.api-key=sk-...
 *   openai.model=gpt-4o
 *   openai.max-tokens=3000
 *   openai.temperature=0.7
 *
 * AiCompanionService delegates all API calls here so the key
 * is NEVER exposed to the frontend.
 */
@Service
public class OpenAiClientService {

    @Value("${openai.api-key}")
    private String openAiApiKey;

    @Value("${openai.model:gpt-4o}")
    private String model;

    @Value("${openai.max-tokens:3000}")
    private int maxTokens;

    @Value("${openai.temperature:0.7}")
    private double temperature;

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

    private final RestTemplate restTemplate;

    public OpenAiClientService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Send a single system + user prompt pair to OpenAI.
     * Returns the raw text response from the model.
     */
    public String chat(String systemPrompt, String userMessage) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.add(Map.of("role", "user", "content", userMessage));

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("max_tokens", maxTokens);
        body.put("temperature", temperature);
        body.put("messages", messages);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> responseEntity = restTemplate.exchange(
            OPENAI_URL, HttpMethod.POST, entity, Map.class
        );

        Map<String, Object> responseBody = responseEntity.getBody();
        if (responseBody == null) {
            throw new RuntimeException("Empty response from OpenAI");
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> choices =
            (List<Map<String, Object>>) responseBody.get("choices");

        if (choices == null || choices.isEmpty()) {
            throw new RuntimeException("No choices returned by OpenAI");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return (String) message.get("content");
    }

    /**
     * Send a full conversation history (multi-turn) to OpenAI.
     * Each entry in the messages list must have keys: "role" and "content".
     */
    public String chatWithHistory(String systemPrompt, List<Map<String, String>> conversationMessages) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.addAll(conversationMessages);

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("max_tokens", maxTokens);
        body.put("temperature", temperature);
        body.put("messages", messages);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> responseEntity = restTemplate.exchange(
            OPENAI_URL, HttpMethod.POST, entity, Map.class
        );

        Map<String, Object> responseBody = responseEntity.getBody();
        if (responseBody == null) throw new RuntimeException("Empty response from OpenAI");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> choices =
            (List<Map<String, Object>>) responseBody.get("choices");
        if (choices == null || choices.isEmpty()) throw new RuntimeException("No choices");

        @SuppressWarnings("unchecked")
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return (String) message.get("content");
    }

    public String getModel() { return model; }
}