package com.codebattlearena.controller;

import com.codebattlearena.model.User;
import com.codebattlearena.model.UserRole;
import com.codebattlearena.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminApiController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/users")
    public List<Map<String, Object>> getUsers() {
        try {
            List<User> users = userRepository.findAll();
            List<Map<String, Object>> userList = new ArrayList<>();
            
            for (User user : users) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", user.getId());
                userMap.put("name", user.getName());
                userMap.put("email", user.getEmail());
                userMap.put("role", user.getRole().toString());
                userMap.put("onlineStatus", user.getOnlineStatus());
                userMap.put("createdAt", user.getCreatedAt().toString());
                userList.add(userMap);
            }
            
            return userList;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @PostMapping("/users")
    public Map<String, Object> createUser(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = new User();
            user.setName(request.get("name"));
            user.setEmail(request.get("email"));
            user.setPassword(request.get("password"));
            user.setRole(UserRole.valueOf(request.getOrDefault("role", "STUDENT")));
            user.setCreatedAt(LocalDateTime.now());
            user.setOnlineStatus(false);
            
            userRepository.save(user);
            
            response.put("success", true);
            response.put("message", "사용자가 생성되었습니다");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "사용자 생성 실패: " + e.getMessage());
        }
        
        return response;
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
    public Map<String, Object> updateRole(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setRole(UserRole.valueOf(request.get("role")));
                userRepository.save(user);
                
                response.put("success", true);
                response.put("message", "역할이 변경되었습니다");
            } else {
                response.put("success", false);
                response.put("message", "사용자를 찾을 수 없습니다");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "역할 변경 실패: " + e.getMessage());
        }
        
        return response;
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            long totalUsers = userRepository.count();
            long studentCount = userRepository.countByRole(UserRole.STUDENT);
            long teacherCount = userRepository.countByRole(UserRole.TEACHER);
            long onlineCount = userRepository.countByOnlineStatus(true);
            
            stats.put("totalUsers", totalUsers);
            stats.put("studentCount", studentCount);
            stats.put("teacherCount", teacherCount);
            stats.put("onlineCount", onlineCount);
        } catch (Exception e) {
            stats.put("totalUsers", 0);
            stats.put("studentCount", 0);
            stats.put("teacherCount", 0);
            stats.put("onlineCount", 0);
        }
        
        return stats;
    }
}
