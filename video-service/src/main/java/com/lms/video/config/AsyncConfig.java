package com.lms.video.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    // Transcription is CPU-bound, and whisper itself already uses multiple
    // threads internally per job (see --threads in TranscriptGenerationService).
    // Running more than ONE transcription job at a time causes CPU contention
    // that makes every concurrent job dramatically slower — this is what
    // caused ~1hr runs in production for a video that takes ~5-8min run
    // alone. Cap this pool at 1 so jobs queue and run strictly one-at-a-time
    // instead of fighting each other for cores.
    @Bean(name = "transcriptionExecutor")
    public Executor transcriptionExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("transcript-job-");
        executor.initialize();
        return executor;
    }
}