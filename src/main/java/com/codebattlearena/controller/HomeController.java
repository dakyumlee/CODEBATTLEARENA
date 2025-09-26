package com.codebattlearena.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    
    @GetMapping("/")
    public String home() {
        return "index";
    }
    
    @GetMapping("/student")
    public String student() {
        return "student/dashboard";
    }
    
    @GetMapping("/teacher")
    public String teacher() {
        return "teacher/dashboard";
    }
    
    @GetMapping("/admin")
    public String admin() {
        return "admin/dashboard";
    }
}
