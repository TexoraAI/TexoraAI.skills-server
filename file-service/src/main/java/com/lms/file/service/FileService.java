


package com.lms.file.service;

import com.lms.file.kafka.FileEventProducer;
import com.lms.file.model.FileClassroomAccess;
import com.lms.file.model.FileResource;
import com.lms.file.repository.FileClassroomAccessRepository;
import com.lms.file.repository.FileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.Optional;
import java.nio.file.*;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Service
public class FileService {

    @Value("${file.storage-dir}")
    private String storageDir;

    private final FileRepository repo;
    private final FileClassroomAccessRepository accessRepo;
    private final FileEventProducer producer;

    public FileService(FileRepository repo,
                       FileClassroomAccessRepository accessRepo,
                       FileEventProducer producer) {
        this.repo = repo;
        this.accessRepo = accessRepo;
        this.producer = producer;
    }

    // ================= TRAINER UPLOAD =================
    public FileResource upload(MultipartFile file, Long batchId, String title,
                               String description, Long courseId, String category,
                               String status) throws Exception {

        String trainerEmail = SecurityContextHolder
                .getContext().getAuthentication().getName();

        // ✅ Only check batch ownership if batchId is provided
        if (batchId != null) {
            boolean allowed = accessRepo
                    .findByTrainerEmailAndBatchId(trainerEmail, batchId)
                    .size() > 0;
            if (!allowed)
                throw new RuntimeException("You are not assigned to this batch");
        }

        Path dir = Paths.get(storageDir);
        if (!Files.exists(dir))
            Files.createDirectories(dir);

        String storedName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path path = dir.resolve(storedName);
        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

        FileResource fr = new FileResource();
        fr.setOriginalName(file.getOriginalFilename());
        fr.setStoredName(storedName);
        fr.setTitle(title);
        fr.setDescription(description);
        fr.setCategory(category);
        fr.setCourseId(courseId);
        fr.setContentType(file.getContentType());
        fr.setSize(file.getSize());
        fr.setUploadedAt(Instant.now());
        fr.setBatchId(batchId);        // ✅ null is fine — no batch yet
        fr.setTrainerEmail(trainerEmail);
        fr.setStatus(status != null ? status : "draft");  // ✅ set status

        FileResource saved = repo.save(fr);

        // ✅ Only fire Kafka event if batch is assigned AND published
        if (batchId != null && "published".equals(status)) {
            try {
                producer.sendFileUploadedEvent(saved.getId(), saved.getTitle(),
                        saved.getBatchId(), saved.getTrainerEmail());
            } catch (Exception ignored) {}
        }

        return saved;
    }

 // ═══════════════════════════════════════════════════════════════════
//  ADD THIS METHOD to your existing FileService.java
//  (paste before the last closing brace)
// ═══════════════════════════════════════════════════════════════════

