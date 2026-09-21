package com.lms.chat.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.poi.xslf.usermodel.*;
import org.springframework.stereotype.Service;

import java.awt.Rectangle;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

@Service
public class NotebookSlideService {

    private final S3Service s3Service;

    public NotebookSlideService(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    /**
     * Builds a real .pptx from structured slide JSON (array of
     * {title, bullets[], speakerNotes}) using Apache POI and uploads it to S3.
     * Returns the S3 key of the uploaded file.
     */
    public String generate(JsonNode slidesArray, Long notebookId) {
        if (slidesArray == null || !slidesArray.isArray() || slidesArray.isEmpty()) {
            throw new RuntimeException("Slide generation failed: no slide content to render");
        }

        try (XMLSlideShow ppt = new XMLSlideShow()) {
            for (JsonNode slideNode : slidesArray) {
                XSLFSlide slide = ppt.createSlide();

                // Title
                XSLFTextBox titleBox = slide.createTextBox();
                titleBox.setAnchor(new Rectangle(30, 20, 900, 80));
                XSLFTextParagraph titlePara = titleBox.addNewTextParagraph();
                XSLFTextRun titleRun = titlePara.addNewTextRun();
                titleRun.setText(slideNode.path("title").asText(""));
                titleRun.setFontSize(28.0);
                titleRun.setBold(true);

                // Bullets
                XSLFTextBox bodyBox = slide.createTextBox();
                bodyBox.setAnchor(new Rectangle(30, 120, 900, 400));
                JsonNode bullets = slideNode.path("bullets");
                if (bullets.isArray()) {
                    for (JsonNode bullet : bullets) {
                        XSLFTextParagraph para = bodyBox.addNewTextParagraph();
                        para.setBullet(true);
                        XSLFTextRun run = para.addNewTextRun();
                        run.setText(bullet.asText(""));
                        run.setFontSize(20.0);
                    }
                }

                // Speaker notes
                String speakerNotes = slideNode.path("speakerNotes").asText(null);
                if (speakerNotes != null && !speakerNotes.isBlank()) {
                    try {
                        XSLFNotes notes = ppt.getNotesSlide(slide);
                        XSLFTextShape notesShape = notes.getPlaceholder(1);
                        if (notesShape != null) {
                            notesShape.clearText();
                            notesShape.setText(speakerNotes);
                        } else {
                            XSLFTextBox notesBox = notes.createTextBox();
                            notesBox.setAnchor(new Rectangle(30, 30, 600, 400));
                            notesBox.setText(speakerNotes);
                        }
                    } catch (Exception notesEx) {
                        // Notes are a nice-to-have; don't fail the whole deck over them
                        System.err.println("Could not attach speaker notes for a slide: " + notesEx.getMessage());
                    }
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ppt.write(out);

            String key = "files/notebook-service-files/slides/" + notebookId + "/" + UUID.randomUUID() + ".pptx";
            s3Service.uploadBytes(key, out.toByteArray(),
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation");
            return key;
        } catch (Exception e) {
            throw new RuntimeException("Slide generation failed: " + e.getMessage(), e);
        }
    }
}