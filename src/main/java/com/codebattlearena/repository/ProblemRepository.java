package com.codebattlearena.repository;

import com.codebattlearena.model.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {
    List<Problem> findByDifficulty(String difficulty);
    List<Problem> findByCreatorType(String creatorType);
    List<Problem> findByCreatorId(Long creatorId);
    
    @Query("SELECT p FROM Problem p WHERE p.creatorType = 'AI' ORDER BY p.createdAt DESC")
    List<Problem> findAIProblems();
    
    @Query("SELECT p FROM Problem p WHERE p.difficulty = :difficulty ORDER BY RANDOM() LIMIT 1")
    Problem findRandomByDifficulty(@Param("difficulty") String difficulty);
}
