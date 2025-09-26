package com.codebattlearena.controller;

import com.codebattlearena.model.User;
import com.codebattlearena.model.UserRole;
import com.codebattlearena.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/students")
    public ResponseEntity<?> getStudents() {
        List<User> students = userRepository.findByRole(UserRole.STUDENT);
        
        List<Map<String, Object>> studentList = students.stream().map(student -> {
            Map<String, Object> studentData = new HashMap<>();
            studentData.put("id", student.getId());
            studentData.put("name", student.getName());
            studentData.put("email", student.getEmail());
            studentData.put("onlineStatus", student.getOnlineStatus());
            studentData.put("currentActivity", student.getOnlineStatus() ? "학습 중" : "오프라인");
            studentData.put("currentPage", student.getOnlineStatus() ? "대시보드" : "-");
            studentData.put("lastActivity", student.getLastActivity() != null ? 
                student.getLastActivity().toString().substring(11, 16) : "-");
            return studentData;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(studentList);
    }

    @GetMapping("/groups")
    public ResponseEntity<?> getGroups() {
        List<Map<String, Object>> groups = new ArrayList<>();
        Map<String, Object> defaultGroup = new HashMap<>();
        defaultGroup.put("id", 1);
        defaultGroup.put("name", "기본 그룹");
        groups.add(defaultGroup);

        return ResponseEntity.ok(groups);
    }

    @PostMapping("/notification")
    public ResponseEntity<?> sendNotification(@RequestBody NotificationRequest request) {
        return ResponseEntity.ok("공지사항이 전송되었습니다: " + request.getMessage());
    }

    public static class NotificationRequest {
        private String message;
        private String target;

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getTarget() { return target; }
        public void setTarget(String target) { this.target = target; }
    }
}
