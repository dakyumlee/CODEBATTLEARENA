package com.codebattlearena.controller;

import com.codebattlearena.model.User;
import com.codebattlearena.model.UserRole;
import com.codebattlearena.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                return ResponseEntity.status(400).body("이미 존재하는 이메일입니다.");
            }

            User user = new User();
            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setPassword(request.getPassword());
            user.setRole(UserRole.valueOf(request.getRole()));
            user.setCreatedAt(LocalDateTime.now());
            user.setOnlineStatus(false);

            userRepository.save(user);
            return ResponseEntity.ok("회원가입 성공");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("회원가입 오류: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            System.out.println("로그인 시도: " + request.getEmail());
            
            Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
            
            if (userOpt.isEmpty()) {
                System.out.println("사용자 없음: " + request.getEmail());
                return ResponseEntity.status(400).body("사용자를 찾을 수 없습니다.");
            }

            User user = userOpt.get();
            System.out.println("사용자 찾음: " + user.getName());
            
            if (!user.getPassword().equals(request.getPassword())) {
                System.out.println("비밀번호 불일치");
                return ResponseEntity.status(400).body("비밀번호가 틀렸습니다.");
            }

            user.setOnlineStatus(true);
            user.setLastActivity(LocalDateTime.now());
            userRepository.save(user);

            // 임시 토큰 생성 (UUID 기반)
            String token = "Bearer_" + UUID.randomUUID().toString().replace("-", "");

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("role", user.getRole().toString());
            response.put("user", Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "email", user.getEmail()
            ));
            
            System.out.println("로그인 성공: " + user.getName());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("로그인 오류: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("로그인 오류: " + e.getMessage());
        }
    }

    @PostMapping("/fix-passwords")
    public ResponseEntity<?> fixPasswords() {
        try {
            userRepository.findByEmail("admin@test.com").ifPresent(user -> {
                user.setPassword("1234");
                userRepository.save(user);
            });
            
            userRepository.findByEmail("teacher@test.com").ifPresent(user -> {
                user.setPassword("1234");
                userRepository.save(user);
            });
            
            userRepository.findByEmail("student@test.com").ifPresent(user -> {
                user.setPassword("1234");
                userRepository.save(user);
            });
            
            userRepository.findByEmail("oicrcutie@gmail.com").ifPresent(user -> {
                user.setPassword("aa667788!!");
                userRepository.save(user);
            });

            return ResponseEntity.ok("모든 계정 비밀번호가 평문으로 변경되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("비밀번호 변경 오류: " + e.getMessage());
        }
    }

    public static class RegisterRequest {
        private String name;
        private String email;
        private String password;
        private String role = "STUDENT";

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

    public static class LoginRequest {
        private String email;
        private String password;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
