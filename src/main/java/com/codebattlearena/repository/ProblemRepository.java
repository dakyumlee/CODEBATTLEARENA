package com.codebattlearena.repository;

import com.codebattlearena.model.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {
    
    List<Problem> findByCreatorTypeOrderByCreatedAtDesc(String creatorType);
    
    List<Problem> findByCreatorIdOrderByCreatedAtDesc(Long creatorId);
    
    List<Problem> findByTypeOrderByCreatedAtDesc(String type);
    
    @Query("SELECT p FROM Problem p WHERE p.creatorType = 'TEACHER' ORDER BY p.createdAt DESC")
    List<Problem> findAllTeacherProblems();
}
