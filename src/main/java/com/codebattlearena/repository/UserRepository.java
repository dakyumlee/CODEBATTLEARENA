package com.codebattlearena.repository;

import com.codebattlearena.model.User;
import com.codebattlearena.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    void deleteByEmail(String email);
    
    long countByRole(UserRole role);
    
    long countByOnlineStatus(boolean onlineStatus);
    
    @Query("SELECT u FROM User u WHERE u.role = 'STUDENT'")
    List<User> findAllStudents();
    
    List<User> findByGroupId(Long groupId);
    
    List<User> findByGroupIdAndRole(Long groupId, String role);
    
    @Query("SELECT u FROM User u WHERE u.groupId = :groupId AND u.role = 'STUDENT'")
    List<User> findStudentsByGroupId(@Param("groupId") Long groupId);
}

    long countByRole(UserRole role);
    long countByOnlineStatusTrue();
    List<User> findAllByOrderByCreatedAtDesc();
    List<User> findByRoleAndGroupIdIsNull(UserRole role);
