package com.lms.chat.service;

import com.lms.chat.dto.AlertConfigDTO;
import com.lms.chat.entity.AlertConfig;
import com.lms.chat.kafka.FeedbackEventProducer;
import com.lms.chat.repository.AlertConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AlertConfigService {

    private final AlertConfigRepository alertConfigRepository;
    private final FeedbackEventProducer eventProducer;
    private static final Logger log = LoggerFactory.getLogger(AlertConfigService.class);

    public AlertConfigService(AlertConfigRepository alertConfigRepository,
                              FeedbackEventProducer eventProducer) {
        this.alertConfigRepository = alertConfigRepository;
        this.eventProducer = eventProducer;
    }

//    @Transactional
//    public AlertConfigDTO createOrUpdateAlertConfig(AlertConfigDTO dto) {
//        AlertConfig config = alertConfigRepository
//                .findByBatchId(dto.getBatchId())
//                .orElseGet(() -> {
//                    AlertConfig newConfig = new AlertConfig();
//                    newConfig.setBatchId(dto.getBatchId());
//                    return newConfig;
//                });
//
//        config.setTrainerEmail(dto.getTrainerEmail());
//        config.setSendToTrainer(dto.isSendToTrainer());
//        config.setSendToStudent(dto.isSendToStudent());
//        config.setSendToAdmin(dto.isSendToAdmin());
//        config.setAlertLowRatings(dto.isAlertLowRatings());
//        config.setLowRatingThreshold(dto.getLowRatingThreshold());
//        config.setAlertAnonymous(dto.isAlertAnonymous());
//        config.setTrainerMessage(dto.getTrainerMessage());   // ✅ ADD
//        config.setStudentMessage(dto.getStudentMessage());   // ✅ ADD
//        config.setAdminMessage(dto.getAdminMessage());       // ✅ ADD
//
//
//
//        AlertConfig saved = alertConfigRepository.save(config);
//        
//        // ✅ Publish event when alert config is saved
//        try {
//            eventProducer.publishAlertConfigUpdated(
//                saved.getBatchId(),
//                saved.getTrainerEmail(),
//                saved.isSendToTrainer(),
//                saved.isSendToStudent(),
//                saved.isSendToAdmin(),
//                saved.isAlertLowRatings(),
//                saved.getLowRatingThreshold(),
//                saved.getTrainerMessage(),   // ✅ ADD
//                saved.getStudentMessage(),   // ✅ ADD
//                saved.getAdminMessage(),   
//                saved.getTrainerMessage(),   // ✅ now works
//                saved.getStudentMessage(),   // ✅ now works
//                saved.getAdminMessage()  
//                
//            );
//            log.info("✅ Alert config event published: batch={}", saved.getBatchId());
//            System.out.println("✅ Alert config event published for batch: " + saved.getBatchId());
//        } catch (Exception e) {
//            log.error("❌ Error publishing alert config event: {}", e.getMessage());
//            System.out.println("❌ Error publishing alert config event: " + e.getMessage());
//        }
//
//        return AlertConfigDTO.from(saved);
//    }

    @Transactional
    public AlertConfigDTO createOrUpdateAlertConfig(AlertConfigDTO dto) {
        AlertConfig config = alertConfigRepository
                .findByBatchId(dto.getBatchId())
                .orElseGet(() -> {
                    AlertConfig newConfig = new AlertConfig();
                    newConfig.setBatchId(dto.getBatchId());
                    return newConfig;
                });

        config.setTrainerEmail(dto.getTrainerEmail());
        config.setSendToTrainer(dto.isSendToTrainer());
        config.setSendToStudent(dto.isSendToStudent());
        config.setSendToAdmin(dto.isSendToAdmin());
        config.setAlertLowRatings(dto.isAlertLowRatings());
        config.setLowRatingThreshold(dto.getLowRatingThreshold());
        config.setAlertAnonymous(dto.isAlertAnonymous());
        config.setTrainerMessage(dto.getTrainerMessage());   // ✅ ADD
        config.setStudentMessage(dto.getStudentMessage());   // ✅ ADD
        config.setAdminMessage(dto.getAdminMessage());       // ✅ ADD

        AlertConfig saved = alertConfigRepository.save(config);

        try {
            eventProducer.publishAlertConfigUpdated(
                saved.getBatchId(),
                saved.getTrainerEmail(),
                saved.isSendToTrainer(),
                saved.isSendToStudent(),
                saved.isSendToAdmin(),
                saved.isAlertLowRatings(),
                saved.getLowRatingThreshold(),
                saved.getTrainerMessage(),   // ✅ now works
                saved.getStudentMessage(),   // ✅ now works
                saved.getAdminMessage()      // ✅ now works
            );
            log.info("✅ Alert config event published: batch={}", saved.getBatchId());
        } catch (Exception e) {
            log.error("❌ Error publishing alert config event: {}", e.getMessage());
        }

        return AlertConfigDTO.from(saved);
    }
    
    
    
    
    
    
    
    
    
    @Transactional(readOnly = true)
    public AlertConfigDTO getAlertConfig(Long batchId) {
        return alertConfigRepository
                .findByBatchId(batchId)
                .map(AlertConfigDTO::from)
                .orElseGet(() -> {
                    // Return default config if not found
                    AlertConfigDTO defaultConfig = new AlertConfigDTO();
                    defaultConfig.setBatchId(batchId);
                    defaultConfig.setSendToTrainer(true);
                    defaultConfig.setSendToStudent(true);
                    defaultConfig.setSendToAdmin(true);
                    defaultConfig.setAlertLowRatings(true);
                    defaultConfig.setLowRatingThreshold(2.0);
                    defaultConfig.setAlertAnonymous(false);
                    return defaultConfig;
                });
    }

    @Transactional
    public void deleteAlertConfig(Long batchId) {
        alertConfigRepository.findByBatchId(batchId)
                .ifPresent(alertConfigRepository::delete);
    }
}