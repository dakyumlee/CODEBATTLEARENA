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
        stats.put("solvedProblems", 0);
        stats.put("battleWins", 0);
        stats.put("accuracy", 0);
        stats.put("studyDays", 0);
        return stats;
    }

    @GetMapping("/battle-stats")
    public Map<String, Object> getBattleStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("rating", 1000);
        stats.put("wins", 0);
        stats.put("losses", 0);
        return stats;
    }

    @GetMapping("/practice-stats")
    public Map<String, Object> getPracticeStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAttempts", 0);
        stats.put("solvedCount", 0);
        stats.put("successRate", 0);
        stats.put("avgScore", 0);
        return stats;
    }

    @GetMapping("/notes")
    public List<Map<String, Object>> getNotes() {
        return new ArrayList<>();
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
