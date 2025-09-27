package com.codebattlearena.controller;

import com.codebattlearena.model.User;
import com.codebattlearena.repository.UserRepository;
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

    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody ChatRequest request, HttpSession session) {
        try {
            Long userId = getUserIdFromSession(session);
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
            requestBody.put("max_tokens", 200);
            requestBody.put("temperature", 0.3);
            
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", 
                "당신은 간결한 프로그래밍 튜터입니다. " +
                "한국어로 2-3문장 이내로 짧고 친근하게 답변하세요. " +
                "불필요한 예시나 긴 설명은 피하고 핵심만 말하세요."));
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
            return "안녕하세요! 무엇을 도와드릴까요?";
        }
        
        if (message.contains("자바") || message.contains("java")) {
            if (message.contains("어려워") || message.contains("힘들어")) {
                return "한 단계씩 꾸준히 하는게 중요해요! 화이팅!\n어떤 부분이 제일 어려우신가요?";
            }
            return "자바 관련해서 어떤 것이 궁금하신가요?";
        }
        
        if (message.contains("객체") || message.contains("클래스")) {
            return "클래스는 설계도, 객체는 실제 만든 것이라고 생각하면 돼요.";
        }
        
        return "구체적으로 어떤 부분이 궁금하신가요?";
    }

    public static class ChatRequest {
        private String message;

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}