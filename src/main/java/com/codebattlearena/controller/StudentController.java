package com.codebattlearena.controller;

import com.codebattlearena.config.JwtUtil;
import com.codebattlearena.model.StudyNote;
import com.codebattlearena.repository.StudyNoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    @Autowired
    private StudyNoteRepository studyNoteRepository;
    
    @Autowired
    private JwtUtil jwtUtil;

    private Long getUserIdFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtUtil.extractUserId(token);
        }
        return null;
    }

    @GetMapping("/today")
    public Map<String, Object> getTodayContent(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Unauthorized");
            return error;
        }
        
        Map<String, Object> response = new HashMap<>();
        
        Map<String, Object> aiProblem = new HashMap<>();
        aiProblem.put("id", "ai_daily_" + userId);
        aiProblem.put("title", "오늘의 AI 추천 문제");
        aiProblem.put("description", "AI가 분석한 맞춤형 문제입니다");
        aiProblem.put("difficulty", "중");
        aiProblem.put("points", 20);
        
        response.put("aiProblem", aiProblem);
        response.put("materials", new ArrayList<>());
        response.put("submissions", new ArrayList<>());
        
        return response;
    }

    @GetMapping("/teacher-problems")
    public List<Map<String, Object>> getTeacherProblems(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return new ArrayList<>();
        }
        
        List<Map<String, Object>> problems = new ArrayList<>();
        
        Map<String, Object> problem = new HashMap<>();
        problem.put("id", 1);
        problem.put("title", "실시간 테스트 문제");
        problem.put("description", "강사님이 방금 출제한 문제입니다");
        problem.put("difficulty", "중");
        problem.put("timeLimit", 30);
        problem.put("createdAt", System.currentTimeMillis());
        problems.add(problem);
        
        return problems;
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Unauthorized");
            return error;
        }
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("solvedProblems", 23 + (userId.intValue() % 10));
        stats.put("battleWins", 12 + (userId.intValue() % 5));
        stats.put("accuracy", 76 + (userId.intValue() % 20));
        stats.put("studyDays", 15 + (userId.intValue() % 30));
        return stats;
    }

    @GetMapping("/battle-stats")
    public Map<String, Object> getBattleStats(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Unauthorized");
            return error;
        }
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("rating", 1250 + (userId.intValue() * 10));
        stats.put("wins", 12 + (userId.intValue() % 8));
        stats.put("losses", 8 + (userId.intValue() % 6));
        return stats;
    }

    @GetMapping("/practice-stats")
    public Map<String, Object> getPracticeStats(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Unauthorized");
            return error;
        }
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAttempts", 45 + (userId.intValue() % 20));
        stats.put("solvedCount", 23 + (userId.intValue() % 15));
        stats.put("successRate", 51 + (userId.intValue() % 40));
        stats.put("avgScore", 78 + (userId.intValue() % 20));
        return stats;
    }

    @GetMapping("/notes")
    public List<Map<String, Object>> getNotes(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return new ArrayList<>();
        }
        
        try {
            List<StudyNote> notes = studyNoteRepository.findByUserId(userId);
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
    public Map<String, Object> createNote(@RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        if (userId == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Unauthorized");
            return error;
        }
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            StudyNote note = new StudyNote();
            note.setUserId(userId);
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
    public Map<String, Object> updateNote(@PathVariable Long id, @RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        if (userId == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Unauthorized");
            return error;
        }
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<StudyNote> noteOpt = studyNoteRepository.findByIdAndUserId(id, userId);
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
    public Map<String, Object> deleteNote(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        if (userId == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Unauthorized");
            return error;
        }
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (studyNoteRepository.existsByIdAndUserId(id, userId)) {
                studyNoteRepository.deleteById(id);
                response.put("success", true);
                response.put("message", "노트가 삭제되었습니다");
            } else {
                response.put("success", false);
                response.put("message", "노트를 찾을 수 없습니다");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "삭제 실패: " + e.getMessage());
        }
        
        return response;
    }
}
