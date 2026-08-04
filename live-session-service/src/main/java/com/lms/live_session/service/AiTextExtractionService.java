package com.lms.live_session.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
public class AiTextExtractionService {

    /**
     * Extracts plain text from an uploaded file based on its extension.
     * Returns "" (not null) if the type is unsupported or extraction fails,
     * so callers can still save the resource without text content.
     */
    public String extractText(MultipartFile file, String fileName) {
        String lower = fileName == null ? "" : fileName.toLowerCase();
        try (InputStream is = file.getInputStream()) {
            if (lower.endsWith(".pdf")) {
                return extractPdf(is);
            } else if (lower.endsWith(".docx")) {
                return extractDocx(is);
            } else if (lower.endsWith(".txt") || lower.endsWith(".md")) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            } else {
                System.err.println("⚠️ Unsupported file type for text extraction: " + fileName);
                return "";
            }
        } catch (Exception e) {
            System.err.println("❌ Text extraction failed for " + fileName + ": " + e.getMessage());
            return "";
        }
    }

    private String extractPdf(InputStream is) throws IOException {
        try (PDDocument doc = PDDocument.load(is)) {
            return new PDFTextStripper().getText(doc);
        }
    }

    private String extractDocx(InputStream is) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(is);
             XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
            return extractor.getText();
        }
    }
}