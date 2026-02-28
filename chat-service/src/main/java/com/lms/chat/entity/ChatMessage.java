//package com.lms.chat.entity;
//
//import jakarta.persistence.*;
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "chat_messages")
//public class ChatMessage {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(nullable = false, length = 2000)
//    private String message;
//
//    @Column(name = "sender_email", nullable = false)
//    private String senderEmail;
//
//    @Column(name = "receiver_email", nullable = false)
//    private String receiverEmail;
//
//    @Enumerated(EnumType.STRING)
//    @Column(name = "sender_role", nullable = false)
//    private SenderRole senderRole;
//
//    @Column(name = "sent_at", nullable = false)
//    private LocalDateTime sentAt;
//
//    @Column(nullable = false)
//    private Boolean seen = false;
//
//    // Default Constructor (Required by JPA)
//    public ChatMessage() {
//    }
//
//    // --- Getters and Setters ---
//
//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public String getMessage() {
//        return message;
//    }
//
//    public void setMessage(String message) {
//        this.message = message;
//    }
//
//    public String getSenderEmail() {
//        return senderEmail;
//    }
//
//    public void setSenderEmail(String senderEmail) {
//        this.senderEmail = senderEmail;
//    }
//
//    public String getReceiverEmail() {
//        return receiverEmail;
//    }
//
//    public void setReceiverEmail(String receiverEmail) {
//        this.receiverEmail = receiverEmail;
//    }
//
//    public SenderRole getSenderRole() {
//        return senderRole;
//    }
//
//    public void setSenderRole(SenderRole senderRole) {
//        this.senderRole = senderRole;
//    }
//
//    public LocalDateTime getSentAt() {
//        return sentAt;
//    }
//
//    public void setSentAt(LocalDateTime sentAt) {
//        this.sentAt = sentAt;
//    }
//
//    public Boolean getSeen() {
//        return seen;
//    }
//
//    public void setSeen(Boolean seen) {
//        this.seen = seen;
//    }
//
//    /**
//     * Automatically sets the 'sentAt' field before the entity is saved to the DB.
//     * This prevents null constraint violations on the sent_at column.
//     */
//    @PrePersist
//    protected void onCreate() {
//        if (this.sentAt == null) {
//            this.sentAt = LocalDateTime.now();
//        }
//    }
//}




package com.lms.chat.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long batchId;

    private String senderEmail;

    private String receiverEmail;

    @Column(columnDefinition = "TEXT")
    private String message;

    private boolean seen = false;

    private LocalDateTime sentAt = LocalDateTime.now();

    // ===== Getters & Setters =====

    public Long getId() { return id; }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public String getSenderEmail() { return senderEmail; }
    public void setSenderEmail(String senderEmail) { this.senderEmail = senderEmail; }

    public String getReceiverEmail() { return receiverEmail; }
    public void setReceiverEmail(String receiverEmail) { this.receiverEmail = receiverEmail; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isSeen() { return seen; }
    public void setSeen(boolean seen) { this.seen = seen; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
}
