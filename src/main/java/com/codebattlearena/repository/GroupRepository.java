package com.codebattlearena.repository;

import com.codebattlearena.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    List<Group> findByTeacherId(Long teacherId);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.groupId = :groupId AND u.role = 'STUDENT'")
    Long countStudentsByGroupId(@Param("groupId") Long groupId);
}
