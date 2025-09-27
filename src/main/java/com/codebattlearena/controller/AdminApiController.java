package com.codebattlearena.controller;

import com.codebattlearena.config.JwtUtil;
import com.codebattlearena.model.User;
import com.codebattlearena.model.UserRole;
import com.codebattlearena.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminApiController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

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
            System.err.println("권한 확인 오류: " + e.getMessage());
        }
        return false;
    }

    @GetMapping("/statistics")
    public Map<String, Object> getStatistics(HttpServletRequest request) {
        if (!isAdmin(request)) {
            return Map.of("error", "Unauthorized");
        }

        try {
            long totalUsers = userRepository.count();
            long totalStudents = userRepository.countByRole(UserRole.STUDENT);
            long totalTeachers = userRepository.countByRole(UserRole.TEACHER);
            long onlineUsers = userRepository.countByOnlineStatusTrue();

            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalUsers", totalUsers);
            statistics.put("totalStudents", totalStudents);
            statistics.put("totalTeachers", totalTeachers);
            statistics.put("onlineUsers", onlineUsers);

            return Map.of("statistics", statistics);
        } catch (Exception e) {
            return Map.of("error", "Failed to load statistics: " + e.getMessage());
        }
    }

    @GetMapping("/users")
    public Map<String, Object> getAllUsers(HttpServletRequest request) {
        if (!isAdmin(request)) {
            return Map.of("error", "Unauthorized");
        }

        try {
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
            }).collect(Collectors.toList());

            return Map.of("users", userData);
        } catch (Exception e) {
            return Map.of("error", "Failed to load users: " + e.getMessage());
        }
    }

    @PostMapping("/users")
    public Map<String, Object> createUser(@RequestBody CreateUserRequest request, HttpServletRequest httpRequest) {
        if (!isAdmin(httpRequest)) {
            return Map.of("success", false, "message", "Unauthorized");
        }

        try {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                return Map.of("success", false, "message", "이미 존재하는 이메일입니다.");
            }

            User user = new User();
            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setRole(UserRole.valueOf(request.getRole()));
            user.setCreatedAt(LocalDateTime.now());
            user.setOnlineStatus(false);

            User savedUser = userRepository.save(user);

            return Map.of(
                "success", true,
                "message", "사용자가 성공적으로 생성되었습니다.",
                "user", Map.of(
                    "id", savedUser.getId(),
                    "name", savedUser.getName(),
                    "email", savedUser.getEmail(),
                    "role", savedUser.getRole().toString()
                )
            );
        } catch (Exception e) {
            return Map.of("success", false, "message", "사용자 생성 실패: " + e.getMessage());
        }
    }

    @PutMapping("/users/{id}/role")
    public Map<String, Object> changeUserRole(@PathVariable Long id, @RequestBody Map<String, String> roleData, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return Map.of("success", false, "message", "Unauthorized");
        }

        try {
            User user = userRepository.findById(id).orElse(null);
            if (user == null) {
                return Map.of("success", false, "message", "사용자를 찾을 수 없습니다.");
            }

            String newRole = roleData.get("role");
            if (newRole == null || newRole.trim().isEmpty()) {
                return Map.of("success", false, "message", "역할을 입력해주세요.");
            }

            try {
                UserRole role = UserRole.valueOf(newRole.toUpperCase());
                user.setRole(role);
                userRepository.save(user);

                return Map.of("success", true, "message", "역할이 성공적으로 변경되었습니다.");
            } catch (IllegalArgumentException e) {
                return Map.of("success", false, "message", "잘못된 역할입니다.");
            }
        } catch (Exception e) {
            return Map.of("success", false, "message", "역할 변경 실패: " + e.getMessage());
        }
    }

    @DeleteMapping("/users/{id}")
    public Map<String, Object> deleteUser(@PathVariable Long id, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return Map.of("success", false, "message", "Unauthorized");
        }

        try {
            User user = userRepository.findById(id).orElse(null);
            if (user == null) {
                return Map.of("success", false, "message", "사용자를 찾을 수 없습니다.");
            }

            userRepository.delete(user);
            return Map.of("success", true, "message", "사용자가 삭제되었습니다.");
        } catch (Exception e) {
            return Map.of("success", false, "message", "사용자 삭제 실패: " + e.getMessage());
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
}
