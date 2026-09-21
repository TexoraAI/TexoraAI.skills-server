package com.lms.payment.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

// These NewTopic beans are picked up by Spring Boot's auto-configured KafkaAdmin
// on startup, which creates any topic that doesn't already exist. This is fine for
// local/dev; in a real cluster topics are usually pre-provisioned with proper
// partition counts and replication factors, so treat the values below as dev defaults.
//
// Names are read from application.yml's topics.* block (not hardcoded here) so this
// class, PaymentTopics constants, and the yml file can't silently drift apart.
@Configuration
public class KafkaConfig {

    @Value("${topics.payment:payment-events}")
    private String paymentTopic;

    @Value("${topics.analytics:analytics-events}")
    private String analyticsTopic;

    @Value("${topics.invoice:invoice-events}")
    private String invoiceTopic;

    @Value("${kafka.topics.partitions:1}")
    private int partitions;

    @Value("${kafka.topics.replication-factor:1}")
    private short replicationFactor;

    @Bean
    public NewTopic paymentEventsTopic() {
        return TopicBuilder.name(paymentTopic)
                .partitions(partitions)
                .replicas(replicationFactor)
                .build();
    }

    @Bean
    public NewTopic analyticsEventsTopic() {
        return TopicBuilder.name(analyticsTopic)
                .partitions(partitions)
                .replicas(replicationFactor)
                .build();
    }

    @Bean
    public NewTopic invoiceEventsTopic() {
        return TopicBuilder.name(invoiceTopic)
                .partitions(partitions)
                .replicas(replicationFactor)
                .build();
    }
}