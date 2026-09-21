package com.lms.chat.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class OpenAiService {

    // Two distinct OpenAI TTS voice ids so a two-host podcast can sound like two people.
    public static final String VOICE_HOST_A = "alloy";
    public static final String VOICE_HOST_B = "onyx";

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.model:gpt-3.5-turbo}")
    private String model;

    @Value("${openai.tts.model:tts-1}")
    private String ttsModel;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";
    private static final String OPENAI_TTS_URL = "https://api.openai.com/v1/audio/speech";
    private static final String OPENAI_IMAGE_URL = "https://api.openai.com/v1/images/generations";
    // Default completion token cap for lightweight calls (short reports,
    // quiz/flashcard batches, topic suggestions, etc). Callers generating
    // large structured JSON (e.g. a multi-slide deck with per-slide bullets
    // and speaker notes) should use the chat(..., maxTokens) overload below
    // with a higher explicit limit — otherwise the response gets silently
    // truncated mid-JSON, which fails parsing even after a retry.
    private static final int DEFAULT_MAX_TOKENS = 2000;

    public String chat(String systemPrompt, String userMessage) {
        return chat(systemPrompt, userMessage, DEFAULT_MAX_TOKENS);
    }

    /**
     * Same as {@link #chat(String, String)} but with an explicit completion
     * token cap, for calls that need more room than the default (e.g. large
     * structured JSON like a multi-slide deck).
     */
    public String chat(String systemPrompt, String userMessage, int maxTokens) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);

        Map<String, Object> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", List.of(systemMsg, userMsg));
        body.put("max_tokens", maxTokens);
        body.put("temperature", 0.7);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        try {
        	System.out.println("▶ Sending request to OpenAI Chat API...");
            ResponseEntity<Map> response = restTemplate.postForEntity(
                OPENAI_URL, request, Map.class
            );
            System.out.println("Response Status = " + response.getStatusCode());

            Map<?, ?> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("choices")) {
                List<?> choices = (List<?>) responseBody.get("choices");
                if (!choices.isEmpty()) {
                    Map<?, ?> choice = (Map<?, ?>) choices.get(0);
                    Map<?, ?> message = (Map<?, ?>) choice.get("message");
                    return (String) message.get("content");
                }
            }
        } 
//        catch (Exception e) {
//            throw new RuntimeException("OpenAI API call failed: " + e.getMessage());
//        }
        catch (Exception e) {

            System.err.println("❌ OpenAI Chat FAILED");

            e.printStackTrace();

            throw new RuntimeException("OpenAI API call failed", e);
        }

        return "I could not generate a response. Please try again.";
    }

    /**
     * Calls OpenAI's audio speech (TTS) endpoint and returns raw MP3 bytes.
     * Pass VOICE_HOST_A / VOICE_HOST_B (or any other valid OpenAI voice id) to
     * control which voice speaks a given line.
     */
    public byte[] textToSpeech(String text, String voice) {
        if (text == null || text.isBlank()) {
            throw new RuntimeException("OpenAI TTS call failed: no text to synthesize");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        headers.setAccept(List.of(MediaType.valueOf("audio/mpeg"), MediaType.APPLICATION_OCTET_STREAM));

        Map<String, Object> body = new HashMap<>();
        body.put("model", ttsModel);
        body.put("voice", voice);
        body.put("input", text);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            System.out.println("▶ Sending request to OpenAI TTS API...");
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    OPENAI_TTS_URL, HttpMethod.POST, request, byte[].class
            );
            System.out.println("Response Status = " + response.getStatusCode());

            byte[] audio = response.getBody();
            if (audio == null || audio.length == 0) {
                throw new RuntimeException("OpenAI TTS returned empty audio");
            }
            return audio;
        } catch (Exception e) {
            System.err.println("❌ OpenAI TTS FAILED");
            e.printStackTrace();
            throw new RuntimeException("OpenAI TTS call failed", e);
        }
    }

    /**
     * Calls OpenAI's image generation endpoint (gpt-image-1) and returns raw
     * decoded image bytes (PNG). OpenAI returns the image base64-encoded in
     * the response body; this method decodes it before returning.
     *
     * Overload that omits quality — preserves existing behavior exactly
     * (no "quality" field sent, so OpenAI uses its own default) for callers
     * like Infographic generation that haven't been updated to pass one.
     *
     * @param prompt description of the image to generate
     * @param size   a valid OpenAI image size string, e.g. "1536x1024",
     *               "1024x1536", or "1024x1024"
     */
    public byte[] generateImage(String prompt, String size) {
        return generateImage(prompt, size, null);
    }

    /**
     * Same as {@link #generateImage(String, String)} but allows an explicit
     * OpenAI image "quality" value (e.g. "low", "medium", "high") to be
     * requested. Pass {@code null} (or blank) to omit the field entirely and
     * fall back to OpenAI's default, matching prior behavior.
     *
     * @param prompt  description of the image to generate
     * @param size    a valid OpenAI image size string, e.g. "1536x1024",
     *                "1024x1536", or "1024x1024"
     * @param quality optional OpenAI image quality value, or null/blank to omit
     */
    public byte[] generateImage(String prompt, String size, String quality) {
        if (prompt == null || prompt.isBlank()) {
            throw new RuntimeException("OpenAI image generation failed: no prompt provided");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("model", "gpt-image-1");
        body.put("prompt", prompt);
        body.put("size", size);
        body.put("n", 1);
        if (quality != null && !quality.isBlank()) {
            body.put("quality", quality);
        }

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            System.out.println("▶ Sending request to OpenAI Image API...");
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    OPENAI_IMAGE_URL, request, Map.class
            );
            System.out.println("Response Status = " + response.getStatusCode());

            Map<?, ?> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("data")) {
                List<?> data = (List<?>) responseBody.get("data");
                if (!data.isEmpty()) {
                    Map<?, ?> first = (Map<?, ?>) data.get(0);
                    Object b64 = first.get("b64_json");
                    if (b64 instanceof String b64Str && !b64Str.isBlank()) {
                        return Base64.getDecoder().decode(b64Str);
                    }
                }
            }

            throw new RuntimeException("OpenAI image generation returned no image data "
                    + "(possible content-policy rejection or malformed response)");
        } catch (RuntimeException e) {
            // Re-throw our own diagnostic RuntimeExceptions without re-wrapping them.
            System.err.println("❌ OpenAI Image generation FAILED: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("❌ OpenAI Image generation FAILED");
            e.printStackTrace();
            throw new RuntimeException("OpenAI image generation call failed", e);
        }
    }
}