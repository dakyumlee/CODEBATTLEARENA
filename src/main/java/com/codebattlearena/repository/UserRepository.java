package com.codebattlearena.repository;

import com.codebattlearena.model.User;
import com.codebattlearena.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    
    List<User> findByRole(UserRole role);
    
    long countByRole(UserRole role);
    
    long countByRoleAndOnlineStatus(UserRole role, Boolean onlineStatus);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.lastActivity > :date")
    long countByLastActivityAfter(@Param("date") LocalDateTime date);
    
    List<User> findByRoleAndOnlineStatus(UserRole role, Boolean onlineStatus);
}
