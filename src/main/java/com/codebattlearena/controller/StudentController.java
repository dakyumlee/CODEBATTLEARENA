package com.codebattlearena.controller;

import com.codebattlearena.config.JwtUtil;
import com.codebattlearena.model.*;
import com.codebattlearena.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProblemRepository problemRepository;
    
    @Autowired
    private StudyNoteRepository studyNoteRepository;
    
    @Autowired
    private MaterialRepository materialRepository;
    
    @Autowired
    private JwtUtil jwtUtil;

    private Long getUserIdFromRequest(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String email = jwtUtil.extractEmail(token);
                User user = userRepository.findByEmail(email).orElse(null);
                return user != null ? user.getId() : null;
            }
        } catch (Exception e) {
            System.err.println("토큰 파싱 오류: " + e.getMessage());
        }
        return null;
    }

    @GetMapping("/profile")
    public Map<String, Object> getProfile(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return Map.of("error", "Unauthorized");
            }
            
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return Map.of("error", "User not found");
            }
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("name", user.getName());
            userInfo.put("email", user.getEmail());
            userInfo.put("role", user.getRole().toString());
            
            return Map.of("user", userInfo);
        } catch (Exception e) {
            return Map.of("error", "Failed to load profile: " + e.getMessage());
        }
    }

    @GetMapping("/ai-problem")
    public Map<String, Object> getAIProblem(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return Map.of("error", "Unauthorized");
            }
            
            Map<String, Object> problem = new HashMap<>();
            problem.put("id", 1);
            problem.put("title", "두 수의 합 구하기");
            problem.put("description", "두 정수 a, b를 입력받아 합을 출력하는 프로그램을 작성하세요.");
            problem.put("difficulty", "하");
            
            return Map.of("problem", problem);
        } catch (Exception e) {
            return Map.of("error", "Failed to load AI problem: " + e.getMessage());
        }
    }

    @GetMapping("/materials")
    public Map<String, Object> getMaterials(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return Map.of("error", "Unauthorized");
            }
            
            List<Material> materials = materialRepository.findAll();
            return Map.of("materials", materials);
        } catch (Exception e) {
            return Map.of("error", "Failed to load materials: " + e.getMessage());
        }
    }

    @GetMapping("/problems")
    public Map<String, Object> getProblems(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return Map.of("error", "Unauthorized");
            }
            
            List<Problem> problems = problemRepository.findAll();
            
            List<Map<String, Object>> problemData = problems.stream().map(problem -> {
                Map<String, Object> data = new HashMap<>();
                data.put("id", problem.getId());
                data.put("title", problem.getTitle());
                data.put("description", problem.getDescription());
                data.put("difficulty", problem.getDifficulty());
                data.put("status", "PENDING");
                data.put("feedback", null);
                return data;
            }).collect(Collectors.toList());
            
            return Map.of("problems", problemData);
        } catch (Exception e) {
            return Map.of("error", "Failed to load problems: " + e.getMessage());
        }
    }

    @GetMapping("/notes")
    public Map<String, Object> getNotes(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return Map.of("error", "Unauthorized");
            }
            
            List<StudyNote> notes = studyNoteRepository.findByUserIdOrderByCreatedAtDesc(userId);
            return Map.of("notes", notes);
        } catch (Exception e) {
            return Map.of("error", "Failed to load notes: " + e.getMessage());
        }
    }

    @PostMapping("/notes")
    public Map<String, Object> createNote(@RequestBody Map<String, String> noteData, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return Map.of("success", false, "message", "Unauthorized");
            }
            
            StudyNote note = new StudyNote();
            note.setUserId(userId);
            note.setTitle(noteData.get("title"));
            note.setContent(noteData.get("content"));
            note.setCreatedAt(LocalDateTime.now());
            note.setUpdatedAt(LocalDateTime.now());
            
            StudyNote savedNote = studyNoteRepository.save(note);
            
            return Map.of("success", true, "message", "노트가 저장되었습니다.", "note", savedNote);
        } catch (Exception e) {
            return Map.of("success", false, "message", "노트 저장 실패: " + e.getMessage());
        }
    }

    @PutMapping("/notes/{id}")
    public Map<String, Object> updateNote(@PathVariable Long id, @RequestBody Map<String, String> noteData, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return Map.of("success", false, "message", "Unauthorized");
            }
            
            StudyNote note = studyNoteRepository.findById(id).orElse(null);
            if (note == null || !note.getUserId().equals(userId)) {
                return Map.of("success", false, "message", "노트를 찾을 수 없습니다.");
            }
            
            note.setTitle(noteData.get("title"));
            note.setContent(noteData.get("content"));
            note.setUpdatedAt(LocalDateTime.now());
            
            StudyNote savedNote = studyNoteRepository.save(note);
            
            return Map.of("success", true, "message", "노트가 수정되었습니다.", "note", savedNote);
        } catch (Exception e) {
            return Map.of("success", false, "message", "노트 수정 실패: " + e.getMessage());
        }
    }

    @DeleteMapping("/notes/{id}")
    public Map<String, Object> deleteNote(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return Map.of("success", false, "message", "Unauthorized");
            }
            
            StudyNote note = studyNoteRepository.findById(id).orElse(null);
            if (note == null || !note.getUserId().equals(userId)) {
                return Map.of("success", false, "message", "노트를 찾을 수 없습니다.");
            }
            
            studyNoteRepository.delete(note);
            
            return Map.of("success", true, "message", "노트가 삭제되었습니다.");
        } catch (Exception e) {
            return Map.of("success", false, "message", "노트 삭제 실패: " + e.getMessage());
        }
    }
}
