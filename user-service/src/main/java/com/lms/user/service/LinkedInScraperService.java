

package com.lms.user.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lms.user.dto.ResumeRequestDTO;
import com.lms.user.dto.LinkedInScrapeRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
@Service
public class LinkedInScraperService {

    private static final Logger log = LoggerFactory.getLogger(LinkedInScraperService.class);

    @Value("${openai.api.key:}")
    private String openAiApiKey;

    private static final String OPENAI_CHAT_URL  = "https://api.openai.com/v1/chat/completions";
    private static final String RESPONSES_URL    = "https://api.openai.com/v1/responses";
    private static final String MODEL            = "gpt-4o";

//    private final RestTemplate restTemplate = new RestTemplate();
    private final RestTemplate restTemplate = buildRestTemplate();
    private final ObjectMapper objectMapper  = new ObjectMapper();

    // ─────────────────────────────────────────────────────────────────────────
    // MAIN ENTRY POINT
    // ─────────────────────────────────────────────────────────────────────────
    public ResumeRequestDTO buildResumeFromLinkedIn(LinkedInScrapeRequestDTO request) {

        boolean hasPdf = request.getBase64Pdf() != null && !request.getBase64Pdf().isBlank();
        boolean hasUrl = request.getLinkedInUrl() != null && !request.getLinkedInUrl().isBlank();

        log.info("LinkedIn import — hasPdf={}, hasUrl={}", hasPdf, hasUrl);

        String profileText = null;

        // ── PATH A: PDF uploaded (new LinkedIn PDF import flow) ──
        if (hasPdf) {
            try {
                profileText = extractTextFromBase64Pdf(request.getBase64Pdf());
                log.info("PDF text extracted: {} chars", profileText != null ? profileText.length() : 0);
            } catch (Exception e) {
                log.error("PDF extraction failed: {}", e.getMessage());
            }
        }

        // ── PATH B: URL provided (old web scrape flow) ──
        if ((profileText == null || profileText.length() < 200) && hasUrl) {
            try {
                profileText = fetchLinkedInViaOpenAI(request.getLinkedInUrl());
                log.info("OpenAI web search extracted {} chars", profileText != null ? profileText.length() : 0);
            } catch (Exception e) {
                log.error("OpenAI web search failed: {}", e.getMessage());
            }

            if (profileText == null || profileText.length() < 300) {
                try {
                    profileText = fetchLinkedInViaTargetedSearch(request.getLinkedInUrl());
                } catch (Exception e) {
                    log.error("Targeted search also failed: {}", e.getMessage());
                }
            }
        }

        // ── PATH C: Fallback ──
        if (profileText == null || profileText.length() < 100) {
            log.warn("All fetch methods failed — using minimal context");
            String hint = hasUrl ? request.getLinkedInUrl() : (request.getFileName() != null ? request.getFileName() : "LinkedIn Profile");
            profileText = buildMinimalContext(hint, request.getJobTitle());
        }

        log.info("Profile context ready ({} chars) — building resume...", profileText.length());
        return buildResumeFromText(profileText, request);
    }

