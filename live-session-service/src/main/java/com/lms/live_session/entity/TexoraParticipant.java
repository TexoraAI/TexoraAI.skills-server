package com.lms.live_session.entity;


import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "texora_participants")
public class TexoraParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "texora_meeting_id", nullable = false, length = 64)
    private String texoraMeetingId;

    @Column(name = "role", length = 32)
    private String role;

    @Column(name = "identity", nullable = false, length = 255)
    private String identity;

    @Column(name = "display_name", length = 255)
    private String displayName;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    @Column(name = "audio_egress_id")
    private String audioEgressId;

    @Column(name = "audio_s3_url", length = 1024)
    private String audioS3Url;

    @Column(name = "track_sid", length = 64)
    private String trackSid;

    public TexoraParticipant() {}

    public Long getId() { return id; }

    public String getTexoraMeetingId() { return texoraMeetingId; }
    public void setTexoraMeetingId(String texoraMeetingId) { this.texoraMeetingId = texoraMeetingId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getIdentity() { return identity; }
    public void setIdentity(String identity) { this.identity = identity; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }

    public LocalDateTime getLeftAt() { return leftAt; }
    public void setLeftAt(LocalDateTime leftAt) { this.leftAt = leftAt; }

    public String getAudioEgressId() { return audioEgressId; }
    public void setAudioEgressId(String audioEgressId) { this.audioEgressId = audioEgressId; }

    public String getAudioS3Url() { return audioS3Url; }
    public void setAudioS3Url(String audioS3Url) { this.audioS3Url = audioS3Url; }

    public String getTrackSid() { return trackSid; }
    public void setTrackSid(String trackSid) { this.trackSid = trackSid; }
}