    /**
     * Edit an existing file resource.
     * - newFile is OPTIONAL: if null, the old stored file is kept on disk.
     * - All metadata fields are always updated.
     */
    public FileResource editFile(
            Long fileId,
            MultipartFile newFile,   // nullable — null = keep existing file
            String title,
            String description,
            Long batchId,            // nullable
            Long courseId,
            String category,
            String status
    ) throws Exception {

        String trainerEmail = SecurityContextHolder
                .getContext().getAuthentication().getName()
                .trim().toLowerCase();

        FileResource fr = repo.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found with id: " + fileId));

        // Only the uploader may edit
        if (!fr.getTrainerEmail().equalsIgnoreCase(trainerEmail)) {
            throw new RuntimeException("Not your file");
        }

        // If a new batchId is supplied, verify the trainer is assigned to it
        if (batchId != null) {
            boolean allowed = accessRepo
                    .findByTrainerEmailAndBatchId(trainerEmail, batchId)
                    .size() > 0;
            if (!allowed) {
                throw new RuntimeException("You are not assigned to this batch");
            }
        }

        // ── Replace physical file only when a new one is provided ──
        if (newFile != null && !newFile.isEmpty()) {
            // Delete old file from disk (best-effort)
            if (fr.getStoredName() != null && !fr.getStoredName().isBlank()) {
                Path oldPath = Paths.get(storageDir).resolve(fr.getStoredName());
                try {
                    Files.deleteIfExists(oldPath);
                } catch (Exception e) {
                    System.out.println("Could not delete old file: " + e.getMessage());
                }
            }

            Path dir = Paths.get(storageDir);
            if (!Files.exists(dir)) Files.createDirectories(dir);

            String storedName = System.currentTimeMillis() + "_" + newFile.getOriginalFilename();
            Path path = dir.resolve(storedName);
            Files.copy(newFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            fr.setOriginalName(newFile.getOriginalFilename());
            fr.setStoredName(storedName);
            fr.setSize(newFile.getSize());
            fr.setContentType(newFile.getContentType());
        }

        // ── Update metadata ──
        fr.setTitle(title != null ? title : fr.getTitle());
        fr.setDescription(description != null ? description : "");
        fr.setBatchId(batchId);
        fr.setCourseId(courseId);
        fr.setCategory(category != null ? category : "");
        fr.setStatus(status != null ? status : fr.getStatus());

        FileResource saved = repo.save(fr);

        // Fire Kafka if batch is assigned and published
        if (batchId != null && "published".equals(saved.getStatus())) {
            try {
                producer.sendFileUploadedEvent(saved.getId(), saved.getTitle(),
                        saved.getBatchId(), saved.getTrainerEmail());
            } catch (Exception ignored) {}
        }

        return saved;
    }
    
    
    // ================= PUBLISH FILE =================
    public FileResource publishFile(Long fileId) {
        String trainerEmail = SecurityContextHolder
                .getContext().getAuthentication().getName();

        FileResource fr = repo.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        if (!fr.getTrainerEmail().equals(trainerEmail))
            throw new RuntimeException("Not your file");

        if (fr.getBatchId() == null)
            throw new RuntimeException("Assign a batch before publishing");

        fr.setStatus("published");
        FileResource saved = repo.save(fr);

        try {
            producer.sendFileUploadedEvent(saved.getId(), saved.getTitle(),
                    saved.getBatchId(), saved.getTrainerEmail());
        } catch (Exception ignored) {}

        return saved;
    }

    // ================= ASSIGN BATCH =================
    public FileResource assignBatch(Long fileId, Long batchId) {
        String trainerEmail = SecurityContextHolder
                .getContext().getAuthentication().getName();

        FileResource fr = repo.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        if (!fr.getTrainerEmail().equals(trainerEmail))
            throw new RuntimeException("Not your file");

        boolean allowed = accessRepo
                .findByTrainerEmailAndBatchId(trainerEmail, batchId)
                .size() > 0;
        if (!allowed)
            throw new RuntimeException("You are not assigned to this batch");

        fr.setBatchId(batchId);
        fr.setStatus("published");
        FileResource saved = repo.save(fr);

        try {
            producer.sendFileUploadedEvent(saved.getId(), saved.getTitle(),
                    saved.getBatchId(), saved.getTrainerEmail());
        } catch (Exception ignored) {}

        return saved;
    }

    // ================= TRAINER FILES =================
    public List<FileResource> getTrainerFiles() {
        String trainerEmail = SecurityContextHolder
                .getContext().getAuthentication().getName();
        return repo.findByTrainerEmail(trainerEmail);
    }

    // ================= STUDENT FILES — only published =================
 // Replace getStudentFiles() with this:
    public List<FileResource> getStudentFiles() {
        String studentEmail = SecurityContextHolder
                .getContext().getAuthentication().getName()
                .trim().toLowerCase();

        // ✅ findByStudentEmail returns Optional — handle it correctly
        Optional<FileClassroomAccess> accessOpt = accessRepo.findByStudentEmail(studentEmail);

        if (accessOpt.isEmpty()) return Collections.emptyList();

        Long batchId = accessOpt.get().getBatchId();
        return repo.findByBatchIdInAndStatus(List.of(batchId), "published");
    }

    // ================= DOWNLOAD =================
    public byte[] download(String storedName) throws Exception {
        Path path = Paths.get(storageDir).resolve(storedName);
        return Files.readAllBytes(path);
    }

    // ================= DELETE =================
    public void delete(Long id) throws Exception {
        String trainerEmail = SecurityContextHolder
                .getContext().getAuthentication().getName();

        FileResource fr = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found"));

        if (!fr.getTrainerEmail().equals(trainerEmail))
            throw new RuntimeException("You can delete only your files");

        Path path = Paths.get(storageDir).resolve(fr.getStoredName());
        Files.deleteIfExists(path);
        repo.delete(fr);
    }

    public FileResource getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found with id: " + id));
    }

    public byte[] viewFile(Long id) throws Exception {
        FileResource file = getById(id);
        Path path = Paths.get(storageDir).resolve(file.getStoredName());
        if (!Files.exists(path))
            throw new RuntimeException("File not found on disk");
        return Files.readAllBytes(path);
    }

    public String getStorageDir() { return storageDir; }
}
