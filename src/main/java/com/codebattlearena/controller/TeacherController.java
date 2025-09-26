package com.codebattlearena.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping("/students")
    public List<Map<String, Object>> getStudents() {
        List<Map<String, Object>> students = new ArrayList<>();
        
        Map<String, Object> student1 = new HashMap<>();
        student1.put("id", 1);
        student1.put("name", "김학생");
        student1.put("email", "student1@example.com");
        student1.put("onlineStatus", true);
        student1.put("currentActivity", "문제 풀이 중");
        student1.put("lastActivity", LocalDateTime.now().minusMinutes(2).toString());
        students.add(student1);

        Map<String, Object> student2 = new HashMap<>();
        student2.put("id", 2);
        student2.put("name", "이학생");
        student2.put("email", "student2@example.com");
        student2.put("onlineStatus", false);
        student2.put("currentActivity", "오프라인");
        student2.put("lastActivity", LocalDateTime.now().minusHours(1).toString());
        students.add(student2);
        
        return students;
    }

    @GetMapping("/materials")
    public List<Map<String, Object>> getMaterials() {
        List<Map<String, Object>> materials = new ArrayList<>();
        
        Map<String, Object> material1 = new HashMap<>();
        material1.put("id", 1);
        material1.put("title", "Java 기초 강의자료");
        material1.put("description", "변수와 데이터 타입");
        material1.put("fileType", "file");
        material1.put("createdAt", LocalDateTime.now().minusHours(2).toString());
        material1.put("downloadsCount", 5);
        materials.add(material1);
        
        return materials;
    }

    @PostMapping("/materials/upload")
    public Map<String, Object> uploadMaterial(@RequestParam("files") Object files,
                                            @RequestParam("title") String title,
                                            @RequestParam(value = "description", required = false) String description) {
        Map<String, Object> response = new HashMap<>();
        
        Map<String, Object> material = new HashMap<>();
        material.put("id", System.currentTimeMillis());
        material.put("title", title);
        material.put("description", description);
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
        return response;
    }

    @PostMapping("/materials/link")
    public Map<String, Object> shareLink(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
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
        return response;
    }

    @DeleteMapping("/materials/{id}")
    public Map<String, Object> deleteMaterial(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "자료가 삭제되었습니다");
        return response;
    }

    @PostMapping("/create-quiz")
    public Map<String, Object> createQuiz(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
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
        return response;
    }

    @PostMapping("/notification")
    public Map<String, Object> sendNotification(@RequestBody Map<String, String> request) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "announcement");
        notification.put("message", request.get("message"));
        notification.put("timestamp", LocalDateTime.now().toString());

        messagingTemplate.convertAndSend("/topic/notifications", notification);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "공지사항이 전송되었습니다");
        return response;
    }

    @GetMapping("/grades")
    public Map<String, Object> getGrades() {
        Map<String, Object> response = new HashMap<>();
        
        List<Map<String, Object>> grades = new ArrayList<>();
        Map<String, Object> grade1 = new HashMap<>();
        grade1.put("studentId", 1);
        grade1.put("studentName", "김학생");
        grade1.put("attendanceRate", 95);
        grade1.put("quizAverage", 85);
        grade1.put("assignmentAverage", 90);
        grade1.put("examScore", 88);
        grade1.put("totalScore", 88);
        grade1.put("grade", "A");
        grades.add(grade1);
        
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("classAverage", 85);
        analysis.put("passRate", 92);
        analysis.put("improvementRate", 15);
        analysis.put("attendanceRate", 94);
        
        response.put("grades", grades);
        response.put("analysis", analysis);
        response.put("counseling", new ArrayList<>());
        
        return response;
    }
}
