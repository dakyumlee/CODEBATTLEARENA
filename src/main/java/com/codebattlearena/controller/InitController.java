package com.codebattlearena.controller;

import com.codebattlearena.model.User;
import com.codebattlearena.model.UserRole;
import com.codebattlearena.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/init")
public class InitController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/setup-admin")
    public Map<String, Object> setupAdmin() {
        try {
            // 모든 사용자 삭제 (개발용)
            userRepository.deleteAll();

            // 관리자 계정
            User admin = new User();
            admin.setName("관리자");
            admin.setEmail("oicrcutie@gmail.com");
            admin.setPassword("aa667788!!"); // 평문 저장 (개발용만)
            admin.setRole(UserRole.ADMIN);
            admin.setCreatedAt(LocalDateTime.now());
            admin.setOnlineStatus(false);
            userRepository.save(admin);

            // 테스트 강사 계정
            User teacher = new User();
            teacher.setName("김강사");
            teacher.setEmail("teacher@test.com");
            teacher.setPassword("password123");
            teacher.setRole(UserRole.TEACHER);
            teacher.setCreatedAt(LocalDateTime.now());
            teacher.setOnlineStatus(false);
            userRepository.save(teacher);

            // 테스트 학생 계정
            User student = new User();
            student.setName("박학생");
            student.setEmail("student@test.com");
            student.setPassword("password123");
            student.setRole(UserRole.STUDENT);
            student.setCreatedAt(LocalDateTime.now());
            student.setOnlineStatus(false);
            userRepository.save(student);

            return Map.of("success", true, "message", "계정들이 설정되었습니다 (평문 비밀번호)");
        } catch (Exception e) {
            return Map.of("success", false, "message", "오류: " + e.getMessage());
        }
    }
}
