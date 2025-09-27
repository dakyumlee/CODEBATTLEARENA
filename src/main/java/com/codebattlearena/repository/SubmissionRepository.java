package com.codebattlearena.repository;

import com.codebattlearena.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByUserIdOrderBySubmittedAtDesc(Long userId);
    
    List<Submission> findByProblemIdOrderBySubmittedAtDesc(Long problemId);
    
    @Query("SELECT s FROM Submission s JOIN Problem p ON s.problemId = p.id WHERE p.teacherId = :teacherId AND s.status = 'PENDING' ORDER BY s.submittedAt DESC")
    List<Submission> findPendingSubmissionsByTeacher(@Param("teacherId") Long teacherId);
    
    boolean existsByUserIdAndProblemId(Long userId, Long problemId);
    
    @Query("SELECT s FROM Submission s JOIN Problem p ON s.problemId = p.id WHERE p.teacherId = :teacherId ORDER BY s.submittedAt DESC")
    List<Submission> findSubmissionsByTeacher(@Param("teacherId") Long teacherId);
}
