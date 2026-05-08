//
//
//
//
//
//
//package com.lms.user.service;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.lms.user.dto.ResumeRequestDTO;
//import com.lms.user.dto.LinkedInScrapeRequestDTO;
//import com.lms.user.dto.EducationRequestDTO;
//import com.lms.user.dto.CertificationRequestDTO;
//import com.lms.user.dto.ProjectRequestDTO;
//import com.lms.user.dto.WorkExperienceRequestDTO;
//import com.lms.user.dto.ResumeSkillRequestDTO;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Service
//public class LinkedInScraperService {
//
//    private static final Logger log = LoggerFactory.getLogger(LinkedInScraperService.class);
//
//    @Value("${rapidapi.key}")
//    private String rapidApiKey;
//
//    @Value("${rapidapi.host:linkedin-data-api.p.rapidapi.com}")
//    private String rapidApiHost;
//
//    @Value("${openai.api.key:}")
//    private String openAiApiKey;
//
//    private static final String RAPIDAPI_BASE_URL =
//            "https://linkedin-data-api.p.rapidapi.com/get-profile-data-by-url";
//
//    private static final String OPENAI_CHAT_URL = "https://api.openai.com/v1/chat/completions";
//    private static final String MODEL           = "gpt-4o";
//
//    private final RestTemplate restTemplate = new RestTemplate();
//    private final ObjectMapper objectMapper  = new ObjectMapper();
//
//    // ─────────────────────────────────────────────────────────────────────────
//    // MAIN METHOD
//    // ─────────────────────────────────────────────────────────────────────────
//    public ResumeRequestDTO buildResumeFromLinkedIn(LinkedInScrapeRequestDTO request) {
//
//        log.info("Starting LinkedIn scrape for: {}", request.getLinkedInUrl());
//
//        JsonNode profileJson = fetchFromRapidApi(request.getLinkedInUrl());
//
//        log.info("RapidAPI root keys: {}", iteratorToString(profileJson.fieldNames()));
//
//        // Always unwrap "data" key first — linkedin-data-api always wraps profile there
//        JsonNode dataNode = unwrapDataNode(profileJson);
//
//        // Log all keys inside data node so we can diagnose field name mismatches
//        log.info("Data node keys: {}", iteratorToString(dataNode.fieldNames()));
//        if (dataNode.size() > 0) {
//            String preview = dataNode.toString();
//            log.info("Data node preview (first 800 chars): {}",
//                    preview.substring(0, Math.min(800, preview.length())));
//        } else {
//            log.warn("Data node is EMPTY — profile may be private or API plan limitation");
//        }
//
//        ProfileData profile = mapToProfileData(dataNode);
//
//        log.info("Profile mapped — name:{} {} | positions:{} | edu:{} | skills:{}",
//                profile.firstName, profile.lastName,
//                profile.positions.size(), profile.educations.size(), profile.skills.size());
//
//        return buildResumeDTO(profile, request);
//    }
//
//    /**
//     * FIX: linkedin-data-api.p.rapidapi.com response structure:
//     * { "success": true, "message": "...", "data": { ...profile fields... } }
//     * We must always unwrap "data" first, then try other wrappers.
//     */
//    private JsonNode unwrapDataNode(JsonNode root) {
//        // ALWAYS unwrap "data" key first — this API always wraps in data
//        if (root.has("data") && root.get("data").isObject()) {
//            log.info("Unwrapping response from 'data' key (size={})", root.get("data").size());
//            JsonNode data = root.get("data");
//            // If data itself is empty, log the full root for debugging
//            if (data.size() == 0) {
//                log.warn("'data' node is empty! Full root keys: {}", iteratorToString(root.fieldNames()));
//                log.warn("This usually means the LinkedIn profile is PRIVATE or the RapidAPI plan doesn't include this profile");
//            }
//            return data;
//        }
//
//        // Try other common wrapper keys
//        String[] wrappers = {"profile", "person", "result", "user", "member", "response"};
//        for (String key : wrappers) {
//            if (root.has(key) && root.get(key).isObject() && root.get(key).size() > 0) {
//                log.info("Unwrapping response from '{}' key", key);
//                return root.get(key);
//            }
//        }
//
//        // No wrapper found — return root directly
//        log.info("No wrapper key found — using root node directly. Keys: {}", iteratorToString(root.fieldNames()));
//        return root;
//    }
//
//    // ─────────────────────────────────────────────────────────────────────────
//    // Step 1: Call RapidAPI
//    // ─────────────────────────────────────────────────────────────────────────
//    private JsonNode fetchFromRapidApi(String linkedInUrl) {
//
//        String cleanUrl = normalizeUrl(linkedInUrl);
//        log.info("Calling RapidAPI for: {}", cleanUrl);
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("X-RapidAPI-Key",  rapidApiKey);
//        headers.set("X-RapidAPI-Host", rapidApiHost);
//        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
//
//        String apiUrl = RAPIDAPI_BASE_URL + "?url=" + encodeUrl(cleanUrl);
//        HttpEntity<Void> entity = new HttpEntity<>(headers);
//
//        try {
//            ResponseEntity<String> response = restTemplate.exchange(
//                    apiUrl, HttpMethod.GET, entity, String.class);
//
//            if (response.getStatusCode() != HttpStatus.OK) {
//                throw new RuntimeException("RapidAPI returned status: " + response.getStatusCode());
//            }
//
//            log.debug("RapidAPI raw body: {}", response.getBody());
//            JsonNode root = objectMapper.readTree(response.getBody());
//            log.info("RapidAPI response received successfully");
//            return root;
//
//        } catch (Exception e) {
//            log.error("RapidAPI call failed: {}", e.getMessage());
//            throw new RuntimeException(
//                    "Failed to fetch LinkedIn profile. " +
//                    "Make sure the profile is PUBLIC. Error: " + e.getMessage());
//        }
//    }
//
//    // ─────────────────────────────────────────────────────────────────────────
//    // Step 2: Map JSON → ProfileData
//    // Tries many field-name variants used by different RapidAPI providers
//    // ─────────────────────────────────────────────────────────────────────────
//    private ProfileData mapToProfileData(JsonNode json) {
//
//        ProfileData data = new ProfileData();
//
//        // ── Personal ──────────────────────────────────────────────────────────
//        data.firstName = getText(json, "firstName", "first_name", "given_name");
//        data.lastName  = getText(json, "lastName",  "last_name",  "family_name");
//        data.headline  = getText(json, "headline", "title", "sub_title", "jobTitle");
//        data.email     = getText(json, "email", "emailAddress");
//        data.country   = getText(json, "country", "countryCode", "country_full_name",
//                                  "geoCountry", "geo_country", "location");
//
//        // full_name fallback
//        if (data.firstName.isEmpty()) {
//            String full = getText(json, "fullName", "full_name", "name",
//                                  "displayName", "display_name").trim();
//            if (!full.isEmpty()) {
//                if (full.contains(" ")) {
//                    int sp = full.indexOf(" ");
//                    data.firstName = full.substring(0, sp);
//                    data.lastName  = full.substring(sp + 1);
//                } else {
//                    data.firstName = full;
//                }
//            }
//        }
//
//        log.info("Personal: firstName='{}' lastName='{}' headline='{}'",
//                data.firstName, data.lastName, data.headline);
//
//        // ── Work Experience ───────────────────────────────────────────────────
//        // Try all known array key names for work experience
//        JsonNode experiences = getArray(json,
//                "positions", "experiences", "workExperiences",
//                "work_experience", "work", "jobs", "positionGroups");
//
//        // positionGroups is a nested structure used by some APIs
//        if (experiences == null && json.has("positionGroups")) {
//            experiences = flattenPositionGroups(json.get("positionGroups"));
//        }
//
//        if (experiences != null) {
//            log.info("Found {} work experience entries", experiences.size());
//            for (JsonNode exp : experiences) {
//                Position pos = new Position();
//                pos.title       = getText(exp, "title", "role", "jobTitle", "job_title", "positionTitle");
//                pos.companyName = getText(exp, "companyName", "company", "company_name",
//                                          "organizationName", "employer");
//                pos.location    = getText(exp, "location", "locationName", "geoLocation");
//                pos.description = getText(exp, "description", "summary", "responsibilities");
//                pos.isCurrent   = exp.path("isCurrent").asBoolean(false)
//                                || exp.path("is_current").asBoolean(false)
//                                || "Present".equalsIgnoreCase(getText(exp, "endDate", "end_date"));
//                pos.startDate   = parseDateNode(exp, "startDate", "starts_at", "start_date");
//                pos.endDate     = pos.isCurrent ? "" : parseDateNode(exp, "endDate", "ends_at", "end_date");
//
//                if (!pos.title.isEmpty() || !pos.companyName.isEmpty()) {
//                    data.positions.add(pos);
//                    log.debug("  Added position: {} at {}", pos.title, pos.companyName);
//                }
//            }
//        } else {
//            log.warn("No work experience array found in response");
//        }
//
//        // ── Education ─────────────────────────────────────────────────────────
//        JsonNode educations = getArray(json, "educations", "education",
//                                       "educationHistory", "schools");
//        if (educations != null) {
//            log.info("Found {} education entries", educations.size());
//            for (JsonNode edu : educations) {
//                Education e = new Education();
//                e.school      = getText(edu, "schoolName", "school", "school_name",
//                                        "institution", "university", "college");
//                e.degree      = getText(edu, "degreeName", "degree", "degree_name", "qualification");
//                e.field       = getText(edu, "fieldOfStudy", "field_of_study", "field",
//                                        "major", "discipline");
//                e.grade       = getText(edu, "grade", "gpa", "cgpa");
//                e.description = getText(edu, "activities", "description", "notes");
//                e.startYear   = extractYear(edu, "startDate", "starts_at", "start_date");
//                e.endYear     = extractYear(edu, "endDate",   "ends_at",   "end_date");
//                if (!e.school.isEmpty()) {
//                    data.educations.add(e);
//                    log.debug("  Added education: {} at {}", e.degree, e.school);
//                }
//            }
//        } else {
//            log.warn("No education array found in response");
//        }
//
//        // ── Skills ────────────────────────────────────────────────────────────
//        JsonNode skillsNode = getArray(json, "skills", "skillEndorsements", "topSkills");
//        if (skillsNode != null) {
//            log.info("Found {} skill entries", skillsNode.size());
//            for (JsonNode skill : skillsNode) {
//                String name;
//                if (skill.isTextual()) {
//                    name = skill.asText("").trim();
//                } else {
//                    name = getText(skill, "name", "skill_name", "skillName",
//                                   "title", "text", "displayName");
//                }
//                if (!name.isEmpty()) data.skills.add(name);
//            }
//        } else {
//            log.warn("No skills array found in response");
//        }
//
//        // ── Certifications ────────────────────────────────────────────────────
//        JsonNode certsNode = getArray(json, "certifications", "licenses",
//                                      "licensesAndCertifications", "licenses_and_certifications",
//                                      "honors", "achievements");
//        if (certsNode != null) {
//            for (JsonNode cert : certsNode) {
//                Certification c = new Certification();
//                c.name          = getText(cert, "name", "certificationName", "title");
//                c.authority     = getText(cert, "authority", "organization", "issuer",
//                                          "issuingOrganization", "issuing_organization");
//                c.licenseNumber = getText(cert, "licenseNumber", "license_number",
//                                          "credentialId", "credential_id");
//                c.url           = getText(cert, "url", "credentialUrl", "credential_url");
//                c.issueDate     = parseDateNode(cert, "startDate", "starts_at",
//                                               "issueDate", "issued_on", "issue_date");
//                if (!c.name.isEmpty()) data.certifications.add(c);
//            }
//        }
//
//        // ── Projects ──────────────────────────────────────────────────────────
//        JsonNode projectsNode = getArray(json, "projects");
//        if (projectsNode != null) {
//            for (JsonNode proj : projectsNode) {
//                Project p = new Project();
//                p.title       = getText(proj, "title", "name", "projectName");
//                p.description = getText(proj, "description", "summary");
//                p.url         = getText(proj, "url", "projectUrl");
//                p.startDate   = parseDateNode(proj, "startDate", "starts_at", "start_date");
//                p.endDate     = parseDateNode(proj, "endDate",   "ends_at",   "end_date");
//                if (!p.title.isEmpty()) data.projects.add(p);
//            }
//        }
//
//        return data;
//    }
//
//    /**
//     * FIX: Some RapidAPI LinkedIn endpoints return positionGroups instead of positions.
//     * positionGroups is a list of {company: {name:...}, profilePositions: [{title, dates,...}]}
//     * This method flattens it into a standard positions array.
//     */
//    private JsonNode flattenPositionGroups(JsonNode groups) {
//        com.fasterxml.jackson.databind.node.ArrayNode flat =
//                objectMapper.createArrayNode();
//        if (groups == null || !groups.isArray()) return flat;
//        for (JsonNode group : groups) {
//            String companyName = getText(group.path("company"), "name", "universalName");
//            String location    = getText(group, "location", "geoLocation");
//            JsonNode positions = group.path("profilePositions");
//            if (positions.isArray()) {
//                for (JsonNode pos : positions) {
//                    com.fasterxml.jackson.databind.node.ObjectNode merged =
//                            objectMapper.createObjectNode();
//                    pos.fields().forEachRemaining(e -> merged.set(e.getKey(), e.getValue()));
//                    if (!companyName.isEmpty()) merged.put("companyName", companyName);
//                    if (!location.isEmpty())    merged.put("location",    location);
//                    flat.add(merged);
//                }
//            }
//        }
//        log.info("Flattened positionGroups → {} positions", flat.size());
//        return flat;
//    }
//
//    // ─────────────────────────────────────────────────────────────────────────
//    // Step 3: Build ResumeRequestDTO
//    // ─────────────────────────────────────────────────────────────────────────
//    private ResumeRequestDTO buildResumeDTO(ProfileData profile, LinkedInScrapeRequestDTO request) {
//
//        String targetRole  = (request.getJobTitle() != null && !request.getJobTitle().isBlank())
//                             ? request.getJobTitle().trim() : profile.headline;
//        String tmpl        = (request.getTemplateName() != null && !request.getTemplateName().isBlank())
//                             ? request.getTemplateName() : "classic";
//        String skillsExtra = request.getExtraSkills() != null ? request.getExtraSkills().trim() : "";
//
//        ResumeRequestDTO dto = new ResumeRequestDTO();
//        dto.setTitle(profile.firstName + " " + profile.lastName + "'s Resume");
//        dto.setTemplateName(tmpl);
//        dto.setFirstName(profile.firstName);
//        dto.setLastName(profile.lastName);
//        dto.setJobTitle(targetRole);
//        dto.setEmail(profile.email);
//        dto.setPhone("");
//        dto.setCity("");
//        dto.setCountry(profile.country);
//        dto.setLinkedinUrl(request.getLinkedInUrl());
//        dto.setGithubUrl("");
//        dto.setPortfolioUrl("");
//        dto.setProfileSummary(generateProfileSummary(profile, targetRole));
//
//        // Work Experiences
//        List<WorkExperienceRequestDTO> workList = new ArrayList<>();
//        for (int i = 0; i < profile.positions.size(); i++) {
//            Position pos = profile.positions.get(i);
//            WorkExperienceRequestDTO w = new WorkExperienceRequestDTO();
//            w.setCompanyName(pos.companyName);
//            w.setPosition(pos.title);
//            w.setStartDate(pos.startDate);
//            w.setEndDate(pos.isCurrent ? "" : pos.endDate);
//            w.setIsCurrent(pos.isCurrent);
//            w.setLocation(pos.location);
//            w.setDescription(enhanceDescription(pos.description, pos.title, pos.companyName));
//            w.setDisplayOrder(i);
//            workList.add(w);
//        }
//        dto.setWorkExperiences(workList);
//
//        // Educations
//        List<EducationRequestDTO> eduList = new ArrayList<>();
//        for (int i = 0; i < profile.educations.size(); i++) {
//            Education edu = profile.educations.get(i);
//            EducationRequestDTO e = new EducationRequestDTO();
//            e.setInstitution(edu.school);
//            e.setDegree(edu.degree);
//            e.setFieldOfStudy(edu.field);
//            e.setStartDate(edu.startYear);
//            e.setEndDate(edu.endYear);
//            e.setGrade(edu.grade);
//            e.setDescription(edu.description);
//            e.setDisplayOrder(i);
//            eduList.add(e);
//        }
//        dto.setEducations(eduList);
//
//        // Skills — from LinkedIn + extra
//        List<ResumeSkillRequestDTO> skillList = new ArrayList<>();
//        Set<String> added = new HashSet<>();
//        for (int i = 0; i < profile.skills.size(); i++) {
//            String name = profile.skills.get(i).trim();
//            if (name.isEmpty() || !added.add(name.toLowerCase())) continue;
//            ResumeSkillRequestDTO s = new ResumeSkillRequestDTO();
//            s.setSkillName(name);
//            s.setProficiencyLevel(inferSkillLevel(name, profile));
//            s.setDisplayOrder(i);
//            skillList.add(s);
//        }
//        if (!skillsExtra.isEmpty()) {
//            int order = skillList.size();
//            for (String sk : skillsExtra.split(",")) {
//                String name = sk.trim();
//                if (name.isEmpty() || !added.add(name.toLowerCase())) continue;
//                ResumeSkillRequestDTO s = new ResumeSkillRequestDTO();
//                s.setSkillName(name);
//                s.setProficiencyLevel("INTERMEDIATE");
//                s.setDisplayOrder(order++);
//                skillList.add(s);
//            }
//        }
//        dto.setSkills(skillList);
//
//        // Projects
//        List<ProjectRequestDTO> projList = new ArrayList<>();
//        for (int i = 0; i < profile.projects.size(); i++) {
//            Project proj = profile.projects.get(i);
//            ProjectRequestDTO p = new ProjectRequestDTO();
//            p.setProjectName(proj.title);
//            p.setTechStack("");
//            p.setProjectUrl(proj.url);
//            p.setStartDate(proj.startDate);
//            p.setEndDate(proj.endDate);
//            p.setDescription(proj.description);
//            p.setDisplayOrder(i);
//            projList.add(p);
//        }
//        dto.setProjects(projList);
//
//        // Certifications
//        List<CertificationRequestDTO> certList = new ArrayList<>();
//        for (int i = 0; i < profile.certifications.size(); i++) {
//            Certification cert = profile.certifications.get(i);
//            CertificationRequestDTO c = new CertificationRequestDTO();
//            c.setCertName(cert.name);
//            c.setIssuingOrganization(cert.authority);
//            c.setIssueDate(cert.issueDate);
//            c.setExpiryDate("");
//            c.setCredentialId(cert.licenseNumber);
//            c.setCredentialUrl(cert.url);
//            c.setDisplayOrder(i);
//            certList.add(c);
//        }
//        dto.setCertifications(certList);
//
//        log.info("Resume built — work:{} edu:{} skills:{} projects:{} certs:{}",
//                workList.size(), eduList.size(), skillList.size(), projList.size(), certList.size());
//        return dto;
//    }
//
//    // ─────────────────────────────────────────────────────────────────────────
//    // AI: Generate profile summary
//    // ─────────────────────────────────────────────────────────────────────────
//    private String generateProfileSummary(ProfileData profile, String targetRole) {
//        String recentRole = profile.positions.isEmpty() ? "" :
//                profile.positions.get(0).title + " at " + profile.positions.get(0).companyName;
//        String skills = profile.skills.stream().limit(8).collect(Collectors.joining(", "));
//        String edu    = profile.educations.isEmpty() ? "" :
//                profile.educations.get(0).degree + " from " + profile.educations.get(0).school;
//
//        String system =
//            "You are an expert ATS resume writer. Write ONLY a professional summary paragraph.\n" +
//            "Use ONLY the facts provided — never invent companies, achievements, or experiences.\n" +
//            "4-5 sentences, ATS-keyword-rich, compelling. Return plain text only, no markdown.";
//        String user =
//            "Write a resume summary targeting: " + (targetRole.isEmpty() ? "Software Developer" : targetRole) + "\n\n" +
//            "REAL FACTS ONLY:\n" +
//            "  Person: " + profile.firstName + " " + profile.lastName + "\n" +
//            "  Recent Role: " + (recentRole.isEmpty() ? "Not provided" : recentRole) + "\n" +
//            "  Skills: " + (skills.isEmpty() ? "Not provided" : skills) + "\n" +
//            "  Education: " + (edu.isEmpty() ? "Not provided" : edu) + "\n" +
//            "  Total Positions: " + profile.positions.size();
//        try {
//            return callOpenAI(system, user, 500).trim();
//        } catch (Exception e) {
//            log.warn("Summary generation failed: {}", e.getMessage());
//            return "";
//        }
//    }
//
//    private String enhanceDescription(String raw, String title, String company) {
//        if (raw == null || raw.isBlank()) return "";
//        if (raw.contains("•") || raw.contains("\n-") || raw.contains("\n•")) return raw.trim();
//        String system =
//            "Format the given job description into clean bullet points.\n" +
//            "ONLY use information already present in the text. Never add or invent anything.\n" +
//            "Format: '• Point\\n• Point'. Return ONLY bullet points, no extra text.";
//        String user = "Format into bullets for " + title + " at " + company + ":\n" + raw;
//        try {
//            return callOpenAI(system, user, 400).trim();
//        } catch (Exception e) {
//            return raw.trim();
//        }
//    }
//
//    private String callOpenAI(String system, String user, int maxTokens) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.setBearerAuth(openAiApiKey);
//
//        Map<String, Object> body = new HashMap<>();
//        body.put("model",       MODEL);
//        body.put("max_tokens",  maxTokens);
//        body.put("temperature", 0.1);
//        body.put("messages",    List.of(
//                Map.of("role", "system", "content", system),
//                Map.of("role", "user",   "content", user)
//        ));
//
//        try {
//            ResponseEntity<String> resp = restTemplate.postForEntity(
//                    OPENAI_CHAT_URL, new HttpEntity<>(body, headers), String.class);
//            JsonNode root = objectMapper.readTree(resp.getBody());
//            return root.path("choices").get(0).path("message").path("content").asText();
//        } catch (Exception e) {
//            throw new RuntimeException("OpenAI unavailable: " + e.getMessage());
//        }
//    }
//
//    // ─────────────────────────────────────────────────────────────────────────
//    // Helpers
//    // ─────────────────────────────────────────────────────────────────────────
//    private String inferSkillLevel(String skill, ProfileData profile) {
//        String lower = skill.toLowerCase();
//        long count = profile.positions.stream()
//                .filter(p -> p.description != null && p.description.toLowerCase().contains(lower))
//                .count();
//        if (count >= 3) return "EXPERT";
//        if (count >= 2) return "ADVANCED";
//        if (count >= 1) return "INTERMEDIATE";
//        return "INTERMEDIATE";
//    }
//
//    /** Extract year from a date field — handles both object {year:2022} and string "Jan 2022" */
//    private String extractYear(JsonNode parent, String... fieldNames) {
//        for (String field : fieldNames) {
//            JsonNode n = parent.path(field);
//            if (!n.isMissingNode() && !n.isNull()) {
//                if (n.isObject()) {
//                    int y = n.path("year").asInt(0);
//                    return y > 0 ? String.valueOf(y) : "";
//                }
//                if (n.isTextual()) {
//                    String txt = n.asText("").trim();
//                    // Extract 4-digit year from string like "Jan 2022" or "2022"
//                    java.util.regex.Matcher m =
//                            java.util.regex.Pattern.compile("\\b(19|20)\\d{2}\\b").matcher(txt);
//                    if (m.find()) return m.group();
//                }
//                if (n.isNumber()) {
//                    int y = n.asInt(0);
//                    return y > 0 ? String.valueOf(y) : "";
//                }
//            }
//        }
//        return "";
//    }
//
//    private String parseDateNode(JsonNode parent, String... fieldNames) {
//        JsonNode dateNode = getFirstPresent(parent, fieldNames);
//        if (dateNode == null || dateNode.isNull()) return "";
//        if (dateNode.isTextual()) return dateNode.asText("").trim();
//        if (dateNode.isNumber()) {
//            // epoch milliseconds
//            long ms = dateNode.asLong(0);
//            if (ms > 0) {
//                java.time.LocalDate d = java.time.Instant.ofEpochMilli(ms)
//                        .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
//                return d.getMonth().getDisplayName(java.time.format.TextStyle.SHORT,
//                        java.util.Locale.ENGLISH) + " " + d.getYear();
//            }
//            return "";
//        }
//        // Object like {month:8, year:2025} or {day:1, month:8, year:2025}
//        int month = dateNode.path("month").asInt(0);
//        int year  = dateNode.path("year").asInt(0);
//        if (year == 0) return "";
//        String[] months = {"","Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
//        String m = (month > 0 && month <= 12) ? months[month] + " " : "";
//        return (m + year).trim();
//    }
//
//    private String getText(JsonNode node, String... fields) {
//        for (String field : fields) {
//            if (node.has(field) && !node.get(field).isNull() && !node.get(field).isArray()) {
//                String val = node.get(field).asText("").trim();
//                if (!val.isEmpty() && !val.equals("null") && !val.equals("N/A")) return val;
//            }
//        }
//        return "";
//    }
//
//    private JsonNode getArray(JsonNode node, String... fields) {
//        for (String field : fields) {
//            if (node.has(field) && node.get(field).isArray() && node.get(field).size() > 0)
//                return node.get(field);
//        }
//        return null;
//    }
//
//    private JsonNode getFirstPresent(JsonNode node, String... fields) {
//        for (String field : fields) {
//            JsonNode n = node.path(field);
//            if (!n.isMissingNode() && !n.isNull()) return n;
//        }
//        return null;
//    }
//
//    private String normalizeUrl(String url) {
//        if (url == null || url.isBlank()) throw new RuntimeException("LinkedIn URL is required");
//        url = url.trim();
//        if (!url.startsWith("http")) url = "https://" + url;
//        if (url.endsWith("/")) url = url.substring(0, url.length() - 1);
//        return url;
//    }
//
//    private String encodeUrl(String url) {
//        try { return java.net.URLEncoder.encode(url, "UTF-8"); }
//        catch (Exception e) { return url; }
//    }
//
//    private String iteratorToString(Iterator<String> it) {
//        StringBuilder sb = new StringBuilder("[");
//        while (it.hasNext()) { sb.append(it.next()); if (it.hasNext()) sb.append(", "); }
//        sb.append("]");
//        return sb.toString();
//    }
//
//    // ─────────────────────────────────────────────────────────────────────────
//    // Inner Data Models
//    // ─────────────────────────────────────────────────────────────────────────
//    public static class ProfileData {
//        public String firstName = "", lastName = "", headline = "", email = "", country = "";
//        public List<Position>      positions      = new ArrayList<>();
//        public List<Education>     educations     = new ArrayList<>();
//        public List<String>        skills         = new ArrayList<>();
//        public List<Certification> certifications = new ArrayList<>();
//        public List<Project>       projects       = new ArrayList<>();
//    }
//
//    public static class Position {
//        public String title = "", companyName = "", startDate = "", endDate = "",
//                      location = "", description = "";
//        public boolean isCurrent;
//        public int     displayOrder;
//    }
//
//    public static class Education {
//        public String school = "", degree = "", field = "", startYear = "",
//                      endYear = "", grade = "", description = "";
//        public int    displayOrder;
//    }
//
//    public static class Certification {
//        public String name = "", authority = "", issueDate = "", licenseNumber = "", url = "";
//    }
//
//    public static class Project {
//        public String title = "", description = "", url = "", startDate = "", endDate = "";
//    }
//}

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

