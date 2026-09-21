//// NotebookRepository.java
//package com.lms.chat.repository;
//
//import com.lms.chat.entity.Notebook;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//public interface NotebookRepository extends JpaRepository<Notebook, Long> {
//    List<Notebook> findByStudentEmailOrderByCreatedAtAsc(String studentEmail);
//    Optional<Notebook> findByIdAndStudentEmail(Long id, String studentEmail);
//}

// NotebookRepository.java
package com.lms.chat.repository;

import com.lms.chat.entity.Notebook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotebookRepository extends JpaRepository<Notebook, Long> {
    List<Notebook> findByStudentEmailOrderByCreatedAtAsc(String studentEmail);
    Optional<Notebook> findByIdAndStudentEmail(Long id, String studentEmail);

    // Notebooks shared with a given email via NotebookCollaborator, without
    // needing a mapped relationship on Notebook itself.
    @Query("SELECT n FROM Notebook n WHERE EXISTS (" +
           "SELECT 1 FROM NotebookCollaborator c WHERE c.notebook = n AND c.email = :email" +
           ") ORDER BY n.createdAt ASC")
    List<Notebook> findNotebooksSharedWithEmail(@Param("email") String email);
}