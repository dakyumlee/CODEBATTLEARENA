package com.codebattlearena.controller;

import com.codebattlearena.config.JwtUtil;
import com.codebattlearena.model.User;
import com.codebattlearena.model.Problem;
import com.codebattlearena.repository.UserRepository;
import com.codebattlearena.repository.ProblemRepository;
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
    private ProblemRepository problemRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private WebSocketController webSocketController;

    private Long getUserIdFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtUtil.extractUserId(token);
        }
        return null;
    }

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
        return new ArrayList<>();
    }

    @PostMapping("/problem")
    public Map<String, Object> createProblem(@RequestBody Map<String, Object> problemData, HttpServletRequest request) {
        try {
            Long teacherId = getUserIdFromRequest(request);
            if (teacherId == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Unauthorized");
                return error;
            }
            
            Problem problem = new Problem();
            problem.setCreatorId(teacherId);
            problem.setCreatorType("TEACHER");
            problem.setTitle((String) problemData.get("title"));
            problem.setDescription((String) problemData.get("description"));
            problem.setDifficulty((String) problemData.get("difficulty"));
            problem.setTimeLimit((Integer) problemData.get("timeLimit"));
            problem.setExampleInput((String) problemData.get("exampleInput"));
            problem.setExampleOutput((String) problemData.get("exampleOutput"));
            problem.setType("CODING");
            
            Problem savedProblem = problemRepository.save(problem);
            
            webSocketController.sendProblemNotification(problem.getTitle(), "코딩 문제");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "문제가 생성되고 학생들에게 알림이 전송되었습니다");
            response.put("problemId", savedProblem.getId());
            
            return response;
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error: " + e.getMessage());
            return error;
        }
    }

    @PostMapping("/quiz")
    public Map<String, Object> createQuiz(@RequestBody Map<String, Object> quizData, HttpServletRequest request) {
        try {
            Long teacherId = getUserIdFromRequest(request);
            if (teacherId == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Unauthorized");
                return error;
            }
            
            Problem quiz = new Problem();
            quiz.setCreatorId(teacherId);
            quiz.setCreatorType("TEACHER");
            quiz.setTitle((String) quizData.get("title"));
            quiz.setDescription((String) quizData.get("question"));
            quiz.setTimeLimit((Integer) quizData.get("timeLimit"));
            quiz.setType("QUIZ");
            
            Problem savedQuiz = problemRepository.save(quiz);
            
            webSocketController.sendProblemNotification(quiz.getTitle(), "퀴즈");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "퀴즈가 생성되고 학생들에게 알림이 전송되었습니다");
            response.put("quizId", savedQuiz.getId());
            
            return response;
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error: " + e.getMessage());
            return error;
        }
    }

    @PostMapping("/exam")
    public Map<String, Object> createExam(@RequestBody Map<String, Object> examData, HttpServletRequest request) {
        try {
            Long teacherId = getUserIdFromRequest(request);
            if (teacherId == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Unauthorized");
                return error;
            }
            
            Problem exam = new Problem();
            exam.setCreatorId(teacherId);
            exam.setCreatorType("TEACHER");
            exam.setTitle((String) examData.get("title"));
            exam.setDescription((String) examData.get("instructions"));
            exam.setTimeLimit((Integer) examData.get("timeLimit"));
            exam.setType("EXAM");
            
            Problem savedExam = problemRepository.save(exam);
            
            webSocketController.sendProblemNotification(exam.getTitle(), "시험");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "시험이 생성되고 학생들에게 알림이 전송되었습니다");
            response.put("examId", savedExam.getId());
            
            return response;
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error: " + e.getMessage());
            return error;
        }
    }
}
