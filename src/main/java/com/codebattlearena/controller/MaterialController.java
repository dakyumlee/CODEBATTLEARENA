package com.codebattlearena.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher")
public class MaterialController {

    @GetMapping("/materials")
    public ResponseEntity<?> getMaterials() {
        return ResponseEntity.ok(new ArrayList<>());
    }

    @PostMapping("/materials/upload")
    public ResponseEntity<?> uploadMaterials(@RequestParam("files") MultipartFile[] files) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "파일이 업로드되었습니다.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/materials/link")
    public ResponseEntity<?> shareLink(@RequestBody LinkRequest request) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "링크가 공유되었습니다.");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/materials/{id}")
    public ResponseEntity<?> deleteMaterial(@PathVariable Long id) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "자료가 삭제되었습니다.");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/problems")
    public ResponseEntity<?> getProblems() {
        return ResponseEntity.ok(new ArrayList<>());
    }

    @PostMapping("/problems")
    public ResponseEntity<?> createProblem(@RequestBody ProblemRequest request) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "문제가 출제되었습니다.");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/submissions")
    public ResponseEntity<?> getSubmissions() {
        return ResponseEntity.ok(new ArrayList<>());
    }

    @GetMapping("/grades")
    public ResponseEntity<?> getGrades() {
        Map<String, Object> response = new HashMap<>();
        response.put("grades", new ArrayList<>());
        
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("classAverage", 0);
        analysis.put("passRate", 0);
        analysis.put("improvementRate", 0);
        analysis.put("attendanceRate", 0);
        response.put("analysis", analysis);
        
        response.put("counseling", new ArrayList<>());
        return ResponseEntity.ok(response);
    }

    public static class LinkRequest {
        private String url;
        private String title;

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
    }

    public static class ProblemRequest {
        private String type;
        private String title;
        private String description;
        private String difficulty;
        private int points;
        private String deadline;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getDifficulty() { return difficulty; }
        public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
        public int getPoints() { return points; }
        public void setPoints(int points) { this.points = points; }
        public String getDeadline() { return deadline; }
        public void setDeadline(String deadline) { this.deadline = deadline; }
    }
}
