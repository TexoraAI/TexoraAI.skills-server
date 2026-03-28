//package com.lms.course.controller;
//
//import com.lms.course.dto.FeaturedCourseDTO;
//import com.lms.course.model.FeaturedCourse;
//import com.lms.course.service.FeaturedCourseService;
//
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/featured-courses")
//public class FeaturedCourseController {
//
//    private final FeaturedCourseService service;
//
//    public FeaturedCourseController(FeaturedCourseService service) {
//        this.service = service;
//    }
//
//    // CREATE
//    @PostMapping("/upload")
//    public FeaturedCourse create(@RequestBody FeaturedCourseDTO dto) {
//        return service.create(dto);
//    }
//
//    // GET ALL
//    @GetMapping
//    public List<FeaturedCourse> getAll() {
//        return service.getAll();
//    }
//
//    // DELETE
//    @DeleteMapping("/{id}")
//    public String delete(@PathVariable Long id) {
//        service.delete(id);
//        return "Featured course deleted successfully";
//    }
//}


package com.lms.course.controller;

import com.lms.course.dto.FeaturedCourseDTO;
import com.lms.course.model.FeaturedCourse;
import com.lms.course.service.FeaturedCourseService;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/api/featured-courses")
public class FeaturedCourseController {

    private final FeaturedCourseService service;

    private static final String UPLOAD_DIR =
            System.getProperty("user.dir") + "/uploads/featured-courses/";

    public FeaturedCourseController(FeaturedCourseService service) {
        this.service = service;
    }

    // ✅ CREATE (MULTIPART)
//    @PostMapping("/upload")
//    public FeaturedCourse create(
//            @ModelAttribute FeaturedCourseDTO dto,
//            @RequestPart("thumbnail") MultipartFile file
//    ) throws Exception {
//
//        return service.create(dto, file);
//    }
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public FeaturedCourse create(
            @RequestParam("title") String title,
            @RequestParam("onDemand") boolean onDemand,
            @RequestParam("featured") boolean featured,
            @RequestParam("level") String level,
            @RequestParam("duration") String duration,
            @RequestParam("rating") String rating,
            @RequestParam("students") String students,
            @RequestParam("tags") String tags,
            @RequestParam("instructors") String instructors,
            @RequestPart(value = "thumbnail", required = false) MultipartFile file
    ) throws Exception {

        FeaturedCourseDTO dto = new FeaturedCourseDTO();

        dto.setTitle(title);
        dto.setOnDemand(onDemand);
        dto.setFeatured(featured);
        dto.setLevel(level);
        dto.setDuration(duration);

        // 🔥 FIX parsing
        if (rating != null && !rating.isEmpty()) {
            dto.setRating(Double.parseDouble(rating));
        }

        dto.setStudents(students);
        dto.setTags(tags);
        dto.setInstructors(instructors);

        return service.create(dto, file);
    }

    // GET ALL
    @GetMapping
    public List<FeaturedCourse> getAll() {
        return service.getAll();
    }

    // DELETE
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        service.delete(id);
        return "Featured course deleted successfully";
    }

    // ✅ IMAGE VIEW API
    @GetMapping("/image/{fileName:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String fileName) {

        File file = new File(UPLOAD_DIR + fileName);

        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);

        return ResponseEntity.ok().body(resource);
    }
 // UPDATE
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public FeaturedCourse update(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("onDemand") boolean onDemand,
            @RequestParam("featured") boolean featured,
            @RequestParam("level") String level,
            @RequestParam("duration") String duration,
            @RequestParam("rating") String rating,
            @RequestParam("students") String students,
            @RequestParam("tags") String tags,
            @RequestParam("instructors") String instructors,
            @RequestPart(value = "thumbnail", required = false) MultipartFile file
    ) throws Exception {

        FeaturedCourseDTO dto = new FeaturedCourseDTO();
        dto.setTitle(title);
        dto.setOnDemand(onDemand);
        dto.setFeatured(featured);
        dto.setLevel(level);
        dto.setDuration(duration);

        if (rating != null && !rating.isEmpty()) {
            dto.setRating(Double.parseDouble(rating));
        }

        dto.setStudents(students);
        dto.setTags(tags);
        dto.setInstructors(instructors);

        return service.update(id, dto, file);
    }
}