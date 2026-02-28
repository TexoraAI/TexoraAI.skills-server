//package com.lms.course.kafka;
//
//import com.lms.course.model.Course;
//import com.lms.course.repository.CourseRepository;
//import com.lms.course.repository.ContentRepository;
//import com.lms.course.repository.StudentBatchMapRepository;
//import com.lms.course.repository.TrainerBatchMapRepository;
//
//import jakarta.transaction.Transactional;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Map;
//
//@Service
//public class BatchLifecycleConsumer {
//
//    private final CourseRepository courseRepo;
//    private final ContentRepository contentRepo;
//    private final StudentBatchMapRepository studentRepo;
//    private final TrainerBatchMapRepository trainerRepo;
//
//    public BatchLifecycleConsumer(
//            CourseRepository courseRepo,
//            ContentRepository contentRepo,
//            StudentBatchMapRepository studentRepo,
//            TrainerBatchMapRepository trainerRepo) {
//
//        this.courseRepo = courseRepo;
//        this.contentRepo = contentRepo;
//        this.studentRepo = studentRepo;
//        this.trainerRepo = trainerRepo;
//    }
//
//    @Transactional
//    @KafkaListener(
//            topics = "batch-lifecycle",
//            groupId = "course-service-group"
//    )
//    public void consume(Map<String, Object> event) {
//
//        String type = (String) event.get("type");
//
//        if ("BATCH_DELETED".equals(type)) {
//
//            Long batchId =
//                    ((Number) event.get("batchId")).longValue();
//
//            System.out.println("🔥 COURSE SERVICE CLEANUP -> batch="
//                    + batchId);
//
//            // 1️⃣ Get all courses of that batch
//            List<Course> courses =
//                    courseRepo.findByBatchId(batchId);
//
//            // 2️⃣ Delete all content of those courses
//            for (Course course : courses) {
//                contentRepo.deleteAll(
//                        contentRepo.findByCourseId(
//                                course.getId()
//                        )
//                );
//            }
//
//            // 3️⃣ Delete courses
//            courseRepo.deleteByBatchId(batchId);
//
//            // 4️⃣ Delete mappings
//            studentRepo.deleteByBatchId(batchId);
//            trainerRepo.deleteByBatchId(batchId);
//        }
//    }
//}
package com.lms.course.kafka;

import com.lms.course.model.Course;
import com.lms.course.repository.CourseRepository;
import com.lms.course.repository.ContentRepository;
import com.lms.course.repository.StudentBatchMapRepository;
import com.lms.course.repository.TrainerBatchMapRepository;

import jakarta.transaction.Transactional;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class BatchLifecycleConsumer {

    private final CourseRepository courseRepo;
    private final ContentRepository contentRepo;
    private final StudentBatchMapRepository studentRepo;
    private final TrainerBatchMapRepository trainerRepo;
    private final CourseEventProducer courseEventProducer; // 🔥 NEW

    public BatchLifecycleConsumer(
            CourseRepository courseRepo,
            ContentRepository contentRepo,
            StudentBatchMapRepository studentRepo,
            TrainerBatchMapRepository trainerRepo,
            CourseEventProducer courseEventProducer) { // 🔥 NEW

        this.courseRepo = courseRepo;
        this.contentRepo = contentRepo;
        this.studentRepo = studentRepo;
        this.trainerRepo = trainerRepo;
        this.courseEventProducer = courseEventProducer; // 🔥 NEW
    }

    @Transactional
    @KafkaListener(
            topics = "batch-lifecycle",
            groupId = "course-service-group"
    )
    public void consume(Map<String, Object> event) {

        String type = (String) event.get("type");

        if ("BATCH_DELETED".equals(type)) {

            Long batchId =
                    ((Number) event.get("batchId")).longValue();

            System.out.println("🔥 COURSE SERVICE CLEANUP -> batch="
                    + batchId);

            // 1️⃣ Get all courses of that batch
            List<Course> courses =
                    courseRepo.findByBatchId(batchId);

            for (Course course : courses) {

                Long courseId = course.getId();

                // 🔥 2️⃣ Publish COURSE_DELETED event
                courseEventProducer.publishCourseDeleted(courseId);

                // 3️⃣ Delete all content (modules)
                contentRepo.deleteAll(
                        contentRepo.findByCourseId(courseId)
                );

                // 4️⃣ Delete course
                courseRepo.delete(course);
            }

            // 5️⃣ Delete mappings
            studentRepo.deleteByBatchId(batchId);
            trainerRepo.deleteByBatchId(batchId);
        }
    }
}