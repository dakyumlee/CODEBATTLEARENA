package com.codebattlearena.controller;

import com.codebattlearena.config.JwtUtil;
import com.codebattlearena.model.User;
import com.codebattlearena.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
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
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        Map<String, Object> response = new HashMap<>();
        
        if (userOpt.isPresent() && 
            passwordEncoder.matches(request.getPassword(), userOpt.get().getPassword())) {
            
            User user = userOpt.get();
            String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole().name());
            
            response.put("success", true);
            response.put("token", token);
            response.put("user", Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "role", user.getRole().name()
            ));
            
            return ResponseEntity.ok(response);
        }
        
        response.put("success", false);
        response.put("message", "이메일 또는 비밀번호가 잘못되었습니다.");
        return ResponseEntity.status(401).body(response);
    }
    
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        if (userRepository.existsByEmail(request.getEmail())) {
            response.put("success", false);
            response.put("message", "이미 존재하는 이메일입니다.");
            return ResponseEntity.status(400).body(response);
        }
        
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        
        userRepository.save(user);
        
        response.put("success", true);
        response.put("message", "회원가입이 완료되었습니다.");
        return ResponseEntity.ok(response);
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
}
