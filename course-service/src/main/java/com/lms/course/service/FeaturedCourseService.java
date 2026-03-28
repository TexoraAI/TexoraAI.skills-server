//package com.lms.course.service;
//
//import com.lms.course.dto.FeaturedCourseDTO;
//import com.lms.course.model.FeaturedCourse;
//import com.lms.course.repository.FeaturedCourseRepository;
//
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//public class FeaturedCourseService {
//
//    private final FeaturedCourseRepository repo;
//
//    public FeaturedCourseService(FeaturedCourseRepository repo) {
//        this.repo = repo;
//    }
//
//    // CREATE
//    public FeaturedCourse create(FeaturedCourseDTO dto) {
//
//        FeaturedCourse course = new FeaturedCourse();
//
//        course.setTitle(dto.getTitle());
//        course.setOnDemand(dto.isOnDemand());
//        course.setFeatured(dto.isFeatured());
//        course.setThumbnail(dto.getThumbnail());
//
//        course.setLevel(dto.getLevel());
//        course.setDuration(dto.getDuration());
//        course.setRating(dto.getRating());
//        course.setStudents(dto.getStudents());
//
//        course.setTags(dto.getTags());
//        course.setInstructors(dto.getInstructors());
//
//        return repo.save(course);
//    }
//
//    // GET ALL
//    public List<FeaturedCourse> getAll() {
//        return repo.findAll();
//    }
//
//    // DELETE
//    public void delete(Long id) {
//        repo.deleteById(id);
//    }
//}
package com.lms.course.service;

import com.lms.course.dto.FeaturedCourseDTO;
import com.lms.course.model.FeaturedCourse;
import com.lms.course.repository.FeaturedCourseRepository;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class FeaturedCourseService {

    private final FeaturedCourseRepository repo;

    private static final String UPLOAD_DIR =
            System.getProperty("user.dir") + "/uploads/featured-courses/";

    public FeaturedCourseService(FeaturedCourseRepository repo) {
        this.repo = repo;
    }

    // ✅ CREATE WITH IMAGE
    public FeaturedCourse create(FeaturedCourseDTO dto, MultipartFile file) throws IOException {

        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String fileName = null;

        if (file != null && !file.isEmpty()) {

            fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

            File dest = new File(UPLOAD_DIR + fileName);
            file.transferTo(dest);
        }

        FeaturedCourse course = new FeaturedCourse();

        course.setTitle(dto.getTitle());
        course.setOnDemand(dto.isOnDemand());
        course.setFeatured(dto.isFeatured());
        course.setLevel(dto.getLevel());
        course.setDuration(dto.getDuration());
        course.setRating(dto.getRating());
        course.setStudents(dto.getStudents());
        course.setTags(dto.getTags());
        course.setInstructors(dto.getInstructors());

        // 🔥 SAVE FILE NAME ONLY
        course.setThumbnail(fileName);

        return repo.save(course);
    }

    // GET ALL
    public List<FeaturedCourse> getAll() {
        return repo.findAll();
    }

    // DELETE
    public void delete(Long id) {
        repo.deleteById(id);
    }
    public FeaturedCourse update(Long id, FeaturedCourseDTO dto, MultipartFile file) throws Exception {

        // 1. Find existing course or throw
        FeaturedCourse existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Featured course not found with id: " + id));

        // 2. Update all scalar fields
        existing.setTitle(dto.getTitle());
        existing.setOnDemand(dto.isOnDemand());
        existing.setFeatured(dto.isFeatured());
        existing.setLevel(dto.getLevel());
        existing.setDuration(dto.getDuration());
        existing.setRating(dto.getRating());
        existing.setStudents(dto.getStudents());
        existing.setTags(dto.getTags());
        existing.setInstructors(dto.getInstructors());

        // 3. If a new thumbnail was uploaded, replace the old file
        if (file != null && !file.isEmpty()) {

            // Delete old thumbnail file from disk if it exists
            if (existing.getThumbnail() != null && !existing.getThumbnail().isEmpty()) {
                String oldFileName = existing.getThumbnail()
                        .substring(existing.getThumbnail().lastIndexOf("/") + 1);
                File oldFile = new File(UPLOAD_DIR + oldFileName);
                if (oldFile.exists()) {
                    oldFile.delete();
                }
            }

            // Save new thumbnail file
            String newFileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            File dest = new File(UPLOAD_DIR + newFileName);
            dest.getParentFile().mkdirs();
            file.transferTo(dest);

            // Build the public URL the same way your create() does
            existing.setThumbnail("/api/featured-courses/image/" + newFileName);
        }
        // If no new file uploaded → keep existing thumbnail unchanged

        // 4. Save and return
        return repo.save(existing);
    }
}