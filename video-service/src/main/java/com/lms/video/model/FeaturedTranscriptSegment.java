package com.lms.video.model;

import jakarta.persistence.*;

@Entity
@Table(name = "featured_transcript_segments")
public class FeaturedTranscriptSegment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transcript_id", nullable = false)
    private FeaturedVideoTranscript transcript;

    private Double startSeconds;

    private Double endSeconds;

    @Column(columnDefinition = "TEXT")
    private String text;

    private Integer orderIndex;

    public FeaturedTranscriptSegment() {}

    public Long getId() { return id; }

    public FeaturedVideoTranscript getTranscript() { return transcript; }
    public void setTranscript(FeaturedVideoTranscript transcript) { this.transcript = transcript; }

    public Double getStartSeconds() { return startSeconds; }
    public void setStartSeconds(Double startSeconds) { this.startSeconds = startSeconds; }

    public Double getEndSeconds() { return endSeconds; }
    public void setEndSeconds(Double endSeconds) { this.endSeconds = endSeconds; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
}