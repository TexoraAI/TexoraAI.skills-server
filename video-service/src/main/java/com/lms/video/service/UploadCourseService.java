package com.lms.video.service;

import com.lms.video.dto.UploadCourseDTO;
import com.lms.video.model.UploadCourse;
import com.lms.video.repository.UploadCourseRepository;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class UploadCourseService {

    private final UploadCourseRepository repo;

    private static final String VIDEO_DIR =
            System.getProperty("user.dir") + "/videos/course-content/";

    public UploadCourseService(UploadCourseRepository repo) {
        this.repo = repo;
    }

    public UploadCourse upload(
            MultipartFile video,
            MultipartFile thumbnail,
            UploadCourseDTO dto
    ) throws IOException {

        File dir = new File(VIDEO_DIR);
        if (!dir.exists()) dir.mkdirs();

        String fileName = System.currentTimeMillis() + "_" + video.getOriginalFilename();
        String filePath = VIDEO_DIR + fileName;

        video.transferTo(new File(filePath));

        String thumbName = null;
        if (thumbnail != null) {
            thumbName = System.currentTimeMillis() + "_" + thumbnail.getOriginalFilename();
            thumbnail.transferTo(new File(VIDEO_DIR + thumbName));
        }

        UploadCourse entity = new UploadCourse();

        entity.setCourseId(dto.getCourseId());
        entity.setTitle(dto.getTitle());

        entity.setFileName(fileName);
        entity.setFilePath(filePath);
        entity.setThumbnail(thumbName);

        entity.setInstructorName(dto.getInstructorName());
        entity.setInstructorRole(dto.getInstructorRole());
        entity.setExperience(dto.getExperience());
        entity.setStudentCount(dto.getStudentCount());

        entity.setDescription(dto.getDescription());
        entity.setLearnPoints(dto.getLearnPoints());

        entity.setPublishDate(dto.getPublishDate());
        entity.setLearnersCount(dto.getLearnersCount());

        entity.setShowInstructorLive(dto.isShowInstructorLive());

        return repo.save(entity);
    }

    public void deleteByCourseId(Long courseId) {

        List<UploadCourse> list = repo.findByCourseId(courseId);

        for (UploadCourse v : list) {

            File file = new File(v.getFilePath());
            if (file.exists()) file.delete();

            repo.delete(v);
        }
    }
    public List<UploadCourse> getAll() {
        return repo.findAll();
    }
}