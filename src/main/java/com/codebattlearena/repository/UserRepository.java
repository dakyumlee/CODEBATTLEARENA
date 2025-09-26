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
    List<User> findByRole(UserRole role);
    
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.groupId = :groupId")
    List<User> findByRoleAndGroupId(@Param("role") String role, @Param("groupId") Long groupId);
    
    long countByRole(UserRole role);
    long countByOnlineStatus(boolean onlineStatus);
    void deleteByEmail(String email);
}
