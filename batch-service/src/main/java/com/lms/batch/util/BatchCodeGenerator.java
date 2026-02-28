package com.lms.batch.util;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class BatchCodeGenerator {

    public String generate() {
        return "BATCH-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
