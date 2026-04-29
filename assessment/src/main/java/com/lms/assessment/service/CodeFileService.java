package com.lms.assessment.service;

import com.lms.assessment.dto.CodeFileDTO;
import com.lms.assessment.model.CodeFile;
import com.lms.assessment.repository.CodeFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CodeFileService {

    @Autowired
    private CodeFileRepository repo;

    // ── Save (create or overwrite if same name) ───
    public CodeFileDTO save(CodeFileDTO.SaveRequest req) {

        // If a file with same name exists for this student+batch, overwrite it
        Optional<CodeFile> existing = repo.findByStudentEmailAndBatchIdAndFileName(
            req.getStudentEmail(), req.getBatchId(), req.getFileName()
        );

        CodeFile file = existing.orElse(new CodeFile());
        file.setStudentEmail(req.getStudentEmail());
        file.setBatchId(req.getBatchId());
        file.setLanguage(req.getLanguage().toUpperCase());
        file.setFileName(req.getFileName());
        file.setCode(req.getCode());

        return toDTO(repo.save(file));
    }

    // ── Get all files for student in batch ────────
    public List<CodeFileDTO> getAll(String studentEmail, String batchId) {
        return repo
            .findByStudentEmailAndBatchIdOrderByUpdatedAtDesc(studentEmail, batchId)
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    // ── Get single file by ID ─────────────────────
    public CodeFileDTO getById(Long id) {
        return repo.findById(id)
            .map(this::toDTO)
            .orElseThrow(() -> new RuntimeException("File not found: " + id));
    }

    // ── Update code + optional rename ─────────────
    public CodeFileDTO update(Long id, CodeFileDTO.UpdateRequest req) {
        CodeFile file = repo.findById(id)
            .orElseThrow(() -> new RuntimeException("File not found: " + id));

        if (req.getFileName() != null && !req.getFileName().isBlank()) {
            file.setFileName(req.getFileName());
        }
        if (req.getCode() != null) {
            file.setCode(req.getCode());
        }
        return toDTO(repo.save(file));
    }

    // ── Delete ────────────────────────────────────
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new RuntimeException("File not found: " + id);
        }
        repo.deleteById(id);
    }

    // ── Mapper ────────────────────────────────────
    private CodeFileDTO toDTO(CodeFile f) {
        return new CodeFileDTO(
            f.getId(),
            f.getStudentEmail(),
            f.getBatchId(),
            f.getLanguage(),
            f.getFileName(),
            f.getCode(),
            f.getCreatedAt(),
            f.getUpdatedAt()
        );
    }
}