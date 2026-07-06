package com.lms.course.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * Single responsibility: pull raw text out of an uploaded PDF/DOC/DOCX.
 * Knows nothing about syllabus structure or AI.
 */
@Service
public class FileTextExtractionService {

    public String extractText(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.contains(".")) {
            throw new IllegalArgumentException("Uploaded file has no valid name/extension");
        }
        String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();

        try (InputStream is = file.getInputStream()) {
            switch (ext) {
                case "pdf":  return extractFromPdf(is);
                case "docx": return extractFromDocx(is);
                case "doc":  return extractFromDoc(is);
                default:
                    throw new IllegalArgumentException("Unsupported file type: " + ext);
            }
        }
    }

    private String extractFromPdf(InputStream is) throws IOException {
        try (PDDocument document = PDDocument.load(is)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(document);
        }
    }

    private String extractFromDocx(InputStream is) throws IOException {
        try (XWPFDocument document = new XWPFDocument(is);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }

    private String extractFromDoc(InputStream is) throws IOException {
        try (HWPFDocument document = new HWPFDocument(is);
             WordExtractor extractor = new WordExtractor(document)) {
            return extractor.getText();
        }
    }
}