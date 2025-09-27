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
    
    @Autowired
    private WebSocketController webSocketController;

    @GetMapping("/students")
    public Map<String, Object> getStudents() {
        try {
            List<User> students = userRepository.findAllStudents();
            
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
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to load students: " + e.getMessage());
            return error;
        }
    }

    @GetMapping("/materials")
    public List<Map<String, Object>> getMaterials() {
        // TODO: Material 엔티티와 MaterialRepository 구현 후 실제 데이터 반환
        return new ArrayList<>();
    }

    @PostMapping("/problem")
    public Map<String, Object> createProblem(@RequestBody Map<String, Object> problemData) {
        try {
            String title = (String) problemData.get("title");
            
            // TODO: Problem 엔티티에 실제 저장
            // Problem problem = new Problem();
            // problem.setTitle(title);
            // problem.setDescription((String) problemData.get("description"));
            // problem.setDifficulty((String) problemData.get("difficulty"));
            // problemRepository.save(problem);
            
            webSocketController.sendProblemNotification(title, "코딩 문제");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "문제가 생성되고 학생들에게 알림이 전송되었습니다");
            
            return response;
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error: " + e.getMessage());
            return error;
        }
    }

    @PostMapping("/quiz")
    public Map<String, Object> createQuiz(@RequestBody Map<String, Object> quizData) {
        try {
            String title = (String) quizData.get("title");
            
            // TODO: Quiz 엔티티에 실제 저장
            
            webSocketController.sendProblemNotification(title, "퀴즈");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "퀴즈가 생성되고 학생들에게 알림이 전송되었습니다");
            
            return response;
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error: " + e.getMessage());
            return error;
        }
    }

    @PostMapping("/exam")
    public Map<String, Object> createExam(@RequestBody Map<String, Object> examData) {
        try {
            String title = (String) examData.get("title");
            
            // TODO: Exam 엔티티에 실제 저장
            
            webSocketController.sendProblemNotification(title, "시험");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "시험이 생성되고 학생들에게 알림이 전송되었습니다");
            
            return response;
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error: " + e.getMessage());
            return error;
        }
    }
}
