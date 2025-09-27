package com.codebattlearena.controller;

import com.codebattlearena.config.JwtUtil;
import com.codebattlearena.model.User;
import com.codebattlearena.model.Problem;
import com.codebattlearena.model.Group;
import com.codebattlearena.repository.UserRepository;
import com.codebattlearena.repository.ProblemRepository;
import com.codebattlearena.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProblemRepository problemRepository;
    
    @Autowired
    private GroupRepository groupRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private WebSocketController webSocketController;

    private Long getUserIdFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtUtil.extractUserId(token);
        }
        return null;
    }

    @GetMapping("/students")
    public Map<String, Object> getStudents(HttpServletRequest request) {
        try {
            Long teacherId = getUserIdFromRequest(request);
            if (teacherId == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Unauthorized");
                return error;
            }
            
            // 강사의 그룹 조회
            List<Group> teacherGroups = groupRepository.findByTeacherId(teacherId);
            List<User> allStudents = new ArrayList<>();
            
            for (Group group : teacherGroups) {
                List<User> groupStudents = userRepository.findStudentsByGroupId(group.getId());
                allStudents.addAll(groupStudents);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("students", allStudents.stream().map(student -> {
                Map<String, Object> studentData = new HashMap<>();
                studentData.put("id", student.getId());
                studentData.put("name", student.getName());
                studentData.put("email", student.getEmail());
                studentData.put("online", student.isOnlineStatus());
                studentData.put("lastActivity", student.getLastActivity());
                studentData.put("groupId", student.getGroupId());
                return studentData;
            }).toList());
            
            return response;
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to load students: " + e.getMessage());
            return error;
        }
    }

    @GetMapping("/my-problems")
    public List<Map<String, Object>> getMyProblems(HttpServletRequest request) {
        try {
            Long teacherId = getUserIdFromRequest(request);
            if (teacherId == null) {
                return new ArrayList<>();
            }
            
            List<Problem> problems = problemRepository.findByCreatorIdOrderByCreatedAtDesc(teacherId);
            List<Map<String, Object>> problemList = new ArrayList<>();
            
            for (Problem problem : problems) {
                Map<String, Object> problemMap = new HashMap<>();
                problemMap.put("id", problem.getId());
                problemMap.put("title", problem.getTitle());
                problemMap.put("description", problem.getDescription());
                problemMap.put("difficulty", problem.getDifficulty());
                problemMap.put("timeLimit", problem.getTimeLimit());
                problemMap.put("type", problem.getType());
                problemMap.put("createdAt", problem.getCreatedAt().toString());
                problemList.add(problemMap);
            }
            
            return problemList;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @PostMapping("/problem")
    public Map<String, Object> createProblem(@RequestBody Map<String, Object> problemData, HttpServletRequest request) {
        try {
            Long teacherId = getUserIdFromRequest(request);
            if (teacherId == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Unauthorized");
                return error;
            }
            
            // 강사의 기본 그룹 가져오기 (첫 번째 그룹)
            List<Group> teacherGroups = groupRepository.findByTeacherId(teacherId);
            Long groupId = teacherGroups.isEmpty() ? null : teacherGroups.get(0).getId();
            
            Problem problem = new Problem();
            problem.setCreatorId(teacherId);
            problem.setGroupId(groupId);
            problem.setCreatorType("TEACHER");
            problem.setTitle((String) problemData.get("title"));
            problem.setDescription((String) problemData.get("description"));
            problem.setDifficulty((String) problemData.get("difficulty"));
            problem.setTimeLimit((Integer) problemData.get("timeLimit"));
            problem.setExampleInput((String) problemData.get("exampleInput"));
            problem.setExampleOutput((String) problemData.get("exampleOutput"));
            problem.setType("CODING");
            
            Problem savedProblem = problemRepository.save(problem);
            
            webSocketController.sendProblemNotification(problem.getTitle(), "코딩 문제");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "문제가 생성되고 학생들에게 알림이 전송되었습니다");
            response.put("problemId", savedProblem.getId());
            
            return response;
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error: " + e.getMessage());
            return error;
        }
    }

    @PostMapping("/quiz")
    public Map<String, Object> createQuiz(@RequestBody Map<String, Object> quizData, HttpServletRequest request) {
        try {
            Long teacherId = getUserIdFromRequest(request);
            if (teacherId == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Unauthorized");
                return error;
            }
            
            List<Group> teacherGroups = groupRepository.findByTeacherId(teacherId);
            Long groupId = teacherGroups.isEmpty() ? null : teacherGroups.get(0).getId();
            
            Problem quiz = new Problem();
            quiz.setCreatorId(teacherId);
            quiz.setGroupId(groupId);
            quiz.setCreatorType("TEACHER");
            quiz.setTitle((String) quizData.get("title"));
            quiz.setDescription((String) quizData.get("question"));
            quiz.setTimeLimit((Integer) quizData.get("timeLimit"));
            quiz.setType("QUIZ");
            
            Problem savedQuiz = problemRepository.save(quiz);
            
            webSocketController.sendProblemNotification(quiz.getTitle(), "퀴즈");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "퀴즈가 생성되고 학생들에게 알림이 전송되었습니다");
            response.put("quizId", savedQuiz.getId());
            
            return response;
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error: " + e.getMessage());
            return error;
        }
    }

    @PostMapping("/exam")
    public Map<String, Object> createExam(@RequestBody Map<String, Object> examData, HttpServletRequest request) {
        try {
            Long teacherId = getUserIdFromRequest(request);
            if (teacherId == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Unauthorized");
                return error;
            }
            
            List<Group> teacherGroups = groupRepository.findByTeacherId(teacherId);
            Long groupId = teacherGroups.isEmpty() ? null : teacherGroups.get(0).getId();
            
            Problem exam = new Problem();
            exam.setCreatorId(teacherId);
            exam.setGroupId(groupId);
            exam.setCreatorType("TEACHER");
            exam.setTitle((String) examData.get("title"));
            exam.setDescription((String) examData.get("instructions"));
            exam.setTimeLimit((Integer) examData.get("timeLimit"));
            exam.setType("EXAM");
            
            Problem savedExam = problemRepository.save(exam);
            
            webSocketController.sendProblemNotification(exam.getTitle(), "시험");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "시험이 생성되고 학생들에게 알림이 전송되었습니다");
            response.put("examId", savedExam.getId());
            
            return response;
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error: " + e.getMessage());
            return error;
        }
    }
}

    @GetMapping("/groups")
    public Map<String, Object> getMyGroups(HttpServletRequest request) {
        try {
            Long teacherId = getUserIdFromRequest(request);
            if (teacherId == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Unauthorized");
                return error;
            }
            
            List<Group> groups = groupRepository.findByTeacherId(teacherId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("groups", groups.stream().map(group -> {
                Map<String, Object> groupData = new HashMap<>();
                groupData.put("id", group.getId());
                groupData.put("name", group.getName());
                groupData.put("description", group.getDescription());
                groupData.put("createdAt", group.getCreatedAt().toString());
                
                // 그룹의 학생 수 계산
                long studentCount = userRepository.findStudentsByGroupId(group.getId()).size();
                groupData.put("studentCount", studentCount);
                
                return groupData;
            }).toList());
            
            return response;
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to load groups: " + e.getMessage());
            return error;
        }
    }

    @PostMapping("/groups")
    public Map<String, Object> createGroup(@RequestBody Map<String, Object> groupData, HttpServletRequest request) {
        try {
            Long teacherId = getUserIdFromRequest(request);
            if (teacherId == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Unauthorized");
                return error;
            }
            
            Group group = new Group();
            group.setTeacherId(teacherId);
            group.setName((String) groupData.get("name"));
            group.setDescription((String) groupData.get("description"));
            
            Group savedGroup = groupRepository.save(group);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "그룹이 생성되었습니다");
            response.put("group", Map.of(
                "id", savedGroup.getId(),
                "name", savedGroup.getName(),
                "description", savedGroup.getDescription()
            ));
            
            return response;
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error: " + e.getMessage());
            return error;
        }
    }
