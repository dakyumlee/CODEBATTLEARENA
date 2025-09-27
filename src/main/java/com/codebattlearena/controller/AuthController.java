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
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
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
        );            user.setLastActivity(LocalDateTime.now());
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
        );        } catch (Exception e) {
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
        );            }

            User user = new User();
            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setRole(UserRole.STUDENT);
            user.setCreatedAt(LocalDateTime.now());
            user.setOnlineStatus(false);

            userRepository.save(user);

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
        );        }
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
        );        }
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
