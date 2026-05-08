package com.lms.user.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lms.user.dto.AIResumeRequestDTO;
import com.lms.user.dto.AIResumeResponseDTO;
import com.lms.user.dto.ResumeRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class AIResumeService {

    private static final Logger log = LoggerFactory.getLogger(AIResumeService.class);

    @Value("${openai.api.key:}")
    private String openAiApiKey;

    private static final String CHAT_URL      = "https://api.openai.com/v1/chat/completions";
    private static final String RESPONSES_URL = "https://api.openai.com/v1/responses";
    private static final String MODEL         = "gpt-4o";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper  = new ObjectMapper();

    // ─────────────────────────────────────────────────────────────────────────
    // 1. AI Generate Resume
    // ─────────────────────────────────────────────────────────────────────────
    public ResumeRequestDTO generateResume(AIResumeRequestDTO.GenerateRequest req) {

        String name         = nvl(req.getName());
        String email        = nvl(req.getEmail());
        String linkedinUrl  = nvl(req.getLinkedinUrl());
        String jobTitle     = nvl(req.getJobTitle(), "Software Developer");
        String years        = nvl(req.getYearsOfExperience(), "2");
        String skills       = nvl(req.getSkills());
        String templateName = nvl(req.getTemplateName(), "classic");

        String firstName = name.contains(" ") ? name.substring(0, name.indexOf(" "))  : name;
        String lastName  = name.contains(" ") ? name.substring(name.indexOf(" ") + 1) : "";

        // Path A: LinkedIn URL provided → fetch real profile via OpenAI web search
        if (!linkedinUrl.isEmpty()) {
            log.info("LinkedIn URL provided — fetching real profile via web search");
            try {
                String linkedInRawData = fetchLinkedInProfile(linkedinUrl);
                if (linkedInRawData != null && !linkedInRawData.isBlank()) {
                    return generateFromLinkedInData(
                            linkedInRawData, name, email, linkedinUrl,
                            jobTitle, years, skills, templateName,
                            firstName, lastName);
                }
                log.warn("LinkedIn fetch returned empty — falling back to AI-only");
            } catch (Exception ex) {
                log.error("LinkedIn web-fetch failed: {} — falling back", ex.getMessage());
            }
        }

        // Path B: No LinkedIn → AI-only chunked generation
        return generateAiOnlyChunked(name, email, linkedinUrl, jobTitle, years, skills,
                                     templateName, firstName, lastName);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Fetch LinkedIn via OpenAI Responses API + web_search_preview
    // ─────────────────────────────────────────────────────────────────────────
    private String fetchLinkedInProfile(String linkedinUrl) throws Exception {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("model", MODEL);
        body.put("tools", List.of(Map.of("type", "web_search_preview")));
        body.put("input",
            "Visit this LinkedIn profile URL and extract EVERY professional detail:\n" +
            linkedinUrl + "\n\n" +
            "Extract ALL of the following in plain text:\n" +
            "1. Full Name\n2. Current Job Title / Headline\n3. Location\n4. About / Summary\n" +
            "5. ALL Work Experiences — company, role, start date, end date, location, full description\n" +
            "6. ALL Education — institution, degree, field, dates, grade\n" +
            "7. ALL Skills\n8. ALL Projects — name, dates, tech, description\n" +
            "9. ALL Certifications — name, org, date, credential ID\n\n" +
            "Be exhaustive. Do NOT skip any entry."
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        log.info("Calling OpenAI Responses API for LinkedIn: {}", linkedinUrl);
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
        log.info("LinkedIn extracted {} chars", extracted.length());
        return extracted;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Path A: Generate from real LinkedIn data (chunked)
    // ─────────────────────────────────────────────────────────────────────────
    private ResumeRequestDTO generateFromLinkedInData(
            String linkedInData, String name, String email,
            String linkedinUrl, String jobTitle, String years,
            String skills, String templateName,
            String firstName, String lastName) {

        log.info("Generating resume from LinkedIn data (chunked)");

        String contextBlock =
            "Candidate: " + name + " | Email: " + email + " | LinkedIn: " + linkedinUrl +
            " | Target: " + jobTitle + " | Exp: " + years + "yrs | Extra skills: " + skills + "\n\n" +
            "LINKEDIN DATA:\n" + linkedInData;

        // ── CRITICAL: Explicit flat-field instructions, no nested objects ──
        String system1 =
            "You are an expert ATS resume writer. Return ONLY valid JSON — no markdown, no comments, no code blocks.\n" +
            "IMPORTANT: Output a FLAT JSON object only. Do NOT nest fields under 'personalInfo' or any other wrapper.\n" +
            "The JSON must have these exact top-level keys: title, templateName, firstName, lastName, jobTitle,\n" +
            "email, phone, city, country, linkedinUrl, githubUrl, portfolioUrl, profileSummary,\n" +
            "workExperiences (array), educations (array).\n" +
            "Extract work experiences and education EXACTLY from the LinkedIn data. Never invent data.\n" +
            "Write a polished 4-5 sentence profileSummary for the target role.\n" +
            "isCurrent=true when endDate is Present/Current. displayOrder starts at 0.";

        String user1 =
            "From the LinkedIn data below, return this FLAT JSON (no nested wrappers):\n" +
            "{\"title\":\"" + name + "'s Resume\",\"templateName\":\"" + templateName + "\"," +
            "\"firstName\":\"" + firstName + "\",\"lastName\":\"" + lastName + "\"," +
            "\"jobTitle\":\"<extract from LinkedIn or use: " + jobTitle + ">\"," +
            "\"email\":\"" + email + "\",\"phone\":\"<extract or empty>\",\"city\":\"<extract or empty>\"," +
            "\"country\":\"<extract or empty>\",\"linkedinUrl\":\"" + linkedinUrl + "\"," +
            "\"githubUrl\":\"<extract if available or empty>\",\"portfolioUrl\":\"<extract if available or empty>\"," +
            "\"profileSummary\":\"<write 4-5 ATS-optimised sentences for " + jobTitle + ">\"," +
            "\"workExperiences\":[{\"companyName\":\"\",\"position\":\"\",\"startDate\":\"\"," +
            "\"endDate\":\"\",\"isCurrent\":false,\"location\":\"\",\"description\":\"\",\"displayOrder\":0}]," +
            "\"educations\":[{\"institution\":\"\",\"degree\":\"\",\"fieldOfStudy\":\"\"," +
            "\"startDate\":\"\",\"endDate\":\"\",\"grade\":\"\",\"description\":\"\",\"displayOrder\":0}]}\n\n" +
            "DATA:\n" + contextBlock;

        String system2 =
            "You are an expert ATS resume writer. Return ONLY valid JSON — no markdown, no comments, no code blocks.\n" +
            "IMPORTANT: Output a FLAT JSON object with exactly these top-level keys: skills, projects, certifications.\n" +
            "Do NOT wrap them in any parent object. proficiencyLevel must be: BEGINNER, INTERMEDIATE, ADVANCED, or EXPERT.\n" +
            "Extract ONLY from the LinkedIn data. Never invent data. displayOrder starts at 0.";

        String user2 =
            "From the LinkedIn data below, return this FLAT JSON:\n" +
            "{\"skills\":[{\"skillName\":\"\",\"proficiencyLevel\":\"INTERMEDIATE\",\"displayOrder\":0}]," +
            "\"projects\":[{\"projectName\":\"\",\"techStack\":\"\",\"projectUrl\":\"\"," +
            "\"startDate\":\"\",\"endDate\":\"\",\"description\":\"\",\"displayOrder\":0}]," +
            "\"certifications\":[{\"certName\":\"\",\"issuingOrganization\":\"\"," +
            "\"issueDate\":\"\",\"expiryDate\":\"\",\"credentialId\":\"\",\"credentialUrl\":\"\"," +
            "\"displayOrder\":0}]}\n\n" +
            "DATA:\n" + contextBlock;

        return mergeChunks(callChatApi(system1, user1, 8192), callChatApi(system2, user2, 8192));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Path B: AI-only generation (chunked)
    // ─────────────────────────────────────────────────────────────────────────
//    private ResumeRequestDTO generateAiOnlyChunked(
//            String name, String email, String linkedinUrl,
//            String jobTitle, String years, String skills,
//            String templateName, String firstName, String lastName) {
//
//        log.info("AI-only chunked generation");
//
//        String ctx = "Name: " + name + " | Email: " + email +
//                     " | Target: " + jobTitle + " | Exp: " + years + " yrs | Skills: " + skills;
//
//        String system1 =
//            "You are an expert ATS resume writer. Return ONLY valid JSON — no markdown, no comments, no code blocks.\n" +
//            "IMPORTANT: Output a FLAT JSON object only. Do NOT nest fields under 'personalInfo' or any other wrapper.\n" +
//            "The JSON must have these exact top-level keys: title, templateName, firstName, lastName, jobTitle,\n" +
//            "email, phone, city, country, linkedinUrl, githubUrl, portfolioUrl, profileSummary,\n" +
//            "workExperiences (array), educations (array).\n" +
//            "NEVER invent company names or educational institutions.\n" +
//            "Return workExperiences and educations with empty strings — user will fill them.\n" +
//            "Write a rich 4-5 sentence ATS-optimised profileSummary.";
//
//        String user1 =
//            "Generate resume header + summary for: " + ctx + "\n\n" +
//            "Return this FLAT JSON (no nested wrappers, no personalInfo key):\n" +
//            "{\"title\":\"" + name + "'s Resume\",\"templateName\":\"" + templateName + "\"," +
//            "\"firstName\":\"" + firstName + "\",\"lastName\":\"" + lastName + "\"," +
//            "\"jobTitle\":\"" + jobTitle + "\",\"email\":\"" + email + "\"," +
//            "\"phone\":\"\",\"city\":\"\",\"country\":\"India\"," +
//            "\"linkedinUrl\":\"" + linkedinUrl + "\",\"githubUrl\":\"\",\"portfolioUrl\":\"\"," +
//            "\"profileSummary\":\"<WRITE 4-5 ATS sentences for " + jobTitle + " with " + years + " yrs exp and skills [" + skills + "]>\"," +
//            "\"workExperiences\":[{\"companyName\":\"\",\"position\":\"" + jobTitle + "\"," +
//            "\"startDate\":\"\",\"endDate\":\"\",\"isCurrent\":false,\"location\":\"\"," +
//            "\"description\":\"\",\"displayOrder\":0}]," +
//            "\"educations\":[{\"institution\":\"\",\"degree\":\"\",\"fieldOfStudy\":\"\"," +
//            "\"startDate\":\"\",\"endDate\":\"\",\"grade\":\"\",\"description\":\"\",\"displayOrder\":0}]}";
//
//        String system2 =
//            "You are an expert ATS resume writer. Return ONLY valid JSON — no markdown, no comments, no code blocks.\n" +
//            "IMPORTANT: Output a FLAT JSON with exactly these top-level keys: skills, projects, certifications.\n" +
//            "Do NOT wrap them. proficiencyLevel must be: BEGINNER, INTERMEDIATE, ADVANCED, or EXPERT.\n" +
//            "Generate exactly 14 skills, exactly 3 detailed projects, exactly 2 certifications.";
//
//        String user2 =
//            "Generate skills, projects, certifications for: " + ctx + "\n\n" +
//            "Return this FLAT JSON:\n" +
//            "{\"skills\":[<14 skill objects for " + jobTitle + " including [" + skills + "]. " +
//            "Each:{\"skillName\":\"\",\"proficiencyLevel\":\"\",\"displayOrder\":N}>]," +
//            "\"projects\":[<3 realistic project objects for " + jobTitle + " with " + years + " yrs exp. " +
//            "Each:{\"projectName\":\"\",\"techStack\":\"\",\"projectUrl\":\"https://github.com/" + firstName.toLowerCase() + "/project\"," +
//            "\"startDate\":\"Mon YYYY\",\"endDate\":\"Mon YYYY\"," +
//            "\"description\":\"3-4 detailed sentences\",\"displayOrder\":N}>]," +
//            "\"certifications\":[<2 relevant cert objects for " + jobTitle + ". " +
//            "Each:{\"certName\":\"\",\"issuingOrganization\":\"\",\"issueDate\":\"Mon YYYY\"," +
//            "\"expiryDate\":\"\",\"credentialId\":\"\",\"credentialUrl\":\"\",\"displayOrder\":N}>]}";
//
//        return mergeChunks(callChatApi(system1, user1, 4096), callChatApi(system2, user2, 6000));
//    }
 // ─────────────────────────────────────────────────────────────────────────
 // REPLACE ONLY this ONE method in AIResumeService.java
 // Method: generateAiOnlyChunked(...)
 // Everything else stays untouched.
 // ─────────────────────────────────────────────────────────────────────────

     private ResumeRequestDTO generateAiOnlyChunked(
             String name, String email, String linkedinUrl,
             String jobTitle, String years, String skills,
             String templateName, String firstName, String lastName) {

         log.info("AI-only chunked generation");

         String ctx = "Name: " + name + " | Email: " + email +
                      " | Target: " + jobTitle + " | Exp: " + years + " yrs | Skills: " + skills;

         // ── CHUNK 1: Personal Info + Profile Summary + 2 Work Experiences + 2 Educations ──
         String system1 =
             "You are a senior ATS resume specialist and technical writer. Return ONLY valid JSON — no markdown, no comments, no code blocks.\n" +
             "IMPORTANT: Output a FLAT JSON object only. Do NOT nest fields under 'personalInfo' or any other wrapper.\n" +
             "The JSON must have these exact top-level keys: title, templateName, firstName, lastName, jobTitle,\n" +
             "email, phone, city, country, linkedinUrl, githubUrl, portfolioUrl, profileSummary,\n" +
             "workExperiences (array), educations (array).\n\n" +

             "=== STRICT RULES ===\n" +
             "1. profileSummary: Write 4-5 sentences PACKED with ATS keywords for the target role.\n" +
             "   Include: years of experience, specific tech stack from skills provided, domain expertise,\n" +
             "   soft skills (collaborative, agile, problem-solver), and a strong value statement.\n" +
             "   Example: 'Results-driven Full Stack Developer with 2+ years of hands-on experience building\n" +
             "   scalable web applications using React, Java, Spring Boot, and AWS. Proficient in designing\n" +
             "   RESTful APIs, microservices architecture, and CI/CD pipelines using Docker and Jenkins.\n" +
             "   Demonstrated ability to deliver high-performance solutions in agile environments with a focus\n" +
             "   on clean code, test-driven development, and system optimization. Adept at collaborating with\n" +
             "   cross-functional teams to translate business requirements into robust technical solutions.'\n\n" +

             "2. workExperiences: Generate EXACTLY 2 realistic work experience entries for the target role.\n" +
             "   - Use generic but realistic company names (e.g. 'TechCorp Solutions', 'Infosys Ltd', 'Wipro Technologies', 'Capgemini', 'HCL Technologies')\n" +
             "   - Entry 1: More recent role (e.g. 2023–Present or based on years exp), senior-level\n" +
             "   - Entry 2: Earlier role (e.g. 2021–2023), junior/mid-level or internship\n" +
             "   - description: MANDATORY '• ' bullet points format, 5-6 bullets each.\n" +
             "     Each bullet MUST follow this pattern:\n" +
             "     [Strong Action Verb] + [specific task] + using [Technology from skills] + [quantified outcome]\n" +
             "     Action verbs: Designed, Developed, Architected, Implemented, Engineered, Optimized,\n" +
             "     Automated, Deployed, Integrated, Migrated, Refactored, Led, Delivered, Reduced, Increased,\n" +
             "     Streamlined, Built, Configured, Established, Collaborated\n" +
             "     Examples:\n" +
             "     '• Developed and deployed RESTful APIs using Spring Boot and MySQL, reducing response time by 35% and supporting 10K+ daily active users\\n" +
             "     • Architected microservices-based backend infrastructure using Docker and AWS EC2, improving system scalability by 60%\\n" +
             "     • Implemented JWT-based authentication and role-based access control (RBAC), eliminating unauthorized access vulnerabilities\\n" +
             "     • Optimized SQL query performance using indexing and query rewriting, cutting database load time from 900ms to 150ms\\n" +
             "     • Automated CI/CD pipeline using Jenkins and GitHub Actions, reducing deployment time by 70%\\n" +
             "     • Collaborated with cross-functional agile teams of 8+ members to deliver 3 major product releases on schedule'\n\n" +

             "3. educations: Generate EXACTLY 2 education entries.\n" +
             "   - Entry 1: Bachelor's degree (B.Tech/B.E. in Computer Science or relevant field)\n" +
             "     Use a realistic Indian university name (e.g. 'JNTUH College of Engineering', 'Osmania University',\n" +
             "     'VIT University', 'SRM Institute of Science and Technology', 'Lovely Professional University')\n" +
             "     Grade: generate realistic CGPA like 7.8/10 or 8.2/10\n" +
             "     Dates: align with years of experience (if 2 yrs exp, graduation around 2022-2023)\n" +
             "   - Entry 2: Intermediate / Class XII (MPC or BiPC)\n" +
             "     Use a realistic school name (e.g. 'Sri Chaitanya Junior College', 'Narayana Junior College', 'Sri Gayatri Jr College')\n" +
             "     Grade: realistic percentage like 92% or 88%\n" +
             "     Dates: 2 years before graduation\n" +
             "   - displayOrder: 0 for B.Tech, 1 for XII";

         String user1 =
             "Generate a complete ATS-optimised resume for: " + ctx + "\n\n" +
             "Return this FLAT JSON (no nested wrappers, no personalInfo key):\n" +
             "{\n" +
             "  \"title\": \"" + name + "'s Resume\",\n" +
             "  \"templateName\": \"" + templateName + "\",\n" +
             "  \"firstName\": \"" + firstName + "\",\n" +
             "  \"lastName\": \"" + lastName + "\",\n" +
             "  \"jobTitle\": \"" + jobTitle + "\",\n" +
             "  \"email\": \"" + email + "\",\n" +
             "  \"phone\": \"\",\n" +
             "  \"city\": \"\",\n" +
             "  \"country\": \"India\",\n" +
             "  \"linkedinUrl\": \"" + linkedinUrl + "\",\n" +
             "  \"githubUrl\": \"\",\n" +
             "  \"portfolioUrl\": \"\",\n" +
             "  \"profileSummary\": \"<WRITE 4-5 ATS-keyword-rich sentences for " + jobTitle + " with " + years + " yrs exp using skills: " + skills + ">\",\n" +
             "  \"workExperiences\": [\n" +
             "    {\n" +
             "      \"companyName\": \"<realistic company name>\",\n" +
             "      \"position\": \"" + jobTitle + "\",\n" +
             "      \"startDate\": \"<Mon YYYY>\",\n" +
             "      \"endDate\": \"\",\n" +
             "      \"isCurrent\": true,\n" +
             "      \"location\": \"<City, India>\",\n" +
             "      \"description\": \"• <Action Verb> <task> using <Tech from: " + skills + "> <quantified result>\\n• <Action Verb> <task> using <Tech> <quantified result>\\n• <Action Verb> <task> using <Tech> <quantified result>\\n• <Action Verb> <task> using <Tech> <quantified result>\\n• <Action Verb> <task> using <Tech> <quantified result>\",\n" +
             "      \"displayOrder\": 0\n" +
             "    },\n" +
             "    {\n" +
             "      \"companyName\": \"<different realistic company name>\",\n" +
             "      \"position\": \"Junior " + jobTitle + "\",\n" +
             "      \"startDate\": \"<Mon YYYY>\",\n" +
             "      \"endDate\": \"<Mon YYYY>\",\n" +
             "      \"isCurrent\": false,\n" +
             "      \"location\": \"<City, India>\",\n" +
             "      \"description\": \"• <Action Verb> <task> using <Tech from: " + skills + "> <quantified result>\\n• <Action Verb> <task> using <Tech> <quantified result>\\n• <Action Verb> <task> using <Tech> <quantified result>\\n• <Action Verb> <task> using <Tech> <quantified result>\\n• <Action Verb> <task> using <Tech> <quantified result>\",\n" +
             "      \"displayOrder\": 1\n" +
             "    }\n" +
             "  ],\n" +
             "  \"educations\": [\n" +
             "    {\n" +
             "      \"institution\": \"<realistic Indian university>\",\n" +
             "      \"degree\": \"Bachelor of Technology\",\n" +
             "      \"fieldOfStudy\": \"Computer Science and Engineering\",\n" +
             "      \"startDate\": \"<YYYY>\",\n" +
             "      \"endDate\": \"<YYYY>\",\n" +
             "      \"grade\": \"<X.X / 10>\",\n" +
             "      \"description\": \"\",\n" +
             "      \"displayOrder\": 0\n" +
             "    },\n" +
             "    {\n" +
             "      \"institution\": \"<realistic Indian junior college>\",\n" +
             "      \"degree\": \"Intermediate (MPC)\",\n" +
             "      \"fieldOfStudy\": \"Mathematics, Physics, Chemistry\",\n" +
             "      \"startDate\": \"<YYYY>\",\n" +
             "      \"endDate\": \"<YYYY>\",\n" +
             "      \"grade\": \"<XX%>\",\n" +
             "      \"description\": \"\",\n" +
             "      \"displayOrder\": 1\n" +
             "    }\n" +
             "  ]\n" +
             "}";

         // ── CHUNK 2: Skills + Projects + 3 Certifications ──
         String system2 =
             "You are a senior ATS resume specialist. Return ONLY valid JSON — no markdown, no comments, no code blocks.\n" +
             "IMPORTANT: Output a FLAT JSON with exactly these top-level keys: skills, projects, certifications.\n" +
             "Do NOT wrap them in any parent object.\n\n" +

             "=== STRICT RULES ===\n" +
             "1. skills: Generate EXACTLY 14 skills relevant to the target role and provided skills.\n" +
             "   - Include ALL skills the user mentioned\n" +
             "   - Add complementary skills a " + jobTitle + " would realistically have\n" +
             "   - proficiencyLevel assignment:\n" +
             "     EXPERT: Core skills the user listed (primary technologies)\n" +
             "     ADVANCED: Supporting technologies\n" +
             "     INTERMEDIATE: Complementary tools\n" +
             "     BEGINNER: Emerging/additional tools\n" +
             "   - proficiencyLevel must be EXACTLY one of: BEGINNER, INTERMEDIATE, ADVANCED, EXPERT\n\n" +

             "2. projects: Generate EXACTLY 3 realistic, detailed projects for the target role.\n" +
             "   - Each project must use technologies from the provided skills\n" +
             "   - projectName: clear, professional name (e.g. 'E-Commerce Microservices Platform')\n" +
             "   - techStack: comma-separated list of technologies used\n" +
             "   - projectUrl: 'https://github.com/" + firstName.toLowerCase() + "/project-name'\n" +
             "   - startDate/endDate: realistic dates in 'Mon YYYY' format\n" +
             "   - description MANDATORY format:\n" +
             "     Line 1: One sentence describing what the project does and its purpose.\n" +
             "     Then 3-4 bullet points:\n" +
             "     '• [Action Verb] [what was built] using [specific Tech] [quantified outcome]\\n" +
             "     • [Action Verb] [what was implemented] using [specific Tech] [quantified outcome]\\n" +
             "     • [Action Verb] [what was optimized] using [specific Tech] [quantified outcome]\\n" +
             "     • [Action Verb] [what was deployed/integrated] using [specific Tech] [quantified outcome]'\n\n" +

             "3. certifications: Generate EXACTLY 3 industry-recognized certifications relevant to " + jobTitle + ".\n" +
             "   - Use DIFFERENT, realistic certifications (AWS, Oracle, Google, Microsoft, Meta, MongoDB, etc.)\n" +
             "   - credentialId: realistic format (e.g. 'AWS-DEV-123456', 'OCP-JAVA-789012')\n" +
             "   - issueDate: realistic recent dates in 'Mon YYYY' format\n" +
             "   - expiryDate: 3 years after issue date (or empty if no expiry)\n" +
             "   - credentialUrl: realistic verify URL";

         String user2 =
             "Generate skills, projects, and certifications for: " + ctx + "\n\n" +
             "Return this FLAT JSON:\n" +
             "{\n" +
             "  \"skills\": [\n" +
             "    {\"skillName\": \"<skill from: " + skills + ">\", \"proficiencyLevel\": \"EXPERT\", \"displayOrder\": 0},\n" +
             "    {\"skillName\": \"<skill>\", \"proficiencyLevel\": \"EXPERT\", \"displayOrder\": 1},\n" +
             "    {\"skillName\": \"<skill>\", \"proficiencyLevel\": \"ADVANCED\", \"displayOrder\": 2},\n" +
             "    {\"skillName\": \"<skill>\", \"proficiencyLevel\": \"ADVANCED\", \"displayOrder\": 3},\n" +
             "    {\"skillName\": \"<skill>\", \"proficiencyLevel\": \"ADVANCED\", \"displayOrder\": 4},\n" +
             "    {\"skillName\": \"<skill>\", \"proficiencyLevel\": \"INTERMEDIATE\", \"displayOrder\": 5},\n" +
             "    {\"skillName\": \"<skill>\", \"proficiencyLevel\": \"INTERMEDIATE\", \"displayOrder\": 6},\n" +
             "    {\"skillName\": \"<skill>\", \"proficiencyLevel\": \"INTERMEDIATE\", \"displayOrder\": 7},\n" +
             "    {\"skillName\": \"<skill>\", \"proficiencyLevel\": \"INTERMEDIATE\", \"displayOrder\": 8},\n" +
             "    {\"skillName\": \"<skill>\", \"proficiencyLevel\": \"INTERMEDIATE\", \"displayOrder\": 9},\n" +
             "    {\"skillName\": \"<skill>\", \"proficiencyLevel\": \"BEGINNER\", \"displayOrder\": 10},\n" +
             "    {\"skillName\": \"<skill>\", \"proficiencyLevel\": \"BEGINNER\", \"displayOrder\": 11},\n" +
             "    {\"skillName\": \"<skill>\", \"proficiencyLevel\": \"BEGINNER\", \"displayOrder\": 12},\n" +
             "    {\"skillName\": \"<skill>\", \"proficiencyLevel\": \"BEGINNER\", \"displayOrder\": 13}\n" +
             "  ],\n" +
             "  \"projects\": [\n" +
             "    {\n" +
             "      \"projectName\": \"<Project 1 name for " + jobTitle + ">\",\n" +
             "      \"techStack\": \"<comma-separated techs from: " + skills + ">\",\n" +
             "      \"projectUrl\": \"https://github.com/" + firstName.toLowerCase() + "/<project-1>\",\n" +
             "      \"startDate\": \"<Mon YYYY>\",\n" +
             "      \"endDate\": \"<Mon YYYY>\",\n" +
             "      \"description\": \"<One sentence: what the project does and its purpose>.\\n• <Action Verb> <what built> using <Tech> <quantified outcome>\\n• <Action Verb> <what implemented> using <Tech> <quantified outcome>\\n• <Action Verb> <what optimized> using <Tech> <quantified outcome>\\n• <Action Verb> <what deployed> using <Tech> <quantified outcome>\",\n" +
             "      \"displayOrder\": 0\n" +
             "    },\n" +
             "    {\n" +
             "      \"projectName\": \"<Project 2 name for " + jobTitle + ">\",\n" +
             "      \"techStack\": \"<comma-separated techs from: " + skills + ">\",\n" +
             "      \"projectUrl\": \"https://github.com/" + firstName.toLowerCase() + "/<project-2>\",\n" +
             "      \"startDate\": \"<Mon YYYY>\",\n" +
             "      \"endDate\": \"<Mon YYYY>\",\n" +
             "      \"description\": \"<One sentence: what the project does and its purpose>.\\n• <Action Verb> <what built> using <Tech> <quantified outcome>\\n• <Action Verb> <what implemented> using <Tech> <quantified outcome>\\n• <Action Verb> <what optimized> using <Tech> <quantified outcome>\\n• <Action Verb> <what deployed> using <Tech> <quantified outcome>\",\n" +
             "      \"displayOrder\": 1\n" +
             "    },\n" +
             "    {\n" +
             "      \"projectName\": \"<Project 3 name for " + jobTitle + ">\",\n" +
             "      \"techStack\": \"<comma-separated techs from: " + skills + ">\",\n" +
             "      \"projectUrl\": \"https://github.com/" + firstName.toLowerCase() + "/<project-3>\",\n" +
             "      \"startDate\": \"<Mon YYYY>\",\n" +
             "      \"endDate\": \"<Mon YYYY>\",\n" +
             "      \"description\": \"<One sentence: what the project does and its purpose>.\\n• <Action Verb> <what built> using <Tech> <quantified outcome>\\n• <Action Verb> <what implemented> using <Tech> <quantified outcome>\\n• <Action Verb> <what optimized> using <Tech> <quantified outcome>\\n• <Action Verb> <what deployed> using <Tech> <quantified outcome>\",\n" +
             "      \"displayOrder\": 2\n" +
             "    }\n" +
             "  ],\n" +
             "  \"certifications\": [\n" +
             "    {\n" +
             "      \"certName\": \"<Certification 1 relevant to " + jobTitle + ">\",\n" +
             "      \"issuingOrganization\": \"<Issuer e.g. Amazon Web Services / Oracle / Google / Microsoft>\",\n" +
             "      \"issueDate\": \"<Mon YYYY>\",\n" +
             "      \"expiryDate\": \"<Mon YYYY 3 years later>\",\n" +
             "      \"credentialId\": \"<realistic ID>\",\n" +
             "      \"credentialUrl\": \"<realistic verify URL>\",\n" +
             "      \"displayOrder\": 0\n" +
             "    },\n" +
             "    {\n" +
             "      \"certName\": \"<Certification 2 — DIFFERENT provider>\",\n" +
             "      \"issuingOrganization\": \"<Different Issuer>\",\n" +
             "      \"issueDate\": \"<Mon YYYY>\",\n" +
             "      \"expiryDate\": \"<Mon YYYY>\",\n" +
             "      \"credentialId\": \"<realistic ID>\",\n" +
             "      \"credentialUrl\": \"<realistic verify URL>\",\n" +
             "      \"displayOrder\": 1\n" +
             "    },\n" +
             "    {\n" +
             "      \"certName\": \"<Certification 3 — DIFFERENT provider>\",\n" +
             "      \"issuingOrganization\": \"<Different Issuer>\",\n" +
             "      \"issueDate\": \"<Mon YYYY>\",\n" +
             "      \"expiryDate\": \"<Mon YYYY or empty>\",\n" +
             "      \"credentialId\": \"<realistic ID>\",\n" +
             "      \"credentialUrl\": \"<realistic verify URL>\",\n" +
             "      \"displayOrder\": 2\n" +
             "    }\n" +
             "  ]\n" +
             "}";

         return mergeChunks(callChatApi(system1, user1, 6000), callChatApi(system2, user2, 6000));
     }
    // ─────────────────────────────────────────────────────────────────────────
    // Merge two JSON chunks → one ResumeRequestDTO
    // ─────────────────────────────────────────────────────────────────────────
    private ResumeRequestDTO mergeChunks(String raw1, String raw2) {
        try {
            JsonNode node1 = objectMapper.readTree(sanitizeJson(raw1));
            JsonNode node2 = objectMapper.readTree(sanitizeJson(raw2));

            // ── FIX: Flatten if AI wrapped fields in personalInfo/data/resume ──
            node1 = flattenIfWrapped(node1);
            node2 = flattenIfWrapped(node2);

            ObjectNode merged = objectMapper.createObjectNode();
            node1.fields().forEachRemaining(e -> merged.set(e.getKey(), e.getValue()));

            if (node2.has("skills"))         merged.set("skills",         node2.get("skills"));
            if (node2.has("projects"))       merged.set("projects",       node2.get("projects"));
            if (node2.has("certifications")) merged.set("certifications", node2.get("certifications"));

            // Ensure arrays always exist
            for (String f : new String[]{"workExperiences","educations","skills","projects","certifications"}) {
                if (!merged.has(f) || merged.get(f).isNull() || !merged.get(f).isArray()) {
                    merged.set(f, objectMapper.createArrayNode());
                }
            }

            // Ensure required string fields exist
            for (String f : new String[]{"title","templateName","firstName","lastName","jobTitle",
                                         "email","phone","city","country","linkedinUrl",
                                         "githubUrl","portfolioUrl","profileSummary"}) {
                if (!merged.has(f) || merged.get(f).isNull()) {
                    merged.put(f, "");
                }
            }

            log.info("Merged — work:{} edu:{} skills:{} projects:{} certs:{}",
                    merged.get("workExperiences").size(), merged.get("educations").size(),
                    merged.get("skills").size(), merged.get("projects").size(),
                    merged.get("certifications").size());

            return objectMapper.treeToValue(merged, ResumeRequestDTO.class);

        } catch (Exception e) {
            log.error("mergeChunks failed: {}", e.getMessage());
            throw new RuntimeException("AI returned an unexpected format. Please try again.");
        }
    }

    /**
     * FIX: If AI wraps everything in personalInfo / data / resume / candidate,
     * flatten it. Also handle case where chunk2 wraps skills in "data" etc.
     */
    private JsonNode flattenIfWrapped(JsonNode node) {
        // Known wrapper keys AI likes to use
        String[] wrappers = {"personalInfo", "data", "resume", "candidate", "result", "output", "response"};

        // Check if the node has NO expected flat keys but has a wrapper key
        boolean hasExpectedKey = node.has("firstName") || node.has("title") || node.has("email")
                || node.has("skills") || node.has("workExperiences") || node.has("educations")
                || node.has("projects") || node.has("certifications") || node.has("profileSummary");

        if (!hasExpectedKey) {
            for (String wrapper : wrappers) {
                if (node.has(wrapper) && node.get(wrapper).isObject()) {
                    log.warn("AI wrapped response in '{}' — flattening", wrapper);
                    JsonNode inner = node.get(wrapper);
                    // Merge the wrapper's contents with the rest of the outer node
                    ObjectNode merged = objectMapper.createObjectNode();
                    node.fields().forEachRemaining(e -> {
                        if (!e.getKey().equals(wrapper)) merged.set(e.getKey(), e.getValue());
                    });
                    inner.fields().forEachRemaining(e -> merged.set(e.getKey(), e.getValue()));
                    return merged;
                }
            }
        }

        // Also check if top-level has personalInfo AND expected keys mixed — just extract personalInfo fields
        if (node.has("personalInfo") && node.get("personalInfo").isObject()) {
            log.warn("AI mixed personalInfo wrapper — extracting its fields to top level");
            ObjectNode merged = objectMapper.createObjectNode();
            // Copy all non-personalInfo fields
            node.fields().forEachRemaining(e -> {
                if (!e.getKey().equals("personalInfo")) merged.set(e.getKey(), e.getValue());
            });
            // Flatten personalInfo fields to top level
            node.get("personalInfo").fields().forEachRemaining(e -> merged.set(e.getKey(), e.getValue()));
            return merged;
        }

        return node;
    }


     // ─────────────────────────────────────────────────────────────────────────
     // 2. Parse PDF  — ATS-OPTIMIZED VERSION
     // ─────────────────────────────────────────────────────────────────────────
     public ResumeRequestDTO parsePdf(AIResumeRequestDTO.ParsePdfRequest req) {

         String resumeText;
         try {
             String base64String = req.getBase64Pdf();
             if (base64String == null || base64String.isBlank()) {
                 throw new RuntimeException("No PDF data received.");
             }

             if (base64String.contains(",")) {
                 base64String = base64String.substring(base64String.indexOf(",") + 1);
             }

             byte[] pdfBytes = java.util.Base64.getDecoder().decode(base64String.trim());
             log.info("PDF bytes decoded: {} bytes", pdfBytes.length);

             if (pdfBytes.length < 100) {
                 throw new RuntimeException("PDF data is too small — likely corrupted or empty.");
             }

             try (org.apache.pdfbox.pdmodel.PDDocument doc =
                          org.apache.pdfbox.Loader.loadPDF(pdfBytes)) {
                 org.apache.pdfbox.text.PDFTextStripper stripper =
                         new org.apache.pdfbox.text.PDFTextStripper();
                 stripper.setSortByPosition(true);
                 resumeText = stripper.getText(doc);
             }

         } catch (Exception e) {
             log.error("PDF extraction failed: {}", e.getMessage());
             throw new RuntimeException("Could not read PDF. Please upload a text-based PDF (not a scanned image).");
         }

         log.info("PDF text extracted: {} chars", resumeText.length());

         if (resumeText.isBlank() || resumeText.length() < 50) {
             throw new RuntimeException(
                 "Could not extract text from this PDF. It may be a scanned image PDF. " +
                 "Please use a text-based PDF or type your resume manually.");
         }

         String system =
             "You are a senior ATS resume specialist and technical writer. Return ONLY valid JSON — no markdown, no code blocks, no comments.\n" +
             "IMPORTANT: Return a FLAT JSON object. Do NOT use 'personalInfo' or any nested wrapper.\n" +
             "The JSON must have these exact top-level keys:\n" +
             "title, templateName, firstName, lastName, jobTitle, email, phone, city, country,\n" +
             "linkedinUrl, githubUrl, portfolioUrl, profileSummary,\n" +
             "workExperiences (array), educations (array), skills (array), projects (array), certifications (array).\n\n" +

             "=== ATS WRITING RULES — MUST FOLLOW ===\n" +
             "1. profileSummary: Write 4-5 sentences packed with technical keywords matching their job title. " +
             "Include years of experience, core tech stack, domain expertise, and a value statement. " +
             "Example format: 'Results-driven [Title] with X+ years of hands-on experience in [Tech1], [Tech2], and [Tech3]. " +
             "Proficient in building [domain] solutions using [frameworks]. " +
             "Demonstrated expertise in [specific area] with measurable impact on [outcome].' " +
             "Use industry-standard keywords the recruiter's ATS will scan for.\n\n" +

             "2. workExperiences[].description: MANDATORY bullet point format. Each bullet MUST:\n" +
             "   - Start with a strong past-tense action verb (Designed, Developed, Implemented, Architected, Optimized, " +
             "Engineered, Delivered, Automated, Reduced, Increased, Led, Collaborated, Integrated, Deployed, Built, " +
             "Migrated, Refactored, Streamlined, Configured, Established)\n" +
             "   - Include the specific technology/tool used\n" +
             "   - Include a quantified result wherever possible (e.g., 'reduced load time by 40%', " +
             "'handled 10K+ daily requests', 'cut deployment time by 60%')\n" +
             "   - Follow this pattern: [Action Verb] [what was done] using [Tech] resulting in [quantified outcome]\n" +
             "   - Use '• ' prefix for each bullet\n" +
             "   - Write 4-6 bullets per experience\n" +
             "   - If the original PDF has plain sentences, REWRITE them as ATS bullet points\n" +
             "   - Keep all technical keywords from the original but restructure into bullet format\n\n" +

             "3. projects[].description: MANDATORY format:\n" +
             "   - Line 1: One sentence explaining what the project does and its purpose\n" +
             "   - Then 3-4 bullet points starting with action verbs\n" +
             "   - Each bullet must mention specific technologies and measurable outcomes\n" +
             "   - Example: '• Architected RESTful API using Spring Boot and MySQL serving 5K+ users\\n" +
             "• Implemented JWT-based authentication reducing unauthorized access by 100%\\n" +
             "• Optimized database queries reducing response time from 800ms to 120ms'\n\n" +

             "4. skills[]: Extract ALL technical skills mentioned anywhere in the resume. " +
             "Assign proficiencyLevel based on context clues (years used, role descriptions): " +
             "EXPERT (5+ yrs or primary skill), ADVANCED (3-4 yrs), INTERMEDIATE (1-2 yrs), BEGINNER (mentioned once/learning).\n\n" +

             "5. Never invent data. Extract EVERY job, education, project, skill, certification.\n" +
             "6. isCurrent=true when endDate says Present/Current/Now/ongoing.\n" +
             "7. If PDF has a summary/objective section, use it as the BASE but enrich it with ATS keywords.\n" +
             "8. proficiencyLevel must be exactly: BEGINNER, INTERMEDIATE, ADVANCED, or EXPERT.";

         String singlePrompt =
             "Parse this resume and return ATS-optimized FLAT JSON. " +
             "Rewrite all descriptions as strong action-verb bullet points with metrics. Return:\n" +
             "{\"title\":\"My Resume\",\"templateName\":\"classic\",\"firstName\":\"\",\"lastName\":\"\"," +
             "\"jobTitle\":\"\",\"email\":\"\",\"phone\":\"\",\"city\":\"\",\"country\":\"\"," +
             "\"linkedinUrl\":\"\",\"githubUrl\":\"\",\"portfolioUrl\":\"\",\"profileSummary\":\"\"," +
             "\"workExperiences\":[{\"companyName\":\"\",\"position\":\"\",\"startDate\":\"\",\"endDate\":\"\"," +
             "\"isCurrent\":false,\"location\":\"\",\"description\":\"• [Action Verb] ...\\n• [Action Verb] ...\",\"displayOrder\":0}]," +
             "\"educations\":[{\"institution\":\"\",\"degree\":\"\",\"fieldOfStudy\":\"\"," +
             "\"startDate\":\"\",\"endDate\":\"\",\"grade\":\"\",\"description\":\"\",\"displayOrder\":0}]," +
             "\"skills\":[{\"skillName\":\"\",\"proficiencyLevel\":\"INTERMEDIATE\",\"displayOrder\":0}]," +
             "\"projects\":[{\"projectName\":\"\",\"techStack\":\"\",\"projectUrl\":\"\"," +
             "\"startDate\":\"\",\"endDate\":\"\",\"description\":\"[What project does].\\n• [Action Verb] ...\\n• [Action Verb] ...\",\"displayOrder\":0}]," +
             "\"certifications\":[{\"certName\":\"\",\"issuingOrganization\":\"\",\"issueDate\":\"\"," +
             "\"expiryDate\":\"\",\"credentialId\":\"\",\"credentialUrl\":\"\",\"displayOrder\":0}]}\n\n" +
             "RESUME TEXT:\n===================\n" + resumeText + "\n===================";

         String raw = callChatApi(system, singlePrompt, 16000);

         try {
             JsonNode node = flattenIfWrapped(objectMapper.readTree(sanitizeJson(raw)));

             boolean hasWork  = node.has("workExperiences") && node.get("workExperiences").isArray()
                                && node.get("workExperiences").size() > 0
                                && !node.get("workExperiences").get(0).path("companyName").asText().isBlank();
             boolean hasSkill = node.has("skills") && node.get("skills").isArray()
                                && node.get("skills").size() > 0
                                && !node.get("skills").get(0).path("skillName").asText().isBlank();

             if (!hasWork && !hasSkill) {
                 log.warn("Single-pass PDF parse incomplete — running chunked parse");
                 return parsePdfChunked(resumeText);
             }

             for (String f : new String[]{"workExperiences","educations","skills","projects","certifications"}) {
                 if (!node.has(f) || !node.get(f).isArray()) {
                     ((ObjectNode) node).set(f, objectMapper.createArrayNode());
                 }
             }

             return objectMapper.treeToValue(node, ResumeRequestDTO.class);

         } catch (Exception e) {
             log.warn("Single-pass parse failed ({}), trying chunked", e.getMessage());
             return parsePdfChunked(resumeText);
         }
     }

     private ResumeRequestDTO parsePdfChunked(String resumeText) {

         String atsRules =
             "ATS WRITING RULES:\n" +
             "- workExperiences[].description: Use '• ' bullet points. Each bullet starts with a strong action verb " +
             "(Developed, Implemented, Architected, Optimized, Engineered, Delivered, Automated, Led, Integrated, Deployed). " +
             "Include specific tech and quantified result (e.g., 'reduced latency by 35%', 'served 10K+ requests/day'). " +
             "Write 4-6 bullets per job. Rewrite plain sentences into bullet format.\n" +
             "- profileSummary: 4-5 sentences with technical keywords, years of experience, core stack, domain expertise.\n" +
             "- Never invent data. Extract everything. isCurrent=true for Present jobs.";

         String system1 =
             "You are a senior ATS resume specialist. Return ONLY valid JSON — no markdown, no code blocks.\n" +
             "IMPORTANT: Output a FLAT JSON. Do NOT use 'personalInfo' or any nested wrapper.\n" +
             atsRules;

         String user1 =
             "Extract personal info, ATS-optimized summary, work experience (bullet points!), and education. Return FLAT JSON:\n" +
             "{\"title\":\"My Resume\",\"templateName\":\"classic\"," +
             "\"firstName\":\"\",\"lastName\":\"\",\"jobTitle\":\"\",\"email\":\"\",\"phone\":\"\"," +
             "\"city\":\"\",\"country\":\"\",\"linkedinUrl\":\"\",\"githubUrl\":\"\",\"portfolioUrl\":\"\"," +
             "\"profileSummary\":\"<4-5 ATS-keyword-rich sentences>\"," +
             "\"workExperiences\":[{\"companyName\":\"\",\"position\":\"\",\"startDate\":\"\",\"endDate\":\"\"," +
             "\"isCurrent\":false,\"location\":\"\",\"description\":\"• [Action Verb] [what] using [Tech], resulting in [outcome]\\n• ...\",\"displayOrder\":0}]," +
             "\"educations\":[{\"institution\":\"\",\"degree\":\"\",\"fieldOfStudy\":\"\"," +
             "\"startDate\":\"\",\"endDate\":\"\",\"grade\":\"\",\"description\":\"\",\"displayOrder\":0}]}\n" +
             "RESUME:\n" + resumeText;

         String atsRules2 =
             "ATS WRITING RULES:\n" +
             "- projects[].description: Start with one sentence on what the project does. " +
             "Then 3-4 '• ' bullet points. Each bullet: action verb + specific tech + measurable outcome. " +
             "Example: '• Developed REST API using Spring Boot and MySQL handling 5K+ daily requests\\n" +
             "• Implemented JWT authentication reducing security vulnerabilities by 100%'\n" +
             "- skills[]: Extract ALL technical skills. Assign proficiencyLevel based on context.\n" +
             "- proficiencyLevel must be exactly: BEGINNER, INTERMEDIATE, ADVANCED, or EXPERT.";

         String system2 =
             "You are a senior ATS resume specialist. Return ONLY valid JSON — no markdown, no code blocks.\n" +
             "IMPORTANT: Output a FLAT JSON with exactly these keys: skills, projects, certifications.\n" +
             atsRules2;

         String user2 =
             "Extract skills, ATS-optimized projects (bullet points!), and certifications. Return FLAT JSON:\n" +
             "{\"skills\":[{\"skillName\":\"\",\"proficiencyLevel\":\"INTERMEDIATE\",\"displayOrder\":0}]," +
             "\"projects\":[{\"projectName\":\"\",\"techStack\":\"\",\"projectUrl\":\"\"," +
             "\"startDate\":\"\",\"endDate\":\"\",\"description\":\"[What project does and its purpose].\\n• [Action Verb] [what] using [Tech] [outcome]\\n• ...\",\"displayOrder\":0}]," +
             "\"certifications\":[{\"certName\":\"\",\"issuingOrganization\":\"\",\"issueDate\":\"\"," +
             "\"expiryDate\":\"\",\"credentialId\":\"\",\"credentialUrl\":\"\",\"displayOrder\":0}]}\n" +
             "RESUME:\n" + resumeText;

         return mergeChunks(callChatApi(system1, user1, 8192), callChatApi(system2, user2, 8192));
     }
    // ─────────────────────────────────────────────────────────────────────────
    // 3. ATS Tips
    // ─────────────────────────────────────────────────────────────────────────
    public AIResumeResponseDTO.AtsTipsResponse getAtsTips(AIResumeRequestDTO.AtsTipsRequest req) {

        String system = "You are a certified ATS optimisation expert. Return ONLY valid JSON — no markdown, no code blocks.";
        String user =
            "Analyse this resume profile and give 6-8 specific, actionable ATS improvement tips.\n\n" +
            "Resume profile:\n" +
            "  Job Title       : " + req.getJobTitle() + "\n" +
            "  Profile Summary : " + req.getProfileSummary() + "\n" +
            "  Skills          : " + req.getSkillNames() + "\n" +
            "  Work Experiences: " + req.getWorkExperienceCount() + " entries\n" +
            "  Educations      : " + req.getEducationCount() + " entries\n" +
            "  Projects        : " + req.getProjectCount() + " entries\n" +
            "  Certifications  : " + req.getCertificationCount() + " entries\n" +
            "  Has LinkedIn    : " + req.isHasLinkedin() + "\n" +
            "  Has GitHub      : " + req.isHasGithub() + "\n\n" +
            "Score 0-100. Return JSON:\n" +
            "{\"score\":<0-100>,\"tips\":[{\"type\":\"error|warning|success\",\"title\":\"short title\",\"detail\":\"detailed advice\"}]}";

        return parseAs(callChatApi(system, user, 2000), AIResumeResponseDTO.AtsTipsResponse.class);
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
        body.put("messages",    List.of(
            Map.of("role", "system", "content", systemPrompt),
            Map.of("role", "user",   "content", userPrompt)
        ));

        try {
            ResponseEntity<String> response =
                    restTemplate.postForEntity(CHAT_URL, new HttpEntity<>(body, headers), String.class);
            JsonNode root = objectMapper.readTree(response.getBody());

            JsonNode usage = root.path("usage");
            log.info("OpenAI tokens — prompt:{} completion:{} total:{}",
                    usage.path("prompt_tokens").asInt(),
                    usage.path("completion_tokens").asInt(),
                    usage.path("total_tokens").asInt());

            String finishReason = root.path("choices").get(0).path("finish_reason").asText();
            if ("length".equals(finishReason)) {
                log.warn("Response TRUNCATED by max_tokens ({})!", maxTokens);
            }

            return sanitizeJson(root.path("choices").get(0).path("message").path("content").asText());

        } catch (Exception e) {
            log.error("OpenAI Chat API failed: {}", e.getMessage(), e);
            throw new RuntimeException("AI service unavailable. Please try again later.");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────
    private String sanitizeJson(String raw) {
        if (raw == null) return "{}";
        // Strip markdown code fences
        raw = raw.replaceAll("(?s)```json\\s*", "").replaceAll("(?s)```\\s*", "").trim();
        // Find the first { and last } to extract just the JSON object
        int start = raw.indexOf('{');
        int end   = raw.lastIndexOf('}');
        if (start >= 0 && end > start) {
            raw = raw.substring(start, end + 1);
        }
        return raw;
    }
    
    public String rewriteSection(String section, String input) {
        Map<String, String> prompts = new HashMap<>();
        prompts.put("profileSummary",
            "Rewrite the following into a polished, ATS-optimised professional profile summary " +
            "(4-5 sentences). Use strong action words and include relevant technical keywords. " +
            "Return ONLY the summary text, no JSON, no quotes:\n\n" + input);
        prompts.put("workDescription",
            "Rewrite the following work experience into 5 ATS-optimised bullet points. " +
            "Each bullet starts with a strong past-tense action verb and includes specific " +
            "technology and quantified outcome. Use • prefix. Return ONLY the bullets:\n\n" + input);
        prompts.put("projectDescription",
            "Rewrite the following project into an ATS-optimised description: one sentence " +
            "explaining what the project does, then 3-4 bullet points (• prefix) each starting " +
            "with an action verb and including technology + outcome. Return ONLY the description:\n\n" + input);

        String prompt = prompts.getOrDefault(section, prompts.get("profileSummary"));
        return callChatApi(
            "You are an expert ATS resume writer. Return ONLY plain text, no JSON, no markdown.",
            prompt, 1000);
    }
    

    private <T> T parseAs(String json, Class<T> type) {
        try {
            return objectMapper.readValue(sanitizeJson(json), type);
        } catch (Exception e) {
            log.error("JSON parse failed for {}: {}", type.getSimpleName(), e.getMessage());
            throw new RuntimeException("AI returned an unexpected format. Please try again.");
        }
    }

    private String nvl(String v)               { return v != null ? v.trim() : ""; }
    private String nvl(String v, String defVal){ return (v != null && !v.isBlank()) ? v.trim() : defVal; }
}