//package com.lms.file.service;
//
//import com.lms.file.kafka.FileEventProducer;
//import com.lms.file.model.FileClassroomAccess;
//import com.lms.file.model.FileResource;
//import com.lms.file.repository.FileClassroomAccessRepository;
//import com.lms.file.repository.FileRepository;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.nio.file.*;
//import java.time.Instant;
//import java.util.List;
//
//@Service
//public class FileService {
//
//    @Value("${file.storage-dir}")
//    private String storageDir;
//
//    private final FileRepository repo;
//    private final FileClassroomAccessRepository accessRepo;
//    private final FileEventProducer producer;
//
//    public FileService(FileRepository repo,
//                       FileClassroomAccessRepository accessRepo,
//                       FileEventProducer producer) {
//        this.repo = repo;
//        this.accessRepo = accessRepo;
//        this.producer = producer;
//    }
//
//    // ================= TRAINER UPLOAD =================
//    public FileResource upload(MultipartFile file, Long batchId,String title, String description) throws Exception {
//
//        String trainerEmail = SecurityContextHolder
//                .getContext()
//                .getAuthentication()
//                .getName();
//
//        // 🔒 check trainer teaches this batch
//        boolean allowed = accessRepo
//                .findByTrainerEmailAndBatchId(trainerEmail, batchId)
//                .size() > 0;
//
//        if (!allowed)
//            throw new RuntimeException("You are not assigned to this batch");
//
//        Path dir = Paths.get(storageDir);
//        if (!Files.exists(dir))
//            Files.createDirectories(dir);
//
//        String storedName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
//        Path path = dir.resolve(storedName);
//
//        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
//
//        FileResource fr = new FileResource();
//        fr.setOriginalName(file.getOriginalFilename());
//        fr.setStoredName(storedName);
//        fr.setTitle(title);
//        fr.setDescription(description);
//        fr.setContentType(file.getContentType());
//        fr.setSize(file.getSize());
//        fr.setUploadedAt(Instant.now());
//        fr.setBatchId(batchId);
//        fr.setTrainerEmail(trainerEmail);
//
//        FileResource saved = repo.save(fr);
//
//        try {
//        	// ✅ NEW
//        	producer.sendFileUploadedEvent(saved.getId(), saved.getTitle(),
//        	                                saved.getBatchId(), saved.getTrainerEmail());
//        } catch (Exception ignored) {}
//
//        return saved;
//    }
//
//    // ================= TRAINER FILES =================
//    public List<FileResource> getTrainerFiles() {
//
//        String trainerEmail = SecurityContextHolder
//                .getContext()
//                .getAuthentication()
//                .getName();
//
//        return repo.findByTrainerEmail(trainerEmail);
//    }
//
//    // ================= STUDENT FILES =================
//    public List<FileResource> getStudentFiles() {
//
//        String studentEmail = SecurityContextHolder
//                .getContext()
//                .getAuthentication()
//                .getName();
//
//        FileClassroomAccess access = accessRepo
//                .findByStudentEmail(studentEmail)
//                .orElseThrow(() -> new RuntimeException("Student not assigned to any batch"));
//
//        return repo.findByBatchId(access.getBatchId());
//    }
//
//   
//    
//    // ================= DOWNLOAD =================
//    public byte[] download(String storedName) throws Exception {
//        Path path = Paths.get(storageDir).resolve(storedName);
//        return Files.readAllBytes(path);
//    }
//
//    // ================= DELETE =================
//    public void delete(Long id) throws Exception {
//
//        String trainerEmail = SecurityContextHolder
//                .getContext()
//                .getAuthentication()
//                .getName();
//
//        FileResource fr = repo.findById(id)
//                .orElseThrow(() -> new RuntimeException("File not found"));
//
//        // 🔒 trainer can delete only own file
//        if (!fr.getTrainerEmail().equals(trainerEmail))
//            throw new RuntimeException("You can delete only your files");
//
//        Path path = Paths.get(storageDir).resolve(fr.getStoredName());
//        Files.deleteIfExists(path);
//
//        repo.delete(fr);
//    }
//    public FileResource getById(Long id) {
//        return repo.findById(id)
//                .orElseThrow(() -> new RuntimeException("File not found with id: " + id));
//    }
//    public byte[] viewFile(Long id) throws Exception {
//
//        FileResource file = getById(id);
//
//        Path path = Paths.get(storageDir).resolve(file.getStoredName());
//
//        if (!Files.exists(path))
//            throw new RuntimeException("File not found");
//
//        return Files.readAllBytes(path);
//    }
//    public String getStorageDir() {
//        return storageDir;
//    }
//
//}



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

import java.nio.file.*;
import java.time.Instant;
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
                               String description, Long courseId, String category) throws Exception {

        String trainerEmail = SecurityContextHolder
                .getContext().getAuthentication().getName();

        boolean allowed = accessRepo
                .findByTrainerEmailAndBatchId(trainerEmail, batchId)
                .size() > 0;

        if (!allowed)
            throw new RuntimeException("You are not assigned to this batch");

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
        fr.setCategory(category);        // NEW
        fr.setCourseId(courseId);        // NEW
        fr.setContentType(file.getContentType());
        fr.setSize(file.getSize());
        fr.setUploadedAt(Instant.now());
        fr.setBatchId(batchId);
        fr.setTrainerEmail(trainerEmail);

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

    // ================= STUDENT FILES =================
    public List<FileResource> getStudentFiles() {
        String studentEmail = SecurityContextHolder
                .getContext().getAuthentication().getName();

        FileClassroomAccess access = accessRepo
                .findByStudentEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("Student not assigned to any batch"));

        return repo.findByBatchId(access.getBatchId());
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




