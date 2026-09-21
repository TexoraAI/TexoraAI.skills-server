package com.lms.assessment.constants;

import java.util.Map;

// WHY: Generic per-action, per-tier limit lookup. One table backs all 8 action types
// across the four assessment features (quiz, assignment, coding, study plan).
public class AssessmentUsageLimits {

    public enum Action {
        QUIZ_CREATE, QUIZ_ATTEMPT,
        ASSIGNMENT_CREATE, ASSIGNMENT_SUBMIT,
        CODING_CREATE, CODING_SOLVE,
        STUDY_PLAN_CREATE,
        PLAYGROUND_RUN,
        SAVE_CODE_FILE
        // STUDY_PLAN_ACCESS intentionally omitted — left ungated per decision
    }

    // {free, pro} — premium is always unlimited, not stored here
    private static final Map<Action, int[]> LIMITS = Map.of(
    	    Action.QUIZ_CREATE,       new int[]{3, 15},
    	    Action.QUIZ_ATTEMPT,      new int[]{5, 25},
    	    Action.ASSIGNMENT_CREATE, new int[]{3, 15},
    	    Action.ASSIGNMENT_SUBMIT, new int[]{5, 25},
    	    Action.CODING_CREATE,     new int[]{2, 10},
    	    Action.CODING_SOLVE,      new int[]{5, 20},
    	    Action.STUDY_PLAN_CREATE, new int[]{3, 8},
    	    Action.PLAYGROUND_RUN,    new int[]{5, 30},
    	    Action.SAVE_CODE_FILE,    new int[]{5, 30}
    	);

    public static int limitFor(Action action, String tier) {
        if ("premium".equalsIgnoreCase(tier)) {
            return -1; // unlimited
        }
        int[] pair = LIMITS.get(action);
        return "pro".equalsIgnoreCase(tier) ? pair[1] : pair[0];
    }

    public static boolean isUnlimited(Action action, String tier) {
        return limitFor(action, tier) == -1;
    }

    private AssessmentUsageLimits() {}
}