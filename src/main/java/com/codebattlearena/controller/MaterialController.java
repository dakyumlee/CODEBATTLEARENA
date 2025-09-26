package com.codebattlearena.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher")
public class MaterialController {

    @GetMapping("/materials")
    public ResponseEntity<?> getMaterials() {
        List<Map<String, Object>> materials = new ArrayList<>();
        return ResponseEntity.ok(materials);
    }

    @PostMapping("/materials/upload")
    public ResponseEntity<?> uploadMaterials(@RequestParam("files") MultipartFile[] files) {
        return ResponseEntity.ok("파일이 업로드되었습니다.");
    }

    @PostMapping("/materials/link")
    public ResponseEntity<?> shareLink(@RequestBody LinkRequest request) {
        return ResponseEntity.ok("링크가 공유되었습니다.");
    }

    @DeleteMapping("/materials/{id}")
    public ResponseEntity<?> deleteMaterial(@PathVariable Long id) {
        return ResponseEntity.ok("자료가 삭제되었습니다.");
    }

    @GetMapping("/problems")
    public ResponseEntity<?> getProblems() {
        List<Map<String, Object>> problems = new ArrayList<>();
        return ResponseEntity.ok(problems);
    }

    @PostMapping("/problems")
    public ResponseEntity<?> createProblem(@RequestBody ProblemRequest request) {
        return ResponseEntity.ok("문제가 출제되었습니다.");
    }

    @GetMapping("/submissions")
    public ResponseEntity<?> getSubmissions() {
        List<Map<String, Object>> submissions = new ArrayList<>();
        return ResponseEntity.ok(submissions);
    }

    @GetMapping("/grades")
    public ResponseEntity<?> getGrades() {
        Map<String, Object> response = new HashMap<>();
        
        List<Map<String, Object>> grades = new ArrayList<>();
        response.put("grades", grades);
        
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("classAverage", 0);
        analysis.put("passRate", 0);
        analysis.put("improvementRate", 0);
        analysis.put("attendanceRate", 0);
        response.put("analysis", analysis);
        
        List<Map<String, Object>> counseling = new ArrayList<>();
        response.put("counseling", counseling);
        
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
