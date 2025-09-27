package com.codebattlearena.controller;

import com.codebattlearena.config.JwtUtil;
import com.codebattlearena.model.*;
import com.codebattlearena.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;

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
    private SubmissionRepository submissionRepository;
    
    @Autowired
    private JwtUtil jwtUtil;

    private Long getUserIdFromRequest(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                if (request.getCookies() != null) {
                    for (var cookie : request.getCookies()) {
                        if ("authToken".equals(cookie.getName())) {
                            authHeader = "Bearer " + cookie.getValue();
                            break;
                        }
                    }
                }
            }
            
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

    @GetMapping("/auth-check")
    public Map<String, Object> checkAuth(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return Map.of("authenticated", false);
            }
            
            User user = userRepository.findById(userId).orElse(null);
            if (user == null || user.getRole() != UserRole.STUDENT) {
                return Map.of("authenticated", false);
            }
            
            return Map.of("authenticated", true, "userId", userId, "userName", user.getName());
        } catch (Exception e) {
            return Map.of("authenticated", false);
        }
    }

    @GetMapping("/today")
    public Map<String, Object> getTodayData(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return Map.of("error", "Unauthorized");
            }
            
            List<Map<String, Object>> aiProblems = Arrays.asList(
                Map.of(
                    "id", "ai-1",
                    "title", "배열의 최댓값 찾기",
                    "description", "주어진 정수 배열에서 최댓값을 찾는 함수를 작성하세요.",
                    "difficulty", "하",
                    "category", "배열"
                ),
                Map.of(
                    "id", "ai-2", 
                    "title", "문자열 뒤집기",
                    "description", "주어진 문자열을 뒤집어 반환하는 함수를 작성하세요.",
                    "difficulty", "하",
                    "category", "문자열"
                )
            );
            
            return Map.of("aiProblems", aiProblems);
        } catch (Exception e) {
            return Map.of("error", "Failed to load today data: " + e.getMessage());
        }
    }

    @GetMapping("/teacher-problems")
    public List<Map<String, Object>> getTeacherProblems(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return new ArrayList<>();
            }
            
            List<Problem> problems = problemRepository.findAll();
            List<Map<String, Object>> problemList = new ArrayList<>();
            
            for (Problem problem : problems) {
                Map<String, Object> problemData = new HashMap<>();
                problemData.put("id", problem.getId());
                problemData.put("title", problem.getTitle());
                problemData.put("description", problem.getDescription());
                problemData.put("difficulty", problem.getDifficulty());
                problemData.put("timeLimit", problem.getTimeLimit());
                problemData.put("points", problem.getPoints());
                problemData.put("type", problem.getType());
                
                boolean isSubmitted = submissionRepository.existsByUserIdAndProblemId(userId, problem.getId());
                problemData.put("isSubmitted", isSubmitted);
                
                problemList.add(problemData);
            }
            
            return problemList;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @PostMapping("/submit-answer")
    public Map<String, Object> submitAnswer(@RequestBody Map<String, Object> submissionData, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return Map.of("success", false, "message", "인증이 필요합니다.");
            }
            
            Long problemId = Long.parseLong(submissionData.get("problemId").toString());
            String answer = (String) submissionData.get("answer");
            
            if (answer == null || answer.trim().isEmpty()) {
                return Map.of("success", false, "message", "답안을 입력해주세요.");
            }
            
            if (submissionRepository.existsByUserIdAndProblemId(userId, problemId)) {
                return Map.of("success", false, "message", "이미 제출한 문제입니다.");
            }
            
            Problem problem = problemRepository.findById(problemId).orElse(null);
            if (problem == null) {
                return Map.of("success", false, "message", "문제를 찾을 수 없습니다.");
            }
            
            Submission submission = new Submission();
            submission.setUserId(userId);
            submission.setProblemId(problemId);
            submission.setAnswer(answer);
            submission.setSubmittedAt(LocalDateTime.now());
            submission.setStatus("PENDING");
            
            submissionRepository.save(submission);
            
            return Map.of("success", true, "message", "답안이 제출되었습니다. 채점을 기다려주세요.");
        } catch (Exception e) {
            return Map.of("success", false, "message", "제출 실패: " + e.getMessage());
        }
    }

    @GetMapping("/notes")
    public List<StudyNote> getNotes(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return new ArrayList<>();
            }
            
            return studyNoteRepository.findByUserIdOrderByCreatedAtDesc(userId);
        } catch (Exception e) {
            return new ArrayList<>();
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
            
            studyNoteRepository.save(note);
            
            return Map.of("success", true, "message", "노트가 저장되었습니다.");
        } catch (Exception e) {
            return Map.of("success", false, "message", "Error: " + e.getMessage());
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
            
            studyNoteRepository.save(note);
            
            return Map.of("success", true, "message", "노트가 수정되었습니다.");
        } catch (Exception e) {
            return Map.of("success", false, "message", "Error: " + e.getMessage());
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
            return Map.of("success", false, "message", "Error: " + e.getMessage());
        }
    }
}
