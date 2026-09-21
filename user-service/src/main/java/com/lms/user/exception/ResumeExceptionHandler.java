package com.lms.user.exception;

import com.lms.user.exception.AiGenerationLimitExceededException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.lms.user.exception.TemplateNotAllowedException;
@RestControllerAdvice
public class ResumeExceptionHandler {

    @ExceptionHandler(AiGenerationLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleAiGenerationLimitExceeded(
            AiGenerationLimitExceededException ex) {

        Map<String, Object> body = Map.of(
            "error", "AI_GENERATION_LIMIT_REACHED",
            "userId", ex.getUserId(),
            "plan", ex.getPlan(),
            "currentCount", ex.getCurrentCount(),
            "maxAllowed", ex.getMaxAllowed(),
            "period", ex.getPeriod(),
            "message", "AI generation limit reached for this month. Upgrade for more."
        );

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(body);
    }
    @ExceptionHandler(TemplateNotAllowedException.class)
    public ResponseEntity<Map<String, Object>> handleTemplateNotAllowed(
            TemplateNotAllowedException ex) {

        Map<String, Object> body = Map.of(
            "error", "TEMPLATE_LOCKED",
            "templateName", ex.getTemplateName(),
            "currentPlan", ex.getCurrentPlan(),
            "allowedTemplates", ex.getAllowedTemplates(),
            "message", "This template isn't available on your current plan."
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }
}