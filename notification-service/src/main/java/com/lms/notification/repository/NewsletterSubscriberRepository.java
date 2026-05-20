package com.lms.notification.repository;

import com.lms.notification.model.NewsletterSubscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NewsletterSubscriberRepository
        extends JpaRepository<NewsletterSubscriber, Long> {

    boolean existsByEmail(String email);

    Optional<NewsletterSubscriber> findByEmail(String email);
}