package com.lms.course.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.course.dto.SchoolProgrammeDto.BoardDto;
import com.lms.course.dto.SchoolProgrammeDto.ClassDto;
import com.lms.course.dto.SchoolProgrammeDto.SubjectDto;
import com.lms.course.model.SchoolBoard;
import com.lms.course.model.SchoolClass;
import com.lms.course.model.SchoolSubject;
import com.lms.course.repository.SchoolBoardRepository;
import com.lms.course.repository.SchoolClassRepository;
import com.lms.course.repository.SchoolSubjectRepository;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SchoolProgrammeService {

    private final SchoolBoardRepository boardRepository;
    private final SchoolClassRepository classRepository;
    private final SchoolSubjectRepository subjectRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${openai.api.key:}")
    private String openAiApiKey;

    @Value("${openai.model:gpt-4o-mini}")
    private String openAiModel;

    public SchoolProgrammeService(
            SchoolBoardRepository boardRepository,
            SchoolClassRepository classRepository,
            SchoolSubjectRepository subjectRepository) {
        this.boardRepository = boardRepository;
        this.classRepository = classRepository;
        this.subjectRepository = subjectRepository;
        this.objectMapper = new ObjectMapper();
        this.restTemplate = new RestTemplate();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUBLIC METHODS
    // ─────────────────────────────────────────────────────────────────────────

    public List<BoardDto> getAllBoards() {
        return boardRepository.findByActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(this::toBoardDto)
                .collect(Collectors.toList());
    }

    public List<ClassDto> getClassesByBoard(Long boardId) {
        findActiveBoard(boardId);
        return classRepository.findByBoardIdAndActiveTrueOrderByClassNumberAsc(boardId)
                .stream()
                .map(this::toClassDto)
                .collect(Collectors.toList());
    }

    public List<SubjectDto> getSubjectsByClass(Long classId, String stream) {
        findActiveClass(classId);
        List<SchoolSubject> subjects;
        if (stream != null && !stream.isBlank()) {
            // return subjects matching the specific stream + subjects with stream="all"
            List<SchoolSubject> streamSubjects = subjectRepository
                    .findBySchoolClassIdAndStreamAndActiveTrueOrderByDisplayOrderAsc(classId, stream);
            List<SchoolSubject> allStreamSubjects = subjectRepository
                    .findBySchoolClassIdAndStreamAndActiveTrueOrderByDisplayOrderAsc(classId, "all");
            subjects = new ArrayList<>();
            subjects.addAll(allStreamSubjects);
            subjects.addAll(streamSubjects);
            subjects.sort((a, b) -> {
                int ao = a.getDisplayOrder() == null ? 999 : a.getDisplayOrder();
                int bo = b.getDisplayOrder() == null ? 999 : b.getDisplayOrder();
                return Integer.compare(ao, bo);
            });
        } else {
            subjects = subjectRepository
                    .findBySchoolClassIdAndActiveTrueOrderByDisplayOrderAsc(classId);
        }
        return subjects.stream().map(this::toSubjectDto).collect(Collectors.toList());
    }

    public SubjectDto getSubjectById(Long subjectId) {
        return toSubjectDto(findActiveSubject(subjectId));
    }

    public String getSyllabusJson(Long subjectId) {
        SchoolSubject subject = findActiveSubject(subjectId);
        String syllabus = subject.getSyllabusJson();
        if (syllabus == null || syllabus.isBlank()) return "[]";
        return syllabus;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ADMIN BOARD METHODS
    // ─────────────────────────────────────────────────────────────────────────

    public BoardDto createBoard(BoardDto dto) {
        SchoolBoard entity = new SchoolBoard();
        copyBoardDtoToEntity(dto, entity);
        entity.setActive(true);
        return toBoardDto(boardRepository.save(entity));
    }

    public BoardDto updateBoard(Long boardId, BoardDto dto) {
        SchoolBoard existing = findActiveBoard(boardId);
        copyBoardDtoToEntity(dto, existing);
        return toBoardDto(boardRepository.save(existing));
    }

    public void softDeleteBoard(Long boardId) {
        SchoolBoard board = findActiveBoard(boardId);
        board.setActive(false);
        boardRepository.save(board);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ADMIN CLASS METHODS
    // ─────────────────────────────────────────────────────────────────────────

    public ClassDto createClass(ClassDto dto) {
        SchoolBoard board = findActiveBoard(dto.getBoardId());
        SchoolClass entity = new SchoolClass();
        copyClassDtoToEntity(dto, entity, board);
        entity.setActive(true);
        return toClassDto(classRepository.save(entity));
    }

    public ClassDto updateClass(Long classId, ClassDto dto) {
        SchoolClass existing = findActiveClass(classId);
        SchoolBoard board = findActiveBoard(dto.getBoardId());
        copyClassDtoToEntity(dto, existing, board);
        return toClassDto(classRepository.save(existing));
    }

    public void softDeleteClass(Long classId) {
        SchoolClass schoolClass = findActiveClass(classId);
        schoolClass.setActive(false);
        classRepository.save(schoolClass);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ADMIN SUBJECT METHODS
    // ─────────────────────────────────────────────────────────────────────────

    public SubjectDto createSubject(SubjectDto dto) {
        SchoolClass schoolClass = findActiveClass(dto.getSchoolClassId());
        SchoolSubject entity = new SchoolSubject();
        copySubjectDtoToEntity(dto, entity, schoolClass);
        entity.setActive(true);
        return toSubjectDto(subjectRepository.save(entity));
    }

    public SubjectDto updateSubject(Long subjectId, SubjectDto dto) {
        SchoolSubject existing = findActiveSubject(subjectId);
        SchoolClass schoolClass = findActiveClass(dto.getSchoolClassId());
        copySubjectDtoToEntity(dto, existing, schoolClass);
        return toSubjectDto(subjectRepository.save(existing));
    }

    public void softDeleteSubject(Long subjectId) {
        SchoolSubject subject = findActiveSubject(subjectId);
        subject.setActive(false);
        subjectRepository.save(subject);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ADMIN SYLLABUS METHODS — MANUAL UPDATE
    // ─────────────────────────────────────────────────────────────────────────

    public SubjectDto updateSyllabusManually(Long subjectId, String chaptersJson, String syllabusJson) {
        SchoolSubject subject = findActiveSubject(subjectId);

        if (chaptersJson != null && !chaptersJson.isBlank()) {
            validateJson(chaptersJson, "chaptersJson");
            subject.setChaptersJson(chaptersJson);
        }

        if (syllabusJson != null && !syllabusJson.isBlank()) {
            validateJson(syllabusJson, "syllabusJson");
            subject.setSyllabusJson(syllabusJson);
        }

        return toSubjectDto(subjectRepository.save(subject));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // AI SYLLABUS METHODS
    // ─────────────────────────────────────────────────────────────────────────

    public SubjectDto generateSyllabusWithAi(Long subjectId, String rawText) {
        validateOpenAiKey();

        if (rawText == null || rawText.isBlank()) {
            throw new RuntimeException("rawText cannot be empty.");
        }

        SchoolSubject subject = findActiveSubject(subjectId);
        Map<String, String> result = callOpenAiForSyllabus(rawText);

        subject.setChaptersJson(result.get("chaptersJson"));
        subject.setSyllabusJson(result.get("syllabusJson"));

        return toSubjectDto(subjectRepository.save(subject));
    }

    public SubjectDto uploadFileAndGenerateSyllabus(Long subjectId, MultipartFile file) {
        validateOpenAiKey();

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Uploaded file is empty.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new RuntimeException("File name is null.");
        }

        String lowerName = originalFilename.toLowerCase();

        // validate supported file types
        if (!lowerName.endsWith(".txt")
                && !lowerName.endsWith(".pdf")
                && !lowerName.endsWith(".doc")
                && !lowerName.endsWith(".docx")) {
            throw new RuntimeException(
                    "Unsupported file type. Only .txt, .pdf, .doc and .docx files are allowed."
            );
        }

        String rawText;

        try {
            if (lowerName.endsWith(".txt")) {
                // plain text — read directly as UTF-8, no Tika needed
                rawText = new String(file.getBytes(), StandardCharsets.UTF_8);

            } else {
                // PDF, DOC, DOCX — use Apache Tika AutoDetectParser
                // tika-parsers-standard-package handles all three automatically
                AutoDetectParser parser = new AutoDetectParser();

                // -1 means no character limit on extracted text
                BodyContentHandler handler = new BodyContentHandler(-1);

                Metadata metadata = new Metadata();
                metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, originalFilename);

                try (InputStream inputStream = file.getInputStream()) {
                    parser.parse(inputStream, handler, metadata);
                }

                rawText = handler.toString();
            }

        } catch (RuntimeException e) {
            // re-throw our own RuntimeExceptions as-is
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to extract text from file '" + originalFilename + "': " + e.getMessage()
            );
        }

        if (rawText == null || rawText.isBlank()) {
            throw new RuntimeException(
                    "Could not extract any text from the uploaded file. "
                    + "Please check the file is not empty or corrupted."
            );
        }

        return generateSyllabusWithAi(subjectId, rawText);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private SchoolBoard findActiveBoard(Long id) {
        return boardRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Board not found with id: " + id));
    }

    private SchoolClass findActiveClass(Long id) {
        return classRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Class not found with id: " + id));
    }

    private SchoolSubject findActiveSubject(Long id) {
        return subjectRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Subject not found with id: " + id));
    }

    private void validateOpenAiKey() {
        if (openAiApiKey == null || openAiApiKey.isBlank()) {
            throw new RuntimeException(
                    "OpenAI API key is not configured. Please set openai.api.key in application.yml."
            );
        }
    }

    private void validateJson(String json, String fieldName) {
        try {
            objectMapper.readTree(json);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Invalid JSON provided for field: " + fieldName + " — " + e.getMessage()
            );
        }
    }

    private Map<String, String> callOpenAiForSyllabus(String rawText) {
        String prompt = "Convert the following raw syllabus text into STRICT JSON only.\n"
                + "No markdown. No explanation. No code blocks.\n"
                + "Return ONLY a valid JSON object in this exact structure:\n"
                + "{\n"
                + "  \"chaptersJson\": [\"Chapter 1 name\", \"Chapter 2 name\", \"Chapter 3 name\"],\n"
                + "  \"syllabusJson\": [\n"
                + "    {\n"
                + "      \"unit\": \"Unit 1 – Unit Title Here\",\n"
                + "      \"topics\": [\"Topic 1\", \"Topic 2\", \"Topic 3\"]\n"
                + "    }\n"
                + "  ]\n"
                + "}\n\n"
                + "Raw syllabus text:\n"
                + rawText;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", openAiModel);
        requestBody.put("messages", List.of(userMessage));
        requestBody.put("temperature", 0.2);

        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(
                    "https://api.openai.com/v1/chat/completions",
                    HttpMethod.POST,
                    httpEntity,
                    String.class
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to call OpenAI API: " + e.getMessage());
        }

        String responseBody = response.getBody();
        if (responseBody == null || responseBody.isBlank()) {
            throw new RuntimeException("Empty response received from OpenAI.");
        }

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String content = root
                    .path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();

            // validate the full response is valid JSON
            JsonNode parsed = objectMapper.readTree(content);

            // extract chaptersJson array
            JsonNode chaptersNode = parsed.path("chaptersJson");
            if (chaptersNode.isMissingNode()) {
                throw new RuntimeException("OpenAI response missing 'chaptersJson' field.");
            }
            String chaptersJsonStr = objectMapper.writeValueAsString(chaptersNode);

            // extract syllabusJson array
            JsonNode syllabusNode = parsed.path("syllabusJson");
            if (syllabusNode.isMissingNode()) {
                throw new RuntimeException("OpenAI response missing 'syllabusJson' field.");
            }
            String syllabusJsonStr = objectMapper.writeValueAsString(syllabusNode);

            Map<String, String> result = new HashMap<>();
            result.put("chaptersJson", chaptersJsonStr);
            result.put("syllabusJson", syllabusJsonStr);
            return result;

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse OpenAI response: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // toDto METHODS
    // ─────────────────────────────────────────────────────────────────────────

    private BoardDto toBoardDto(SchoolBoard entity) {
        BoardDto dto = new BoardDto();
        dto.setId(entity.getId());
        dto.setBoardKey(entity.getBoardKey());
        dto.setName(entity.getName());
        dto.setFullName(entity.getFullName());
        dto.setColor(entity.getColor());
        dto.setAccent(entity.getAccent());
        dto.setTagline(entity.getTagline());
        dto.setAbbr(entity.getAbbr());
        dto.setLogoUrl(entity.getLogoUrl());
        dto.setDisplayOrder(entity.getDisplayOrder());
        dto.setActive(entity.getActive());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    private ClassDto toClassDto(SchoolClass entity) {
        ClassDto dto = new ClassDto();
        dto.setId(entity.getId());
        dto.setBoardId(entity.getBoard().getId());
        dto.setBoardName(entity.getBoard().getName());
        dto.setClassNumber(entity.getClassNumber());
        dto.setLabel(entity.getLabel());
        dto.setTagline(entity.getTagline());
        dto.setDescription(entity.getDescription());
        dto.setHighlightsJson(entity.getHighlightsJson());
        dto.setStreamsJson(entity.getStreamsJson());
        dto.setDisplayOrder(entity.getDisplayOrder());
        dto.setActive(entity.getActive());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    private SubjectDto toSubjectDto(SchoolSubject entity) {
        SubjectDto dto = new SubjectDto();
        dto.setId(entity.getId());
        dto.setSchoolClassId(entity.getSchoolClass().getId());
        dto.setSchoolClassLabel(entity.getSchoolClass().getLabel());
        dto.setBoardName(entity.getSchoolClass().getBoard().getName());
        dto.setName(entity.getName());
        dto.setStream(entity.getStream());
        dto.setIconKey(entity.getIconKey());
        dto.setChaptersJson(entity.getChaptersJson());
        dto.setSyllabusJson(entity.getSyllabusJson());
        dto.setDisplayOrder(entity.getDisplayOrder());
        dto.setActive(entity.getActive());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // copyDtoToEntity METHODS
    // ─────────────────────────────────────────────────────────────────────────

    private void copyBoardDtoToEntity(BoardDto dto, SchoolBoard entity) {
        entity.setBoardKey(dto.getBoardKey());
        entity.setName(dto.getName());
        entity.setFullName(dto.getFullName());
        entity.setColor(dto.getColor());
        entity.setAccent(dto.getAccent());
        entity.setTagline(dto.getTagline());
        entity.setAbbr(dto.getAbbr());
        entity.setLogoUrl(dto.getLogoUrl());
        entity.setDisplayOrder(dto.getDisplayOrder());
    }

    private void copyClassDtoToEntity(ClassDto dto, SchoolClass entity, SchoolBoard board) {
        entity.setBoard(board);
        entity.setClassNumber(dto.getClassNumber());
        entity.setLabel(dto.getLabel());
        entity.setTagline(dto.getTagline());
        entity.setDescription(dto.getDescription());
        entity.setHighlightsJson(dto.getHighlightsJson());
        entity.setStreamsJson(dto.getStreamsJson());
        entity.setDisplayOrder(dto.getDisplayOrder());
    }

    private void copySubjectDtoToEntity(SubjectDto dto, SchoolSubject entity, SchoolClass schoolClass) {
        entity.setSchoolClass(schoolClass);
        entity.setName(dto.getName());
        entity.setStream(dto.getStream());
        entity.setIconKey(dto.getIconKey());
        entity.setChaptersJson(dto.getChaptersJson());
        entity.setSyllabusJson(dto.getSyllabusJson());
        entity.setDisplayOrder(dto.getDisplayOrder());
    }
}