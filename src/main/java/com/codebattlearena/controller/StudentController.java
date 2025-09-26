package com.codebattlearena.controller;

import com.codebattlearena.model.StudyNote;
import com.codebattlearena.repository.StudyNoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    @Autowired
    private StudyNoteRepository studyNoteRepository;

    @GetMapping("/today")
    public Map<String, Object> getTodayContent() {
        Map<String, Object> response = new HashMap<>();
        
        Map<String, Object> aiProblem = new HashMap<>();
        aiProblem.put("id", "ai_daily_001");
        aiProblem.put("title", "오늘의 AI 추천 문제");
        aiProblem.put("description", "AI가 분석한 맞춤형 문제입니다");
        aiProblem.put("difficulty", "중");
        aiProblem.put("points", 20);
        
        response.put("aiProblem", aiProblem);
        response.put("materials", new ArrayList<>());
        response.put("submissions", new ArrayList<>());
        
        return response;
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("solvedProblems", 23);
        stats.put("battleWins", 12);
        stats.put("accuracy", 76);
        stats.put("studyDays", 15);
        return stats;
    }

    @GetMapping("/battle-stats")
    public Map<String, Object> getBattleStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("rating", 1250);
        stats.put("wins", 12);
        stats.put("losses", 8);
        return stats;
    }

    @GetMapping("/practice-stats")
    public Map<String, Object> getPracticeStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAttempts", 45);
        stats.put("solvedCount", 23);
        stats.put("successRate", 51);
        stats.put("avgScore", 78);
        return stats;
    }

    @GetMapping("/notes")
    public List<Map<String, Object>> getNotes() {
        try {
            List<StudyNote> notes = studyNoteRepository.findAll();
            List<Map<String, Object>> noteList = new ArrayList<>();
            
            for (StudyNote note : notes) {
                Map<String, Object> noteMap = new HashMap<>();
                noteMap.put("id", note.getId());
                noteMap.put("title", note.getTitle());
                noteMap.put("content", note.getContent());
                noteMap.put("createdAt", note.getCreatedAt().toString());
                noteMap.put("updatedAt", note.getUpdatedAt().toString());
                noteList.add(noteMap);
            }
            
            return noteList;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @PostMapping("/notes")
    public Map<String, Object> createNote(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            StudyNote note = new StudyNote();
            note.setUserId(1L); // 임시 사용자 ID
            note.setTitle(request.get("title"));
            note.setContent(request.get("content"));
            note.setCreatedAt(LocalDateTime.now());
            note.setUpdatedAt(LocalDateTime.now());
            
            studyNoteRepository.save(note);
            
            response.put("success", true);
            response.put("message", "노트가 저장되었습니다");
            response.put("id", note.getId());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "저장 실패: " + e.getMessage());
        }
        
        return response;
    }

    @PutMapping("/notes/{id}")
    public Map<String, Object> updateNote(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<StudyNote> noteOpt = studyNoteRepository.findById(id);
            if (noteOpt.isPresent()) {
                StudyNote note = noteOpt.get();
                note.setTitle(request.get("title"));
                note.setContent(request.get("content"));
                note.setUpdatedAt(LocalDateTime.now());
                
                studyNoteRepository.save(note);
                
                response.put("success", true);
                response.put("message", "노트가 수정되었습니다");
            } else {
                response.put("success", false);
                response.put("message", "노트를 찾을 수 없습니다");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "수정 실패: " + e.getMessage());
        }
        
        return response;
    }

    @DeleteMapping("/notes/{id}")
    public Map<String, Object> deleteNote(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            studyNoteRepository.deleteById(id);
            response.put("success", true);
            response.put("message", "노트가 삭제되었습니다");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "삭제 실패: " + e.getMessage());
        }
        
        return response;
    }
}
