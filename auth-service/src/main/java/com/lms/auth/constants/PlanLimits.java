package com.lms.auth.constants;

public enum PlanLimits {
    TRIAL(20, 3, 1, 2, 2, 0),
    STARTER(50, 10, 2, 3, 3, 100),
    GROWTH(150, 25, 5, 5, 5, 1499900);

    private final int maxStudents;
    private final int maxTrainers;
    private final int maxDepartments;
    private final int maxBranchesPerDept;
    private final int maxBatchesPerBranch;
    private final int price;

    PlanLimits(int maxStudents, int maxTrainers, int maxDepartments,
               int maxBranchesPerDept, int maxBatchesPerBranch, int price) {
        this.maxStudents = maxStudents;
        this.maxTrainers = maxTrainers;
        this.maxDepartments = maxDepartments;
        this.maxBranchesPerDept = maxBranchesPerDept;
        this.maxBatchesPerBranch = maxBatchesPerBranch;
        this.price = price;
    }

    public int getMaxStudents() { return maxStudents; }
    public int getMaxTrainers() { return maxTrainers; }
    public int getMaxDepartments() { return maxDepartments; }
    public int getMaxBranchesPerDept() { return maxBranchesPerDept; }
    public int getMaxBatchesPerBranch() { return maxBatchesPerBranch; }
    public int getPrice() { return price; }

    /** Lowercase name matching the string stored in Organization.plan (e.g. "trial", "starter"). */
    public String getPlanName() {
        return name().toLowerCase();
    }

    public static PlanLimits fromPlanName(String planName) {
        if (planName == null) {
            throw new IllegalArgumentException("Plan name is null");
        }
        for (PlanLimits p : values()) {
            if (p.name().equalsIgnoreCase(planName)) {
                return p;
            }
        }
        throw new IllegalArgumentException("Unknown plan: " + planName);
    }
}