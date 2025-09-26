package com.codebattlearena.repository;

import com.codebattlearena.model.User;
import com.codebattlearena.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByRole(UserRole role);
    List<User> findByGroupId(Long groupId);
    List<User> findByOnlineStatusTrue();
    boolean existsByEmail(String email);
}
