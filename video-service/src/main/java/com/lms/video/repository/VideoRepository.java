//package com.lms.video.repository;
//
//import com.lms.video.model.Video;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//public interface VideoRepository extends JpaRepository<Video, Long> {
//}

package com.lms.video.repository;
import java.util.List;
import com.lms.video.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoRepository extends JpaRepository<Video, Long> {
	List<Video> findByBatchId(Long batchId);
	void deleteByBatchId(Long batchId);
	List<Video> findByBatchIdIn(List<Long> batchIds);
	List<Video> findByUploadedBy(String uploadedBy);

}
