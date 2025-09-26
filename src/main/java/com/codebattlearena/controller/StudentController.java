package com.codebattlearena.controller;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/student")
public class StudentController {

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
        stats.put("solvedProblems", 15);
        stats.put("battleWins", 8);
        stats.put("accuracy", 75);
        stats.put("studyDays", 12);
        return stats;
    }

    @GetMapping("/battle-stats")
    public Map<String, Object> getBattleStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("rating", 1250);
        stats.put("wins", 8);
        stats.put("losses", 5);
        return stats;
    }

    @GetMapping("/practice-stats")
    public Map<String, Object> getPracticeStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAttempts", 25);
        stats.put("solvedCount", 18);
        stats.put("successRate", 72);
        stats.put("avgScore", 84);
        return stats;
    }

    @GetMapping("/notes")
    public List<Map<String, Object>> getNotes() {
        List<Map<String, Object>> notes = new ArrayList<>();
        
        Map<String, Object> note1 = new HashMap<>();
        note1.put("id", 1);
        note1.put("title", "Java 기초 정리");
        note1.put("content", "변수, 조건문, 반복문에 대해 학습했습니다.");
        note1.put("createdAt", "2025-09-25T10:30:00");
        notes.add(note1);
        
        return notes;
    }

    @PostMapping("/notes")
    public Map<String, Object> createNote(@RequestBody Map<String, String> note) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "노트가 저장되었습니다");
        return response;
    }

    @PutMapping("/notes/{id}")
    public Map<String, Object> updateNote(@PathVariable Long id, @RequestBody Map<String, String> note) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "노트가 수정되었습니다");
        return response;
    }

    @DeleteMapping("/notes/{id}")
    public Map<String, Object> deleteNote(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "노트가 삭제되었습니다");
        return response;
    }
}
