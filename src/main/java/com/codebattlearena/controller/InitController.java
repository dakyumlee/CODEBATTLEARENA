package com.codebattlearena.controller;

import com.codebattlearena.model.User;
import com.codebattlearena.model.UserRole;
import com.codebattlearena.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/init")
public class InitController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/reset-passwords")
    public Map<String, String> resetPasswords() {
        Map<String, String> result = new HashMap<>();
        
        try {
            // 모든 테스트 계정 삭제 후 재생성
            userRepository.deleteByEmail("admin@test.com");
            userRepository.deleteByEmail("teacher@test.com");  
            userRepository.deleteByEmail("student@test.com");
            userRepository.deleteByEmail("oicrcutie@gmail.com");

            // 새로 생성
            createUser("관리자", "admin@test.com", "1234", UserRole.ADMIN);
            createUser("김강사", "teacher@test.com", "1234", UserRole.TEACHER);
            createUser("박학생", "student@test.com", "1234", UserRole.STUDENT);
            createUser("원관리자", "oicrcutie@gmail.com", "aa667788!!", UserRole.ADMIN);

            result.put("status", "success");
            result.put("message", "모든 테스트 계정이 재생성되었습니다.");
            
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "오류: " + e.getMessage());
        }
        
        return result;
    }

    private void createUser(String name, String email, String password, UserRole role) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);
        user.setOnlineStatus(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setLastActivity(LocalDateTime.now());
        userRepository.save(user);
        
        System.out.println("생성된 사용자: " + name + " (" + email + ") - 비밀번호: " + password);
    }

    @GetMapping("/test-login")
    public Map<String, Object> testLogin() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            var admin = userRepository.findByEmail("admin@test.com");
            var teacher = userRepository.findByEmail("teacher@test.com");
            var student = userRepository.findByEmail("student@test.com");
            var owner = userRepository.findByEmail("oicrcutie@gmail.com");
            
            result.put("admin", admin.isPresent() ? admin.get().getPassword() : "없음");
            result.put("teacher", teacher.isPresent() ? teacher.get().getPassword() : "없음");
            result.put("student", student.isPresent() ? student.get().getPassword() : "없음");
            result.put("owner", owner.isPresent() ? owner.get().getPassword() : "없음");
            
        } catch (Exception e) {
            result.put("error", e.getMessage());
        }
        
        return result;
    }
}
