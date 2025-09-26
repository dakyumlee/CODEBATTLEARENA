package com.codebattlearena.controller;

import com.codebattlearena.model.User;
import com.codebattlearena.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
        List<Map<String, Object>> notes = new ArrayList<>();
        return ResponseEntity.ok(notes);
    }

    @PostMapping("/notes")
    public ResponseEntity<?> createNote(@RequestBody NoteRequest request) {
        return ResponseEntity.ok("노트가 저장되었습니다.");
    }

    @PutMapping("/notes/{id}")
    public ResponseEntity<?> updateNote(@PathVariable Long id, @RequestBody NoteRequest request) {
        return ResponseEntity.ok("노트가 수정되었습니다.");
    }

    @DeleteMapping("/notes/{id}")
    public ResponseEntity<?> deleteNote(@PathVariable Long id) {
        return ResponseEntity.ok("노트가 삭제되었습니다.");
    }

    @PostMapping("/activity")
    public ResponseEntity<?> updateActivity(@RequestBody ActivityRequest request) {
        return ResponseEntity.ok("활동 업데이트 성공");
    }

    public static class NoteRequest {
        private String title;
        private String content;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    public static class ActivityRequest {
        private String activity;
        private String page;

        public String getActivity() { return activity; }
        public void setActivity(String activity) { this.activity = activity; }
        public String getPage() { return page; }
        public void setPage(String page) { this.page = page; }
    }
}
