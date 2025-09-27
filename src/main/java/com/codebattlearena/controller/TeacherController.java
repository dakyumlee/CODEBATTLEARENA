package com.codebattlearena.controller;

import com.codebattlearena.config.JwtUtil;
import com.codebattlearena.model.*;
import com.codebattlearena.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

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
    private JwtUtil jwtUtil;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

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
}
