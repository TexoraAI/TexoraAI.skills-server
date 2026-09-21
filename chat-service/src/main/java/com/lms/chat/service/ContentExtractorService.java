package com.lms.chat.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    // Extract text from a saved .docx file path using Apache POI (poi-ooxml)
    public String extractFromDocx(String filePath) {
        try (FileInputStream fis = new FileInputStream(filePath);
             XWPFDocument doc = new XWPFDocument(fis)) {

            String text = doc.getParagraphs().stream()
                    .map(XWPFParagraph::getText)
                    .collect(Collectors.joining("\n"));

            // Same 8000-char cap pattern used elsewhere in this file
            return text.length() > 8000 ? text.substring(0, 8000) + "..." : text;
        } catch (Exception e) {
            return "Could not extract DOCX content: " + e.getMessage();
        }
    }

    // Extract text from a saved legacy binary .doc file path.
    //
    // TODO: Legacy .doc extraction requires Apache POI's poi-scratchpad module
    // (org.apache.poi:poi-scratchpad), which is NOT currently present in pom.xml
    // (only `poi` and `poi-ooxml` are declared). Once poi-scratchpad is added,
    // implement this using something like:
    //
    //   try (FileInputStream fis = new FileInputStream(filePath);
    //        HWPFDocument doc = new HWPFDocument(fis)) {
    //       WordExtractor extractor = new WordExtractor(doc);
    //       String text = extractor.getText();
    //       return text.length() > 8000 ? text.substring(0, 8000) + "..." : text;
    //   }
    //
    // Until then, return a clear message instead of crashing on the missing classes.
    public String extractFromDoc(String filePath) {
        return "Legacy .doc format not yet supported — please convert to .docx and re-upload.";
    }

    // Extract text from a saved plain-text (.txt) file path
    public String extractFromTxt(String filePath) {
        try {
            String text = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
            return text.length() > 8000 ? text.substring(0, 8000) + "..." : text;
        } catch (Exception e) {
            return "Could not extract text file content: " + e.getMessage();
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

    /**
     * Fetches a YouTube video's transcript/captions and returns it as plain text.
     *
     * YouTube doesn't expose a public "download captions for any video" API
     * (the official Data API's captions.download endpoint only works for videos
     * you own). This uses the same practical approach most transcript
     * extraction libraries use: load the public watch page, pull the caption
     * track list out of the embedded player config, then fetch and parse the
     * timedtext XML for the chosen track. Like extractFromUrl(), this depends
     * on YouTube's current page structure and can break if that changes.
     */
    public String extractFromYoutube(String videoUrlOrId) {
        try {
            String videoId = extractYoutubeVideoId(videoUrlOrId);
            if (videoId == null) {
                return "Could not extract YouTube content: invalid video URL or ID";
            }

            // 1. Load the watch page and locate the embedded caption track list
            String watchPageUrl = "https://www.youtube.com/watch?v=" + videoId;
            String html = Jsoup.connect(watchPageUrl)
                    .userAgent("Mozilla/5.0")
                    .timeout(8000)
                    .ignoreContentType(true)
                    .get()
                    .html();

            Matcher tracksMatcher = Pattern.compile("\"captionTracks\":(\\[.*?\\])").matcher(html);
            if (!tracksMatcher.find()) {
                return "Could not extract YouTube content: no captions available for this video";
            }
            String tracksJson = tracksMatcher.group(1);

            // 2. Prefer an English track; fall back to the first track available
            Matcher urlMatcher = Pattern.compile("\"baseUrl\":\"(.*?)\"").matcher(tracksJson);
            String captionUrl = null;
            String firstUrl = null;
            while (urlMatcher.find()) {
                String candidate = urlMatcher.group(1).replace("\\u0026", "&");
                if (firstUrl == null) {
                    firstUrl = candidate;
                }
                if (candidate.contains("lang=en")) {
                    captionUrl = candidate;
                    break;
                }
            }
            if (captionUrl == null) {
                captionUrl = firstUrl;
            }
            if (captionUrl == null) {
                return "Could not extract YouTube content: no caption track URL found";
            }

            // 3. Fetch the transcript XML and strip it down to plain text
            String transcriptXml = Jsoup.connect(captionUrl)
                    .userAgent("Mozilla/5.0")
                    .timeout(8000)
                    .ignoreContentType(true)
                    .get()
                    .html();

            Document xmlDoc = Jsoup.parse(transcriptXml, "", Parser.xmlParser());
            String text = xmlDoc.select("text").stream()
                    .map(el -> Parser.unescapeEntities(el.text(), true))
                    .collect(Collectors.joining(" "));

            if (text.isBlank()) {
                return "Could not extract YouTube content: transcript was empty";
            }

            return text.length() > 8000 ? text.substring(0, 8000) + "..." : text;
        } catch (Exception e) {
            return "Could not extract YouTube content: " + e.getMessage();
        }
    }

    private String extractYoutubeVideoId(String urlOrId) {
        if (urlOrId == null) {
            return null;
        }
        String s = urlOrId.trim();
        // Already looks like a bare 11-character video ID
        if (s.matches("^[a-zA-Z0-9_-]{11}$")) {
            return s;
        }
        Matcher m = Pattern.compile("(?:v=|youtu\\.be/|/embed/|/shorts/)([a-zA-Z0-9_-]{11})").matcher(s);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }
}