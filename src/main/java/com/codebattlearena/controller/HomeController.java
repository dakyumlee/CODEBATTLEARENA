package com.codebattlearena.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    // 학생 페이지들
    @GetMapping("/student")
    public String studentDashboard() {
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

    @GetMapping("/student/battle/create")
    public String studentBattleCreate() {
        return "student/battle-create";
    }

    @GetMapping("/student/battle/random")
    public String studentBattleRandom() {
        return "student/battle-random";
    }

    @GetMapping("/student/battle/ai")
    public String studentBattleAi() {
        return "student/battle-ai";
    }

    @GetMapping("/student/battle/join")
    public String studentBattleJoin() {
        return "student/battle-join";
    }

    // 강사 페이지들
    @GetMapping("/teacher")
    public String teacherDashboard() {
        return "teacher/dashboard";
    }

    @GetMapping("/teacher/dashboard")  
    public String teacherDashboardPage() {
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

    // 관리자 페이지
    @GetMapping("/admin")
    public String adminDashboard() {
        return "admin/dashboard";
    }
}
