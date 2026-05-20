package com.lms.notification.repository;

import com.lms.notification.model.ContactMessage;
import com.lms.notification.model.ContactMessage.ContactStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactMessageRepository
        extends JpaRepository<ContactMessage, Long> {

    List<ContactMessage> findByStatus(ContactStatus status);

    List<ContactMessage> findByEmailOrderBySubmittedAtDesc(String email);
}