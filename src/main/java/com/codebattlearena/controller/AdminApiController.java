package com.codebattlearena.controller;

import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminApiController {

    @GetMapping("/users")
    public List<Map<String, Object>> getUsers() {
        List<Map<String, Object>> users = new ArrayList<>();
        
        Map<String, Object> user1 = new HashMap<>();
        user1.put("id", 1);
        user1.put("name", "김학생");
        user1.put("email", "student@example.com");
        user1.put("role", "STUDENT");
        user1.put("createdAt", LocalDateTime.now().minusDays(10).toString());
        user1.put("onlineStatus", true);
        users.add(user1);

        Map<String, Object> user2 = new HashMap<>();
        user2.put("id", 2);
        user2.put("name", "이강사");
        user2.put("email", "teacher@example.com");
        user2.put("role", "TEACHER");
        user2.put("createdAt", LocalDateTime.now().minusDays(30).toString());
        user2.put("onlineStatus", false);
        users.add(user2);
        
        return users;
    }

    @PostMapping("/users")
    public Map<String, Object> createUser(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "사용자가 생성되었습니다");
        return response;
    }

    @DeleteMapping("/users/{id}")
    public Map<String, Object> deleteUser(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "사용자가 삭제되었습니다");
        return response;
    }

    @PutMapping("/users/{id}/role")
    public Map<String, Object> updateRole(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "역할이 변경되었습니다");
        return response;
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", 127);
        stats.put("studentCount", 98);
        stats.put("teacherCount", 8);
        stats.put("onlineCount", 45);
        return stats;
    }
}
