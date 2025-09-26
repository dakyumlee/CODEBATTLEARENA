package com.codebattlearena.controller;

import com.codebattlearena.model.StudyNote;
import com.codebattlearena.repository.StudyNoteRepository;
import com.codebattlearena.repository.UserRepository;
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
        stats.put("totalAttempts", 15);
        stats.put("solvedCount", 8);
        stats.put("successRate", 53);
        stats.put("avgScore", 78);
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

    public static class NoteRequest {
        private String title;
        private String content;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}
