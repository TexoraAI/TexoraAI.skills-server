package com.lms.assessment.service;

import com.lms.assessment.dto.FileUploadResponse;
import com.lms.assessment.model.AssignmentAttachment;
import com.lms.assessment.repository.AssignmentAttachmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AssignmentFileService {

    private final FileStorageService fileStorageService;
    private final AssignmentAttachmentRepository repository;

    public AssignmentFileService(FileStorageService fileStorageService,
                                 AssignmentAttachmentRepository repository) {
        this.fileStorageService = fileStorageService;
        this.repository = repository;
    }

    public FileUploadResponse uploadFile(Long assignmentId, MultipartFile file) throws IOException {

        String filePath = fileStorageService.saveFile(file, "assignments");

        AssignmentAttachment attachment = new AssignmentAttachment();
        attachment.setAssignmentId(assignmentId);
        attachment.setFileName(file.getOriginalFilename());
        attachment.setFilePath(filePath);
        attachment.setFileSize(file.getSize());

        AssignmentAttachment saved = repository.save(attachment);

        return new FileUploadResponse(
                saved.getId(),
                saved.getFileName(),
                "/api/assignment-files/download?path=" + saved.getFilePath()
        );
    }

    public List<FileUploadResponse> getFilesByAssignment(Long assignmentId) {

        return repository.findByAssignmentId(assignmentId)
                .stream()
                .map(file -> new FileUploadResponse(
                        file.getId(),
                        file.getFileName(),
                        "/api/assignment-files/download?path=" + file.getFilePath()
                ))
                .collect(Collectors.toList());
    }
}
