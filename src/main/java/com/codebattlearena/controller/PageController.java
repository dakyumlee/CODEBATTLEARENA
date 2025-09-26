package com.codebattlearena.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/student")
    public String studentDashboard() {
        return "student/dashboard";
    }

    @GetMapping("/student/dashboard")
    public String studentDashboardFull() {
        return "student/dashboard";
    }

    @GetMapping("/student/today")
    public String studentToday() {
        return "student/today";
    }

    @GetMapping("/student/ai-tutor")
    public String studentAiTutor() {
        return "student/ai-tutor";
    }

    @GetMapping("/student/practice")
    public String studentPractice() {
        return "student/practice";
    }

    @GetMapping("/student/notes")
    public String studentNotes() {
        return "student/notes";
    }

    @GetMapping("/student/battle")
    public String studentBattle() {
        return "student/battle";
    }

    @GetMapping("/student/ai-problem")
    public String studentAiProblem() {
        return "student/ai-problem";
    }
    
    @GetMapping("/student/teacher-problem")
    public String studentTeacherProblem() {
        return "student/teacher-problem";
    }

    @GetMapping("/teacher")
    public String teacherDashboard() {
        return "teacher/dashboard";
    }

    @GetMapping("/teacher/dashboard")
    public String teacherDashboardFull() {
        return "teacher/dashboard";
    }

    @GetMapping("/teacher/class")
    public String teacherClass() {
        return "teacher/class";
    }

    @GetMapping("/teacher/grades")
    public String teacherGrades() {
        return "teacher/grades";
    }

    @GetMapping("/admin")
    public String adminDashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboardFull() {
        return "admin/dashboard";
    }
}