    // ── NEW METHOD: Extract text from base64-encoded PDF ──
    private String extractTextFromBase64Pdf(String base64Pdf) throws Exception {
        String cleanBase64 = base64Pdf.contains(",") 
            ? base64Pdf.substring(base64Pdf.indexOf(",") + 1) 
            : base64Pdf;
        
        byte[] pdfBytes = java.util.Base64.getDecoder().decode(cleanBase64.trim());
        log.info("LinkedIn PDF bytes: {}", pdfBytes.length);

        try (org.apache.pdfbox.pdmodel.PDDocument doc = 
                 org.apache.pdfbox.Loader.loadPDF(pdfBytes)) {
            org.apache.pdfbox.text.PDFTextStripper stripper = 
                new org.apache.pdfbox.text.PDFTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(doc);
            log.info("LinkedIn PDF text extracted: {} chars", text.length());
            return text;
        }
    }
    // ─────────────────────────────────────────────────────────────────────────
    // METHOD 1: OpenAI Responses API + web_search_preview
    // This is the ONLY reliable way to get real LinkedIn data
    // ─────────────────────────────────────────────────────────────────────────
    private String fetchLinkedInViaOpenAI(String linkedInUrl) throws Exception {

        log.info("Fetching LinkedIn via OpenAI web_search_preview: {}", linkedInUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("model", MODEL);
        body.put("tools", List.of(Map.of("type", "web_search_preview")));
        body.put("input",
            "Please visit this LinkedIn profile and extract ALL professional information:\n" +
            linkedInUrl + "\n\n" +
            "I need you to extract EVERY detail from this public LinkedIn profile page:\n\n" +
            "1. FULL NAME (exact name as shown on profile)\n" +
            "2. HEADLINE / CURRENT JOB TITLE\n" +
            "3. LOCATION (city, country)\n" +
            "4. ABOUT / SUMMARY SECTION (full text)\n" +
            "5. ALL WORK EXPERIENCES — for each:\n" +
            "   - Company name\n" +
            "   - Job title / position\n" +
            "   - Start date and end date (or 'Present')\n" +
            "   - Location\n" +
            "   - Full description / responsibilities\n" +
            "6. ALL EDUCATION — for each:\n" +
            "   - Institution name\n" +
            "   - Degree and field of study\n" +
            "   - Start and end year\n" +
            "   - Grade/CGPA if shown\n" +
            "7. ALL SKILLS listed on the profile\n" +
            "8. ALL PROJECTS — name, description, technologies used\n" +
            "9. ALL CERTIFICATIONS — name, issuer, date\n" +
            "10. Contact info if visible (email, phone, GitHub, portfolio)\n\n" +
            "Be EXHAUSTIVE. Extract every single entry. Do NOT summarize or skip entries.\n" +
            "Return everything as plain text with clear section headers."
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(RESPONSES_URL, entity, String.class);

        JsonNode root = objectMapper.readTree(response.getBody());
        StringBuilder result = new StringBuilder();

        for (JsonNode item : root.path("output")) {
            if ("message".equals(item.path("type").asText())) {
                for (JsonNode content : item.path("content")) {
                    if ("output_text".equals(content.path("type").asText())) {
                        result.append(content.path("text").asText()).append("\n");
                    }
                }
            }
        }

        String extracted = result.toString().trim();
        log.info("OpenAI web_search_preview returned {} chars for LinkedIn", extracted.length());

        // Check if the result is meaningful (not just "I couldn't access" type responses)
        if (extracted.toLowerCase().contains("could not access") ||
            extracted.toLowerCase().contains("unable to access") ||
            extracted.toLowerCase().contains("not able to browse") ||
            extracted.toLowerCase().contains("cannot access") ||
            extracted.length() < 200) {
            throw new RuntimeException("OpenAI could not access the LinkedIn profile: " + extracted.substring(0, Math.min(200, extracted.length())));
        }

        return extracted;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // METHOD 2: Targeted web search using name extracted from URL
    // Searches for the person by name + LinkedIn to get public info
    // ─────────────────────────────────────────────────────────────────────────
    private String fetchLinkedInViaTargetedSearch(String linkedInUrl) throws Exception {

        log.info("Fetching LinkedIn via targeted name search");

        String username    = extractUsername(linkedInUrl);
        String displayName = username.replace("-", " ").replaceAll("\\d+", "").trim();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("model", MODEL);
        body.put("tools", List.of(Map.of("type", "web_search_preview")));
        body.put("input",
            "Search for this person's professional profile and extract all available information:\n\n" +
            "LinkedIn URL: " + linkedInUrl + "\n" +
            "Name hint from URL: " + displayName + "\n\n" +
            "Please search for:\n" +
            "1. '" + displayName + " LinkedIn profile'\n" +
            "2. '" + displayName + " developer experience education'\n\n" +
            "From all search results, extract:\n" +
            "- Full name\n" +
            "- Current job title and company\n" +
            "- Work experience history (companies, roles, dates, descriptions)\n" +
            "- Education (universities, degrees, years, grades)\n" +
            "- Technical skills\n" +
            "- Projects\n" +
            "- Certifications\n\n" +
            "Provide ALL information found. Be specific and detailed."
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(RESPONSES_URL, entity, String.class);

        JsonNode root = objectMapper.readTree(response.getBody());
        StringBuilder result = new StringBuilder();

        for (JsonNode item : root.path("output")) {
            if ("message".equals(item.path("type").asText())) {
                for (JsonNode content : item.path("content")) {
                    if ("output_text".equals(content.path("type").asText())) {
                        result.append(content.path("text").asText()).append("\n");
                    }
                }
            }
        }

        return result.toString().trim();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // METHOD 3: Minimal fallback context
    // ─────────────────────────────────────────────────────────────────────────

    private String buildMinimalContext(String hint, String jobTitle) {
        return "LinkedIn Profile: " + hint + "\n" +
               "Target Job Title: " + (jobTitle != null && !jobTitle.isBlank() ? jobTitle : "Software Developer") + "\n" +
               "Note: Profile could not be fully read. User will review and edit all details after import.";
    }
    // ─────────────────────────────────────────────────────────────────────────
    // STEP 2: Convert fetched text → ResumeRequestDTO via 2 AI chunks
    // ─────────────────────────────────────────────────────────────────────────
    private ResumeRequestDTO buildResumeFromText(String profileText, LinkedInScrapeRequestDTO request) {

        String targetRole  = nvl(request.getJobTitle());
        String tmpl        = nvl(request.getTemplateName(), "classic");
        String skillsExtra = nvl(request.getExtraSkills());
        String linkedInUrl = request.getLinkedInUrl();
        String effectiveRole = targetRole.isEmpty() ? "Software Developer" : targetRole;

        String contextBlock =
            "LinkedIn URL: " + linkedInUrl + "\n" +
            "Target Job Role: " + effectiveRole + "\n" +
            (skillsExtra.isEmpty() ? "" : "Extra Skills to include: " + skillsExtra + "\n") +
            "\n=== LINKEDIN PROFILE DATA ===\n" +
            profileText;

        // ── CHUNK 1: Personal Info + Work Experience + Education ──
        String system1 =
            "You are a senior ATS resume specialist. Return ONLY valid JSON — no markdown, no code blocks, no comments.\n" +
            "Output a FLAT JSON. Do NOT nest under 'personalInfo' or any wrapper.\n" +
            "Required top-level keys: title, templateName, firstName, lastName, jobTitle, email, phone,\n" +
            "city, country, linkedinUrl, githubUrl, portfolioUrl, profileSummary,\n" +
            "workExperiences (array), educations (array).\n\n" +

            "=== EXTRACTION RULES ===\n" +
            "1. Extract the REAL name from the profile data. Do NOT use placeholder names.\n" +
            "2. Extract EVERY work experience entry — company, position, dates, location, description.\n" +
            "3. Extract EVERY education entry — institution, degree, field, dates, grade.\n" +
            "4. workExperiences[].description: Rewrite as ATS bullet points:\n" +
            "   - Use '• ' prefix for each bullet\n" +
            "   - Start each bullet with action verb: Developed, Implemented, Architected, Optimized,\n" +
            "     Engineered, Delivered, Automated, Led, Integrated, Deployed, Built, Collaborated\n" +
            "   - Include specific technology and quantified outcome where possible\n" +
            "   - Write 4-5 bullets per experience\n" +
            "   - If description exists in profile, rewrite it in bullet format keeping all facts\n" +
            "5. profileSummary: Write 4-5 ATS-optimised sentences for role: " + effectiveRole + "\n" +
            "   - Include years of experience, tech stack, domain expertise, value statement\n" +
            "   - Use keywords from the actual profile data\n" +
            "6. isCurrent=true when endDate is Present/Current/Now\n" +
            "7. Dates format: 'Mon YYYY'\n" +
            "8. linkedinUrl: use the provided URL\n" +
            "9. NEVER fabricate company names or institutions not found in the profile data";

        String user1 =
            "Extract ALL resume data from the LinkedIn profile below.\n" +
            "Return FLAT JSON (no wrappers):\n" +
            "{\"title\":\"<Name>'s Resume\",\"templateName\":\"" + tmpl + "\"," +
            "\"firstName\":\"\",\"lastName\":\"\"," +
            "\"jobTitle\":\"<extract from profile or use: " + effectiveRole + ">\"," +
            "\"email\":\"\",\"phone\":\"\",\"city\":\"\",\"country\":\"\"," +
            "\"linkedinUrl\":\"" + linkedInUrl + "\",\"githubUrl\":\"\",\"portfolioUrl\":\"\"," +
            "\"profileSummary\":\"<4-5 ATS-keyword sentences for " + effectiveRole + ">\"," +
            "\"workExperiences\":[{\"companyName\":\"\",\"position\":\"\",\"startDate\":\"\",\"endDate\":\"\"," +
            "\"isCurrent\":false,\"location\":\"\",\"description\":\"• <Action Verb> ...\\n• <Action Verb> ...\",\"displayOrder\":0}]," +
            "\"educations\":[{\"institution\":\"\",\"degree\":\"\",\"fieldOfStudy\":\"\"," +
            "\"startDate\":\"\",\"endDate\":\"\",\"grade\":\"\",\"description\":\"\",\"displayOrder\":0}]}\n\n" +
            "PROFILE DATA:\n" + contextBlock;

        // ── CHUNK 2: Skills + Projects + Certifications ──
        String system2 =
            "You are a senior ATS resume specialist. Return ONLY valid JSON — no markdown, no code blocks, no comments.\n" +
            "Output a FLAT JSON with EXACTLY these top-level keys: skills, projects, certifications.\n" +
            "proficiencyLevel must be EXACTLY: BEGINNER, INTERMEDIATE, ADVANCED, or EXPERT.\n\n" +

            "=== EXTRACTION RULES ===\n" +
            "1. skills: Extract ALL skills from the profile. Add extra skills if provided.\n" +
            "   Assign proficiencyLevel based on context (years, role, description):\n" +
            "   EXPERT = primary/core skills, ADVANCED = supporting skills,\n" +
            "   INTERMEDIATE = mentioned skills, BEGINNER = tools/technologies briefly mentioned\n" +
            (skillsExtra.isEmpty() ? "" :
            "   MUST include these extra skills: " + skillsExtra + " at INTERMEDIATE level minimum\n") +
            "2. projects: Extract ALL projects from profile.\n" +
            "   description format: One purpose sentence + 3-4 '• ' bullet points with action verbs + tech + outcome\n" +
            "   If no projects in profile, generate 2 realistic ones based on their work experience and skills\n" +
            "3. certifications: Extract ALL certifications from profile.\n" +
            "   If none found, generate 2 relevant ones for the role: " + effectiveRole;

        String user2 =
            "Extract skills, projects, and certifications from the LinkedIn profile below.\n" +
            (skillsExtra.isEmpty() ? "" : "MUST include extra skills: " + skillsExtra + "\n") +
            "Return FLAT JSON:\n" +
            "{\"skills\":[{\"skillName\":\"\",\"proficiencyLevel\":\"INTERMEDIATE\",\"displayOrder\":0}]," +
            "\"projects\":[{\"projectName\":\"\",\"techStack\":\"\",\"projectUrl\":\"\"," +
            "\"startDate\":\"\",\"endDate\":\"\",\"description\":\"<purpose sentence>.\\n• <Action Verb> ...\\n• <Action Verb> ...\",\"displayOrder\":0}]," +
            "\"certifications\":[{\"certName\":\"\",\"issuingOrganization\":\"\",\"issueDate\":\"\"," +
            "\"expiryDate\":\"\",\"credentialId\":\"\",\"credentialUrl\":\"\",\"displayOrder\":0}]}\n\n" +
            "PROFILE DATA:\n" + contextBlock;

        log.info("Calling OpenAI Chat — chunk 1 (personal+work+edu)");
        String raw1 = callChatApi(system1, user1, 8192);

        log.info("Calling OpenAI Chat — chunk 2 (skills+projects+certs)");
        String raw2 = callChatApi(system2, user2, 6000);

        return mergeChunks(raw1, raw2);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Merge two JSON chunks → ResumeRequestDTO
    // ─────────────────────────────────────────────────────────────────────────
    private ResumeRequestDTO mergeChunks(String raw1, String raw2) {
        try {
            JsonNode node1 = flattenIfWrapped(objectMapper.readTree(sanitizeJson(raw1)));
            JsonNode node2 = flattenIfWrapped(objectMapper.readTree(sanitizeJson(raw2)));

            ObjectNode merged = objectMapper.createObjectNode();
            node1.fields().forEachRemaining(e -> merged.set(e.getKey(), e.getValue()));

            if (node2.has("skills"))         merged.set("skills",         node2.get("skills"));
            if (node2.has("projects"))       merged.set("projects",       node2.get("projects"));
            if (node2.has("certifications")) merged.set("certifications", node2.get("certifications"));

            for (String f : new String[]{"workExperiences","educations","skills","projects","certifications"}) {
                if (!merged.has(f) || merged.get(f).isNull() || !merged.get(f).isArray())
                    merged.set(f, objectMapper.createArrayNode());
            }
            for (String f : new String[]{"title","templateName","firstName","lastName","jobTitle",
                                         "email","phone","city","country","linkedinUrl",
                                         "githubUrl","portfolioUrl","profileSummary"}) {
                if (!merged.has(f) || merged.get(f).isNull()) merged.put(f, "");
            }

            log.info("Resume merged — work:{} edu:{} skills:{} projects:{} certs:{}",
                    merged.get("workExperiences").size(), merged.get("educations").size(),
                    merged.get("skills").size(), merged.get("projects").size(),
                    merged.get("certifications").size());

            return objectMapper.treeToValue(merged, ResumeRequestDTO.class);

        } catch (Exception e) {
            log.error("mergeChunks failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to build resume from LinkedIn data. Please try again.");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Flatten AI wrapper keys (personalInfo, data, resume, etc.)
    // ─────────────────────────────────────────────────────────────────────────
    private JsonNode flattenIfWrapped(JsonNode node) {
        String[] wrappers = {"personalInfo","data","resume","candidate","result","output","response"};
        boolean hasExpected = node.has("firstName") || node.has("title") || node.has("skills")
                           || node.has("workExperiences") || node.has("profileSummary");
        if (!hasExpected) {
            for (String w : wrappers) {
                if (node.has(w) && node.get(w).isObject()) {
                    log.warn("Flattening AI wrapper: '{}'", w);
                    ObjectNode m = objectMapper.createObjectNode();
                    node.fields().forEachRemaining(e -> { if (!e.getKey().equals(w)) m.set(e.getKey(), e.getValue()); });
                    node.get(w).fields().forEachRemaining(e -> m.set(e.getKey(), e.getValue()));
                    return m;
                }
            }
        }
        if (node.has("personalInfo") && node.get("personalInfo").isObject()) {
            ObjectNode m = objectMapper.createObjectNode();
            node.fields().forEachRemaining(e -> { if (!e.getKey().equals("personalInfo")) m.set(e.getKey(), e.getValue()); });
            node.get("personalInfo").fields().forEachRemaining(e -> m.set(e.getKey(), e.getValue()));
            return m;
        }
        return node;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // OpenAI Chat Completions
    // ─────────────────────────────────────────────────────────────────────────
    private String callChatApi(String systemPrompt, String userPrompt, int maxTokens) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("model",       MODEL);
        body.put("max_tokens",  maxTokens);
        body.put("temperature", 0.1);
        body.put("messages", List.of(
            Map.of("role", "system", "content", systemPrompt),
            Map.of("role", "user",   "content", userPrompt)
        ));

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    OPENAI_CHAT_URL, new HttpEntity<>(body, headers), String.class);
            JsonNode root = objectMapper.readTree(response.getBody());

            if (root.has("error"))
                throw new RuntimeException(root.path("error").path("message").asText());

            JsonNode usage = root.path("usage");
            log.info("Tokens — prompt:{} completion:{} total:{}",
                    usage.path("prompt_tokens").asInt(),
                    usage.path("completion_tokens").asInt(),
                    usage.path("total_tokens").asInt());

            if ("length".equals(root.path("choices").get(0).path("finish_reason").asText()))
                log.warn("Response TRUNCATED at max_tokens={}", maxTokens);

            return sanitizeJson(root.path("choices").get(0).path("message").path("content").asText());

        } catch (Exception e) {
            log.error("OpenAI Chat error: {}", e.getMessage(), e);
            throw new RuntimeException("AI service error: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────
    private String extractUsername(String url) {
        if (url == null) return "";
        return url.replaceAll(".*/in/", "")
                  .replaceAll("[/?#].*", "")
                  .trim();
    }

    private String sanitizeJson(String raw) {
        if (raw == null) return "{}";
        raw = raw.replaceAll("(?s)```json\\s*", "").replaceAll("(?s)```\\s*", "").trim();
        int start = raw.indexOf('{');
        int end   = raw.lastIndexOf('}');
        if (start >= 0 && end > start) raw = raw.substring(start, end + 1);
        return raw;
    }

    private String nvl(String v)                { return v != null ? v.trim() : ""; }
    private String nvl(String v, String defVal) { return (v != null && !v.isBlank()) ? v.trim() : defVal; }
    
    private RestTemplate buildRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5_000);
        factory.setReadTimeout(120_000);
        return new RestTemplate(factory);
    }
}