package com.codebattlearena.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/teacher")
public class TeacherProblemController {

    @PostMapping("/create-problem")
    public ResponseEntity<?> createProblem(@RequestBody CreateProblemRequest request) {
        try {
            String problemId = UUID.randomUUID().toString();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("problemId", problemId);
            response.put("message", "문제가 성공적으로 출제되었습니다.");
            response.put("title", request.getTitle());
            response.put("difficulty", request.getDifficulty());
            response.put("points", request.getPoints());
            response.put("createdAt", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "문제 출제에 실패했습니다."));
        }
    }

    @PostMapping("/create-quiz")
    public ResponseEntity<?> createQuiz(@RequestBody CreateQuizRequest request) {
        try {
            String quizId = UUID.randomUUID().toString();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("quizId", quizId);
            response.put("message", "퀴즈가 시작되었습니다.");
            response.put("title", request.getTitle());
            response.put("duration", request.getDuration());
            response.put("questionCount", request.getQuestions().size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "퀴즈 생성에 실패했습니다."));
        }
    }

    public static class CreateProblemRequest {
        private String title;
        private String description;
        private String difficulty;
        private int points;
        private String type;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getDifficulty() { return difficulty; }
        public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
        public int getPoints() { return points; }
        public void setPoints(int points) { this.points = points; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }

    public static class CreateQuizRequest {
        private String title;
        private int duration;
        private java.util.List<String> questions;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public int getDuration() { return duration; }
        public void setDuration(int duration) { this.duration = duration; }
        public java.util.List<String> getQuestions() { return questions; }
        public void setQuestions(java.util.List<String> questions) { this.questions = questions; }
    }
}
