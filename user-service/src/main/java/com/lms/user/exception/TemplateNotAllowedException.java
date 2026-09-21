package com.lms.user.exception;

import java.util.List;

public class TemplateNotAllowedException extends RuntimeException {

    private final String templateName;
    private final String currentPlan;
    private final List<String> allowedTemplates;

    public TemplateNotAllowedException(String templateName, String currentPlan,
                                        List<String> allowedTemplates) {
        super("Template '" + templateName + "' is not available on the " + currentPlan
                + " plan. Allowed templates: " + allowedTemplates);
        this.templateName = templateName;
        this.currentPlan = currentPlan;
        this.allowedTemplates = allowedTemplates;
    }

    public String getTemplateName() {
        return templateName;
    }

    public String getCurrentPlan() {
        return currentPlan;
    }

    public List<String> getAllowedTemplates() {
        return allowedTemplates;
    }
}