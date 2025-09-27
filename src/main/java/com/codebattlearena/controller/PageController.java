package com.codebattlearena.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/student/today")
    public String studentToday() {
        return "student/today";
    }

    @GetMapping("/teacher/dashboard")
    public String teacherDashboard() {
        return "teacher/dashboard";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "admin/dashboard";
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

    @GetMapping("/teacher/class")
    public String teacherClass() {
        return "teacher/class";
    }

    @GetMapping("/teacher/grades")
    public String teacherGrades() {
        return "teacher/grades";
    }
}