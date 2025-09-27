package com.codebattlearena.repository;

import com.codebattlearena.model.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {
    List<Material> findByTeacherIdOrderByCreatedAtDesc(Long teacherId);
    List<Material> findByGroupIdOrderByCreatedAtDesc(Long groupId);
}
