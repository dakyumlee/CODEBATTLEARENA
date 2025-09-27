package com.codebattlearena.repository;

import com.codebattlearena.model.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {
    List<Problem> findByTeacherIdOrderByCreatedAtDesc(Long teacherId);
    Long countByTeacherId(Long teacherId);
}