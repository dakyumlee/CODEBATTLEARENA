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

    @GetMapping("/student/today")
    public String studentToday(HttpServletRequest request) {
        if (!isValidUser(request, UserRole.STUDENT)) {
            return "redirect:/";
        }
        return "student/today";
    }

    @GetMapping("/student/ai-tutor")
    public String studentAiTutor(HttpServletRequest request) {
        if (!isValidUser(request, UserRole.STUDENT)) {
            return "redirect:/";
        }
        return "student/ai-tutor";
    }

    @GetMapping("/student/practice")
    public String studentPractice(HttpServletRequest request) {
        if (!isValidUser(request, UserRole.STUDENT)) {
            return "redirect:/";
        }
        return "student/practice";
    }

    @GetMapping("/student/notes")
    public String studentNotes(HttpServletRequest request) {
        if (!isValidUser(request, UserRole.STUDENT)) {
            return "redirect:/";
        }
        return "student/notes";
    }

    @GetMapping("/student/battle")
    public String studentBattle(HttpServletRequest request) {
        if (!isValidUser(request, UserRole.STUDENT)) {
            return "redirect:/";
        }
        return "student/battle";
    }

    @GetMapping("/student/battle/create")
    public String studentBattleCreate(HttpServletRequest request) {
        if (!isValidUser(request, UserRole.STUDENT)) {
            return "redirect:/";
        }
        return "student/battle-create";
    }

    @GetMapping("/student/battle/join")
    public String studentBattleJoin(HttpServletRequest request) {
        if (!isValidUser(request, UserRole.STUDENT)) {
            return "redirect:/";
        }
        return "student/battle-join";
    }

    @GetMapping("/student/battle/random")
    public String studentBattleRandom(HttpServletRequest request) {
        if (!isValidUser(request, UserRole.STUDENT)) {
            return "redirect:/";
        }
        return "student/battle-random";
    }

    @GetMapping("/student/battle/ai")
    public String studentBattleAi(HttpServletRequest request) {
        if (!isValidUser(request, UserRole.STUDENT)) {
            return "redirect:/";
        }
        return "student/battle-ai";
    }

    @GetMapping("/student/ai-problem")
    public String studentAiProblem(HttpServletRequest request) {
        if (!isValidUser(request, UserRole.STUDENT)) {
            return "redirect:/";
        }
        return "student/ai-problem";
    }

    @GetMapping("/student/teacher-problem")
    public String studentTeacherProblem(HttpServletRequest request) {
        if (!isValidUser(request, UserRole.STUDENT)) {
            return "redirect:/";
        }
        return "student/teacher-problem";
    }

    @GetMapping("/student/dashboard")
    public String studentDashboard(HttpServletRequest request) {
        if (!isValidUser(request, UserRole.STUDENT)) {
            return "redirect:/";
        }
        return "student/dashboard";
    }

    @GetMapping("/teacher")
    public String teacherRedirect(HttpServletRequest request) {
        if (!isValidUser(request, UserRole.TEACHER)) {
            return "redirect:/";
        }
        return "redirect:/teacher/dashboard";
    }

    @GetMapping("/teacher/dashboard")
    public String teacherDashboard(HttpServletRequest request) {
        if (!isValidUser(request, UserRole.TEACHER)) {
            return "redirect:/";
        }
        return "teacher/dashboard";
    }

    @GetMapping("/teacher/class")
    public String teacherClass(HttpServletRequest request) {
        if (!isValidUser(request, UserRole.TEACHER)) {
            return "redirect:/";
        }
        return "teacher/class";
    }

    @GetMapping("/teacher/grades")
    public String teacherGrades(HttpServletRequest request) {
        if (!isValidUser(request, UserRole.TEACHER)) {
            return "redirect:/";
        }
        return "teacher/grades";
    }

    @GetMapping("/teacher/create-problem")
    public String teacherCreateProblem(HttpServletRequest request) {
        if (!isValidUser(request, UserRole.TEACHER)) {
            return "redirect:/";
        }
        return "teacher/create-problem";
    }

    @GetMapping("/admin")
    public String adminRedirect(HttpServletRequest request) {
        if (!isValidUser(request, UserRole.ADMIN)) {
            return "redirect:/";
        }
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(HttpServletRequest request) {
        if (!isValidUser(request, UserRole.ADMIN)) {
            return "redirect:/";
        }
        return "admin/dashboard";
    }

    @GetMapping("/admin/users")
    public String adminUsers(HttpServletRequest request) {
        if (!isValidUser(request, UserRole.ADMIN)) {
            return "redirect:/";
        }
        return "admin/dashboard";
    }

    private boolean isValidUser(HttpServletRequest request, UserRole requiredRole) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
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
            System.err.println("인증 확인 오류: " + e.getMessage());
            return false;
        }
    }
}
