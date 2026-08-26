package com.lms.progress.dto;

public class NodeBottleneckStat {

    private Long nodeId;
    private String nodeTitle;
    private long stuckCount;
    private long notStartedCount;
    private long inProgressCount;
    private long doneCount;
    private double averageTimeSpentMinutes;

    public NodeBottleneckStat() {
    }

    public NodeBottleneckStat(Long nodeId, String nodeTitle, long stuckCount, long notStartedCount,
                               long inProgressCount, long doneCount, double averageTimeSpentMinutes) {
        this.nodeId = nodeId;
        this.nodeTitle = nodeTitle;
        this.stuckCount = stuckCount;
        this.notStartedCount = notStartedCount;
        this.inProgressCount = inProgressCount;
        this.doneCount = doneCount;
        this.averageTimeSpentMinutes = averageTimeSpentMinutes;
    }

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeTitle() {
        return nodeTitle;
    }

    public void setNodeTitle(String nodeTitle) {
        this.nodeTitle = nodeTitle;
    }

    public long getStuckCount() {
        return stuckCount;
    }

    public void setStuckCount(long stuckCount) {
        this.stuckCount = stuckCount;
    }

    public long getNotStartedCount() {
        return notStartedCount;
    }

    public void setNotStartedCount(long notStartedCount) {
        this.notStartedCount = notStartedCount;
    }

    public long getInProgressCount() {
        return inProgressCount;
    }

    public void setInProgressCount(long inProgressCount) {
        this.inProgressCount = inProgressCount;
    }

    public long getDoneCount() {
        return doneCount;
    }

    public void setDoneCount(long doneCount) {
        this.doneCount = doneCount;
    }

    public double getAverageTimeSpentMinutes() {
        return averageTimeSpentMinutes;
    }

    public void setAverageTimeSpentMinutes(double averageTimeSpentMinutes) {
        this.averageTimeSpentMinutes = averageTimeSpentMinutes;
    }
}
