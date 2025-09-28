package com.codebattlearena.controller;

import com.codebattlearena.model.User;
import com.codebattlearena.repository.UserRepository;
import com.codebattlearena.service.AiProblemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpSession;
import java.util.*;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AiProblemService aiProblemService;
    
    @Value("${openai.api.key:}")
    private String openaiApiKey;

    private Long getUserIdFromSession(HttpSession session) {
        try {
            Object userId = session.getAttribute("userId");
            return userId != null ? (Long) userId : null;
        } catch (Exception e) {
            return null;
        }
    }

    @PostMapping("/generate-problem")
    public Map<String, Object> generateProblem(@RequestBody Map<String, String> request) {
        try {
            String difficulty = request.getOrDefault("difficulty", "중");
            String topic = request.getOrDefault("topic", "기본");
            String language = request.getOrDefault("language", "java");
            
            Map<String, Object> problem = aiProblemService.generateProblemWithLanguage(difficulty, topic, language);
            return problem;
        } catch (Exception e) {
            System.err.println("AI 문제 생성 오류: " + e.getMessage());
            return aiProblemService.generateProblemWithLanguage(
                request.getOrDefault("difficulty", "중"),
                request.getOrDefault("topic", "기본"),
                request.getOrDefault("language", "java")
            );
        }
    }

    @PostMapping("/grade")
    public Map<String, Object> gradeCode(@RequestBody Map<String, String> request, HttpSession session) {
        try {
            Long userId = getUserIdFromSession(session);
            if (userId == null) {
                return Map.of("success", false, "error", "Unauthorized");
            }

            String problemId = request.get("problemId");
            String answer = request.get("answer");
            String language = request.get("language");

            if (answer == null || answer.trim().isEmpty()) {
                return Map.of("success", false, "error", "코드가 비어있습니다.");
            }

            Map<String, Object> feedback = aiProblemService.gradeAnswer(problemId, answer, language);
            return Map.of("success", true, "feedback", feedback);
            
        } catch (Exception e) {
            System.err.println("AI 채점 오류: " + e.getMessage());
            return Map.of("success", false, "error", "채점 중 오류가 발생했습니다.");
        }
    }

    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody ChatRequest request, HttpSession session) {
        try {
            Long userId = getUserIdFromSession(session);
            if (userId == null) {
                return Map.of("success", false, "error", "Unauthorized");
            }

            String userMessage = request.getMessage();
            if (userMessage == null || userMessage.trim().isEmpty()) {
                return Map.of("success", false, "error", "메시지가 비어있습니다.");
            }

            String conversationHistory = request.getHistory();
            String aiResponse = aiProblemService.chatWithAI(userMessage, conversationHistory);
            return Map.of("success", true, "response", aiResponse);
            
        } catch (Exception e) {
            System.err.println("AI 채팅 오류: " + e.getMessage());
            return Map.of("success", true, "response", "죄송합니다. 잠시 후 다시 시도해주세요.");
        }
    }

    public static class ChatRequest {
        private String message;
        private String history;

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getHistory() { return history; }
        public void setHistory(String history) { this.history = history; }
    }
}