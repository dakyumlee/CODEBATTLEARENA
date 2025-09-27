package com.codebattlearena.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/student/today")
    public String studentToday() {
        checkRole("ROLE_STUDENT");
        return "student/today";
    }

    @GetMapping("/student/ai-tutor")
    public String studentAiTutor() {
        checkRole("ROLE_STUDENT");
        return "student/ai-tutor";
    }

    @GetMapping("/student/practice")
    public String studentPractice() {
        checkRole("ROLE_STUDENT");
        return "student/practice";
    }

    @GetMapping("/student/notes")
    public String studentNotes() {
        checkRole("ROLE_STUDENT");
        return "student/notes";
    }

    @GetMapping("/student/battle")
    public String studentBattle() {
        checkRole("ROLE_STUDENT");
        return "student/battle";
    }

    @GetMapping("/teacher/dashboard")
    public String teacherDashboard() {
        checkRole("ROLE_TEACHER");
        return "teacher/dashboard";
    }

    @GetMapping("/teacher/class")
    public String teacherClass() {
        checkRole("ROLE_TEACHER");
        return "teacher/class";
    }

    @GetMapping("/teacher/grades")
    public String teacherGrades() {
        checkRole("ROLE_TEACHER");
        return "teacher/grades";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        checkRole("ROLE_ADMIN");
        return "admin/dashboard";
    }

    private void checkRole(String requiredRole) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(requiredRole))) {
            throw new org.springframework.security.access.AccessDeniedException("접근 권한이 없습니다.");
        }
    }
}