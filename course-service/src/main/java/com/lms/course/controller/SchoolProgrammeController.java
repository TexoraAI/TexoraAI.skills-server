package com.lms.course.controller;

import com.lms.course.dto.SchoolProgrammeDto.BoardDto;
import com.lms.course.dto.SchoolProgrammeDto.ClassDto;
import com.lms.course.dto.SchoolProgrammeDto.SubjectDto;
import com.lms.course.service.SchoolProgrammeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/homepage/schoolprogramme")
public class SchoolProgrammeController {

    private final SchoolProgrammeService service;

    public SchoolProgrammeController(SchoolProgrammeService service) {
        this.service = service;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUBLIC ENDPOINTS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * GET /api/v1/homepage/schoolprogramme/boards
     * Returns all active boards ordered by displayOrder.
     * Used by frontend to show board cards (CBSE, Bihar Board, ICSE, UP Board).
     */
    @GetMapping("/boards")
    public ResponseEntity<List<BoardDto>> getAllBoards() {
        return ResponseEntity.ok(service.getAllBoards());
    }

    /**
     * GET /api/v1/homepage/schoolprogramme/boards/{boardId}/classes
     * Returns all active classes for a board ordered by classNumber (9,10,11,12).
     */
    @GetMapping("/boards/{boardId}/classes")
    public ResponseEntity<List<ClassDto>> getClassesByBoard(@PathVariable Long boardId) {
        return ResponseEntity.ok(service.getClassesByBoard(boardId));
    }

    /**
     * GET /api/v1/homepage/schoolprogramme/boards/{boardId}/classes/{classId}/subjects
     * Returns all active subjects for a class.
     * Optional query param ?stream=Science — filters by stream.
     * Subjects with stream="all" are always included.
     */
    @GetMapping("/boards/{boardId}/classes/{classId}/subjects")
    public ResponseEntity<List<SubjectDto>> getSubjectsByClass(
            @PathVariable Long boardId,
            @PathVariable Long classId,
            @RequestParam(required = false) String stream) {
        return ResponseEntity.ok(service.getSubjectsByClass(classId, stream));
    }

    /**
     * GET /api/v1/homepage/schoolprogramme/subjects/{subjectId}
     * Returns single subject with full chaptersJson and syllabusJson.
     */
    @GetMapping("/subjects/{subjectId}")
    public ResponseEntity<SubjectDto> getSubjectById(@PathVariable Long subjectId) {
        return ResponseEntity.ok(service.getSubjectById(subjectId));
    }

    /**
     * GET /api/v1/homepage/schoolprogramme/subjects/{subjectId}/syllabus
     * Returns raw syllabusJson string only.
     */
    @GetMapping("/subjects/{subjectId}/syllabus")
    public ResponseEntity<String> getSyllabus(@PathVariable Long subjectId) {
        return ResponseEntity.ok(service.getSyllabusJson(subjectId));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ADMIN BOARD ENDPOINTS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * POST /api/v1/homepage/schoolprogramme/admin/boards
     * Super admin creates a new board.
     * Body: BoardDto with boardKey, name, fullName, color, accent, tagline, abbr, logoUrl, displayOrder
     */
    @PostMapping("/admin/boards")
    public ResponseEntity<BoardDto> createBoard(@RequestBody BoardDto dto) {
        return ResponseEntity.ok(service.createBoard(dto));
    }

    /**
     * PUT /api/v1/homepage/schoolprogramme/admin/boards/{boardId}
     * Super admin updates an existing board.
     */
    @PutMapping("/admin/boards/{boardId}")
    public ResponseEntity<BoardDto> updateBoard(
            @PathVariable Long boardId,
            @RequestBody BoardDto dto) {
        return ResponseEntity.ok(service.updateBoard(boardId, dto));
    }

    /**
     * DELETE /api/v1/homepage/schoolprogramme/admin/boards/{boardId}
     * Soft delete — sets active=false only. Never hard deletes.
     */
    @DeleteMapping("/admin/boards/{boardId}")
    public ResponseEntity<String> deleteBoard(@PathVariable Long boardId) {
        service.softDeleteBoard(boardId);
        return ResponseEntity.ok("Board with id " + boardId + " has been deleted successfully.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ADMIN CLASS ENDPOINTS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * POST /api/v1/homepage/schoolprogramme/admin/classes
     * Super admin creates a new class under a board.
     * Body: ClassDto with boardId, classNumber, label, tagline, description,
     *       highlightsJson, streamsJson (null for Class 9 and 10), displayOrder
     */
    @PostMapping("/admin/classes")
    public ResponseEntity<ClassDto> createClass(@RequestBody ClassDto dto) {
        return ResponseEntity.ok(service.createClass(dto));
    }

    /**
     * PUT /api/v1/homepage/schoolprogramme/admin/classes/{classId}
     * Super admin updates an existing class.
     */
    @PutMapping("/admin/classes/{classId}")
    public ResponseEntity<ClassDto> updateClass(
            @PathVariable Long classId,
            @RequestBody ClassDto dto) {
        return ResponseEntity.ok(service.updateClass(classId, dto));
    }

    /**
     * DELETE /api/v1/homepage/schoolprogramme/admin/classes/{classId}
     * Soft delete — sets active=false only.
     */
    @DeleteMapping("/admin/classes/{classId}")
    public ResponseEntity<String> deleteClass(@PathVariable Long classId) {
        service.softDeleteClass(classId);
        return ResponseEntity.ok("Class with id " + classId + " has been deleted successfully.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ADMIN SUBJECT ENDPOINTS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * POST /api/v1/homepage/schoolprogramme/admin/subjects
     * Super admin creates a new subject under a class.
     * Body: SubjectDto with schoolClassId, name, stream, iconKey,
     *       chaptersJson, syllabusJson, displayOrder
     */
    @PostMapping("/admin/subjects")
    public ResponseEntity<SubjectDto> createSubject(@RequestBody SubjectDto dto) {
        return ResponseEntity.ok(service.createSubject(dto));
    }

    /**
     * PUT /api/v1/homepage/schoolprogramme/admin/subjects/{subjectId}
     * Super admin updates an existing subject.
     */
    @PutMapping("/admin/subjects/{subjectId}")
    public ResponseEntity<SubjectDto> updateSubject(
            @PathVariable Long subjectId,
            @RequestBody SubjectDto dto) {
        return ResponseEntity.ok(service.updateSubject(subjectId, dto));
    }

    /**
     * DELETE /api/v1/homepage/schoolprogramme/admin/subjects/{subjectId}
     * Soft delete — sets active=false only.
     */
    @DeleteMapping("/admin/subjects/{subjectId}")
    public ResponseEntity<String> deleteSubject(@PathVariable Long subjectId) {
        service.softDeleteSubject(subjectId);
        return ResponseEntity.ok("Subject with id " + subjectId + " has been deleted successfully.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ADMIN SYLLABUS ENDPOINTS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * PUT /api/v1/homepage/schoolprogramme/admin/subjects/{subjectId}/syllabus
     * Super admin manually updates chaptersJson and/or syllabusJson for a subject.
     * Body: { "chaptersJson": "[...]", "syllabusJson": "[...]" }
     *
     * chaptersJson example:
     * ["Number Systems", "Polynomials", "Coordinate Geometry"]
     *
     * syllabusJson example:
     * [{"unit":"Unit 1 – Number Systems","topics":["Real Numbers","Irrational Numbers"]}]
     */
    @PutMapping("/admin/subjects/{subjectId}/syllabus")
    public ResponseEntity<SubjectDto> updateSyllabusManually(
            @PathVariable Long subjectId,
            @RequestBody Map<String, String> body) {
        String chaptersJson = body.get("chaptersJson");
        String syllabusJson = body.get("syllabusJson");
        return ResponseEntity.ok(service.updateSyllabusManually(subjectId, chaptersJson, syllabusJson));
    }

    /**
     * POST /api/v1/homepage/schoolprogramme/admin/subjects/{subjectId}/syllabus/ai-generate
     * Super admin sends raw syllabus text → OpenAI parses it →
     * saves both chaptersJson and syllabusJson automatically.
     * Body: { "rawText": "Chapter 1: Number Systems\nTopics: Real Numbers, Irrational Numbers..." }
     */
    @PostMapping("/admin/subjects/{subjectId}/syllabus/ai-generate")
    public ResponseEntity<SubjectDto> generateSyllabusWithAi(
            @PathVariable Long subjectId,
            @RequestBody Map<String, String> body) {
        String rawText = body.get("rawText");
        return ResponseEntity.ok(service.generateSyllabusWithAi(subjectId, rawText));
    }

    /**
     * POST /api/v1/homepage/schoolprogramme/admin/subjects/{subjectId}/syllabus/upload
     * Super admin uploads a .txt or .pdf file →
     * text is extracted → sent to OpenAI →
     * saves both chaptersJson and syllabusJson automatically.
     * Form field name: "file"
     */
    @PostMapping("/admin/subjects/{subjectId}/syllabus/upload")
    public ResponseEntity<SubjectDto> uploadFileAndGenerateSyllabus(
            @PathVariable Long subjectId,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(service.uploadFileAndGenerateSyllabus(subjectId, file));
    }
}