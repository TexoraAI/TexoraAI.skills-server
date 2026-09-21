package com.lms.chat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Dedicated thread pool for Studio generation background work
 * (NotebookStudioService.processGenerationAsync). Kept separate from any
 * other @Async usage elsewhere in the app so studio generations can't starve
 * (or be starved by) unrelated async work.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "studioGenerationExecutor")
    public Executor studioGenerationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(20);
        executor.setThreadNamePrefix("studio-gen-");
        // AbortPolicy (the default, set explicitly for clarity) makes a saturated
        // pool throw TaskRejectedException synchronously back to the caller
        // instead of silently dropping the task or blocking the submitting
        // thread — NotebookStudioService.createPendingOutput(...) catches this
        // and marks the output FAILED with a clear message.
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.initialize();
        return executor;
    }

    /**
     * Dedicated pool for concurrent per-slide OpenAI image-generation calls
     * made *within* a single NotebookVideoService.generate(...) invocation
     * (custom/whiteboard/kawaii visual styles). This is intentionally not an
     * @Async-dispatch executor — it's injected directly as a plain
     * ExecutorService so NotebookVideoService can submit a batch of
     * CompletableFuture.supplyAsync(...) calls and wait on all of them
     * together via CompletableFuture.allOf(...).
     *
     * Kept separate from studioGenerationExecutor so a burst of parallel
     * image calls for one video can't starve (or be starved by) Studio's
     * background generation work.
     */
    @Bean(name = "imageGenerationExecutor")
    public ExecutorService imageGenerationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(6);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(20);
        executor.setThreadNamePrefix("img-gen-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.initialize();
        return executor.getThreadPoolExecutor();
    }
}