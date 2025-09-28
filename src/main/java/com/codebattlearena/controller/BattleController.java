package com.codebattlearena.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/student/battle")
public class BattleController {

    @GetMapping("")
    public String battleDashboard(HttpSession session, Model model) {
        Object userId = session.getAttribute("userId");
        Object userRole = session.getAttribute("userRole");
        
        if (userId == null || !"STUDENT".equals(userRole)) {
            return "redirect:/";
        }
        
        return "student/battle";
    }

    @GetMapping("/friend")
    public String createBattlePage(HttpSession session, Model model) {
        Object userId = session.getAttribute("userId");
        Object userRole = session.getAttribute("userRole");
        
        if (userId == null || !"STUDENT".equals(userRole)) {
            return "redirect:/";
        }
        
        return "student/battle-create";
    }

    @GetMapping("/join")
    public String joinBattlePage(HttpSession session, Model model) {
        Object userId = session.getAttribute("userId");
        Object userRole = session.getAttribute("userRole");
        
        if (userId == null || !"STUDENT".equals(userRole)) {
            return "redirect:/";
        }
        
        return "student/battle-join";
    }

    @GetMapping("/random")
    public String randomBattlePage(HttpSession session, Model model) {
        Object userId = session.getAttribute("userId");
        Object userRole = session.getAttribute("userRole");
        
        if (userId == null || !"STUDENT".equals(userRole)) {
            return "redirect:/";
        }
        
        return "student/battle-random";
    }

    @GetMapping("/vs-ai")
    public String aiBattlePage(HttpSession session, Model model) {
        Object userId = session.getAttribute("userId");
        Object userRole = session.getAttribute("userRole");
        
        if (userId == null || !"STUDENT".equals(userRole)) {
            return "redirect:/";
        }
        
        return "student/battle-ai";
    }
}