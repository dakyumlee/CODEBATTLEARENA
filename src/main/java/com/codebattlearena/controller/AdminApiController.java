package com.codebattlearena.controller;

import com.codebattlearena.model.User;
import com.codebattlearena.model.UserRole;
import com.codebattlearena.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminApiController {
    
    @Autowired
    private UserRepository userRepository;
    
    @GetMapping("/users")
    public List<Map<String, Object>> getUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map(user -> {
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("name", user.getName());
            userData.put("email", user.getEmail());
            userData.put("role", user.getRole().toString());
            userData.put("online", user.isOnlineStatus());
            userData.put("createdAt", user.getCreatedAt());
            return userData;
        }).toList();
    }
    
    @DeleteMapping("/users/{id}")
    public Map<String, Object> deleteUser(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            userRepository.deleteById(id);
            response.put("success", true);
            response.put("message", "사용자가 삭제되었습니다");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "삭제 실패: " + e.getMessage());
        }
        
        return response;
    }
    
    @PutMapping("/users/{id}/role")
    public Map<String, Object> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = userRepository.findById(id).orElse(null);
            if (user == null) {
                response.put("success", false);
                response.put("message", "사용자를 찾을 수 없습니다");
                return response;
            }
            
            String newRole = request.get("role");
            user.setRole(UserRole.valueOf(newRole));
            userRepository.save(user);
            
            response.put("success", true);
            response.put("message", "역할이 변경되었습니다");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "역할 변경 실패: " + e.getMessage());
        }
        
        return response;
    }
    
    @GetMapping("/statistics")
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalUsers = userRepository.count();
        long studentCount = userRepository.countByRole(UserRole.STUDENT);
        long teacherCount = userRepository.countByRole(UserRole.TEACHER);
        long onlineCount = userRepository.countByOnlineStatus(true);
        
        stats.put("totalUsers", totalUsers);
        stats.put("studentCount", studentCount);
        stats.put("teacherCount", teacherCount);
        stats.put("onlineCount", onlineCount);
        
        return stats;
    }
    
    @PostMapping("/users")
    public Map<String, Object> createUser(@RequestBody CreateUserRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                response.put("success", false);
                response.put("message", "이미 존재하는 이메일입니다");
                return response;
            }
            
            User user = new User();
            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setPassword(request.getPassword());
            user.setRole(UserRole.valueOf(request.getRole()));
            user.setCreatedAt(LocalDateTime.now());
            
            userRepository.save(user);
            
            response.put("success", true);
            response.put("message", "사용자가 생성되었습니다");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "사용자 생성 실패: " + e.getMessage());
        }
        
        return response;
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
}
