//package com.lms.assessment.service;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.lms.assessment.dto.BulkUploadResponse;
//import org.apache.pdfbox.pdmodel.PDDocument;
//import org.apache.pdfbox.text.PDFTextStripper;
//import org.apache.poi.hwpf.HWPFDocument;
//import org.apache.poi.hwpf.extractor.WordExtractor;
//import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
//import org.apache.poi.xwpf.usermodel.XWPFDocument;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.web.multipart.MultipartFile;
//import org.apache.pdfbox.Loader;
//import java.io.InputStream;
//import java.nio.charset.StandardCharsets;
//import java.util.*;
//
//@Service
//public class BulkQuizParserService {
//
//    @Value("${openai.api.key}")
//    private String openaiApiKey;
//
//    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";
//
//    private final RestTemplate restTemplate = new RestTemplate();
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    public BulkUploadResponse parseFile(MultipartFile file) throws Exception {
//
//        String name = file.getOriginalFilename().toLowerCase();
//        String text = extractText(file, name);
//
//        String prompt = buildPrompt(text, name);
//
//        return callOpenAI(prompt);
//    }
//
//    /* ================= TEXT EXTRACTION ================= */
//
//    private String extractText(MultipartFile file, String name) throws Exception {
//        if (name.endsWith(".pdf")) return extractPdf(file);
//        if (name.endsWith(".docx")) return extractDocx(file);
//        if (name.endsWith(".doc")) return extractDoc(file);
//        return new String(file.getBytes(), StandardCharsets.UTF_8);
//    }
//
//    private String extractPdf(MultipartFile file) throws Exception {
//        try (InputStream is = file.getInputStream();
//             PDDocument doc = Loader.loadPDF(is.readAllBytes())) {
//
//            PDFTextStripper stripper = new PDFTextStripper();
//            return stripper.getText(doc);
//        }
//    }
//
//    private String extractDocx(MultipartFile file) throws Exception {
//        try (InputStream is = file.getInputStream();
//             XWPFDocument doc = new XWPFDocument(is);
//             XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
//            return extractor.getText();
//        }
//    }
//
//    private String extractDoc(MultipartFile file) throws Exception {
//        try (InputStream is = file.getInputStream();
//             HWPFDocument doc = new HWPFDocument(is);
//             WordExtractor extractor = new WordExtractor(doc)) {
//            return extractor.getText();
//        }
//    }
//
//    /* ================= OPENAI CALL ================= */
//
//    private BulkUploadResponse callOpenAI(String prompt) throws Exception {
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.setBearerAuth(openaiApiKey);
//
//        Map<String, Object> body = new HashMap<>();
//        body.put("model", "gpt-4o-mini");
//
//        List<Map<String, String>> messages = List.of(
//                Map.of("role", "user", "content", prompt)
//        );
//
//        body.put("messages", messages);
//
//        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
//
//        ResponseEntity<String> response =
//                restTemplate.postForEntity(OPENAI_URL, request, String.class);
//
//        JsonNode root = objectMapper.readTree(response.getBody());
//
//        String content = root
//                .path("choices")
//                .get(0)
//                .path("message")
//                .path("content")
//                .asText();
//
//        return parseResponse(content);
//    }
//
//    /* ================= PROMPT ================= */
//
//    private String buildPrompt(String text, String fileName) {
//        return """
//        Convert this text into quiz JSON.
//
//        Output ONLY JSON:
//        {
//          "title": "Quiz",
//          "questions": [
//            {
//              "text": "",
//              "options": {
//                "A": "",
//                "B": "",
//                "C": "",
//                "D": ""
//              },
//              "correctOption": "A"
//            }
//          ]
//        }
//
//        TEXT:
//        """ + text;
//    }
//
//    /* ================= PARSE ================= */
//
//    private BulkUploadResponse parseResponse(String reply) throws Exception {
//
//        String clean = reply.replaceAll("```json", "")
//                .replaceAll("```", "")
//                .trim();
//
//        JsonNode root = objectMapper.readTree(clean);
//
//        BulkUploadResponse res = new BulkUploadResponse();
//        res.setTitle(root.path("title").asText());
//
//        List<BulkUploadResponse.QuestionDTO> list = new ArrayList<>();
//
//        for (JsonNode q : root.path("questions")) {
//            BulkUploadResponse.QuestionDTO dto = new BulkUploadResponse.QuestionDTO();
//
//            dto.setText(q.path("text").asText());
//
//            Map<String, String> options = new HashMap<>();
//            options.put("A", q.path("options").path("A").asText());
//            options.put("B", q.path("options").path("B").asText());
//            options.put("C", q.path("options").path("C").asText());
//            options.put("D", q.path("options").path("D").asText());
//
//            dto.setOptions(options);
//            dto.setCorrectOption(q.path("correctOption").asText());
//
//            list.add(dto);
//        }
//
//        res.setQuestions(list);
//        return res;
//    }
//}


package com.lms.assessment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.assessment.dto.BulkUploadResponse;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.apache.pdfbox.Loader;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class BulkQuizParserService {

    @Value("${openai.api.key}")
    private String openaiApiKey;

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public BulkUploadResponse parseFile(MultipartFile file) throws Exception {
        String name = file.getOriginalFilename().toLowerCase();
        String text = extractText(file, name);
        String prompt = buildPrompt(text);
        return callOpenAI(prompt);
    }

    /* ================= TEXT EXTRACTION ================= */

    private String extractText(MultipartFile file, String name) throws Exception {
        if (name.endsWith(".pdf"))  return extractPdf(file);
        if (name.endsWith(".docx")) return extractDocx(file);
        if (name.endsWith(".doc"))  return extractDoc(file);
        return new String(file.getBytes(), StandardCharsets.UTF_8);
    }

    private String extractPdf(MultipartFile file) throws Exception {
        try (InputStream is = file.getInputStream();
             PDDocument doc = Loader.loadPDF(is.readAllBytes())) {
            return new PDFTextStripper().getText(doc);
        }
    }

    private String extractDocx(MultipartFile file) throws Exception {
        try (InputStream is = file.getInputStream();
             XWPFDocument doc = new XWPFDocument(is);
             XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
            return extractor.getText();
        }
    }

    private String extractDoc(MultipartFile file) throws Exception {
        try (InputStream is = file.getInputStream();
             HWPFDocument doc = new HWPFDocument(is);
             WordExtractor extractor = new WordExtractor(doc)) {
            return extractor.getText();
        }
    }

    /* ================= PROMPT ================= */

    private String buildPrompt(String text) {
        return """
        Analyze this text and convert it into a quiz JSON.

        Detect the quiz type automatically:
        - "Multiple Choice" if questions have 4 options A/B/C/D
        - "True / False" if questions have only True/False options
        - "Fill in the Blank" if questions have blanks to fill
        - "Short Answer" if questions need written answers

        Output ONLY valid JSON, no extra text, no markdown:
        {
          "title": "Quiz Title",
          "quizType": "Multiple Choice",
          "questions": [
            {
              "text": "Question text here",
              "options": { "A": "", "B": "", "C": "", "D": "" },
              "correctOption": "A",
              "answer": ""
            }
          ]
        }

        Rules:
        - For "Multiple Choice": fill options A/B/C/D and set correctOption, leave answer empty
        - For "True / False": set options as {"A":"True","B":"False"}, set correctOption to "A" or "B", leave answer empty
        - For "Fill in the Blank" or "Short Answer": fill answer field, set options to {} and correctOption to ""

        TEXT:
        """ + text;
    }

    /* ================= OPENAI CALL ================= */

    private BulkUploadResponse callOpenAI(String prompt) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openaiApiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("model", "gpt-4o-mini");
        body.put("messages", List.of(Map.of("role", "user", "content", prompt)));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(OPENAI_URL, request, String.class);

        JsonNode root = objectMapper.readTree(response.getBody());
        String content = root.path("choices").get(0).path("message").path("content").asText();

        return parseResponse(content);
    }

    /* ================= PARSE ================= */

    private BulkUploadResponse parseResponse(String reply) throws Exception {
        String clean = reply
                .replaceAll("(?s)```json", "")
                .replaceAll("```", "")
                .trim();

        JsonNode root = objectMapper.readTree(clean);

        BulkUploadResponse res = new BulkUploadResponse();
        res.setTitle(root.path("title").asText("Quiz"));
        res.setQuizType(root.path("quizType").asText("Multiple Choice"));

        List<BulkUploadResponse.QuestionDTO> list = new ArrayList<>();

        for (JsonNode q : root.path("questions")) {
            BulkUploadResponse.QuestionDTO dto = new BulkUploadResponse.QuestionDTO();
            dto.setText(q.path("text").asText());
            dto.setCorrectOption(q.path("correctOption").asText(""));
            dto.setAnswer(q.path("answer").asText(""));

            Map<String, String> options = new LinkedHashMap<>();
            JsonNode optNode = q.path("options");
            if (optNode.isObject()) {
                optNode.fields().forEachRemaining(e -> options.put(e.getKey(), e.getValue().asText()));
            }
            dto.setOptions(options);

            list.add(dto);
        }

        res.setQuestions(list);
        return res;
    }
}