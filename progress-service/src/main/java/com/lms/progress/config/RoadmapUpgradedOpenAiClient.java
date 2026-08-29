package com.lms.progress.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Thin wrapper around the OpenAI Chat Completions API for the RoadmapUpgraded
 * feature. All prompt construction (system prompt, message history, JSON-mode
 * requests) happens in RoadmapUpgradedService - this class only knows how to
 * make the HTTP call and hand back either raw text or a parsed JsonNode.
 *
 * Not shared with any other feature's OpenAI usage - see the note in
 * RoadmapUpgradedOpenAiConfig.
 */
@Component
public class RoadmapUpgradedOpenAiClient {

    private final RestTemplate restTemplate;
    private final RoadmapUpgradedOpenAiConfig config;
    private final ObjectMapper objectMapper;

    public RoadmapUpgradedOpenAiClient(RestTemplate roadmapUpgradedRestTemplate,
                                        RoadmapUpgradedOpenAiConfig config,
                                        ObjectMapper objectMapper) {
        this.restTemplate = roadmapUpgradedRestTemplate;
        this.config = config;
        this.objectMapper = objectMapper;
    }

    /**
     * Simple system+user prompt call, returns the assistant's raw text reply.
     */
    public String completeText(String systemPrompt, String userPrompt) {
        ObjectNode body = buildChatBody(systemPrompt, userPrompt, false);
        return callAndExtractContent(body);
    }

    /**
     * Same as completeText, but forces the model to respond with a single
     * JSON object (response_format: json_object). Caller is responsible for
     * parsing the returned string with an ObjectMapper against their target
     * shape (e.g. quiz questions, mentor reply + suggestedFollowUps).
     */
    public String completeJson(String systemPrompt, String userPrompt) {
        ObjectNode body = buildChatBody(systemPrompt, userPrompt, true);
        return callAndExtractContent(body);
    }

    /**
     * Multi-turn variant used by the AI Mentor, where recent conversation
     * history needs to be replayed back into the prompt. historyMessages is
     * a list of "role:content" pairs already ordered oldest-first; role must
     * be "user" or "assistant".
     */
    public String completeJsonWithHistory(String systemPrompt,
                                           List<RoadmapUpgradedChatTurn> historyMessages,
                                           String latestUserMessage) {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", config.getModel());
        body.put("temperature", 0.7);

        ObjectNode responseFormat = objectMapper.createObjectNode();
        responseFormat.put("type", "json_object");
        body.set("response_format", responseFormat);

        ArrayNode messages = objectMapper.createArrayNode();
        messages.add(chatMessage("system", systemPrompt));
        if (historyMessages != null) {
            for (RoadmapUpgradedChatTurn turn : historyMessages) {
                messages.add(chatMessage(turn.getRole(), turn.getContent()));
            }
        }
        messages.add(chatMessage("user", latestUserMessage));
        body.set("messages", messages);

        return callAndExtractContent(body);
    }

    private ObjectNode buildChatBody(String systemPrompt, String userPrompt, boolean jsonMode) {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", config.getModel());
        body.put("temperature", 0.7);

        if (jsonMode) {
            ObjectNode responseFormat = objectMapper.createObjectNode();
            responseFormat.put("type", "json_object");
            body.set("response_format", responseFormat);
        }

        ArrayNode messages = objectMapper.createArrayNode();
        messages.add(chatMessage("system", systemPrompt));
        messages.add(chatMessage("user", userPrompt));
        body.set("messages", messages);

        return body;
    }

    private ObjectNode chatMessage(String role, String content) {
        ObjectNode message = objectMapper.createObjectNode();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    private String callAndExtractContent(ObjectNode body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(config.getApiKey());

        HttpEntity<String> requestEntity = new HttpEntity<>(body.toString(), headers);

        ResponseEntity<String> response = restTemplate.exchange(
                config.getApiUrl(),
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        if (response.getBody() == null) {
            throw new RoadmapUpgradedOpenAiException("Empty response body from OpenAI");
        }

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                throw new RoadmapUpgradedOpenAiException("No choices returned from OpenAI");
            }
            JsonNode messageContent = choices.get(0).path("message").path("content");
            if (messageContent.isMissingNode()) {
                throw new RoadmapUpgradedOpenAiException("No message content returned from OpenAI");
            }
            return messageContent.asText();
        } catch (RoadmapUpgradedOpenAiException e) {
            throw e;
        } catch (Exception e) {
            throw new RoadmapUpgradedOpenAiException("Failed to parse OpenAI response: " + e.getMessage(), e);
        }
    }

    /**
     * One turn of prior conversation, role is "user" or "assistant".
     */
    public static class RoadmapUpgradedChatTurn {
        private final String role;
        private final String content;

        public RoadmapUpgradedChatTurn(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return role;
        }

        public String getContent() {
            return content;
        }
    }

    public static class RoadmapUpgradedOpenAiException extends RuntimeException {
        public RoadmapUpgradedOpenAiException(String message) {
            super(message);
        }

        public RoadmapUpgradedOpenAiException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
