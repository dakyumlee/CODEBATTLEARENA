package com.codebattlearena.controller;

import com.codebattlearena.model.User;
import com.codebattlearena.model.UserRole;
import com.codebattlearena.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/create-admin")
    public ResponseEntity<?> createAdmin() {
        if (!userRepository.findByEmail("oicrcutie@gmail.com").isPresent()) {
            User admin = new User();
            admin.setName("관리자");
            admin.setEmail("oicrcutie@gmail.com");
            admin.setPassword(passwordEncoder.encode("aa667788!!"));
            admin.setRole(UserRole.ADMIN);
            admin.setCreatedAt(LocalDateTime.now());
            admin.setOnlineStatus(false);
            
            userRepository.save(admin);
            return ResponseEntity.ok("관리자 계정이 생성되었습니다");
        }
        return ResponseEntity.ok("관리자 계정이 이미 존재합니다");
    }
}
