// com/lms/notification/config/FirebaseConfig.java
package com.lms.notification.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
//
//@Configuration
//public class FirebaseConfig {
//
//    @PostConstruct
//    public void initialize() throws IOException {
//        if (FirebaseApp.getApps().isEmpty()) {
//            InputStream serviceAccount =
//                getClass().getClassLoader()
//                    .getResourceAsStream("firebase-service-account.json");
//
//            FirebaseOptions options = FirebaseOptions.builder()
//                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
//                .build();
//
//            FirebaseApp.initializeApp(options);
//            System.out.println("✅ Firebase initialized");
//        }
//    }
//}

@Configuration
public class FirebaseConfig {
    @PostConstruct
    public void initialize() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            
            InputStream serviceAccount;
            
            // Production: read from mounted file
            File prodFile = new File("/app/firebase-service-account.json");
            if (prodFile.exists()) {
                serviceAccount = new FileInputStream(prodFile);
            } else {
                // Local: read from classpath/resources
                serviceAccount = getClass().getClassLoader()
                    .getResourceAsStream("firebase-service-account.json");
            }
            
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            FirebaseApp.initializeApp(options);
            System.out.println("✅ Firebase initialized successfully!");
        }
    }
}