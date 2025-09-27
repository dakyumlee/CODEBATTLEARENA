package com.codebattlearena.controller;

import com.codebattlearena.config.JwtUtil;
import com.codebattlearena.model.*;
import com.codebattlearena.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private StudyNoteRepository studyNoteRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

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
            System.out.println("Cloudinary initialized successfully");
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
                if (!jwtUtil.isTokenValid(token)) {
                    return null;
                }
                String email = jwtUtil.extractEmail(token);
                User user = userRepository.findByEmail(email).orElse(null);
                if (user != null && user.getRole() == UserRole.TEACHER) {
                    return user.getId();
                }
            }
        } catch (Exception e) {
            System.err.println("토큰 파싱 오류: " + e.getMessage());
        }
        return null;
    }

    @GetMapping("/materials")
    public ResponseEntity<Map<String, Object>> getMaterials(HttpServletRequest request) {
        try {
            Long teacherId = getUserIdFromRequest(request);
            if (teacherId == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }

            List<Material> materials = materialRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId);

            List<Map<String, Object>> materialData = materials.stream().map(material -> {
                Map<String, Object> data = new HashMap<>();
                data.put("id", material.getId());
                data.put("title", material.getTitle());
                data.put("description", material.getDescription());
                data.put("fileSize", material.getFileSize());
                data.put("fileType", material.getFileType());
                data.put("originalFilename", material.getOriginalFilename());
                data.put("downloadCount", material.getDownloadCount());
                data.put("createdAt", material.getCreatedAt());
                data.put("filePath", material.getFilePath());
                return data;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(Map.of("materials", materialData));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "자료를 불러올 수 없습니다: " + e.getMessage()));
        }
    }

    @PostMapping("/materials")
    public Map<String, Object> uploadMaterial(
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {

        try {
            Long teacherId = getUserIdFromRequest(request);
            if (teacherId == null) {
                return Map.of("success", false, "message", "인증이 필요합니다.");
            }

            if (file.isEmpty()) {
                return Map.of("success", false, "message", "파일을 선택해주세요.");
            }

            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);

            if (!isAllowedFileType(fileExtension)) {
                return Map.of("success", false, "message", "지원하지 않는 파일 형식입니다.");
            }

            if (cloudinary == null) {
                return Map.of("success", false, "message", "클라우드 스토리지가 설정되지 않았습니다.");
            }

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    Map.of("folder", "codebattlearena/materials",
                            "public_id",
                            System.currentTimeMillis() + "_" + originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_"),
                            "resource_type", "auto"));

            String cloudinaryUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");

            Material material = new Material();
            material.setTeacherId(teacherId);
            material.setTitle(title);
            material.setDescription(description);
            material.setFilePath(cloudinaryUrl);
            material.setFileType(fileExtension);
            material.setFileSize(file.getSize());
            material.setOriginalFilename(originalFilename);
            material.setCloudinaryPublicId(publicId);
            material.setCreatedAt(LocalDateTime.now());

            materialRepository.save(material);

            return Map.of("success", true, "message", "자료가 업로드되었습니다.");

        } catch (Exception e) {
            System.err.println("Upload error: " + e.getMessage());
            e.printStackTrace();
            return Map.of("success", false, "message", "업로드 실패: " + e.getMessage());
        }
    }

    @GetMapping("/materials/{id}/download")
    public ResponseEntity<Void> downloadMaterial(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long teacherId = getUserIdFromRequest(request);
            if (teacherId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Material material = materialRepository.findById(id).orElse(null);
            if (material == null || !material.getTeacherId().equals(teacherId)) {
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

    @DeleteMapping("/materials/{id}")
    public Map<String, Object> deleteMaterial(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long teacherId = getUserIdFromRequest(request);
            if (teacherId == null) {
                return Map.of("success", false, "message", "인증이 필요합니다.");
            }

            Material material = materialRepository.findById(id).orElse(null);
            if (material == null || !material.getTeacherId().equals(teacherId)) {
                return Map.of("success", false, "message", "자료를 찾을 수 없습니다.");
            }

            try {
                if (cloudinary != null && material.getCloudinaryPublicId() != null) {
                    cloudinary.uploader().destroy(material.getCloudinaryPublicId(), Map.of());
                }
            } catch (Exception e) {
                System.err.println("Cloudinary 파일 삭제 실패: " + e.getMessage());
            }

            materialRepository.delete(material);
            return Map.of("success", true, "message", "자료가 삭제되었습니다.");
        } catch (Exception e) {
            return Map.of("success", false, "message", "삭제 실패: " + e.getMessage());
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    private boolean isAllowedFileType(String extension) {
        Set<String> allowedTypes = Set.of(
                "pdf", "doc", "docx", "ppt", "pptx", "xls", "xlsx",
                "txt", "rtf", "hwp",
                "jpg", "jpeg", "png", "gif", "bmp", "svg",
                "mp4", "avi", "mov", "wmv", "flv", "webm",
                "mp3", "wav", "ogg", "m4a",
                "zip", "rar", "7z", "tar", "gz",
                "java", "js", "html", "css", "json", "xml",
                "py", "cpp", "c", "h", "cs", "php", "rb", "go");
        return allowedTypes.contains(extension);
    }

    @PostMapping("/problems")
    public Map<String, Object> createProblem(@RequestBody Map<String, Object> problemData, HttpServletRequest request) {
        try {
            Long teacherId = getUserIdFromRequest(request);
            if (teacherId == null) {
                return Map.of("success", false, "message", "인증이 필요합니다.");
            }

            Problem problem = new Problem();
            problem.setTeacherId(teacherId);
            problem.setTitle((String) problemData.get("title"));
            problem.setDescription((String) problemData.get("description"));
            problem.setDifficulty((String) problemData.get("difficulty"));
            problem.setType((String) problemData.get("type"));
            problem.setTimeLimit(
                    problemData.get("timeLimit") != null ? Integer.parseInt(problemData.get("timeLimit").toString())
                            : 60);
            problem.setPoints(
                    problemData.get("points") != null ? Integer.parseInt(problemData.get("points").toString()) : 100);
            problem.setCreatedAt(LocalDateTime.now());

            if ("QUIZ".equals(problemData.get("type"))) {
                problem.setOptionA((String) problemData.get("optionA"));
                problem.setOptionB((String) problemData.get("optionB"));
                problem.setOptionC((String) problemData.get("optionC"));
                problem.setOptionD((String) problemData.get("optionD"));
                problem.setCorrectAnswer((String) problemData.get("correctAnswer"));
            }

            Problem savedProblem = problemRepository.save(problem);

            if (messagingTemplate != null) {
                Map<String, Object> notification = new HashMap<>();
                notification.put("type", "NEW_PROBLEM");
                notification.put("title", "새로운 " + getTypeKorean((String) problemData.get("type")) + "가 출제되었습니다!");
                notification.put("message", savedProblem.getTitle());
                notification.put("problemId", savedProblem.getId());
                notification.put("timestamp", LocalDateTime.now().toString());
                messagingTemplate.convertAndSend("/topic/notifications", notification);
            }

            return Map.of("success", true, "message", getTypeKorean((String) problemData.get("type")) + "가 성공적으로 출제되었습니다.", "problem", savedProblem);
        } catch (Exception e) {
            return Map.of("success", false, "message", "오류: " + e.getMessage());
        }
    }

    private String getTypeKorean(String type) {
        switch (type) {
            case "QUIZ": return "퀴즈";
            case "EXAM": return "시험";
            default: return "문제";
        }
    }

    @GetMapping("/problems")
    public ResponseEntity<Map<String, Object>> getMyProblems(HttpServletRequest request) {
        try {
            Long teacherId = getUserIdFromRequest(request);
            if (teacherId == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }

            List<Problem> problems = problemRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId);
            return ResponseEntity.ok(Map.of("problems", problems));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to load problems: " + e.getMessage()));
        }
    }

    @GetMapping("/submissions")
    public ResponseEntity<Map<String, Object>> getSubmissions(HttpServletRequest request) {
        try {
            Long teacherId = getUserIdFromRequest(request);
            if (teacherId == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }

            List<Submission> submissions = submissionRepository.findSubmissionsByTeacher(teacherId);

            List<Map<String, Object>> submissionData = submissions.stream().map(submission -> {
                Map<String, Object> data = new HashMap<>();
                data.put("id", submission.getId());
                data.put("userId", submission.getUserId());
                data.put("problemId", submission.getProblemId());
                data.put("answer", submission.getAnswer());
                data.put("status", submission.getStatus());
                data.put("score", submission.getScore());
                data.put("feedback", submission.getFeedback());
                data.put("submittedAt", submission.getSubmittedAt());

                User student = userRepository.findById(submission.getUserId()).orElse(null);
                data.put("studentName", student != null ? student.getName() : "Unknown");

                Problem problem = problemRepository.findById(submission.getProblemId()).orElse(null);
                data.put("problemTitle", problem != null ? problem.getTitle() : "Unknown");
                data.put("problemType", problem != null ? problem.getType() : "PROBLEM");

                return data;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(Map.of("submissions", submissionData));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to load submissions: " + e.getMessage()));
        }
    }

    @PostMapping("/grade-submission")
    public Map<String, Object> gradeSubmission(@RequestBody Map<String, Object> gradeData, HttpServletRequest request) {
        try {
            Long teacherId = getUserIdFromRequest(request);
            if (teacherId == null) {
                return Map.of("success", false, "message", "인증이 필요합니다.");
            }

            Long submissionId = Long.parseLong(gradeData.get("submissionId").toString());
            Integer score = Integer.parseInt(gradeData.get("score").toString());
            String feedback = (String) gradeData.get("feedback");

            Submission submission = submissionRepository.findById(submissionId).orElse(null);
            if (submission == null) {
                return Map.of("success", false, "message", "제출물을 찾을 수 없습니다.");
            }

            submission.setScore(score);
            submission.setFeedback(feedback);
            submission.setGradedBy(teacherId);
            submission.setGradedAt(LocalDateTime.now());
            submission.setStatus("GRADED");

            submissionRepository.save(submission);

            return Map.of("success", true, "message", "채점이 완료되었습니다.");
        } catch (Exception e) {
            return Map.of("success", false, "message", "채점 실패: " + e.getMessage());
        }
    }

    @PutMapping("/problems/{id}")
    public Map<String, Object> updateProblem(@PathVariable Long id, @RequestBody Map<String, Object> problemData,
            HttpServletRequest request) {
        try {
            Long teacherId = getUserIdFromRequest(request);
            if (teacherId == null) {
                return Map.of("success", false, "message", "인증이 필요합니다.");
            }

            Problem problem = problemRepository.findById(id).orElse(null);
            if (problem == null || !problem.getTeacherId().equals(teacherId)) {
                return Map.of("success", false, "message", "문제를 찾을 수 없습니다.");
            }

            problem.setTitle((String) problemData.get("title"));
            problem.setDescription((String) problemData.get("description"));
            problem.setDifficulty((String) problemData.get("difficulty"));
            problem.setType((String) problemData.get("type"));
            problem.setTimeLimit(
                    problemData.get("timeLimit") != null ? Integer.parseInt(problemData.get("timeLimit").toString())
                            : 60);
            problem.setPoints(
                    problemData.get("points") != null ? Integer.parseInt(problemData.get("points").toString()) : 100);

            problemRepository.save(problem);

            return Map.of("success", true, "message", "문제가 수정되었습니다.");
        } catch (Exception e) {
            return Map.of("success", false, "message", "수정 실패: " + e.getMessage());
        }
    }

    @DeleteMapping("/problems/{id}")
    public Map<String, Object> deleteProblem(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long teacherId = getUserIdFromRequest(request);
            if (teacherId == null) {
                return Map.of("success", false, "message", "인증이 필요합니다.");
            }

            Problem problem = problemRepository.findById(id).orElse(null);
            if (problem == null || !problem.getTeacherId().equals(teacherId)) {
                return Map.of("success", false, "message", "문제를 찾을 수 없습니다.");
            }

            problemRepository.delete(problem);

            return Map.of("success", true, "message", "문제가 삭제되었습니다.");
        } catch (Exception e) {
            return Map.of("success", false, "message", "삭제 실패: " + e.getMessage());
        }
    }

    @GetMapping("/students")
    public Map<String, Object> getMyStudents(HttpServletRequest request) {
        try {
            Long teacherId = getUserIdFromRequest(request);
            if (teacherId == null) {
                return Map.of("error", "Unauthorized");
            }

            List<User> allStudents = userRepository.findByRole(UserRole.STUDENT);

            List<Map<String, Object>> studentData = allStudents.stream().map(student -> {
                Map<String, Object> data = new HashMap<>();
                data.put("id", student.getId());
                data.put("name", student.getName());
                data.put("email", student.getEmail());
                data.put("online", student.isOnlineStatus());
                data.put("lastActivity", student.getLastActivity());
                data.put("groupId", student.getGroupId());

                if (student.getGroupId() != null) {
                    Group group = groupRepository.findById(student.getGroupId()).orElse(null);
                    data.put("groupName", group != null ? group.getName() : "Unknown");
                } else {
                    data.put("groupName", "미배정");
                }

                return data;
            }).collect(Collectors.toList());

            return Map.of("students", studentData);
        } catch (Exception e) {
            return Map.of("error", "Failed to load students: " + e.getMessage());
        }
    }
}
