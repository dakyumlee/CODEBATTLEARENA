package com.codebattlearena.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher")
public class TeacherProblemController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PostMapping("/create-problem")
    public Map<String, Object> createProblem(@RequestBody ProblemRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> problem = new HashMap<>();
            problem.put("id", System.currentTimeMillis());
            problem.put("title", request.getTitle());
            problem.put("description", request.getDescription());
            problem.put("difficulty", request.getDifficulty());
            problem.put("points", request.getPoints());
            problem.put("type", request.getType());
            problem.put("createdAt", LocalDateTime.now().toString());

            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "new_problem");
            notification.put("message", "새로운 문제가 출제되었습니다: " + request.getTitle());
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

    public static class ProblemRequest {
        private String title;
        private String description;
        private String difficulty;
        private Integer points;
        private String type;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getDifficulty() { return difficulty; }
        public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
        public Integer getPoints() { return points; }
        public void setPoints(Integer points) { this.points = points; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }
}
