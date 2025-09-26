package com.codebattlearena.controller;

import com.codebattlearena.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("solvedProblems", 0);
        stats.put("battleWins", 0);
        stats.put("accuracy", 0);
        stats.put("studyDays", 0);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/battle-stats")
    public ResponseEntity<?> getBattleStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("rating", 1000);
        stats.put("wins", 0);
        stats.put("losses", 0);
        stats.put("totalBattles", 0);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/today")
    public ResponseEntity<?> getTodayContent() {
        Map<String, Object> content = new HashMap<>();
        content.put("aiProblem", null);
        content.put("materials", new ArrayList<>());
        content.put("submissions", new ArrayList<>());
        return ResponseEntity.ok(content);
    }

    @GetMapping("/practice-stats")
    public ResponseEntity<?> getPracticeStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAttempts", 0);
        stats.put("solvedCount", 0);
        stats.put("successRate", 0);
        stats.put("avgScore", 0);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/notes")
    public ResponseEntity<?> getNotes() {
        return ResponseEntity.ok(new ArrayList<>());
    }

    @PostMapping("/notes")
    public ResponseEntity<?> createNote(@RequestBody NoteRequest request) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "노트가 저장되었습니다.");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/notes/{id}")
    public ResponseEntity<?> updateNote(@PathVariable Long id, @RequestBody NoteRequest request) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "노트가 수정되었습니다.");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/notes/{id}")
    public ResponseEntity<?> deleteNote(@PathVariable Long id) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "노트가 삭제되었습니다.");
        return ResponseEntity.ok(response);
    }

    public static class NoteRequest {
        private String title;
        private String content;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}
