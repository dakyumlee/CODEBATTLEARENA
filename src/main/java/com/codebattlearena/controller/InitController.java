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
            userRepository.findAll().forEach(user -> {
                if (user.getRole() == UserRole.ADMIN) {
                    userRepository.delete(user);
                }
            });

            User admin = new User();
            admin.setName("관리자");
            admin.setEmail("oicrcutie@gmail.com");
            admin.setPassword(passwordEncoder.encode("aa667788!!"));
            admin.setRole(UserRole.ADMIN);
            admin.setCreatedAt(LocalDateTime.now());
            admin.setOnlineStatus(false);
            userRepository.save(admin);

            return Map.of("success", true, "message", "관리자 계정이 설정되었습니다.");
        } catch (Exception e) {
            return Map.of("success", false, "message", "오류: " + e.getMessage());
        }
    }
}
