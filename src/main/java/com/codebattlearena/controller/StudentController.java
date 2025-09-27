package com.codebattlearena.controller;

import com.codebattlearena.config.JwtUtil;
import com.codebattlearena.model.*;
import com.codebattlearena.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
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
    private SubmissionRepository submissionRepository;
    
    @Autowired
    private MaterialRepository materialRepository;
    
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private Environment env;

    private com.cloudinary.Cloudinary cloudinary;

    @PostConstruct
    public void init() {
        try {
            cloudinary = new com.cloudinary.Cloudinary();
            cloudinary.config.cloudName = "dgtxjgdit";
            cloudinary.config.apiKey = "838357254369448";
            cloudinary.config.apiSecret = "c5xRZ7AD5wTd6l0KBqTnaBYpWSU";
            cloudinary.config.secure = true;
        } catch (Exception e) {
            System.err.println("Cloudinary initialization failed: " + e.getMessage());
        }
    }

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
            
            return Map.of("name", user.getName(), "email", user.getEmail(), "role", user.getRole().toString());
        } catch (Exception e) {
            return Map.of("error", "Failed to load profile: " + e.getMessage());
        }
    }

    @GetMapping("/ai-problems/today")
    public Map<String, Object> getTodayAiProblem(HttpServletRequest request) {
    try {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Map.of("error", "Unauthorized");
        }
        
        Map<String, Object> todayProblem = Map.of(
            "id", "ai-today-" + LocalDateTime.now().getDayOfYear(),
            "title", "배열의 최댓값 찾기",
            "description", "주어진 정수 배열에서 최댓값을 찾는 함수를 작성하세요.\n\n입력: [3, 1, 4, 1, 5, 9, 2, 6]\n출력: 9\n\n힌트: 반복문을 사용하여 배열을 순회하면서 최댓값을 찾아보세요.",
            "difficulty", "하",
            "category", "배열",
            "timeLimit", 30,
            "points", 100
        );
        
        return Map.of("success", true, "problem", todayProblem);
    } catch (Exception e) {
        return Map.of("error", "AI 문제를 불러올 수 없습니다: " + e.getMessage());
    }
}

    @GetMapping("/today")
    public Map<String, Object> getTodayData(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return Map.of("error", "Unauthorized");
            }
            
            List<Material> allMaterials = materialRepository.findAll();
            
            List<Map<String, Object>> materialData = allMaterials.stream().map(material -> {
                Map<String, Object> data = new HashMap<>();
                data.put("id", material.getId());
                data.put("title", material.getTitle());
                data.put("description", material.getDescription());
                data.put("fileType", material.getFileType());
                data.put("fileSize", material.getFileSize());
                data.put("originalFilename", material.getOriginalFilename());
                data.put("createdAt", material.getCreatedAt());
                data.put("filePath", material.getFilePath());
                return data;
            }).collect(Collectors.toList());
            
            return Map.of("materials", materialData);
        } catch (Exception e) {
            return Map.of("error", "Failed to load today data: " + e.getMessage());
        }
    }

    @GetMapping("/materials/{id}/download")
    public ResponseEntity<Void> downloadMaterial(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Material material = materialRepository.findById(id).orElse(null);
            if (material == null) {
                return ResponseEntity.notFound().build();
            }

            material.setDownloadCount(material.getDownloadCount() + 1);
            materialRepository.save(material);

            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", material.getFilePath())
                    .build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/ai-problems")
    public Map<String, Object> getAiProblems(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return Map.of("error", "Unauthorized");
            }
            
            List<Map<String, Object>> aiProblems = Arrays.asList(
                Map.of(
                    "id", "ai-1",
                    "title", "배열의 최댓값 찾기",
                    "description", "주어진 정수 배열에서 최댓값을 찾는 함수를 작성하세요.\n\n입력: [3, 1, 4, 1, 5, 9, 2, 6]\n출력: 9\n\n힌트: 반복문을 사용하여 배열을 순회하면서 최댓값을 찾아보세요.",
                    "difficulty", "하",
                    "category", "배열",
                    "timeLimit", 30,
                    "points", 100
                ),
                Map.of(
                    "id", "ai-2", 
                    "title", "문자열 뒤집기",
                    "description", "주어진 문자열을 뒤집어 반환하는 함수를 작성하세요.\n\n입력: \"Hello\"\n출력: \"olleH\"\n\n힌트: 문자열의 끝에서부터 시작까지 역순으로 문자를 이어붙이면 됩니다.",
                    "difficulty", "하",
                    "category", "문자열",
                    "timeLimit", 20,
                    "points", 80
                ),
                Map.of(
                    "id", "ai-3", 
                    "title", "피보나치 수열",
                    "description", "n번째 피보나치 수를 구하는 함수를 작성하세요.\n\n입력: 10\n출력: 55\n\n설명: 피보나치 수열은 0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, ... 입니다.\n각 수는 앞의 두 수의 합으로 이루어집니다.",
                    "difficulty", "중",
                    "category", "DP",
                    "timeLimit", 45,
                    "points", 150
                )
            );
            
            return Map.of("success", true, "problems", aiProblems);
        } catch (Exception e) {
            return Map.of("error", "AI 문제를 불러올 수 없습니다: " + e.getMessage());
        }
    }

    @GetMapping("/teacher-problems")
    public Map<String, Object> getTeacherProblems(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return Map.of("error", "Unauthorized");
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
                
                if ("QUIZ".equals(problem.getType())) {
                    problemData.put("optionA", problem.getOptionA());
                    problemData.put("optionB", problem.getOptionB());
                    problemData.put("optionC", problem.getOptionC());
                    problemData.put("optionD", problem.getOptionD());
                }
                
                Optional<Submission> submissionOpt = submissionRepository.findByUserIdAndProblemId(userId, problem.getId());
                boolean submitted = submissionOpt.isPresent();
                boolean graded = submissionOpt.isPresent() && "GRADED".equals(submissionOpt.get().getStatus());
                
                problemData.put("submitted", submitted);
                problemData.put("graded", graded);
                
                if (submissionOpt.isPresent()) {
                    Submission submission = submissionOpt.get();
                    problemData.put("score", submission.getScore());
                    problemData.put("feedback", submission.getFeedback());
                    problemData.put("submissionStatus", submission.getStatus());
                }
                
                problemList.add(problemData);
            }
            
            return Map.of("problems", problemList);
        } catch (Exception e) {
            return Map.of("error", "Failed to load teacher problems: " + e.getMessage());
        }
    }

    @GetMapping("/problem/{id}")
    public Map<String, Object> getProblemDetail(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return Map.of("error", "Unauthorized");
            }
            
            Problem problem = problemRepository.findById(id).orElse(null);
            if (problem == null) {
                return Map.of("error", "문제를 찾을 수 없습니다.");
            }
            
            Map<String, Object> problemData = new HashMap<>();
            problemData.put("id", problem.getId());
            problemData.put("title", problem.getTitle());
            problemData.put("description", problem.getDescription());
            problemData.put("difficulty", problem.getDifficulty());
            problemData.put("timeLimit", problem.getTimeLimit());
            problemData.put("points", problem.getPoints());
            problemData.put("type", problem.getType());
            problemData.put("createdAt", problem.getCreatedAt());
            
            if ("QUIZ".equals(problem.getType())) {
                problemData.put("optionA", problem.getOptionA());
                problemData.put("optionB", problem.getOptionB());
                problemData.put("optionC", problem.getOptionC());
                problemData.put("optionD", problem.getOptionD());
            }
            
            Optional<Submission> submissionOpt = submissionRepository.findByUserIdAndProblemId(userId, problem.getId());
            if (submissionOpt.isPresent()) {
                Submission submission = submissionOpt.get();
                problemData.put("submitted", true);
                problemData.put("submissionAnswer", submission.getAnswer());
                problemData.put("submissionScore", submission.getScore());
                problemData.put("submissionFeedback", submission.getFeedback());
                problemData.put("submissionStatus", submission.getStatus());
                problemData.put("submittedAt", submission.getSubmittedAt());
            } else {
                problemData.put("submitted", false);
            }
            
            return Map.of("success", true, "problem", problemData);
        } catch (Exception e) {
            return Map.of("success", false, "message", "문제 상세 정보를 불러올 수 없습니다: " + e.getMessage());
        }
    }

    @PostMapping("/submit-answer")
    public Map<String, Object> submitAnswer(
            @RequestParam("problemId") Long problemId,
            @RequestParam(value = "answer", required = false) String answer,
            @RequestParam(value = "file", required = false) MultipartFile file,
            HttpServletRequest request) {
        
        try {
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return Map.of("success", false, "message", "인증이 필요합니다.");
            }
            
            if ((answer == null || answer.trim().isEmpty()) && (file == null || file.isEmpty())) {
                return Map.of("success", false, "message", "텍스트 답안을 작성하거나 파일을 첨부해주세요.");
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
            submission.setSubmittedAt(LocalDateTime.now());
            submission.setStatus("PENDING");
            
            if (answer != null && !answer.trim().isEmpty()) {
                submission.setAnswer(answer);
            }
            
            if (file != null && !file.isEmpty()) {
                try {
                    if (cloudinary != null) {
                        String originalFilename = file.getOriginalFilename();
                        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                                Map.of("folder", "codebattlearena/submissions",
                                        "public_id", System.currentTimeMillis() + "_" + userId + "_" + problemId,
                                        "resource_type", "auto"));
                        
                        String fileUrl = (String) uploadResult.get("secure_url");
                        String existingAnswer = submission.getAnswer();
                        String combinedAnswer = (existingAnswer != null ? existingAnswer + "\n\n" : "") + 
                                              "첨부 파일: " + originalFilename + "\n파일 링크: " + fileUrl;
                        submission.setAnswer(combinedAnswer);
                    } else {
                        return Map.of("success", false, "message", "파일 업로드 서비스가 설정되지 않았습니다.");
                    }
                } catch (Exception e) {
                    return Map.of("success", false, "message", "파일 업로드 실패: " + e.getMessage());
                }
            }
            
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
