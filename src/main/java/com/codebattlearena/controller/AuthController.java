package com.codebattlearena.controller;

import com.codebattlearena.model.User;
import com.codebattlearena.model.UserRole;
import com.codebattlearena.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    public static class LoginRequest {
        private String email;
        private String password;
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class RegisterRequest {
        private String email;
        private String password;
        private String name;
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody RegisterRequest request) {
        try {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                return Map.of("success", false, "message", "이미 존재하는 이메일입니다.");
            }
            
            User user = new User();
            user.setEmail(request.getEmail());
            user.setPassword(request.getPassword());
            user.setName(request.getName());
            user.setRole(UserRole.STUDENT);
            user.setCreatedAt(LocalDateTime.now());
            
            userRepository.save(user);
            
            return Map.of("success", true, "message", "회원가입이 완료되었습니다.");
        } catch (Exception e) {
            return Map.of("success", false, "message", "회원가입 실패: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest request, HttpSession session) {
        try {
            User user = userRepository.findByEmail(request.getEmail()).orElse(null);
            
            if (user == null || !user.getPassword().equals(request.getPassword())) {
                return Map.of("success", false, "message", "이메일 또는 비밀번호가 올바르지 않습니다.");
            }
            
            session.setAttribute("userId", user.getId());
            session.setAttribute("userRole", user.getRole().toString());
            session.setAttribute("userName", user.getName());
            
            return Map.of(
                "success", true,
                "message", "로그인 성공",
                "user", Map.of(
                    "id", user.getId(),
                    "email", user.getEmail(),
                    "name", user.getName(),
                    "role", user.getRole().toString()
                )
            );
        } catch (Exception e) {
            return Map.of("success", false, "message", "로그인 실패: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(HttpSession session) {
        session.invalidate();
        return Map.of("success", true, "message", "로그아웃 되었습니다.");
    }
}