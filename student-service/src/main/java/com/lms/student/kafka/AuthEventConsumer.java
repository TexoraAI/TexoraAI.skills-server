package com.lms.student.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.student.event.AuthEvent;
import com.lms.student.model.Student;
import com.lms.student.model.Trainer;
import com.lms.student.repo.StudentRepository;
import com.lms.student.repo.TrainerRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AuthEventConsumer {

    private final StudentRepository studentRepository;
    private final TrainerRepository trainerRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    public AuthEventConsumer(
            StudentRepository studentRepository,
            TrainerRepository trainerRepository
    ) {
        this.studentRepository = studentRepository;
        this.trainerRepository = trainerRepository;
    }

    @KafkaListener(
            topics = "auth-events",
            groupId = "student-service-group"
    )
    public void consume(String message) {

        try {
            AuthEvent event = mapper.readValue(message, AuthEvent.class);

            // ✅ only when admin approved user
            if (!"USER_CREATED".equals(event.getEventType())) return;

            // ✅ must have auth userId
            if (event.getUserId() == null) return;

            // ---------- STUDENT ----------
            if ("STUDENT".equals(event.getRole())) {

                if (studentRepository.existsByUserId(event.getUserId())) return;

                Student s = new Student();
                s.setUserId(event.getUserId()); // 🔑 AUTH USER ID
                s.setEmail(event.getEmail());
                s.setStatus("ACTIVE");

                studentRepository.save(s);

                System.out.println(
                        "✅ STUDENT CREATED | userId=" + event.getUserId()
                );
            }

            // ---------- TRAINER ----------
            if ("TRAINER".equals(event.getRole())) {

                if (trainerRepository.existsByUserId(event.getUserId())) return;

                Trainer t = new Trainer();
                t.setUserId(event.getUserId());
                t.setEmail(event.getEmail());
                t.setName(event.getDisplayName()); 
                t.setStatus("ACTIVE");

                trainerRepository.save(t);

                System.out.println(
                        "✅ TRAINER CREATED | userId=" + event.getUserId()
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
