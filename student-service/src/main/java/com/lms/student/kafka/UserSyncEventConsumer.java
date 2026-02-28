package com.lms.student.kafka;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.student.event.UserEvent;
import com.lms.student.model.Student;
import com.lms.student.model.Trainer;
import com.lms.student.repo.StudentRepository;
import com.lms.student.repo.TrainerRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class UserSyncEventConsumer {

    private final StudentRepository studentRepository;
    private final TrainerRepository trainerRepository;
    private final ObjectMapper mapper;

    public UserSyncEventConsumer(
            StudentRepository studentRepository,
            TrainerRepository trainerRepository
    ) {
        this.studentRepository = studentRepository;
        this.trainerRepository = trainerRepository;

        // 🔑 VERY IMPORTANT
        this.mapper = new ObjectMapper();
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @KafkaListener(
            topics = "auth-sync-events",
            groupId = "student-service-group"
    )
    public void consume(String message) {

        try {
            UserEvent e = mapper.readValue(message, UserEvent.class);

            if (e.getEventType() == null) {
                return;
            }

            System.out.println("📥 USER SYNC EVENT RECEIVED → " + e.getEventType());

            switch (e.getEventType()) {

                case "USER_UPDATED":
                    handleUserUpdated(e);
                    break;

                case "USER_ROLE_CHANGED":
                    handleRoleChange(e);
                    break;

                case "USER_DELETED":
                    handleDelete(e);
                    break;

                default:
                    System.out.println("⚠️ UNKNOWN EVENT TYPE → " + e.getEventType());
            }

        } catch (Exception ex) {
            System.err.println("❌ USER SYNC EVENT FAILED");
            ex.printStackTrace();
        }
    }

    // ================= UPDATE =================
   private void handleUserUpdated(UserEvent e) {
//
//        studentRepository.findByEmail(e.getEmail())
//                .ifPresent(s -> {
//                    s.setEmail(e.getEmail());
//                    studentRepository.save(s);
//                    System.out.println("✏️ STUDENT UPDATED | email=" + e.getEmail());
//                });
//        
	   studentRepository.findByUserId(e.getUserId())
       .ifPresent(s -> {
           s.setEmail(e.getEmail());
           studentRepository.save(s);
           System.out.println("✏️ STUDENT UPDATED | userId=" + e.getUserId());
       });

    
    
    
    
//        trainerRepository.findByEmail(e.getEmail())
//                .ifPresent(t -> {
//                    t.setEmail(e.getEmail());
//                    t.setName(e.getDisplayName());
//                    trainerRepository.save(t);
//                    System.out.println("✏️ TRAINER UPDATED | email=" + e.getEmail());
//                });
        
        trainerRepository.findByUserId(e.getUserId())
        .ifPresent(t -> {
            t.setEmail(e.getEmail());
            t.setName(e.getDisplayName());
            trainerRepository.save(t);
        });

    }

    // ================= ROLE MIGRATION =================
    private void handleRoleChange(UserEvent e) {

        // 🔥 ALWAYS REMOVE FROM BOTH FIRST
        studentRepository.findByUserId(e.getUserId())
                .ifPresent(studentRepository::delete);

        trainerRepository.findByUserId(e.getUserId())
                .ifPresent(trainerRepository::delete);

        // ✅ INSERT BASED ON NEW ROLE
        if ("STUDENT".equals(e.getRole())) {
            Student s = new Student();
            s.setUserId(e.getUserId());
            s.setEmail(e.getEmail());
            s.setStatus("ACTIVE");
            studentRepository.save(s);

            System.out.println("🆕 STUDENT CREATED | userId=" + e.getUserId());
        }

//        if ("TRAINER".equals(e.getRole())) {
//
//            // 🚨 HARD GUARD — DO NOT INSERT WITHOUT USER ID
//            if (e.getUserId() == null) {
//                System.err.println("❌ TRAINER CREATION SKIPPED — userId is NULL | email=" + e.getEmail());
//                return;
//            }
//
//            Trainer t = new Trainer();
//            t.setUserId(e.getUserId());
//            t.setEmail(e.getEmail());
//
//            // ✅ name must never be null
//            String name = e.getDisplayName();
//            if (name == null || name.isBlank()) {
//                name = "Trainer-" + e.getUserId();
//            }
//
//            t.setName(e.getDisplayName());
//            t.setStatus("ACTIVE");
//
//            trainerRepository.save(t);
//
//            System.out.println("🆕 TRAINER CREATED | userId=" + e.getUserId());
        
        
        
        
        if ("TRAINER".equals(e.getRole())) {

            if (e.getUserId() == null) {
                System.err.println("❌ TRAINER CREATION SKIPPED — userId is NULL | email=" + e.getEmail());
                return;
            }

            Trainer t = new Trainer();
            t.setUserId(e.getUserId());
            t.setEmail(e.getEmail());

            // ✅ SAFE NAME HANDLING
            String name = e.getDisplayName();
            if (name == null || name.isBlank()) {
                name = "Trainer-" + e.getUserId();
            }

            t.setName(name);          // ✅ THIS FIXES EVERYTHING
            t.setStatus("ACTIVE");

            trainerRepository.save(t);

            System.out.println("🆕 TRAINER CREATED | userId=" + e.getUserId());
        }



        System.out.println("🔁 ROLE UPDATED → " + e.getRole() + " | userId=" + e.getUserId());
    }

    // ================= DELETE =================
    private void handleDelete(UserEvent e) {

        studentRepository.findByEmail(e.getEmail())
                .ifPresent(s -> {
                    studentRepository.delete(s);
                    System.out.println("🧹 STUDENT DELETED | email=" + e.getEmail());
                });

        trainerRepository.findByEmail(e.getEmail())
                .ifPresent(t -> {
                    trainerRepository.delete(t);
                    System.out.println("🧹 TRAINER DELETED | email=" + e.getEmail());
                });
    }
}
