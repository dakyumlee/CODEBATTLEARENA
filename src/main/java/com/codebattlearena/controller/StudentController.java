package com.codebattlearena.controller;

import com.codebattlearena.model.*;
import com.codebattlearena.repository.*;
import com.codebattlearena.service.AiProblemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private AiProblemService aiProblemService;

    private final Path fileStorageLocation = Paths.get(System.getProperty("java.io.tmpdir"), "uploads").toAbsolutePath().normalize();

    private Long getUserIdFromSession(HttpSession session) {
        try {
            Object userId = session.getAttribute("userId");
            Object userRole = session.getAttribute("userRole");
            
            if (userId != null && "STUDENT".equals(userRole)) {
                return (Long) userId;
            }
        } catch (Exception e) {
            System.err.println("세션 확인 오류: " + e.getMessage());
        }
        return null;
    }

    @GetMapping("/profile")
    public Map<String, Object> getProfile(HttpSession session) {
        try {
            Long userId = getUserIdFromSession(session);
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
    public Map<String, Object> getTodayAiProblem(HttpSession session) {
        try {
            Long userId = getUserIdFromSession(session);
            if (userId == null) {
                return Map.of("error", "Unauthorized");
            }
            
            Map<String, Object> problem = aiProblemService.generateProblem("중", "기본");
            return Map.of("success", true, "problem", problem);
        } catch (Exception e) {
            return Map.of("error", "AI 문제를 불러올 수 없습니다: " + e.getMessage());
        }
    }

    @PostMapping("/ai-problem/generate")
    public Map<String, Object> generateAiProblem(@RequestBody Map<String, String> request, HttpSession session) {
        try {
            Long userId = getUserIdFromSession(session);
            if (userId == null) {
                return Map.of("error", "Unauthorized");
            }
            
            String difficulty = request.getOrDefault("difficulty", "중");
            String topic = request.getOrDefault("topic", "기본");
            
            Map<String, Object> problem = aiProblemService.generateProblem(difficulty, topic);
            return Map.of("success", true, "problem", problem);
        } catch (Exception e) {
            return Map.of("error", "AI 문제 생성 실패: " + e.getMessage());
        }
    }

    @GetMapping("/practice-problems")
    public Map<String, Object> getPracticeProblems(HttpSession session) {
        try {
            Long userId = getUserIdFromSession(session);
            if (userId == null) {
                return Map.of("error", "Unauthorized");
            }
            
            List<Problem> problems = problemRepository.findAll();
            
            List<Map<String, Object>> practiceProblems = new ArrayList<>();
            for (Problem problem : problems) {
                Map<String, Object> problemData = new HashMap<>();
                problemData.put("id", problem.getId());
                problemData.put("title", problem.getTitle());
                problemData.put("description", problem.getDescription());
                problemData.put("difficulty", problem.getDifficulty());
                problemData.put("points", problem.getPoints());
                problemData.put("type", problem.getType());
                
                Optional<Submission> submission = submissionRepository.findByUserIdAndProblemId(userId, problem.getId());
                problemData.put("solved", submission.isPresent() && "GRADED".equals(submission.get().getStatus()));
                problemData.put("score", submission.map(Submission::getScore).orElse(null));
                
                practiceProblems.add(problemData);
            }
            
            return Map.of("problems", practiceProblems);
        } catch (Exception e) {
            return Map.of("error", "문제를 불러올 수 없습니다: " + e.getMessage());
        }
    }

    @GetMapping("/today")
    public Map<String, Object> getTodayData(HttpSession session) {
        try {
            Long userId = getUserIdFromSession(session);
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
                data.put("downloadCount", material.getDownloadCount());
                return data;
            }).collect(Collectors.toList());
            
            return Map.of("materials", materialData);
        } catch (Exception e) {
            return Map.of("error", "Failed to load today data: " + e.getMessage());
        }
    }

    @GetMapping("/materials/{id}/download")
    public ResponseEntity<Resource> downloadMaterial(@PathVariable Long id, HttpSession session) {
        try {
            Long userId = getUserIdFromSession(session);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Material material = materialRepository.findById(id).orElse(null);
            if (material == null) {
                return ResponseEntity.notFound().build();
            }

            Path filePath = this.fileStorageLocation.resolve(material.getLocalFilePath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                material.setDownloadCount(material.getDownloadCount() + 1);
                materialRepository.save(material);

                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + material.getOriginalFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/materials/{id}/preview")
    public ResponseEntity<Resource> previewMaterial(@PathVariable Long id, HttpSession session) {
        try {
            Long userId = getUserIdFromSession(session);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Material material = materialRepository.findById(id).orElse(null);
            if (material == null) {
                return ResponseEntity.notFound().build();
            }

            Path filePath = this.fileStorageLocation.resolve(material.getLocalFilePath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                String fileType = material.getFileType() != null ? material.getFileType().toLowerCase() : "";
                MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
                
                if (fileType.equals("pdf")) {
                    mediaType = MediaType.APPLICATION_PDF;
                } else if (Arrays.asList("jpg", "jpeg", "png", "gif", "bmp").contains(fileType)) {
                    mediaType = MediaType.IMAGE_JPEG;
                }

                return ResponseEntity.ok()
                        .contentType(mediaType)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + material.getOriginalFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/teacher-problems")
    public Map<String, Object> getTeacherProblems(HttpSession session) {
        try {
            Long userId = getUserIdFromSession(session);
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
                problemData.put("createdAt", problem.getCreatedAt());
                
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
    public Map<String, Object> getProblemDetail(@PathVariable Long id, HttpSession session) {
        try {
            Long userId = getUserIdFromSession(session);
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
            @RequestParam("answer") String answer,
            HttpSession session) {
        
        try {
            Long userId = getUserIdFromSession(session);
            if (userId == null) {
                return Map.of("success", false, "message", "인증이 필요합니다.");
            }
            
            if (answer == null || answer.trim().isEmpty()) {
                return Map.of("success", false, "message", "답안을 작성해주세요.");
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
            
            if ("QUIZ".equals(problem.getType()) && problem.getCorrectAnswer() != null) {
                boolean isCorrect = problem.getCorrectAnswer().equalsIgnoreCase(answer.trim());
                submission.setScore(isCorrect ? problem.getPoints() : 0);
                submission.setStatus("GRADED");
                submission.setFeedback(isCorrect ? "정답입니다!" : "오답입니다. 정답: " + problem.getCorrectAnswer());
                submission.setGradedAt(LocalDateTime.now());
            }
            
            submissionRepository.save(submission);
            
            return Map.of("success", true, "message", "답안이 제출되었습니다.");
        } catch (Exception e) {
            return Map.of("success", false, "message", "제출 실패: " + e.getMessage());
        }
    }

    @GetMapping("/notes")
    public List<StudyNote> getNotes(HttpSession session) {
        try {
            Long userId = getUserIdFromSession(session);
            if (userId == null) {
                return new ArrayList<>();
            }
            
            return studyNoteRepository.findByUserIdOrderByCreatedAtDesc(userId);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @PostMapping("/notes")
    public Map<String, Object> createNote(@RequestBody Map<String, String> noteData, HttpSession session) {
        try {
            Long userId = getUserIdFromSession(session);
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
    public Map<String, Object> updateNote(@PathVariable Long id, @RequestBody Map<String, String> noteData, HttpSession session) {
        try {
            Long userId = getUserIdFromSession(session);
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

    @GetMapping("/student/materials/{id}/preview")
    public String previewMaterial(@PathVariable Long id, Model model, HttpSession session) {
        Object userId = session.getAttribute("userId");
        Object userRole = session.getAttribute("userRole");
        
        if (userId == null || !"STUDENT".equals(userRole)) {
            return "redirect:/";
        }
        
        Material material = materialRepository.findById(id).orElse(null);
        
        if (material == null) {
            model.addAttribute("error", "자료를 찾을 수 없습니다.");
            return "student/preview-error";
        }
        
        model.addAttribute("material", material);
        
        String fileType = material.getFileType() != null ? material.getFileType().toLowerCase() : "";
        if (fileType.equals("pdf")) {
            return "student/preview-pdf";
        } else if (Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "svg").contains(fileType)) {
            return "student/preview-image";
        } else {
            return "student/preview-general";
        }
    }

    @DeleteMapping("/notes/{id}")
    public Map<String, Object> deleteNote(@PathVariable Long id, HttpSession session) {
        try {
            Long userId = getUserIdFromSession(session);
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