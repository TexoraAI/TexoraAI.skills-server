package com.lms.file.repository;

import com.lms.file.model.FileResource;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface FileRepository extends JpaRepository<FileResource, Long> {
	void deleteAllByBatchId(Long batchId);
	List<FileResource> findByTrainerEmail(String trainerEmail);
	List<FileResource> findByBatchId(Long batchId);
	List<FileResource> findByBatchIdAndTrainerEmail(Long batchId, String trainerEmail);
}

