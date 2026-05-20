package com.lms.live_session.controller;
 
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.security.Principal;
import java.util.List;
 
@RestController
@RequestMapping("/api/v1/ai-companion/resources")
public class AiResourceController {
 
    // GET /api/v1/ai-companion/resources/meetings
    @GetMapping("/meetings")
    public ResponseEntity<?> getMeetings(Principal principal) {
        // TODO: return all sessions accessible by the user
        return ResponseEntity.ok(List.of());
    }
 
    // GET /api/v1/ai-companion/resources/docs
    @GetMapping("/docs")
    public ResponseEntity<?> getDocs(Principal principal) {
        // TODO: return uploaded course documents
        return ResponseEntity.ok(List.of());
    }
 
    // GET /api/v1/ai-companion/resources/chat
    @GetMapping("/chat")
    public ResponseEntity<?> getChatHistory(@RequestParam(required=false) Long sessionId, Principal principal) {
        // TODO: return session chat messages
        return ResponseEntity.ok(List.of());
    }
 
    // GET /api/v1/ai-companion/resources/whiteboard
    @GetMapping("/whiteboard")
    public ResponseEntity<?> getWhiteboard(@RequestParam(required=false) Long sessionId, Principal principal) {
        // TODO: return whiteboard snapshot data
        return ResponseEntity.ok(List.of());
    }
 
    // GET /api/v1/ai-companion/resources/recordings
    @GetMapping("/recordings")
    public ResponseEntity<?> getRecordings(@RequestParam(required=false) Long sessionId, Principal principal) {
        // TODO: return session recordings
        return ResponseEntity.ok(List.of());
    }
 
    // POST /api/v1/ai-companion/resources/upload
    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file, Principal principal) {
        // TODO: upload to S3, extract text, save AiUploadedResource
        return ResponseEntity.ok(java.util.Map.of("message", "TODO: upload implementation pending"));
    }
}