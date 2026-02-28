


package com.lms.student.service;

import com.lms.student.dto.TrainerResponse;
import com.lms.student.model.Trainer;
import com.lms.student.repo.TrainerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrainerService {

    private final TrainerRepository repo;

    public TrainerService(TrainerRepository repo) {
        this.repo = repo;
    }

    // -------------------------------------------------
    // ❌ BLOCK MANUAL CREATE (Controller still calls this)
    // -------------------------------------------------
    public TrainerResponse create(Object ignored) {
        throw new UnsupportedOperationException(
            "Trainer creation is managed by Auth Service via Kafka only"
        );
    }

    // -------------------------------------------------
    // LIST (Admin UI)
    // -------------------------------------------------
    public List<TrainerResponse> list() {
        return repo.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // -------------------------------------------------
    // UPDATE STATUS (Admin)
    // -------------------------------------------------
    public TrainerResponse updateStatus(Long id, String status) {

        Trainer trainer = repo.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Trainer not found"));

        trainer.setStatus(status);
        Trainer saved = repo.save(trainer);

        return toResponse(saved);
    }

    // -------------------------------------------------
    // DELETE (Admin)
    // -------------------------------------------------
    public void delete(Long id) {
        repo.deleteById(id);
    }

    // -------------------------------------------------
    // ✅ CREATE FROM AUTH EVENT (Kafka ONLY)
    // -------------------------------------------------
    public void createFromAuthEvent(
            Long authUserId,
            String email,
            String name,
            String role
    ) {

        // only TRAINER role
        if (!"TRAINER".equals(role)) {
            return;
        }

        if (repo.existsByUserId(authUserId)) {
            return;
        }

        Trainer trainer = new Trainer();
        trainer.setUserId(authUserId);   // 🔑 REQUIRED
        trainer.setEmail(email);
        trainer.setName(name);
        trainer.setStatus("ACTIVE");

        repo.save(trainer);

        System.out.println(
            "✅ Trainer auto-created via Kafka for authUserId=" + authUserId
        );
    }

    // -------------------------------------------------
    // MAPPER
    // -------------------------------------------------
    private TrainerResponse toResponse(Trainer t) {

        TrainerResponse r = new TrainerResponse();
        r.setId(t.getId());
        r.setUserId(t.getUserId());   // authUserId
        r.setName(t.getName());
        r.setEmail(t.getEmail());
        r.setExpertise(t.getExpertise());
        r.setStatus(t.getStatus());
        r.setCreatedAt(t.getCreatedAt());

        return r;
    }
}
