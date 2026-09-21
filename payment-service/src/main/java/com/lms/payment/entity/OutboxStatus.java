package com.lms.payment.entity;

public enum OutboxStatus {
    PENDING,
    PUBLISHED,
    // Exhausted its retry budget in OutboxRelayJob. Left in place (not deleted) as a
    // dead-letter row for manual investigation/replay — the relay job stops retrying it.
    FAILED
}