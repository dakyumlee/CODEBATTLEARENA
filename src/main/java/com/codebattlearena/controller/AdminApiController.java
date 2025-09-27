package com.codebattlearena.controller;

import com.codebattlearena.model.User;
import com.codebattlearena.model.UserRole;
import com.codebattlearena.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminApiController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/users")
    public Map<String, Object> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();
            
            Map<String, Object> response = new HashMap<>();
            response.put("users", users.stream().map(user -> {
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", user.getId());
                userData.put("name", user.getName());
                userData.put("email", user.getEmail());
                userData.put("role", user.getRole());
                userData.put("groupId", user.getGroupId());
                userData.put("online", user.isOnlineStatus());
                userData.put("lastActivity", user.getLastActivity());
                return userData;
            }).toList());
            
            return response;
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to load users: " + e.getMessage());
            return error;
        }
    }

    @PostMapping("/users")
    public Map<String, Object> createUser(@RequestBody Map<String, Object> userData) {
        try {
            User user = new User();
            user.setName((String) userData.get("name"));
            user.setEmail((String) userData.get("email"));
            user.setPassword("password123"); // 기본 비밀번호
            user.setRole((String) userData.get("role"));
            
            if (userData.get("groupId") != null) {
                user.setGroupId(Long.valueOf(userData.get("groupId").toString()));
            }
            
            User savedUser = userRepository.save(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "사용자가 생성되었습니다");
            response.put("user", Map.of(
                "id", savedUser.getId(),
                "name", savedUser.getName(),
                "email", savedUser.getEmail(),
                "role", savedUser.getRole()
            ));
            
            return response;
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "사용자 생성 실패: " + e.getMessage());
            return error;
        }
    }

    @DeleteMapping("/users/{id}")
    @Transactional
    public Map<String, Object> deleteUser(@PathVariable Long id) {
        try {
            if (userRepository.existsById(id)) {
                userRepository.deleteById(id);
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "사용자가 삭제되었습니다");
                return response;
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "사용자를 찾을 수 없습니다");
                return error;
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "삭제 실패: " + e.getMessage());
            return error;
        }
    }

    @PutMapping("/users/{id}/role")
    public Map<String, Object> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setRole(request.get("role"));
                userRepository.save(user);
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "역할이 변경되었습니다");
                return response;
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "사용자를 찾을 수 없습니다");
                return error;
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "역할 변경 실패: " + e.getMessage());
            return error;
        }
    }

    @GetMapping("/stats")
    public Map<String, Object> getSystemStats() {
        try {
            long totalUsers = userRepository.count();
            long studentCount = userRepository.findAllStudents().size();
            long teacherCount = totalUsers - studentCount - 1; // 관리자 1명 제외
            long onlineUsers = userRepository.findAll().stream()
                .mapToLong(user -> user.isOnlineStatus() ? 1 : 0)
                .sum();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", totalUsers);
            stats.put("studentCount", studentCount);
            stats.put("teacherCount", teacherCount);
            stats.put("onlineUsers", onlineUsers);
            
            return stats;
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "통계 로드 실패: " + e.getMessage());
            return error;
        }
    }
}
