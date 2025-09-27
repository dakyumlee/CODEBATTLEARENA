package com.codebattlearena.controller;

import com.codebattlearena.config.JwtUtil;
import com.codebattlearena.model.User;
import com.codebattlearena.model.UserRole;
import com.codebattlearena.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest request) {
        try {
            Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
            
            if (userOpt.isEmpty()) {
                return Map.of("success", false, "message", "사용자를 찾을 수 없습니다.");
            }
            
            User user = userOpt.get();
            
            // 임시로 평문 비교 (개발용)
            boolean passwordMatch = false;
            try {
                // BCrypt 비교 시도
                passwordMatch = passwordEncoder.matches(request.getPassword(), user.getPassword());
            } catch (Exception e) {
                // BCrypt 실패 시 평문 비교
                passwordMatch = request.getPassword().equals(user.getPassword());
            }
            
            if (!passwordMatch) {
                return Map.of("success", false, "message", "비밀번호가 올바르지 않습니다.");
            }
            
            user.setOnlineStatus(true);
            user.setLastActivity(LocalDateTime.now());
            userRepository.save(user);
            
            String token = jwtUtil.generateToken(user.getEmail());
            
            String redirectUrl;
            switch (user.getRole()) {
                case ADMIN:
                    redirectUrl = "/admin";
                    break;
                case TEACHER:
                    redirectUrl = "/teacher";
                    break;
                case STUDENT:
                default:
                    redirectUrl = "/student";
                    break;
            }
            
            return Map.of(
                "success", true,
                "token", token,
                "redirectUrl", redirectUrl,
                "user", Map.of(
                    "id", user.getId(),
                    "name", user.getName(),
                    "email", user.getEmail(),
                    "role", user.getRole().toString()
                )
            );
        } catch (Exception e) {
            return Map.of("success", false, "message", "로그인 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody RegisterRequest request) {
        try {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                return Map.of("success", false, "message", "이미 존재하는 이메일입니다.");
            }

            User user = new User();
            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setPassword(request.getPassword()); // 평문 저장 (개발용)
            user.setRole(UserRole.STUDENT);
            user.setCreatedAt(LocalDateTime.now());
            user.setOnlineStatus(false);

            userRepository.save(user);

            return Map.of("success", true, "message", "회원가입이 완료되었습니다.");
        } catch (Exception e) {
            return Map.of("success", false, "message", "회원가입 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String email = jwtUtil.extractEmail(token);
                
                Optional<User> userOpt = userRepository.findByEmail(email);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    user.setOnlineStatus(false);
                    user.setLastActivity(LocalDateTime.now());
                    userRepository.save(user);
                }
            }
            
            return Map.of("success", true, "message", "로그아웃되었습니다.");
        } catch (Exception e) {
            return Map.of("success", false, "message", "로그아웃 중 오류가 발생했습니다.");
        }
    }

    public static class LoginRequest {
        private String email;
        private String password;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class RegisterRequest {
        private String name;
        private String email;
        private String password;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
