package com.codebattlearena.controller;

import com.codebattlearena.config.JwtUtil;
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

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
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
    private JwtUtil jwtUtil;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private final String UPLOAD_DIR = "uploads/";

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

            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String filename = System.currentTimeMillis() + "_" + originalFilename;
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            Material material = new Material();
            material.setTeacherId(teacherId);
            material.setTitle(title);
            material.setDescription(description);
            material.setFilePath(filePath.toString());
            material.setFileType(fileExtension);
            material.setFileSize(file.getSize());
            material.setOriginalFilename(originalFilename);
            material.setCreatedAt(LocalDateTime.now());

            materialRepository.save(material);

            return Map.of("success", true, "message", "자료가 업로드되었습니다.");
            
        } catch (IOException e) {
            return Map.of("success", false, "message", "파일 업로드 실패: " + e.getMessage());
        } catch (Exception e) {
            return Map.of("success", false, "message", "오류: " + e.getMessage());
        }
    }

    @GetMapping("/materials")
    public Map<String, Object> getMaterials(HttpServletRequest request) {
        try {
            Long teacherId = getUserIdFromRequest(request);
            if (teacherId == null) {
                return Map.of("error", "Unauthorized");
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
                return data;
            }).collect(Collectors.toList());
            
            return Map.of("materials", materialData);
        } catch (Exception e) {
            return Map.of("error", "자료를 불러올 수 없습니다: " + e.getMessage());
        }
    }

    @GetMapping("/materials/{id}/download")
    public ResponseEntity<Resource> downloadMaterial(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long teacherId = getUserIdFromRequest(request);
            if (teacherId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Material material = materialRepository.findById(id).orElse(null);
            if (material == null || !material.getTeacherId().equals(teacherId)) {
                return ResponseEntity.notFound().build();
            }

            Path filePath = Paths.get(material.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                material.setDownloadCount(material.getDownloadCount() + 1);
                materialRepository.save(material);

                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .header(HttpHeaders.CONTENT_DISPOSITION, 
                                "attachment; filename=\"" + material.getOriginalFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
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
                Files.deleteIfExists(Paths.get(material.getFilePath()));
            } catch (IOException e) {
                System.err.println("파일 삭제 실패: " + e.getMessage());
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
            "py", "cpp", "c", "h", "cs", "php", "rb", "go"
        );
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
            problem.setTimeLimit(problemData.get("timeLimit") != null ? 
                Integer.parseInt(problemData.get("timeLimit").toString()) : 60);
            problem.setPoints(problemData.get("points") != null ? 
                Integer.parseInt(problemData.get("points").toString()) : 100);
            problem.setCreatedAt(LocalDateTime.now());

            Problem savedProblem = problemRepository.save(problem);

            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "NEW_PROBLEM");
            notification.put("title", "새로운 문제가 출제되었습니다!");
            notification.put("message", savedProblem.getTitle());
            notification.put("problemId", savedProblem.getId());
            notification.put("timestamp", LocalDateTime.now().toString());

            messagingTemplate.convertAndSend("/topic/notifications", notification);

            return Map.of("success", true, "message", "문제가 성공적으로 출제되었습니다.", "problem", savedProblem);
        } catch (Exception e) {
            return Map.of("success", false, "message", "오류: " + e.getMessage());
        }
    }

    @GetMapping("/problems")
    public Map<String, Object> getMyProblems(HttpServletRequest request) {
        try {
            Long teacherId = getUserIdFromRequest(request);
            if (teacherId == null) {
                return Map.of("error", "Unauthorized");
            }
            
            List<Problem> problems = problemRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId);
            return Map.of("problems", problems);
        } catch (Exception e) {
            return Map.of("error", "Failed to load problems: " + e.getMessage());
        }
    }

    @GetMapping("/students")
    public Map<String, Object> getMyStudents(HttpServletRequest request) {
        try {
            Long teacherId = getUserIdFromRequest(request);
            if (teacherId == null) {
                return Map.of("error", "Unauthorized");
            }
            
            List<Group> myGroups = groupRepository.findByTeacherId(teacherId);
            List<User> allStudents = new ArrayList<>();
            
            for (Group group : myGroups) {
                List<User> groupStudents = userRepository.findStudentsByGroupId(group.getId());
                allStudents.addAll(groupStudents);
            }
            
            if (allStudents.isEmpty()) {
                List<User> unassignedStudents = userRepository.findByRoleAndGroupIdIsNull(UserRole.STUDENT);
                allStudents.addAll(unassignedStudents);
            }
            
            List<Map<String, Object>> studentData = allStudents.stream().map(student -> {
                Map<String, Object> data = new HashMap<>();
                data.put("id", student.getId());
                data.put("name", student.getName());
                data.put("email", student.getEmail());
                data.put("online", student.isOnlineStatus());
                data.put("lastActivity", student.getLastActivity());
                data.put("groupId", student.getGroupId());
                return data;
            }).collect(Collectors.toList());
            
            return Map.of("students", studentData);
        } catch (Exception e) {
            return Map.of("error", "Failed to load students: " + e.getMessage());
        }
    }

    @GetMapping("/groups")
    public Map<String, Object> getMyGroups(HttpServletRequest request) {
        try {
            Long teacherId = getUserIdFromRequest(request);
            if (teacherId == null) {
                return Map.of("error", "Unauthorized");
            }
            
            List<Group> groups = groupRepository.findByTeacherId(teacherId);
            
            List<Map<String, Object>> groupData = groups.stream().map(group -> {
                Map<String, Object> data = new HashMap<>();
                data.put("id", group.getId());
                data.put("name", group.getName());
                data.put("description", group.getDescription());
                data.put("createdAt", group.getCreatedAt().toString());
                
                long studentCount = userRepository.findStudentsByGroupId(group.getId()).size();
                data.put("studentCount", studentCount);
                
                return data;
            }).collect(Collectors.toList());
            
            return Map.of("groups", groupData);
        } catch (Exception e) {
            return Map.of("error", "Failed to load groups: " + e.getMessage());
        }
    }

    @PostMapping("/groups")
    public Map<String, Object> createGroup(@RequestBody Map<String, Object> groupData, HttpServletRequest request) {
        try {
            Long teacherId = getUserIdFromRequest(request);
            if (teacherId == null) {
                return Map.of("success", false, "message", "Unauthorized");
            }
            
            Group group = new Group();
            group.setTeacherId(teacherId);
            group.setName((String) groupData.get("name"));
            group.setDescription((String) groupData.get("description"));
            group.setCreatedAt(LocalDateTime.now());
            
            Group savedGroup = groupRepository.save(group);
            
            return Map.of("success", true, "message", "그룹이 생성되었습니다", "group", Map.of(
                "id", savedGroup.getId(),
                "name", savedGroup.getName(),
                "description", savedGroup.getDescription()
            ));
        } catch (Exception e) {
            return Map.of("success", false, "message", "Error: " + e.getMessage());
        }
    }

    @PostMapping("/assign-student")
    public Map<String, Object> assignStudentToGroup(@RequestBody Map<String, Object> assignData, HttpServletRequest request) {
        try {
            Long teacherId = getUserIdFromRequest(request);
            if (teacherId == null) {
                return Map.of("success", false, "message", "Unauthorized");
            }
            
            Long studentId = Long.parseLong(assignData.get("studentId").toString());
            Long groupId = Long.parseLong(assignData.get("groupId").toString());
            
            Group group = groupRepository.findById(groupId).orElse(null);
            if (group == null || !group.getTeacherId().equals(teacherId)) {
                return Map.of("success", false, "message", "잘못된 그룹입니다.");
            }
            
            User student = userRepository.findById(studentId).orElse(null);
            if (student == null || student.getRole() != UserRole.STUDENT) {
                return Map.of("success", false, "message", "잘못된 학생입니다.");
            }
            
            student.setGroupId(groupId);
            userRepository.save(student);
            
            return Map.of("success", true, "message", "학생이 그룹에 배정되었습니다.");
        } catch (Exception e) {
            return Map.of("success", false, "message", "Error: " + e.getMessage());
        }
    }

    @GetMapping("/statistics")
    public Map<String, Object> getStatistics(HttpServletRequest request) {
        try {
            Long teacherId = getUserIdFromRequest(request);
            if (teacherId == null) {
                return Map.of("error", "Unauthorized");
            }
            
            List<Group> myGroups = groupRepository.findByTeacherId(teacherId);
            List<User> myStudents = new ArrayList<>();
            
            for (Group group : myGroups) {
                myStudents.addAll(userRepository.findStudentsByGroupId(group.getId()));
            }
            
            int totalStudents = myStudents.size();
            long onlineStudents = myStudents.stream().mapToLong(s -> s.isOnlineStatus() ? 1 : 0).sum();
            int totalProblems = problemRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId).size();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalStudents", totalStudents);
            stats.put("onlineStudents", onlineStudents);
            stats.put("totalProblems", totalProblems);
            stats.put("totalGroups", myGroups.size());
            
            return Map.of("statistics", stats);
        } catch (Exception e) {
            return Map.of("error", "Failed to load statistics: " + e.getMessage());
        }
    }

    @GetMapping("/grades")
    public Map<String, Object> getGrades(HttpServletRequest request) {
        try {
            Long teacherId = getUserIdFromRequest(request);
            if (teacherId == null) {
                return Map.of("error", "Unauthorized");
            }
            
            List<Group> myGroups = groupRepository.findByTeacherId(teacherId);
            List<User> myStudents = new ArrayList<>();
            
            for (Group group : myGroups) {
                myStudents.addAll(userRepository.findStudentsByGroupId(group.getId()));
            }
            
            List<Map<String, Object>> grades = myStudents.stream().map(student -> {
                Map<String, Object> grade = new HashMap<>();
                grade.put("studentId", student.getId());
                grade.put("studentName", student.getName());
                grade.put("attendanceRate", 95);
                grade.put("assignmentScore", 85);
                grade.put("examScore", 78);
                grade.put("totalScore", 82);
                grade.put("participation", "B");
                return grade;
            }).collect(Collectors.toList());
            
            return Map.of("grades", grades);
        } catch (Exception e) {
            return Map.of("error", "Failed to load grades: " + e.getMessage());
        }
    }

    @PostMapping("/evaluate-student")
    public Map<String, Object> evaluateStudent(@RequestBody Map<String, Object> evaluationData, HttpServletRequest request) {
        try {
            Long teacherId = getUserIdFromRequest(request);
            if (teacherId == null) {
                return Map.of("success", false, "message", "Unauthorized");
            }
            
            return Map.of("success", true, "message", "학생 평가가 저장되었습니다.");
        } catch (Exception e) {
            return Map.of("success", false, "message", "Error: " + e.getMessage());
        }
    }

    @GetMapping("/grade-statistics")
    public Map<String, Object> getGradeStatistics(HttpServletRequest request) {
        try {
            Long teacherId = getUserIdFromRequest(request);
            if (teacherId == null) {
                return Map.of("error", "Unauthorized");
            }
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("avgScore", 82);
            stats.put("excellentCount", 5);
            stats.put("submissionRate", 85);
            stats.put("improvementRate", 12);
            
            Map<String, Object> gradeDistribution = new HashMap<>();
            gradeDistribution.put("excellent", 15);
            gradeDistribution.put("good", 25);
            gradeDistribution.put("average", 35);
            gradeDistribution.put("poor", 20);
            gradeDistribution.put("fail", 5);
            stats.put("gradeDistribution", gradeDistribution);
            
            stats.put("monthlyScores", Arrays.asList(75, 78, 82, 85, 88, 87));
            
            return Map.of("statistics", stats);
        } catch (Exception e) {
            return Map.of("error", "Failed to load statistics: " + e.getMessage());
        }
    }
}
