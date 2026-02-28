




package com.lms.student.service;

import com.lms.student.dto.CreateStudentRequest;
import com.lms.student.dto.StudentResponse;
import com.lms.student.model.Student;
import com.lms.student.repo.StudentRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class StudentService {

    private final StudentRepository repo;

    public StudentService(StudentRepository repo) {
        this.repo = repo;
    }

    // -------------------------------------------------
    // CREATE (Admin / UI only — NO Kafka here)
    // -------------------------------------------------
    public StudentResponse create(CreateStudentRequest req) {

        if (repo.existsByUserId(req.getUserId())) {
            throw new IllegalStateException("Student already exists");
        }

        Student s = new Student();
        s.setUserId(req.getUserId());     // authUserId
        s.setEmail(req.getEmail());
        s.setStatus("ACTIVE");

        Student saved = repo.save(s);
        return map(saved);
    }

    // -------------------------------------------------
    // LIST
    // -------------------------------------------------
    public List<StudentResponse> list() {
        return repo.findAll()
                .stream()
                .map(this::map)
                .toList();
    }

    // -------------------------------------------------
    // FIND BY AUTH USER
    // -------------------------------------------------
    public StudentResponse byUser(Long userId) {
        return repo.findByUserId(userId)
                .map(this::map)
                .orElseThrow(() ->
                        new RuntimeException("Student not found"));
    }

    // -------------------------------------------------
    // UPDATE STATUS
    // -------------------------------------------------
    public StudentResponse updateStatus(Long id, String status) {

        Student s = repo.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Student not found"));

        s.setStatus(status);
        return map(repo.save(s));
    }

    // -------------------------------------------------
    // TOUCH ACTIVITY
    // -------------------------------------------------
    public void touch(Long id) {

        Student s = repo.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Student not found"));

        s.setLastActiveAt(Instant.now());
        repo.save(s);
    }

    // -------------------------------------------------
    // DELETE
    // -------------------------------------------------
    public void delete(Long id) {
        repo.deleteById(id);
    }

    // -------------------------------------------------
    // 🔥 AUTO-CREATE FROM AUTH (Kafka Consumer calls this)
    // -------------------------------------------------
    public void createFromAuth(Long authUserId, String email) {

        if (repo.existsByUserId(authUserId)) {
            return;
        }

        Student student = new Student();
        student.setUserId(authUserId);
        student.setEmail(email);
        student.setStatus("ACTIVE");

        repo.save(student);

        System.out.println(
                "✅ Student auto-created via Kafka for authUserId=" + authUserId
        );
    }

    // -------------------------------------------------
    // MAPPER
    // -------------------------------------------------
    private StudentResponse map(Student s) {

        StudentResponse r = new StudentResponse();
        r.setId(s.getId());
        r.setUserId(s.getUserId());
        r.setEmail(s.getEmail());
        r.setStatus(s.getStatus());
        r.setJoinedAt(s.getJoinedAt());
        r.setLastActiveAt(s.getLastActiveAt());

        return r;
    }
}
