package com.codebattlearena.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping("/students")
    public List<Map<String, Object>> getStudents() {
        return new ArrayList<>();
    }

    @GetMapping("/materials")
    public List<Map<String, Object>> getMaterials() {
        return new ArrayList<>();
    }

    @PostMapping("/materials/upload")
    public Map<String, Object> uploadMaterial(@RequestParam(value = "files", required = false) MultipartFile[] files,
                                            @RequestParam("title") String title,
                                            @RequestParam(value = "description", required = false) String description) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> material = new HashMap<>();
            material.put("id", System.currentTimeMillis());
            material.put("title", title);
            material.put("description", description != null ? description : "");
            material.put("fileType", "file");
            material.put("createdAt", LocalDateTime.now().toString());

            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "new_material");
            notification.put("message", "새로운 자료가 공유되었습니다: " + title);
            notification.put("material", material);
            notification.put("timestamp", LocalDateTime.now().toString());

            messagingTemplate.convertAndSend("/topic/notifications", notification);
            
            response.put("success", true);
            response.put("message", "파일이 업로드되었습니다");
            response.put("material", material);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "업로드 실패: " + e.getMessage());
        }
        
        return response;
    }

    @PostMapping("/materials/link")
    public Map<String, Object> shareLink(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> material = new HashMap<>();
            material.put("id", System.currentTimeMillis());
            material.put("title", request.get("title"));
            material.put("description", request.get("description"));
            material.put("url", request.get("url"));
            material.put("fileType", "link");
            material.put("createdAt", LocalDateTime.now().toString());

            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "new_material");
            notification.put("message", "새로운 링크가 공유되었습니다: " + request.get("title"));
            notification.put("material", material);
            notification.put("timestamp", LocalDateTime.now().toString());

            messagingTemplate.convertAndSend("/topic/notifications", notification);
            
            response.put("success", true);
            response.put("message", "링크가 공유되었습니다");
            response.put("material", material);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "링크 공유 실패: " + e.getMessage());
        }
        
        return response;
    }

    @DeleteMapping("/materials/{id}")
    public Map<String, Object> deleteMaterial(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "자료가 삭제되었습니다");
        return response;
    }

    @PostMapping("/problems")
    public Map<String, Object> createProblem(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> problem = new HashMap<>();
            problem.put("id", System.currentTimeMillis());
            problem.put("title", request.get("title"));
            problem.put("description", request.get("description"));
            problem.put("difficulty", request.get("difficulty"));
            problem.put("points", request.get("points"));
            problem.put("type", "teacher");
            problem.put("createdAt", LocalDateTime.now().toString());

            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "new_problem");
            notification.put("message", "새로운 문제가 출제되었습니다: " + request.get("title"));
            notification.put("problem", problem);
            notification.put("timestamp", LocalDateTime.now().toString());

            messagingTemplate.convertAndSend("/topic/notifications", notification);

            response.put("success", true);
            response.put("message", "문제가 출제되었습니다");
            response.put("problem", problem);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "문제 출제 실패: " + e.getMessage());
        }

        return response;
    }

    @PostMapping("/quiz")
    public Map<String, Object> createQuiz(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> quiz = new HashMap<>();
            quiz.put("id", System.currentTimeMillis());
            quiz.put("title", request.get("title"));
            quiz.put("duration", request.get("duration"));
            quiz.put("questions", request.get("questions"));
            quiz.put("createdAt", LocalDateTime.now().toString());

            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "new_quiz");
            notification.put("message", "새로운 퀴즈가 시작되었습니다: " + request.get("title"));
            notification.put("quiz", quiz);
            notification.put("timestamp", LocalDateTime.now().toString());

            messagingTemplate.convertAndSend("/topic/notifications", notification);
            
            response.put("success", true);
            response.put("message", "퀴즈가 시작되었습니다");
            response.put("quiz", quiz);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "퀴즈 시작 실패: " + e.getMessage());
        }
        
        return response;
    }

    @PostMapping("/announce")
    public Map<String, Object> sendAnnouncement(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "announcement");
            notification.put("message", request.get("message"));
            notification.put("timestamp", LocalDateTime.now().toString());

            messagingTemplate.convertAndSend("/topic/notifications", notification);
            
            response.put("success", true);
            response.put("message", "공지사항이 전송되었습니다");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "전송 실패: " + e.getMessage());
        }
        
        return response;
    }

    @GetMapping("/grades")
    public Map<String, Object> getGrades() {
        Map<String, Object> response = new HashMap<>();
        
        response.put("grades", new ArrayList<>());
        
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("classAverage", 0);
        analysis.put("passRate", 0);
        analysis.put("improvementRate", 0);
        analysis.put("attendanceRate", 0);
        
        response.put("analysis", analysis);
        response.put("counseling", new ArrayList<>());
        
        return response;
    }
}
