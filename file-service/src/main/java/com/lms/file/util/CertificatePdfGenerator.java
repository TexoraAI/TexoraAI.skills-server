package com.lms.file.util;

import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class CertificatePdfGenerator {

    private static final String BASE_PATH = "files/certificates/";

    public String generate(String studentName,
                           String courseName,
                           String certificateType) {

        try {
            File directory = new File(BASE_PATH);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String fileName = studentName.replace(" ", "_")
                    + "_" + certificateType + ".pdf";

            File file = new File(BASE_PATH + fileName);

            // For now just create empty PDF file
            file.createNewFile();

            return fileName;

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate certificate", e);
        }
    }
}
