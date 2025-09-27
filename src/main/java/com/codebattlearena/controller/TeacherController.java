package com.codebattlearena.controller;

import com.codebattlearena.model.*;
import com.codebattlearena.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
    private SimpMessagingTemplate messagingTemplate;

    private final Path fileStorageLocation = Paths.get("src/main/resources/uploads").toAbsolutePath().normalize();

    public TeacherController() {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    private Long getUserIdFromSession(HttpSession session) {
        try {
            Object userId = session.getAttribute("userId");
            Object userRole = session.getAttribute("userRole");
            
            if (userId != null && "TEACHER".equals(userRole)) {
                return (Long) userId;
            }
        } catch (Exception e) {
            System.err.println("세션 확인 오류: " + e.getMessage());
        }
        return null;
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics(HttpSession session) {
        try {
            Long teacherId = getUserIdFromSession(session);
            if (teacherId == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }

            List<Group> myGroups = groupRepository.findByTeacherId(teacherId);
            List<Long> groupIds = myGroups.stream().map(Group::getId).collect(Collectors.toList());
            
            long totalStudents = 0;
            long onlineStudents = 0;
            
            if (!groupIds.isEmpty()) {
                totalStudents = userRepository.countByGroupIdIn(groupIds);
                onlineStudents = userRepository.countByGroupIdInAndOnlineStatusTrue(groupIds);
            }
            
            long totalProblems = problemRepository.countByTeacherId(teacherId);
            long totalGroups = myGroups.size();

            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalStudents", totalStudents);
            statistics.put("onlineStudents", onlineStudents);
            statistics.put("totalProblems", totalProblems);
            statistics.put("totalGroups", totalGroups);

            return ResponseEntity.ok(Map.of("statistics", statistics));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to load statistics: " + e.getMessage()));
        }
    }

    @GetMapping("/groups")
    public ResponseEntity<Map<String, Object>> getGroups(HttpSession session) {
        try {
            Long teacherId = getUserIdFromSession(session);
            if (teacherId == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }

            List<Group> groups = groupRepository.findByTeacherId(teacherId);

            List<Map<String, Object>> groupData = groups.stream().map(group -> {
                Map<String, Object> data = new HashMap<>();
                data.put("id", group.getId());
                data.put("name", group.getName());
                data.put("description", group.getDescription());
                data.put("createdAt", group.getCreatedAt());
                
                long studentCount = userRepository.countByGroupId(group.getId());
                data.put("studentCount", studentCount);
                
                return data;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(Map.of("groups", groupData));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to load groups: " + e.getMessage()));
        }
    }

    @PostMapping("/groups")
    public Map<String, Object> createGroup(@RequestBody Map<String, String> groupData, HttpSession session) {
        try {
            Long teacherId = getUserIdFromSession(session);
            if (teacherId == null) {
                return Map.of("success", false, "message", "인증이 필요합니다.");
            }

            Group group = new Group();
            group.setTeacherId(teacherId);
            group.setName(groupData.get("name"));
            group.setDescription(groupData.get("description"));
            group.setCreatedAt(LocalDateTime.now());

            groupRepository.save(group);

            return Map.of("success", true, "message", "그룹이 생성되었습니다.");
        } catch (Exception e) {
            return Map.of("success", false, "message", "그룹 생성 실패: " + e.getMessage());
        }
    }

    @GetMapping("/group/{groupId}/students")
    public ResponseEntity<Map<String, Object>> getGroupStudents(@PathVariable Long groupId, HttpSession session) {
        try {
            Long teacherId = getUserIdFromSession(session);
            if (teacherId == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }

            Group group = groupRepository.findById(groupId).orElse(null);
            if (group == null || !group.getTeacherId().equals(teacherId)) {
                return ResponseEntity.status(404).body(Map.of("error", "Group not found"));
            }

            List<User> students = userRepository.findByGroupId(groupId);

            List<Map<String, Object>> studentData = students.stream().map(student -> {
                Map<String, Object> data = new HashMap<>();
                data.put("id", student.getId());
                data.put("name", student.getName());
                data.put("email", student.getEmail());
                data.put("online", student.isOnlineStatus());
                data.put("lastActivity", student.getLastActivity());
                return data;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(Map.of("students", studentData));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to load group students: " + e.getMessage()));
        }
    }

    @PostMapping("/assign-student")
    public Map<String, Object> assignStudentToGroup(@RequestBody Map<String, Object> assignData, HttpSession session) {
        try {
            Long teacherId = getUserIdFromSession(session);
            if (teacherId == null) {
                return Map.of("success", false, "message", "인증이 필요합니다.");
            }

            Long studentId = Long.parseLong(assignData.get("studentId").toString());
            Long groupId = Long.parseLong(assignData.get("groupId").toString());

            Group group = groupRepository.findById(groupId).orElse(null);
            if (group == null || !group.getTeacherId().equals(teacherId)) {
                return Map.of("success", false, "message", "그룹을 찾을 수 없습니다.");
            }

            User student = userRepository.findById(studentId).orElse(null);
            if (student == null || student.getRole() != UserRole.STUDENT) {
                return Map.of("success", false, "message", "학생을 찾을 수 없습니다.");
            }

            student.setGroupId(groupId);
            userRepository.save(student);

            return Map.of("success", true, "message", "학생이 그룹에 배정되었습니다.");
        } catch (Exception e) {
            return Map.of("success", false, "message", "배정 실패: " + e.getMessage());
        }
    }

    @DeleteMapping("/groups/{id}")
    public Map<String, Object> deleteGroup(@PathVariable Long id, HttpSession session) {
        try {
            Long teacherId = getUserIdFromSession(session);
            if (teacherId == null) {
                return Map.of("success", false, "message", "인증이 필요합니다.");
            }

            Group group = groupRepository.findById(id).orElse(null);
            if (group == null || !group.getTeacherId().equals(teacherId)) {
                return Map.of("success", false, "message", "그룹을 찾을 수 없습니다.");
            }

            List<User> studentsInGroup = userRepository.findByGroupId(id);
            for (User student : studentsInGroup) {
                student.setGroupId(null);
                userRepository.save(student);
            }

            groupRepository.delete(group);
            return Map.of("success", true, "message", "그룹이 삭제되었습니다.");
        } catch (Exception e) {
            return Map.of("success", false, "message", "삭제 실패: " + e.getMessage());
        }
    }

    @GetMapping("/students")
    public Map<String, Object> getMyStudents(HttpSession session) {
        try {
            Long teacherId = getUserIdFromSession(session);
            if (teacherId == null) {
                return Map.of("error", "Unauthorized");
            }

            List<Group> myGroups = groupRepository.findByTeacherId(teacherId);
            List<Long> groupIds = myGroups.stream().map(Group::getId).collect(Collectors.toList());
            
            List<User> myStudents;
            if (groupIds.isEmpty()) {
                myStudents = userRepository.findByRoleAndGroupIdIsNull(UserRole.STUDENT);
            } else {
                myStudents = userRepository.findByGroupIdIn(groupIds);
                List<User> unassignedStudents = userRepository.findByRoleAndGroupIdIsNull(UserRole.STUDENT);
                myStudents.addAll(unassignedStudents);
            }

            List<Map<String, Object>> studentData = myStudents.stream().map(student -> {
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

    @GetMapping("/materials")
    public ResponseEntity<Map<String, Object>> getMaterials(HttpSession session) {
        try {
            Long teacherId = getUserIdFromSession(session);
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
            HttpSession session) {

        try {
            Long teacherId = getUserIdFromSession(session);
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

            String fileName = System.currentTimeMillis() + "_" + originalFilename;
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            Material material = new Material();
            material.setTeacherId(teacherId);
            material.setTitle(title);
            material.setDescription(description);
            material.setFilePath("/api/teacher/materials/" + materialRepository.count() + "/download");
            material.setFileType(fileExtension);
            material.setFileSize(file.getSize());
            material.setOriginalFilename(originalFilename);
            material.setLocalFilePath(fileName);
            material.setCreatedAt(LocalDateTime.now());

            Material savedMaterial = materialRepository.save(material);
            savedMaterial.setFilePath("/api/teacher/materials/" + savedMaterial.getId() + "/download");
            materialRepository.save(savedMaterial);

            return Map.of("success", true, "message", "자료가 업로드되었습니다.");

        } catch (Exception e) {
            System.err.println("Upload error: " + e.getMessage());
            e.printStackTrace();
            return Map.of("success", false, "message", "업로드 실패: " + e.getMessage());
        }
    }

    @GetMapping("/materials/{id}/download")
    public ResponseEntity<Resource> downloadMaterial(@PathVariable Long id, HttpSession session) {
        try {
            Long teacherId = getUserIdFromSession(session);
            if (teacherId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Material material = materialRepository.findById(id).orElse(null);
            if (material == null || !material.getTeacherId().equals(teacherId)) {
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
    public ResponseEntity<Map<String, Object>> previewMaterial(@PathVariable Long id, HttpSession session) {
        try {
            Long teacherId = getUserIdFromSession(session);
            if (teacherId == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }

            Material material = materialRepository.findById(id).orElse(null);
            if (material == null || !material.getTeacherId().equals(teacherId)) {
                return ResponseEntity.status(404).body(Map.of("error", "Material not found"));
            }

            Map<String, Object> previewData = new HashMap<>();
            previewData.put("id", material.getId());
            previewData.put("title", material.getTitle());
            previewData.put("description", material.getDescription());
            previewData.put("fileType", material.getFileType());
            previewData.put("originalFilename", material.getOriginalFilename());
            previewData.put("fileSize", material.getFileSize());
            previewData.put("downloadUrl", "/api/teacher/materials/" + id + "/download");

            return ResponseEntity.ok(previewData);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Preview failed: " + e.getMessage()));
        }
    }

    @DeleteMapping("/materials/{id}")
    public Map<String, Object> deleteMaterial(@PathVariable Long id, HttpSession session) {
        try {
            Long teacherId = getUserIdFromSession(session);
            if (teacherId == null) {
                return Map.of("success", false, "message", "인증이 필요합니다.");
            }

            Material material = materialRepository.findById(id).orElse(null);
            if (material == null || !material.getTeacherId().equals(teacherId)) {
                return Map.of("success", false, "message", "자료를 찾을 수 없습니다.");
            }

            try {
                Path filePath = this.fileStorageLocation.resolve(material.getLocalFilePath()).normalize();
                Files.deleteIfExists(filePath);
            } catch (Exception e) {
                System.err.println("파일 삭제 실패: " + e.getMessage());
            }

            materialRepository.delete(material);
            return Map.of("success", true, "message", "자료가 삭제되었습니다.");
        } catch (Exception e) {
            return Map.of("success", false, "message", "삭제 실패: " + e.getMessage());
        }
    }

    @GetMapping("/problems")
    public ResponseEntity<Map<String, Object>> getMyProblems(HttpSession session) {
        try {
            Long teacherId = getUserIdFromSession(session);
            if (teacherId == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }

            List<Problem> problems = problemRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId);
            return ResponseEntity.ok(Map.of("problems", problems));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to load problems: " + e.getMessage()));
        }
    }

    @PostMapping("/problems")
    public Map<String, Object> createProblem(@RequestBody Map<String, Object> problemData, HttpSession session) {
        try {
            Long teacherId = getUserIdFromSession(session);
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

    @GetMapping("/problems/{id}/detail")
    public ResponseEntity<Map<String, Object>> getProblemDetail(@PathVariable Long id, HttpSession session) {
        try {
            Long teacherId = getUserIdFromSession(session);
            if (teacherId == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }

            Problem problem = problemRepository.findById(id).orElse(null);
            if (problem == null || !problem.getTeacherId().equals(teacherId)) {
                return ResponseEntity.status(404).body(Map.of("error", "Problem not found"));
            }

            Map<String, Object> problemData = new HashMap<>();
            problemData.put("id", problem.getId());
            problemData.put("title", problem.getTitle());
            problemData.put("description", problem.getDescription());
            problemData.put("difficulty", problem.getDifficulty());
            problemData.put("type", problem.getType());
            problemData.put("timeLimit", problem.getTimeLimit());
            problemData.put("points", problem.getPoints());
            problemData.put("createdAt", problem.getCreatedAt());

            if ("QUIZ".equals(problem.getType())) {
                problemData.put("optionA", problem.getOptionA());
                problemData.put("optionB", problem.getOptionB());
                problemData.put("optionC", problem.getOptionC());
                problemData.put("optionD", problem.getOptionD());
                problemData.put("correctAnswer", problem.getCorrectAnswer());
            }

            return ResponseEntity.ok(Map.of("success", true, "problem", problemData));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to load problem: " + e.getMessage()));
        }
    }

    @PutMapping("/problems/{id}")
    public Map<String, Object> updateProblem(@PathVariable Long id, @RequestBody Map<String, Object> problemData,
            HttpSession session) {
        try {
            Long teacherId = getUserIdFromSession(session);
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
    public Map<String, Object> deleteProblem(@PathVariable Long id, HttpSession session) {
        try {
            Long teacherId = getUserIdFromSession(session);
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

    @GetMapping("/submissions")
    public ResponseEntity<Map<String, Object>> getSubmissions(HttpSession session) {
        try {
            Long teacherId = getUserIdFromSession(session);
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
    public Map<String, Object> gradeSubmission(@RequestBody Map<String, Object> gradeData, HttpSession session) {
        try {
            Long teacherId = getUserIdFromSession(session);
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

    private String getTypeKorean(String type) {
        switch (type) {
            case "QUIZ": return "퀴즈";
            case "EXAM": return "시험";
            default: return "문제";
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
}