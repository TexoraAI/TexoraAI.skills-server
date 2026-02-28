//package com.lms.chat.repository;
//
//import com.lms.chat.entity.ChatMessage;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//
//import java.util.List;
//
//public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
//
//    @Query("""
//        SELECT c FROM ChatMessage c
//        WHERE 
//        (c.senderEmail = :user1 AND c.receiverEmail = :user2)
//        OR
//        (c.senderEmail = :user2 AND c.receiverEmail = :user1)
//        ORDER BY c.sentAt ASC
//    """)
//    List<ChatMessage> getConversation(
//            @Param("user1") String user1,
//            @Param("user2") String user2
//    );
//
//    List<ChatMessage> findByReceiverEmailOrderBySentAtDesc(String receiverEmail);
//}

package com.lms.chat.repository;

import com.lms.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("""
        SELECT c FROM ChatMessage c
        WHERE c.batchId = :batchId
        AND (
            (c.senderEmail = :user1 AND c.receiverEmail = :user2)
            OR
            (c.senderEmail = :user2 AND c.receiverEmail = :user1)
        )
        ORDER BY c.sentAt ASC
    """)
    List<ChatMessage> getConversation(
            @Param("batchId") Long batchId,
            @Param("user1") String user1,
            @Param("user2") String user2
    );

    @Query("""
        SELECT c FROM ChatMessage c
        WHERE c.batchId = :batchId
        AND c.receiverEmail = :trainerEmail
        ORDER BY c.sentAt DESC
    """)
    List<ChatMessage> getTrainerInbox(
            @Param("batchId") Long batchId,
            @Param("trainerEmail") String trainerEmail
    );
}
