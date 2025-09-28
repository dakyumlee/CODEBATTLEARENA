package com.codebattlearena.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PageController {

    @GetMapping("/test")
    @ResponseBody
    public String test() {
        return "페이지 컨트롤러 작동 중!";
    }

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

    @GetMapping("/student/battles")
    public String studentBattle() {
        return "student/battle";
    }

    @GetMapping("/student/battles/create")
    public String battleCreate() {
        return "student/battle-create";
    }

    @GetMapping("/student/battles/join")
    public String battleJoin() {
        return "student/battle-join";
    }

    @GetMapping("/student/battles/random")
    public String battleRandom() {
        return "student/battle-random";
    }

    @GetMapping("/student/battles/ai")
    public String battleAi() {
        return "student/battle-ai";
    }

    @GetMapping("/student/dashboard")
    public String studentDashboard() {
        return "student/dashboard";
    }

    @GetMapping("/teacher/dashboard")
    public String teacherDashboard() {
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

    @GetMapping("/teacher/create-problem")
    public String teacherCreateProblem() {
        return "teacher/create-problem";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "admin/dashboard";
    }
}