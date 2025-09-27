package com.codebattlearena.controller;

import com.codebattlearena.model.User;
import com.codebattlearena.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        Object role = session.getAttribute("userRole");
        if (role == null) return "index";
        return "redirect:" + switch (role.toString()) {
            case "TEACHER" -> "/teacher/dashboard";
            case "ADMIN" -> "/admin/dashboard";
            default -> "/student/today";
        };
    }

    @PostMapping("/api/auth/login")
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody LoginRequest req, HttpSession session) {
        User user = userRepository.findByEmail(req.email()).orElse(null);
        if (user == null || !passwordEncoder.matches(req.password(), user.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "이메일 또는 비밀번호가 올바르지 않습니다"));
        }
        session.setAttribute("userId", user.getId());
        session.setAttribute("userRole", user.getRole().name());
        session.setAttribute("userName", user.getName());
        String next = switch (user.getRole()) {
            case STUDENT -> "/student/today";
            case TEACHER -> "/teacher/dashboard";
            case ADMIN -> "/admin/dashboard";
        };
        return ResponseEntity.ok(Map.of(
                "success", true,
                "id", user.getId(),
                "name", user.getName(),
                "role", user.getRole().name(),
                "next", next
        ));
    }

    @PostMapping("/api/auth/logout")
    @ResponseBody
    public Map<String, Object> logout(HttpSession session) {
        session.invalidate();
        return Map.of("success", true);
    }

    public record LoginRequest(String email, String password) {}
}
