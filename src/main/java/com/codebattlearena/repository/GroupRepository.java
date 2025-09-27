package com.codebattlearena.repository;

import com.codebattlearena.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    
    List<Group> findByTeacherId(Long teacherId);
    
    List<Group> findByTeacherIdOrderByCreatedAtDesc(Long teacherId);
}
