package com.codebattlearena.controller;

import com.codebattlearena.model.StudyNote;
import com.codebattlearena.repository.StudyNoteRepository;
import com.codebattlearena.repository.UserRepository;
import com.codebattlearena.service.OpenAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudyNoteRepository studyNoteRepository;

    @Autowired
    private OpenAIService openAIService;

    @GetMapping("/today")
    public ResponseEntity<?> getTodayContent() {
        Map<String, Object> content = new HashMap<>();
        
        // AI 추천 문제 생성
        try {
            String aiProblem = openAIService.generateProblem("중", "자바 기초");
            Map<String, Object> problemData = new HashMap<>();
            problemData.put("title", "오늘의 AI 추천 문제");
            problemData.put("description", aiProblem);
            problemData.put("difficulty", "중");
            problemData.put("points", 50);
            problemData.put("type", "ai_daily");
            content.put("aiProblem", problemData);
        } catch (Exception e) {
            // AI 서비스 오류시 기본 문제 제공
            Map<String, Object> defaultProblem = new HashMap<>();
            defaultProblem.put("title", "배열 요소의 합 구하기");
            defaultProblem.put("description", "정수 배열이 주어졌을 때, 모든 요소의 합을 구하는 함수를 작성하세요.\n\n입력: [1, 2, 3, 4, 5]\n출력: 15");
            defaultProblem.put("difficulty", "하");
            defaultProblem.put("points", 30);
            defaultProblem.put("type", "basic");
            content.put("aiProblem", defaultProblem);
        }
        
        content.put("materials", new ArrayList<>());
        content.put("submissions", new ArrayList<>());
        return ResponseEntity.ok(content);
    }

    @GetMapping("/practice-stats")
    public ResponseEntity<?> getPracticeStats() {
        // 실제 통계는 추후 구현, 현재는 기본값
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAttempts", 0);
        stats.put("solvedCount", 0);
        stats.put("successRate", 0);
        stats.put("avgScore", 0);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/notes")
    public ResponseEntity<?> getNotes() {
        try {
            List<StudyNote> notes = studyNoteRepository.findAllByOrderByCreatedAtDesc();
            return ResponseEntity.ok(notes);
        } catch (Exception e) {
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    @PostMapping("/notes")
    public ResponseEntity<?> createNote(@RequestBody NoteRequest request) {
        try {
            StudyNote note = new StudyNote();
            note.setTitle(request.getTitle());
            note.setContent(request.getContent());
            note.setCreatedAt(LocalDateTime.now());
            note.setUpdatedAt(LocalDateTime.now());
            
            StudyNote savedNote = studyNoteRepository.save(note);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "노트가 저장되었습니다.");
            response.put("note", savedNote);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "노트 저장에 실패했습니다."));
        }
    }

    @PutMapping("/notes/{id}")
    public ResponseEntity<?> updateNote(@PathVariable Long id, @RequestBody NoteRequest request) {
        try {
            Optional<StudyNote> noteOpt = studyNoteRepository.findById(id);
            if (noteOpt.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("error", "노트를 찾을 수 없습니다."));
            }
            
            StudyNote note = noteOpt.get();
            note.setTitle(request.getTitle());
            note.setContent(request.getContent());
            note.setUpdatedAt(LocalDateTime.now());
            
            StudyNote savedNote = studyNoteRepository.save(note);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "노트가 수정되었습니다.");
            response.put("note", savedNote);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "노트 수정에 실패했습니다."));
        }
    }

    @DeleteMapping("/notes/{id}")
    public ResponseEntity<?> deleteNote(@PathVariable Long id) {
        try {
            if (!studyNoteRepository.existsById(id)) {
                return ResponseEntity.status(404).body(Map.of("error", "노트를 찾을 수 없습니다."));
            }
            
            studyNoteRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "노트가 삭제되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "노트 삭제에 실패했습니다."));
        }
    }

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

    public static class NoteRequest {
        private String title;
        private String content;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}
