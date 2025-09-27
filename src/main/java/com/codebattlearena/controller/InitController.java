package com.codebattlearena.controller;

import com.codebattlearena.model.User;
import com.codebattlearena.model.UserRole;
import com.codebattlearena.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/create-default-users")
    public Map<String, Object> createDefaultUsers() {
        try {
            // 기존 사용자들 삭제
            userRepository.findByEmail("admin@admin.com").ifPresent(userRepository::delete);
            userRepository.findByEmail("teacher@test.com").ifPresent(userRepository::delete);
            userRepository.findByEmail("student@test.com").ifPresent(userRepository::delete);
            userRepository.findByEmail("student2@test.com").ifPresent(userRepository::delete);

            // 관리자 생성
            User admin = new User();
            admin.setName("관리자");
            admin.setEmail("admin@admin.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(UserRole.ADMIN);
            admin.setCreatedAt(LocalDateTime.now());
            userRepository.save(admin);

            // 강사 생성
            User teacher = new User();
            teacher.setName("김강사");
            teacher.setEmail("teacher@test.com");
            teacher.setPassword(passwordEncoder.encode("password123"));
            teacher.setRole(UserRole.TEACHER);
            teacher.setCreatedAt(LocalDateTime.now());
            userRepository.save(teacher);

            // 학생1 생성
            User student1 = new User();
            student1.setName("박학생");
            student1.setEmail("student@test.com");
            student1.setPassword(passwordEncoder.encode("password123"));
            student1.setRole(UserRole.STUDENT);
            student1.setCreatedAt(LocalDateTime.now());
            userRepository.save(student1);

            // 학생2 생성
            User student2 = new User();
            student2.setName("이학생");
            student2.setEmail("student2@test.com");
            student2.setPassword(passwordEncoder.encode("password123"));
            student2.setRole(UserRole.STUDENT);
            student2.setCreatedAt(LocalDateTime.now());
            userRepository.save(student2);

            return Map.of("success", true, "message", "기본 사용자들이 생성되었습니다.");
        } catch (Exception e) {
            return Map.of("success", false, "message", "사용자 생성 실패: " + e.getMessage());
        }
    }
}
