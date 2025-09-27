package com.codebattlearena.controller;

import com.codebattlearena.config.JwtUtil;
import com.codebattlearena.model.User;
import com.codebattlearena.model.UserRole;
import com.codebattlearena.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class PageController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/student")
    public String studentRedirect(HttpServletRequest request) {
        if (!isValidUser(request, UserRole.STUDENT)) {
            return "redirect:/";
        }
        return "redirect:/student/today";
    }

    @GetMapping("/student/**")
    public String studentPages(HttpServletRequest request) {
        if (!isValidUser(request, UserRole.STUDENT)) {
            return "redirect:/";
        }
        
        String path = request.getRequestURI().substring("/student/".length());
        return "student/" + path;
    }

    @GetMapping("/teacher")
    public String teacherRedirect(HttpServletRequest request) {
        if (!isValidUser(request, UserRole.TEACHER)) {
            return "redirect:/";
        }
        return "redirect:/teacher/dashboard";
    }

    @GetMapping("/teacher/**")
    public String teacherPages(HttpServletRequest request) {
        if (!isValidUser(request, UserRole.TEACHER)) {
            return "redirect:/";
        }
        
        String path = request.getRequestURI().substring("/teacher/".length());
        return "teacher/" + path;
    }

    @GetMapping("/admin")
    public String adminRedirect(HttpServletRequest request) {
        if (!isValidUser(request, UserRole.ADMIN)) {
            return "redirect:/";
        }
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/admin/**")
    public String adminPages(HttpServletRequest request) {
        if (!isValidUser(request, UserRole.ADMIN)) {
            return "redirect:/";
        }
        
        String path = request.getRequestURI().substring("/admin/".length());
        return "admin/" + path;
    }

    private boolean isValidUser(HttpServletRequest request, UserRole requiredRole) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                // 쿠키에서 토큰 확인
                if (request.getCookies() != null) {
                    for (var cookie : request.getCookies()) {
                        if ("authToken".equals(cookie.getName())) {
                            authHeader = "Bearer " + cookie.getValue();
                            break;
                        }
                    }
                }
            }

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return false;
            }

            String token = authHeader.substring(7);
            String email = jwtUtil.extractEmail(token);
            User user = userRepository.findByEmail(email).orElse(null);
            
            return user != null && user.getRole() == requiredRole;
        } catch (Exception e) {
            return false;
        }
    }
}
