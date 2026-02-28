package com.lms.file.repository;

import com.lms.file.model.CertificateFile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface CertificateFileRepository
        extends JpaRepository<CertificateFile, Long> {
	List<CertificateFile> findByStudentEmail(String studentEmail);
}
