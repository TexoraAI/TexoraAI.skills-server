package com.lms.live_session.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class WhiteboardTextExtractionService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Parses an Excalidraw "elements" JSON array string and extracts the
     * text content of all type:"text" elements, joined by newlines.
     * Returns "" if elements is null/blank/unparseable or has no text elements.
     */
    public String extractText(String elementsJson) {
        if (elementsJson == null || elementsJson.isBlank()) {
            return "";
        }
        try {
            JsonNode root = objectMapper.readTree(elementsJson);
            if (!root.isArray()) {
                return "";
            }
            StringBuilder sb = new StringBuilder();
            for (JsonNode el : root) {
                JsonNode typeNode = el.get("type");
                JsonNode textNode = el.get("text");
                if (typeNode != null && "text".equals(typeNode.asText())
                        && textNode != null && !textNode.asText().isBlank()) {
                    if (sb.length() > 0) sb.append("\n");
                    sb.append(textNode.asText());
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
}