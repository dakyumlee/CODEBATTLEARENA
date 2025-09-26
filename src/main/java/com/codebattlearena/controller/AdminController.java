package com.codebattlearena.controller;

import com.codebattlearena.model.User;
import com.codebattlearena.model.UserRole;
import com.codebattlearena.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "사용자 목록을 불러올 수 없습니다."));
        }
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
        try {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                return ResponseEntity.status(400).body("이미 존재하는 이메일입니다.");
            }

            User user = new User();
            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setPassword(request.getPassword());
            user.setRole(UserRole.valueOf(request.getRole()));
            user.setCreatedAt(LocalDateTime.now());
            user.setOnlineStatus(false);

            userRepository.save(user);
            return ResponseEntity.ok(Map.of("message", "사용자가 생성되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("사용자 생성 오류: " + e.getMessage());
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            if (!userRepository.existsById(id)) {
                return ResponseEntity.status(404).body(Map.of("error", "사용자를 찾을 수 없습니다."));
            }
            
            userRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "사용자가 삭제되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "사용자 삭제에 실패했습니다."));
        }
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestBody RoleUpdateRequest request) {
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("error", "사용자를 찾을 수 없습니다."));
            }
            
            User user = userOpt.get();
            user.setRole(UserRole.valueOf(request.getRole()));
            userRepository.save(user);
            
            return ResponseEntity.ok(Map.of("message", "역할이 업데이트되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "역할 업데이트에 실패했습니다."));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getSystemStats() {
        try {
            long totalUsers = userRepository.count();
            long studentCount = userRepository.countByRole(UserRole.STUDENT);
            long teacherCount = userRepository.countByRole(UserRole.TEACHER);
            long onlineCount = userRepository.countByOnlineStatus(true);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", totalUsers);
            stats.put("studentCount", studentCount);
            stats.put("teacherCount", teacherCount);
            stats.put("onlineCount", onlineCount);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "통계를 불러올 수 없습니다."));
        }
    }

    public static class CreateUserRequest {
        private String name;
        private String email;
        private String password;
        private String role;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

    public static class RoleUpdateRequest {
        private String role;

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }
}
