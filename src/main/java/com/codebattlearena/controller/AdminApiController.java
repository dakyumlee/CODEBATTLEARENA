package com.codebattlearena.controller;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminApiController {

    @GetMapping("/users")
    public List<Map<String, Object>> getUsers() {
        return new ArrayList<>();
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
        stats.put("totalUsers", 0);
        stats.put("studentCount", 0);
        stats.put("teacherCount", 0);
        stats.put("onlineCount", 0);
        return stats;
    }
}
