package com.codebattlearena.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/student/ai-problem")
    public String studentAiProblem() {
        return "student/ai-problem";
    }
    
    @GetMapping("/student/teacher-problem") 
    public String studentTeacherProblem() {
        return "student/teacher-problem";
    }
}
