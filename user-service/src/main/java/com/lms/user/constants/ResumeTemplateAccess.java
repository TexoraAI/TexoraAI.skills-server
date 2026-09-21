package com.lms.user.constants;

import java.util.List;

public final class ResumeTemplateAccess {

    public static final List<String> ALL_TEMPLATES = List.of(
        "classic", "modern", "minimal", "professional", "creative",
        "executive", "technical", "elegant", "bold", "compact"
    );

    public static final int FREE_TEMPLATE_COUNT = 3;
    public static final int PRO_TEMPLATE_COUNT = 5;

    public static List<String> allowedTemplatesFor(String plan) {
        String p = plan == null ? "free" : plan.toLowerCase();
        int limit = switch (p) {
            case "premium" -> ALL_TEMPLATES.size();
            case "pro" -> PRO_TEMPLATE_COUNT;
            default -> FREE_TEMPLATE_COUNT;
        };
        return ALL_TEMPLATES.subList(0, Math.min(limit, ALL_TEMPLATES.size()));
    }

    public static boolean isTemplateAllowed(String templateName, String plan) {
        if (templateName == null) return true;
        return allowedTemplatesFor(plan).stream()
                .anyMatch(t -> t.equalsIgnoreCase(templateName));
    }

    private ResumeTemplateAccess() {}
}