package com.codebattlearena.controller;

import com.codebattlearena.config.JwtUtil;
import com.codebattlearena.model.User;
import com.codebattlearena.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Value("${openai.api.key:}")
    private String openaiApiKey;

    private Long getUserIdFromRequest(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String email = jwtUtil.extractEmail(token);
                User user = userRepository.findByEmail(email).orElse(null);
                return user != null ? user.getId() : null;
            }
        } catch (Exception e) {
            System.err.println("토큰 파싱 오류: " + e.getMessage());
        }
        return null;
    }

    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody ChatRequest request, HttpServletRequest httpRequest) {
        try {
            Long userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return Map.of("error", "Unauthorized");
            }

            String userMessage = request.getMessage();
            if (userMessage == null || userMessage.trim().isEmpty()) {
                return Map.of("error", "메시지가 비어있습니다.");
            }

            if (openaiApiKey == null || openaiApiKey.trim().isEmpty()) {
                return Map.of("response", generateFallbackResponse(userMessage));
            }

            String aiResponse = callOpenAI(userMessage);
            return Map.of("response", aiResponse);
            
        } catch (Exception e) {
            System.err.println("AI 서비스 오류: " + e.getMessage());
            return Map.of("response", generateFallbackResponse(request.getMessage()));
        }
    }

    private String callOpenAI(String userMessage) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openaiApiKey);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4o-mini");
            requestBody.put("max_tokens", 1500);
            requestBody.put("temperature", 0.7);
            
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", 
                "당신은 친근하고 전문적인 프로그래밍 튜터입니다. 한국어로 답변하며, " +
                "학생들이 코딩을 쉽게 이해할 수 있도록 도와주세요. " +
                "구체적인 예시 코드와 단계별 설명을 포함하여 답변해주세요."));
            messages.add(Map.of("role", "user", "content", userMessage));
            
            requestBody.put("messages", messages);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.openai.com/v1/chat/completions", 
                entity, 
                Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> firstChoice = choices.get(0);
                    Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
                    return (String) message.get("content");
                }
            }
            
            return generateFallbackResponse(userMessage);
            
        } catch (Exception e) {
            System.err.println("OpenAI API 호출 실패: " + e.getMessage());
            return generateFallbackResponse(userMessage);
        }
    }

    private String generateFallbackResponse(String userMessage) {
        String message = userMessage.toLowerCase();
        
        if (message.contains("안녕") || message.contains("hello")) {
            return "안녕하세요! 프로그래밍 학습을 도와드릴게요. 어떤 것이 궁금하신가요?";
        }
        
        if (message.contains("자바") || message.contains("java")) {
            return "자바는 객체지향 프로그래밍 언어입니다. 클래스와 객체의 개념을 이해하는 것이 중요해요.";
        }
        
        return "현재 AI 서비스 설정이 필요합니다. 기본적인 프로그래밍 질문에 대해서는 도움을 드릴 수 있습니다.";
    }

    public static class ChatRequest {
        private String message;

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
