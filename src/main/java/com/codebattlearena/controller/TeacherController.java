package com.codebattlearena.controller;

import com.codebattlearena.config.JwtUtil;
import com.codebattlearena.model.User;
import com.codebattlearena.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JwtUtil jwtUtil;

    private Long getUserIdFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtUtil.extractUserId(token);
        }
        return null;
    }

    @GetMapping("/students")
    public Map<String, Object> getStudents(HttpServletRequest request) {
        Long teacherId = getUserIdFromRequest(request);
        if (teacherId == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Unauthorized");
            return error;
        }

        List<User> students = userRepository.findByRoleAndGroupId("STUDENT", teacherId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("students", students.stream().map(student -> {
            Map<String, Object> studentData = new HashMap<>();
            studentData.put("id", student.getId());
            studentData.put("name", student.getName());
            studentData.put("email", student.getEmail());
            studentData.put("online", student.isOnlineStatus());
            studentData.put("lastActivity", student.getLastActivity());
            return studentData;
        }).toList());
        
        return response;
    }

    @GetMapping("/materials")
    public List<Map<String, Object>> getMaterials(HttpServletRequest request) {
        Long teacherId = getUserIdFromRequest(request);
        if (teacherId == null) {
            return new ArrayList<>();
        }
        
        return new ArrayList<>();
    }

    @PostMapping("/problem")
    public Map<String, Object> createProblem(@RequestBody Map<String, Object> problemData, HttpServletRequest request) {
        Long teacherId = getUserIdFromRequest(request);
        if (teacherId == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Unauthorized");
            return error;
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "문제가 생성되었습니다");
        response.put("problemId", System.currentTimeMillis());
        
        return response;
    }

    @PostMapping("/quiz")
    public Map<String, Object> createQuiz(@RequestBody Map<String, Object> quizData, HttpServletRequest request) {
        Long teacherId = getUserIdFromRequest(request);
        if (teacherId == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Unauthorized");
            return error;
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "퀴즈가 생성되었습니다");
        response.put("quizId", System.currentTimeMillis());
        
        return response;
    }
}
