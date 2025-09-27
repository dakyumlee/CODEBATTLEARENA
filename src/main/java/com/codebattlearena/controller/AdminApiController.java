package com.codebattlearena.controller;

import com.codebattlearena.config.JwtUtil;
import com.codebattlearena.model.User;
import com.codebattlearena.model.UserRole;
import com.codebattlearena.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminApiController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JwtUtil jwtUtil;

    private Long getUserIdFromRequest(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String email = jwtUtil.extractEmail(token);
                User user = userRepository.findByEmail(email).orElse(null);
                return user != null ? user.getId() : null;
            }
        } catch (Exception e) {
            System.err.println("토큰 파싱 오류: " + e.getMessage());
        }
        return null;
    }

    private boolean isAdmin(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String email = jwtUtil.extractEmail(token);
                User user = userRepository.findByEmail(email).orElse(null);
                return user != null && user.getRole() == UserRole.ADMIN;
            }
        } catch (Exception e) {
            System.err.println("관리자 권한 확인 오류: " + e.getMessage());
        }
        return false;
    }

    public static class CreateUserRequest {
        private String email;
        private String password;
        private String name;
        private UserRole role;
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public UserRole getRole() { return role; }
        public void setRole(UserRole role) { this.role = role; }
    }

    @GetMapping("/statistics")
    public Map<String, Object> getSystemStatistics(HttpServletRequest request) {
        try {
            if (!isAdmin(request)) {
                return Map.of("error", "Unauthorized");
            }
            
            long totalUsers = userRepository.count();
            long studentCount = userRepository.countByRole(UserRole.STUDENT);
            long teacherCount = userRepository.countByRole(UserRole.TEACHER);
            long onlineUsers = userRepository.countByOnlineStatusTrue();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", totalUsers);
            stats.put("studentCount", studentCount);
            stats.put("teacherCount", teacherCount);
            stats.put("onlineUsers", onlineUsers);
            stats.put("adminCount", totalUsers - studentCount - teacherCount);
            
            return Map.of("statistics", stats);
        } catch (Exception e) {
            return Map.of("error", "Failed to load statistics: " + e.getMessage());
        }
    }

    @GetMapping("/users")
    public Map<String, Object> getAllUsers(HttpServletRequest request) {
        try {
            if (!isAdmin(request)) {
                return Map.of("error", "Unauthorized");
            }
            
            List<User> users = userRepository.findAllByOrderByCreatedAtDesc();
            
            List<Map<String, Object>> userData = users.stream().map(user -> {
                Map<String, Object> data = new HashMap<>();
                data.put("id", user.getId());
                data.put("name", user.getName());
                data.put("email", user.getEmail());
                data.put("role", user.getRole().toString());
                data.put("createdAt", user.getCreatedAt());
                data.put("onlineStatus", user.isOnlineStatus());
                data.put("lastActivity", user.getLastActivity());
                return data;
            }).toList();
            
            return Map.of("users", userData);
        } catch (Exception e) {
            return Map.of("error", "Failed to load users: " + e.getMessage());
        }
    }

    @PostMapping("/users")
    public Map<String, Object> createUser(@RequestBody CreateUserRequest userRequest, HttpServletRequest request) {
        try {
            if (!isAdmin(request)) {
                return Map.of("success", false, "message", "Unauthorized");
            }
            
            if (userRepository.findByEmail(userRequest.getEmail()).isPresent()) {
                return Map.of("success", false, "message", "이미 존재하는 이메일입니다.");
            }
            
            User user = new User();
            user.setEmail(userRequest.getEmail());
            user.setPassword(userRequest.getPassword());
            user.setName(userRequest.getName());
            user.setRole(userRequest.getRole());
            user.setCreatedAt(LocalDateTime.now());
            
            userRepository.save(user);
            
            return Map.of("success", true, "message", "사용자가 생성되었습니다.");
        } catch (Exception e) {
            return Map.of("success", false, "message", "Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/users/{id}")
    public Map<String, Object> deleteUser(@PathVariable Long id, HttpServletRequest request) {
        try {
            if (!isAdmin(request)) {
                return Map.of("success", false, "message", "Unauthorized");
            }
            
            User user = userRepository.findById(id).orElse(null);
            if (user == null) {
                return Map.of("success", false, "message", "사용자를 찾을 수 없습니다.");
            }
            
            if (user.getRole() == UserRole.ADMIN) {
                return Map.of("success", false, "message", "관리자는 삭제할 수 없습니다.");
            }
            
            userRepository.delete(user);
            
            return Map.of("success", true, "message", "사용자가 삭제되었습니다.");
        } catch (Exception e) {
            return Map.of("success", false, "message", "Error: " + e.getMessage());
        }
    }

    @PutMapping("/users/{id}/role")
    public Map<String, Object> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> roleData, HttpServletRequest request) {
        try {
            if (!isAdmin(request)) {
                return Map.of("success", false, "message", "Unauthorized");
            }
            
            User user = userRepository.findById(id).orElse(null);
            if (user == null) {
                return Map.of("success", false, "message", "사용자를 찾을 수 없습니다.");
            }
            
            String newRole = roleData.get("role");
            user.setRole(UserRole.valueOf(newRole));
            userRepository.save(user);
            
            return Map.of("success", true, "message", "역할이 업데이트되었습니다.");
        } catch (Exception e) {
            return Map.of("success", false, "message", "Error: " + e.getMessage());
        }
    }
}
