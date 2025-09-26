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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/students")
    public ResponseEntity<?> getStudents() {
        try {
            List<User> students = userRepository.findByRole(UserRole.STUDENT);
            
            List<Map<String, Object>> studentData = students.stream().map(student -> {
                Map<String, Object> data = new HashMap<>();
                data.put("id", student.getId());
                data.put("name", student.getName());
                data.put("email", student.getEmail());
                data.put("onlineStatus", student.getOnlineStatus());
                data.put("lastActivity", student.getLastActivity());
                data.put("currentActivity", "학습 중");
                data.put("currentPage", "/student/dashboard");
                return data;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(studentData);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "학생 목록을 불러올 수 없습니다."));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getTeacherStats() {
        try {
            long totalStudents = userRepository.countByRole(UserRole.STUDENT);
            long onlineStudents = userRepository.countByRoleAndOnlineStatus(UserRole.STUDENT, true);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalStudents", totalStudents);
            stats.put("onlineStudents", onlineStudents);
            stats.put("activeClasses", 1);
            stats.put("todaySubmissions", 0);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "통계를 불러올 수 없습니다."));
        }
    }
}
