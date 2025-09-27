package com.codebattlearena.controller;

import com.codebattlearena.model.User;
import com.codebattlearena.model.UserRole;
import com.codebattlearena.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/init")
public class InitController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/create-admin")
    public Map<String, Object> createAdmin() {
        try {
            if (userRepository.findByEmail("admin@codebattle.com").isPresent()) {
                return Map.of("success", false, "message", "관리자 계정이 이미 존재합니다.");
            }
            
            User admin = new User();
            admin.setName("시스템 관리자");
            admin.setEmail("admin@codebattle.com");
            admin.setPassword("admin123!@#");
            admin.setRole(UserRole.ADMIN);
            admin.setCreatedAt(LocalDateTime.now());
            admin.setOnlineStatus(false);
            userRepository.save(admin);

            return Map.of("success", true, "message", "관리자 계정 생성완료: admin@codebattle.com / admin123!@#");
        } catch (Exception e) {
            return Map.of("success", false, "message", "오류: " + e.getMessage());
        }
    }
}
