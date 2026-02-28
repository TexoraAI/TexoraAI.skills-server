package com.lms.file;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
@EnableWebSecurity
@SpringBootApplication
public class FileServiceApplication2 {

    public static void main(String[] args) {
        SpringApplication.run(FileServiceApplication2.class, args);
    }
}
