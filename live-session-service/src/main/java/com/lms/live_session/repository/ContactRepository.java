package com.lms.live_session.repository;

import com.lms.live_session.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactRepository extends JpaRepository<Contact, Long> {

    List<Contact> findByCreatorIdOrderByCreatedAtDesc(String creatorId);

    List<Contact> findByEmail(String email);

    List<Contact> findByCreatorIdAndEmailContainingIgnoreCase(String creatorId, String email);

    List<Contact> findByCreatorIdAndFirstNameContainingIgnoreCase(String creatorId, String firstName);

    List<Contact> findByCreatorIdAndLastNameContainingIgnoreCase(String creatorId, String lastName);

    boolean existsByCreatorIdAndEmail(String creatorId, String email);

    void deleteByCreatorId(String creatorId);
}