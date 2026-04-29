package com.lms.chat.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import java.io.File;
import org.apache.pdfbox.Loader;
@Service
public class ContentExtractorService {

    // Extract text from a saved PDF file path
    public String extractFromPdf(String filePath) {
        try {
        	PDDocument doc = Loader.loadPDF(new File(filePath));
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc);
            doc.close();
            // Limit to 8000 chars to avoid token overflow
            return text.length() > 8000 ? text.substring(0, 8000) + "..." : text;
        } catch (Exception e) {
            return "Could not extract PDF content: " + e.getMessage();
        }
    }

    // Scrape text from a website URL
    public String extractFromUrl(String url) {
        try {
            String text = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(8000)
                    .get()
                    .body()
                    .text();
            return text.length() > 8000 ? text.substring(0, 8000) + "..." : text;
        } catch (Exception e) {
            return "Could not scrape website content: " + e.getMessage();
        }
    }
}