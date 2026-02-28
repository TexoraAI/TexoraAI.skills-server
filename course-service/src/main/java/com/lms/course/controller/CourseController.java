package com.lms.course.controller;

import com.lms.course.model.Course;
import com.lms.course.service.CourseService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService service;

    public CourseController(CourseService service) {
        this.service = service;
    }

    // 🔐 CREATE COURSE (JWT REQUIRED)
    @PostMapping
    public Course create(@RequestBody Course course, Authentication auth) {

        if (auth == null || auth.getName() == null) {
            throw new RuntimeException("Unauthorized");
        }

        return service.create(course, auth.getName());
    }

    // 🔐 COURSES OF LOGGED-IN USER
    @GetMapping("/my")
    public List<Course> myCourses(Authentication auth) {

        if (auth == null || auth.getName() == null) {
            throw new RuntimeException("Unauthorized");
        }

        return service.getTrainerCourses(auth.getName());
    }



//    @GetMapping("/my")
//    public List<Course> trainerCourses(Authentication auth) {
//
//        if (auth == null)
//            throw new RuntimeException("Unauthorized");
//
//        return service.getTrainerCourses(auth.getName());
//    }
    
    @GetMapping("/student")
    public List<Course> studentCourses(Authentication auth) {

        if (auth == null)
            throw new RuntimeException("Unauthorized");

        return service.getStudentCourses(auth.getName());
    }
    
    // 🔓 GET BY ID
//    @GetMapping("/{id}")
//    public Course getById(@PathVariable Long id) {
//        return service.getById(id);
//    }
    @GetMapping("/{id}")
    public Course getById(@PathVariable Long id, Authentication auth) {

        return service.getById(
                id,
                auth.getName(),
                auth.getAuthorities().iterator().next().getAuthority()
        );
    }

    // 🔐 UPDATE COURSE
    @PutMapping("/{id}")
    public Course update(@PathVariable Long id,
                         @RequestBody Course updated) {
        return service.update(id, updated);
    }

    // 🔐 DELETE COURSE
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        return service.delete(id);
    }
    
    
 // ============================
 // 🔐 ADMIN - GET ALL COURSES
 // ============================
    @PreAuthorize("hasRole('ADMIN')")
 @GetMapping("/admin")
 public List<Course> getAllCourses(Authentication auth) {

     if (auth == null)
         throw new RuntimeException("Unauthorized");

     // Optional role check
     boolean isAdmin = auth.getAuthorities().stream()
    	        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
     System.out.println("Authorities: " + auth.getAuthorities());
     if (!isAdmin) {
         throw new RuntimeException("Access Denied - Admin Only");
     }

     return service.getAllCoursesForAdmin();
 }
 @GetMapping("/admin/category/{category}")
 public List<Course> getByCategory(
         @PathVariable String category,
         Authentication auth) {

     if (auth == null) {
         throw new RuntimeException("Unauthorized");
     }

     boolean isAdmin = auth.getAuthorities().stream()
             .anyMatch(a -> a.getAuthority().equals("ADMIN"));

     if (!isAdmin) {
         throw new RuntimeException("Access Denied - Admin Only");
     }

     return service.getByCategory(category);
 }
 
}