@Service
public class LinkedInScraperService {

    private static final Logger log = LoggerFactory.getLogger(LinkedInScraperService.class);

    @Value("${openai.api.key:}")
    private String openAiApiKey;

    private static final String OPENAI_CHAT_URL  = "https://api.openai.com/v1/chat/completions";
    private static final String RESPONSES_URL    = "https://api.openai.com/v1/responses";
    private static final String MODEL            = "gpt-4o";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper  = new ObjectMapper();

    // ─────────────────────────────────────────────────────────────────────────
    // MAIN ENTRY POINT
    // ─────────────────────────────────────────────────────────────────────────
//    public ResumeRequestDTO buildResumeFromLinkedIn(LinkedInScrapeRequestDTO request) {
//
//        log.info("Starting LinkedIn import for: {}", request.getLinkedInUrl());
//
//        // STEP 1: Fetch real LinkedIn profile data via OpenAI web_search_preview
//        String profileText = null;
//        try {
//            profileText = fetchLinkedInViaOpenAI(request.getLinkedInUrl());
//            log.info("OpenAI web search extracted {} chars", profileText != null ? profileText.length() : 0);
//        } catch (Exception e) {
//            log.error("OpenAI web search failed: {}", e.getMessage());
//        }
//
//        // STEP 2: If OpenAI web search failed or returned too little, try a targeted search
//        if (profileText == null || profileText.length() < 300) {
//            log.warn("First fetch insufficient — trying targeted name+role search");
//            try {
//                profileText = fetchLinkedInViaTargetedSearch(request.getLinkedInUrl());
//                log.info("Targeted search extracted {} chars", profileText != null ? profileText.length() : 0);
//            } catch (Exception e) {
//                log.error("Targeted search also failed: {}", e.getMessage());
//            }
//        }
//
//        // STEP 3: Last resort — build minimal context so AI can at least structure properly
//        if (profileText == null || profileText.length() < 100) {
//            log.warn("All fetch methods failed — using minimal URL context only");
//            profileText = buildMinimalContext(request.getLinkedInUrl(), request.getJobTitle());
//        }
//
//        log.info("Profile context ready ({} chars) — building resume...", profileText.length());
//        return buildResumeFromText(profileText, request);
//    }
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
//    private String buildMinimalContext(String linkedInUrl, String jobTitle) {
//        String username    = extractUsername(linkedInUrl);
//        String displayName = username.replace("-", " ").replaceAll("\\d+", "").trim();
//        return
//            "LinkedIn Profile URL: " + linkedInUrl + "\n" +
//            "Name hint from URL: " + displayName + "\n" +
//            "Target Job Title: " + (jobTitle != null && !jobTitle.isBlank() ? jobTitle : "Software Developer") + "\n" +
//            "Note: LinkedIn profile could not be accessed directly. " +
//            "Please build the best possible resume structure. " +
//            "The user will review and edit all details after import.";
//    }
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
}