package com.lms.chat.service;

import com.lms.chat.dto.*;
import com.lms.chat.entity.*;
import com.lms.chat.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotebookService {

    private final NotebookRepository        notebookRepository;
    private final NotebookSectionRepository sectionRepository;
    private final NotebookPageRepository    pageRepository;
    private final NotebookSourceRepository  sourceRepository;
    
    private ContentExtractorService contentExtractorService;
    public NotebookService(NotebookRepository notebookRepository,
                           NotebookSectionRepository sectionRepository,
                           NotebookPageRepository pageRepository,NotebookSourceRepository  sourceRepository,
                           ContentExtractorService contentExtractorService) {
    	
        this.notebookRepository = notebookRepository;
        this.sectionRepository  = sectionRepository;
        this.pageRepository     = pageRepository;
        this.sourceRepository=sourceRepository;
        this.contentExtractorService=contentExtractorService;
    }

    // ── NOTEBOOK ──────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<NotebookResponse> getMyNotebooks(String studentEmail) {
        return notebookRepository
                .findByStudentEmailOrderByCreatedAtAsc(studentEmail)
                .stream()
                .map(NotebookResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public NotebookResponse getNotebook(Long id, String studentEmail) {
        Notebook nb = notebookRepository
                .findByIdAndStudentEmail(id, studentEmail)
                .orElseThrow(() -> new RuntimeException("Notebook not found"));
        return NotebookResponse.from(nb);
    }

    @Transactional
    public NotebookResponse createNotebook(NotebookRequest req, String studentEmail) {
        Notebook nb = new Notebook();
        nb.setStudentEmail(studentEmail);
        nb.setTitle(req.getTitle());
        nb.setColor(req.getColor());
        nb.setIcon(req.getIcon());

        // auto-create default section + page
        NotebookSection section = new NotebookSection();
        section.setTitle("General");
        section.setColor(req.getColor());
        section.setPosition(0);
        section.setNotebook(nb);

        NotebookPage page = new NotebookPage();
        page.setTitle("Page 1");
        page.setContent("");
        page.setPosition(0);
        page.setSection(section);

        section.getPages().add(page);
        nb.getSections().add(section);

        return NotebookResponse.from(notebookRepository.save(nb));
    }

    @Transactional
    public NotebookResponse updateNotebook(Long id, NotebookRequest req, String studentEmail) {
        Notebook nb = notebookRepository
                .findByIdAndStudentEmail(id, studentEmail)
                .orElseThrow(() -> new RuntimeException("Notebook not found"));
        nb.setTitle(req.getTitle());
        nb.setColor(req.getColor());
        nb.setIcon(req.getIcon());
        return NotebookResponse.from(notebookRepository.save(nb));
    }

    @Transactional
    public void deleteNotebook(Long id, String studentEmail) {
        Notebook nb = notebookRepository
                .findByIdAndStudentEmail(id, studentEmail)
                .orElseThrow(() -> new RuntimeException("Notebook not found"));
        notebookRepository.delete(nb);
    }

    // ── SECTION ───────────────────────────────────────────────────

    @Transactional
    public NotebookResponse addSection(NotebookSectionRequest req, String studentEmail) {
        Notebook nb = notebookRepository
                .findByIdAndStudentEmail(req.getNotebookId(), studentEmail)
                .orElseThrow(() -> new RuntimeException("Notebook not found"));

        NotebookSection section = new NotebookSection();
        section.setTitle(req.getTitle());
        section.setColor(req.getColor());
        section.setPosition(nb.getSections().size());
        section.setNotebook(nb);

        // auto-create first page
        NotebookPage page = new NotebookPage();
        page.setTitle("Page 1");
        page.setContent("");
        page.setPosition(0);
        page.setSection(section);
        section.getPages().add(page);

        nb.getSections().add(section);
        return NotebookResponse.from(notebookRepository.save(nb));
    }

    @Transactional
    public NotebookResponse updateSection(Long sectionId, NotebookSectionRequest req, String studentEmail) {
        NotebookSection section = sectionRepository
                .findByIdAndNotebook_StudentEmail(sectionId, studentEmail)
                .orElseThrow(() -> new RuntimeException("Section not found"));
        section.setTitle(req.getTitle());
        section.setColor(req.getColor());
        sectionRepository.save(section);
        return NotebookResponse.from(
                notebookRepository.findByIdAndStudentEmail(section.getNotebook().getId(), studentEmail)
                        .orElseThrow());
    }

    @Transactional
    public NotebookResponse deleteSection(Long sectionId, String studentEmail) {
        NotebookSection section = sectionRepository
                .findByIdAndNotebook_StudentEmail(sectionId, studentEmail)
                .orElseThrow(() -> new RuntimeException("Section not found"));

        Notebook nb = section.getNotebook();
        if (nb.getSections().size() <= 1) {
            throw new IllegalStateException("Cannot delete the last section");
        }
        nb.getSections().remove(section);
        return NotebookResponse.from(notebookRepository.save(nb));
    }

    // ── PAGE ──────────────────────────────────────────────────────

    @Transactional
    public NotebookResponse addPage(NotebookPageRequest req, String studentEmail) {
        NotebookSection section = sectionRepository
                .findByIdAndNotebook_StudentEmail(req.getSectionId(), studentEmail)
                .orElseThrow(() -> new RuntimeException("Section not found"));

        NotebookPage page = new NotebookPage();
        page.setTitle(req.getTitle() != null && !req.getTitle().isBlank()
                ? req.getTitle()
                : "Page " + (section.getPages().size() + 1));
        page.setContent("");
        page.setPosition(section.getPages().size());
        page.setSection(section);
        section.getPages().add(page);

        sectionRepository.save(section);
        return NotebookResponse.from(
                notebookRepository.findByIdAndStudentEmail(section.getNotebook().getId(), studentEmail)
                        .orElseThrow());
    }

    @Transactional
    public NotebookPageResponse savePage(Long pageId, NotebookPageRequest req, String studentEmail) {
        NotebookPage page = pageRepository
                .findByIdAndSection_Notebook_StudentEmail(pageId, studentEmail)
                .orElseThrow(() -> new RuntimeException("Page not found"));
        if (req.getTitle() != null) page.setTitle(req.getTitle());
        if (req.getContent() != null) page.setContent(req.getContent());
        return NotebookPageResponse.from(pageRepository.save(page));
    }

    @Transactional
    public NotebookResponse deletePage(Long pageId, String studentEmail) {
        NotebookPage page = pageRepository
                .findByIdAndSection_Notebook_StudentEmail(pageId, studentEmail)
                .orElseThrow(() -> new RuntimeException("Page not found"));

        NotebookSection section = page.getSection();
        if (section.getPages().size() <= 1) {
            throw new IllegalStateException("Cannot delete the last page");
        }
        section.getPages().remove(page);
        sectionRepository.save(section);
        return NotebookResponse.from(
                notebookRepository.findByIdAndStudentEmail(section.getNotebook().getId(), studentEmail)
                        .orElseThrow());
    }
    
 // Add to NotebookService.java

    

    @Transactional
    public NotebookResponse deleteSource(Long sourceId, String studentEmail) {
        // find source, verify ownership, delete
        NotebookSource source = sourceRepository
                .findByIdAndNotebook_StudentEmail(sourceId, studentEmail)
                .orElseThrow(() -> new RuntimeException("Source not found"));
        Notebook nb = source.getNotebook();
        nb.getSources().remove(source);
        return NotebookResponse.from(notebookRepository.save(nb));
    }
    @Transactional
    public NotebookResponse addUrlSource(Long notebookId, String url, String studentEmail) {
        Notebook nb = notebookRepository
                .findByIdAndStudentEmail(notebookId, studentEmail)
                .orElseThrow(() -> new RuntimeException("Notebook not found"));

        NotebookSource source = new NotebookSource();
        source.setNotebook(nb);
        source.setUrl(url);
        source.setTitle(url);

        if (url.contains("youtube.com") || url.contains("youtu.be")) {
            source.setSourceType(NotebookSource.SourceType.YOUTUBE);
            source.setExtractedContent("YouTube video: " + url);
        } else {
            source.setSourceType(NotebookSource.SourceType.WEBSITE);
            // ✅ Extract website content
            String content = contentExtractorService.extractFromUrl(url);
            source.setExtractedContent(content);
        }

        nb.getSources().add(source);
        return NotebookResponse.from(notebookRepository.save(nb));
    }
    @Transactional
    public NotebookResponse addFileSource(Long notebookId, MultipartFile file, String studentEmail) {
        Notebook nb = notebookRepository
                .findByIdAndStudentEmail(notebookId, studentEmail)
                .orElseThrow(() -> new RuntimeException("Notebook not found"));

        // Save file to disk
        String uploadDir = System.getProperty("user.home") + "/lms-uploads/";
        new File(uploadDir).mkdirs();
        String savedPath = uploadDir + System.currentTimeMillis() + "_" + file.getOriginalFilename();
        
        try {
            file.transferTo(new File(savedPath));
        } catch (Exception e) {
            throw new RuntimeException("File save failed");
        }

        NotebookSource source = new NotebookSource();
        source.setNotebook(nb);
        source.setTitle(file.getOriginalFilename());
        source.setFilePath(savedPath);

        String mime = file.getContentType();
        if (mime != null && mime.contains("pdf")) {
            source.setSourceType(NotebookSource.SourceType.PDF);
            // ✅ Extract PDF content
            String content = contentExtractorService.extractFromPdf(savedPath);
            source.setExtractedContent(content);
        } else {
            source.setSourceType(NotebookSource.SourceType.TEXT);
            source.setExtractedContent("File: " + file.getOriginalFilename());
        }

        nb.getSources().add(source);
        return NotebookResponse.from(notebookRepository.save(nb));
    }
}