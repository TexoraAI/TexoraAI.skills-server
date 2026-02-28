//package com.lms.student.controller;
//
//import com.lms.student.dto.CreateTrainerRequest;
//import com.lms.student.dto.TrainerResponse;
//import com.lms.student.service.TrainerService;
//import jakarta.validation.Valid;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/trainers")
//public class TrainerController {
//
//    private final TrainerService service;
//
//    public TrainerController(TrainerService service) {
//        this.service = service;
//    }
//
//    @PostMapping
//    public TrainerResponse create(@Valid @RequestBody CreateTrainerRequest req) {
//        return service.create(req);
//    }
//
//    @GetMapping
//    public List<TrainerResponse> list() {
//        return service.list();
//    }
//
//    @PutMapping("/{id}/status")
//    public TrainerResponse status(
//            @PathVariable Long id,
//            @RequestParam String status) {
//        return service.updateStatus(id, status);
//    }
//
//    @DeleteMapping("/{id}")
//    public void delete(@PathVariable Long id) {
//        service.delete(id);
//    }
//}







package com.lms.student.controller;

import com.lms.student.dto.CreateTrainerRequest;
import com.lms.student.dto.TrainerResponse;
import com.lms.student.service.TrainerService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trainers")
public class TrainerController {

    private final TrainerService service;

    public TrainerController(TrainerService service) {
        this.service = service;
    }

    @PostMapping
    public TrainerResponse create(@Valid @RequestBody CreateTrainerRequest req) {
        return service.create(req); // ✅ blocked internally
    }

    @GetMapping
    public List<TrainerResponse> list() {
        return service.list();
    }

    @PutMapping("/{id}/status")
    public TrainerResponse status(
            @PathVariable Long id,
            @RequestParam String status) {
        return service.updateStatus(id, status);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
